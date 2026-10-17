package com.fod.system.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * 异步事件总线 (观察者模式 + 生产者-消费者模式)
 * 核心设计：异步分发、优先级排序、背压降级、事件去重
 */
public class EventBus {

    private static final Logger log = LoggerFactory.getLogger(EventBus.class);
    private static volatile EventBus instance;

    private final Map<GameEvent.EventType, CopyOnWriteArrayList<PrioritizedListener>> listeners;
    private final BlockingQueue<GameEvent> eventQueue;
    private final ExecutorService executor;
    private final ConcurrentHashMap<GameEvent.EventType, Long> dedupCache;
    private volatile boolean running = true;

    private static final long DEDUP_WINDOW_MS = 1000;

    private EventBus() {
        this.listeners = new ConcurrentHashMap<>();
        this.eventQueue = new LinkedBlockingQueue<>();
        this.executor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "event-bus-worker");
            t.setDaemon(true);
            return t;
        });
        this.dedupCache = new ConcurrentHashMap<>();
        startDispatcher();
    }

    public static EventBus getInstance() {
        if (instance == null) {
            synchronized (EventBus.class) {
                if (instance == null) instance = new EventBus();
            }
        }
        return instance;
    }

    public void register(GameEvent.EventType type, Consumer<GameEvent> listener, int priority) {
        listeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>())
                .add(new PrioritizedListener(listener, priority));
        listeners.get(type).sort((a, b) -> Integer.compare(b.priority, a.priority));
    }

    public void register(GameEvent.EventType type, Consumer<GameEvent> listener) {
        register(type, listener, 0);
    }

    public void publish(GameEvent event) {
        if (!running) return;
        if (isDuplicate(event)) { log.debug("去重丢弃事件: {}", event.getType()); return; }
        boolean offered = eventQueue.offer(event);
        if (!offered) {
            log.warn("事件队列积压，同步分发降级: {}", event.getType());
            try { dispatch(event); } catch (Exception e) { log.error("同步分发失败", e); }
        }
    }

    public void publishSync(GameEvent event) { dispatch(event); }

    private void startDispatcher() {
        executor.submit(() -> {
            while (running) {
                try {
                    GameEvent event = eventQueue.poll(1, TimeUnit.SECONDS);
                    if (event != null) dispatch(event);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    private void dispatch(GameEvent event) {
        List<PrioritizedListener> typeListeners = listeners.get(event.getType());
        if (typeListeners == null) return;
        for (PrioritizedListener pl : typeListeners) {
            try { pl.listener.accept(event); } catch (Exception e) {
                log.error("事件监听器异常: type={}", event.getType(), e);
            }
        }
    }

    private boolean isDuplicate(GameEvent event) {
        Long lastTime = dedupCache.get(event.getType());
        long now = System.currentTimeMillis();
        if (lastTime != null && (now - lastTime) < DEDUP_WINDOW_MS) return true;
        dedupCache.put(event.getType(), now);
        return false;
    }

    public int getQueueSize() { return eventQueue.size(); }

    public void shutdown() {
        running = false;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) executor.shutdownNow();
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private record PrioritizedListener(Consumer<GameEvent> listener, int priority) {}
}
