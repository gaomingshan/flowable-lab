import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/Dashboard.vue'),
    meta: { title: '仪表盘' }
  },
  {
    path: '/process/definition',
    name: 'ProcessList',
    component: () => import('@/views/process/ProcessList.vue'),
    meta: { title: '流程定义' }
  },
  {
    path: '/process/modeler',
    name: 'ProcessModeler',
    component: () => import('@/views/process/ProcessModeler.vue'),
    meta: { title: '流程编辑器' }
  },
  {
    path: '/process/instance',
    name: 'ProcessInstance',
    component: () => import('@/views/process/ProcessInstance.vue'),
    meta: { title: '流程实例' }
  },
  {
    path: '/task/todo',
    name: 'TodoTask',
    component: () => import('@/views/task/TodoTask.vue'),
    meta: { title: '待办任务' }
  },
  {
    path: '/task/detail/:taskId',
    name: 'TaskDetail',
    component: () => import('@/views/task/TaskDetail.vue'),
    meta: { title: '任务详情' }
  },
  {
    path: '/dmn/test',
    name: 'DmnTest',
    component: () => import('@/views/dmn/DmnTest.vue'),
    meta: { title: 'DMN 决策测试' }
  },
  {
    path: '/cmmn/case',
    name: 'CmmnCase',
    component: () => import('@/views/cmmn/CmmnCase.vue'),
    meta: { title: 'CMMN 案例' }
  },
  {
    path: '/identity',
    name: 'Identity',
    component: () => import('@/views/identity/UserGroup.vue'),
    meta: { title: '用户组管理' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
