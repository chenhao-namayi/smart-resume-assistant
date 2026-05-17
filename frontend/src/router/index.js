import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    redirect: () => {
      const role = localStorage.getItem('role')
      return role === 'ADMIN' ? '/admin' : '/dashboard'
    }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/editor/:resumeId',
    name: 'Editor',
    component: () => import('../views/Editor.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/match',
    name: 'Match',
    component: () => import('../views/Match.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/interview',
    name: 'Interview',
    component: () => import('../views/Interview.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/interview/history',
    name: 'InterviewHistory',
    component: () => import('../views/InterviewHistory.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/interview/report',
    name: 'InterviewReport',
    component: () => import('../views/InterviewReport.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/Admin.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  const isAdmin = role === 'ADMIN'

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    next(isAdmin ? '/admin' : '/dashboard')
  } else if (to.path.startsWith('/admin') && !isAdmin) {
    next('/dashboard')
  } else if (isAdmin && !to.path.startsWith('/admin') && to.path !== '/login' && to.path !== '/register') {
    next('/admin')
  } else {
    next()
  }
})

export default router
