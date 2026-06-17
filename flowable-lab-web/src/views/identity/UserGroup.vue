<template>
  <div class="page-container">
    <div class="page-header">
      <h2>用户组管理</h2>
      <p>管理 Flowable 身份管理中的用户和组</p>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <el-space>
              <span>用户列表</span>
              <el-button size="small" type="primary" @click="showUserDialog = true">新增用户</el-button>
            </el-space>
          </template>

          <el-table :data="users" stripe size="small" style="width: 100%">
            <el-table-column prop="id" label="ID" width="100" />
            <el-table-column prop="firstName" label="名" />
            <el-table-column prop="lastName" label="姓" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column label="操作" width="60">
              <template #default="{ row }">
                <el-button size="small" type="danger" @click="handleDeleteUser(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <el-space>
              <span>组列表</span>
              <el-button size="small" type="primary" @click="showGroupDialog = true">新增组</el-button>
            </el-space>
          </template>

          <el-table :data="groups" stripe size="small" style="width: 100%">
            <el-table-column prop="id" label="ID" width="100" />
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="type" label="类型" />
            <el-table-column label="操作" width="60">
              <template #default="{ row }">
                <el-button size="small" type="danger" @click="handleDeleteGroup(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card style="margin-top: 20px">
          <template #header>组成员管理</template>
          <el-space>
            <el-select v-model="membershipUser" placeholder="选择用户" style="width: 140px">
              <el-option v-for="u in users" :key="u.id" :label="u.id" :value="u.id" />
            </el-select>
            <el-select v-model="membershipGroup" placeholder="选择组" style="width: 140px">
              <el-option v-for="g in groups" :key="g.id" :label="g.id" :value="g.id" />
            </el-select>
            <el-button type="primary" size="small" @click="handleAddMembership">加入</el-button>
          </el-space>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showUserDialog" title="新增用户" width="400px">
      <el-form :model="userForm" label-width="80px">
        <el-form-item label="ID">
          <el-input v-model="userForm.id" />
        </el-form-item>
        <el-form-item label="名">
          <el-input v-model="userForm.firstName" />
        </el-form-item>
        <el-form-item label="姓">
          <el-input v-model="userForm.lastName" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUserDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateUser">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showGroupDialog" title="新增组" width="400px">
      <el-form :model="groupForm" label-width="80px">
        <el-form-item label="ID">
          <el-input v-model="groupForm.id" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="groupForm.name" />
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model="groupForm.type" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showGroupDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateGroup">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getUsers, createUser, deleteUser, getGroups, createGroup, deleteGroup, addMembership } from '@/api/dmn'
import { ElMessage } from 'element-plus'

const users = ref<any[]>([])
const groups = ref<any[]>([])
const showUserDialog = ref(false)
const showGroupDialog = ref(false)
const userForm = ref({ id: '', firstName: '', lastName: '', email: '' })
const groupForm = ref({ id: '', name: '', type: '' })
const membershipUser = ref('')
const membershipGroup = ref('')

async function loadData() {
  const uRes = await getUsers()
  users.value = uRes.data || []
  const gRes = await getGroups()
  groups.value = gRes.data || []
}

async function handleCreateUser() {
  await createUser(userForm.value)
  ElMessage.success('用户已创建')
  showUserDialog.value = false
  userForm.value = { id: '', firstName: '', lastName: '', email: '' }
  await loadData()
}

async function handleDeleteUser(id: string) {
  await deleteUser(id)
  ElMessage.success('已删除')
  await loadData()
}

async function handleCreateGroup() {
  await createGroup(groupForm.value)
  ElMessage.success('组已创建')
  showGroupDialog.value = false
  groupForm.value = { id: '', name: '', type: '' }
  await loadData()
}

async function handleDeleteGroup(id: string) {
  await deleteGroup(id)
  ElMessage.success('已删除')
  await loadData()
}

async function handleAddMembership() {
  if (!membershipUser.value || !membershipGroup.value) {
    ElMessage.warning('请选择用户和组')
    return
  }
  await addMembership(membershipUser.value, membershipGroup.value)
  ElMessage.success('已添加')
}

onMounted(loadData)
</script>
