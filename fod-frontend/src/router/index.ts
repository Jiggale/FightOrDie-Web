import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import BattleView from '../views/BattleView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/battle', name: 'battle', component: BattleView },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

export default router
