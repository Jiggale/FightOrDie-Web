-- 战斗历史表
CREATE TABLE IF NOT EXISTS battle_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    player_name VARCHAR(50) NOT NULL,
    monster_name VARCHAR(50) NOT NULL,
    player_won BOOLEAN NOT NULL,
    exp_gained INT DEFAULT 0,
    gold_gained INT DEFAULT 0,
    damage_dealt INT DEFAULT 0,
    damage_taken INT DEFAULT 0,
    turns INT DEFAULT 0,
    finished_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id),
    INDEX idx_player (player_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 玩家统计表
CREATE TABLE IF NOT EXISTS player_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_name VARCHAR(50) NOT NULL UNIQUE,
    total_battles INT DEFAULT 0,
    total_wins INT DEFAULT 0,
    total_exp BIGINT DEFAULT 0,
    total_gold BIGINT DEFAULT 0,
    monsters_killed INT DEFAULT 0,
    highest_level INT DEFAULT 1,
    last_battle_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
