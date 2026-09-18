<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminSysConfig, adminUpdateConfig } from '@/api'

const loading = ref(false)
const configs = ref([])
const saving = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await adminSysConfig()
    configs.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function save(item) {
  saving.value = true
  try {
    await adminUpdateConfig({ configKey: item.configKey, configValue: item.configValue })
    ElMessage.success('设置已保存')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="oa-page">
    <h3 class="oa-page-title">系统设置（保存即生效）</h3>
    <div class="oa-page-card">
      <el-table v-loading="loading" :data="configs" border stripe>
        <el-table-column prop="configKey" label="配置项" width="220">
          <template #default="{ row }"><span class="oa-mono">{{ row.configKey }}</span></template>
        </el-table-column>
        <el-table-column label="配置值" width="260">
          <template #default="{ row }">
            <el-switch v-if="row.configKey === 'message_push_enabled'"
                       v-model="row.configValue" active-value="true" inactive-value="false"
                       active-text="开启" inactive-text="关闭" @change="save(row)" />
            <el-input-number v-else v-model="row.configValue" :min="1" :max="100" style="width: 160px" />
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="200" />
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button v-if="row.configKey !== 'message_push_enabled'" link type="primary" @click="save(row)">保存</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>