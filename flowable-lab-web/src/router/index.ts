import { createRouter, createWebHistory } from 'vue-router'
import WorkflowDefinitionsPage from '../views/WorkflowDefinitionsPage.vue'
import WorkflowDesignerPage from '../views/WorkflowDesignerPage.vue'
import LaunchableWorkflowsPage from '../views/LaunchableWorkflowsPage.vue'
import WorkflowTasksPage from '../views/WorkflowTasksPage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/definitions' },
    { path: '/definitions', component: WorkflowDefinitionsPage },
    { path: '/designer', component: WorkflowDesignerPage },
    { path: '/launchable', component: LaunchableWorkflowsPage },
    { path: '/tasks', component: WorkflowTasksPage },
  ],
})

export default router
