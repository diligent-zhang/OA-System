<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  docFolderTree, docFolderCreate, docFolderRename, docFolderDelete,
  docPerms, docPermAdd, docPermRemove,
  docFiles, docFileUpload, docFileRename, docFileDelete, docFileVersions, docFileDownload, docVersionDownload, docFileSearch,
  downloadBlob
} from '@/api'

const loading = ref(false)
const tree = ref([])
const files = ref([])
const currentFolder = ref(null)
const searchName = ref('')
const searching = ref(false)

const uploadVisible = ref(false)
const uploadList = ref([])
const overwrite = ref(false)
const uploading = ref(false)

const newFolderVisible = ref(false)
const newFolderName = ref('')
const permVisible = ref(false)
const perms = ref([])
const permForm = ref({ permType: 'READ', grantType: 'DEPT', grantId: null })
const versionVisible = ref(false)
const versions = ref([])

const previewVisible = ref(false)
const previewUrl = ref('')
const previewName = ref('')

function flatFolders(nodes, out = []) {
  nodes.forEach((n) => {
    out.push({ folderId: n.folderId, name: n.name, parentId: n.parentId, canEdit: n.canEdit })
    flatFolders(n.children || [], out)
  })
  return out
}

async function loadTree() {
  const res = await docFolderTree()
  tree.value = res.data || []
}

async function selectFolder(node) {
  currentFolder.value = node
  searchName.value = ''
  searching.value = false
  loading.value = true
  try {
    const res = await docFiles(node.folderId)
    files.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function doSearch() {
  if (!searchName.value) return
  searching.value = true
  const res = await docFileSearch({ name: searchName.value, pageNum: 1, pageSize: 50 })
  files.value = res.data.list || []
}

function clearSearch() {
  if (currentFolder.value) selectFolder(currentFolder.value)
}

async function createFolder() {
  if (!newFolderName.value) {
    ElMessage.warning('请输入目录名称')
    return
  }
  const parentId = currentFolder.value ? currentFolder.value.folderId : 1
  await docFolderCreate(parentId, newFolderName.value)
  ElMessage.success('目录已创建')
  newFolderVisible.value = false
  newFolderName.value = ''
  await loadTree()
  if (currentFolder.value) {
    selectFolder(currentFolder.value)
  }
}

function chooseUpload(event) {
  uploadList.value = Array.from(event.target.files || [])
}

async function doUpload(folderId) {
  uploading.value = true
  try {
    for (const file of uploadList.value) {
      const fd = new FormData()
      fd.append('file', file)
      await docFileUpload(folderId, fd, overwrite.value)
    }
    ElMessage.success('上传完成')
    uploadVisible.value = false
    uploadList.value = []
    selectFolder(currentFolder.value)
  } finally {
    uploading.value = false
  }
}

async function renameFile(file) {
  const { value } = await ElMessageBox.prompt('输入新文件名', '重命名', { inputValue: file.name, inputPattern: /.+/, inputErrorMessage: '文件名不能为空' })
  await docFileRename(file.fileId, value)
  ElMessage.success('已重命名')
  selectFolder(currentFolder.value)
}

async function removeFile(file) {
  await ElMessageBox.confirm(`确认删除「${file.name}」？`, '删除', { type: 'warning' })
  await docFileDelete(file.fileId)
  ElMessage.success('已删除')
  selectFolder(currentFolder.value)
}

async function downloadFile(file) {
  const blob = await docFileDownload(file.fileId)
  downloadBlob(blob, file.name)
}

async function showVersions(file) {
  const res = await docFileVersions(file.fileId)
  versions.value = res.data || []
  versionVisible.value = true
}

async function downloadVersion(v) {
  const blob = await docVersionDownload(v.versionId)
  downloadBlob(blob, v.originalName || `version-${v.versionNo}`)
}

function previewFile(file) {
  const ext = (file.ext || '').toLowerCase()
  if (!['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'pdf'].includes(ext)) {
    ElMessage.info('该类型暂不支持在线预览，请下载查看')
    return
  }
  previewName.value = file.name
  previewUrl.value = `http://localhost:8080/api/doc/files/${file.fileId}/download?inline=true`
  previewVisible.value = true
}

async function showPerms() {
  if (!currentFolder.value) return
  const res = await docPerms(currentFolder.value.folderId)
  perms.value = res.data || []
  permVisible.value = true
}

async function addPerm() {
  if (!permForm.value.grantId) {
    ElMessage.warning('请填写授权对象ID')
    return
  }
  await docPermAdd(currentFolder.value.folderId, permForm.value)
  ElMessage.success('已授权')
  const res = await docPerms(currentFolder.value.folderId)
  perms.value = res.data || []
}

async function removePerm(p) {
  await docPermRemove(currentFolder.value.folderId, { permType: p.permType, grantType: p.grantType, grantId: p.grantId })
  ElMessage.success('授权已取消')
  const res = await docPerms(currentFolder.value.folderId)
  perms.value = res.data || []
}

async function renameFolderNode(node) {
  const { value } = await ElMessageBox.prompt('输入新目录名称', '重命名目录', { inputValue: node.name, inputPattern: /.+/, inputErrorMessage: '名称不能为空' })
  await docFolderRename(node.folderId, value)
  ElMessage.success('已重命名')
  await loadTree()
}

async function removeFolderNode(node) {
  await ElMessageBox.confirm(`确认删除目录「${node.name}」？（空目录才可删除）`, '删除', { type: 'warning' })
  await docFolderDelete(node.folderId)
  ElMessage.success('已删除')
  await loadTree()
}

function fmtSize(size) {
  if (!size && size !== 0) return '—'
  if (size < 1024) return `${size}B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`
  return `${(size / 1024 / 1024).toFixed(1)}MB`
}

onMounted(loadTree)
</script>

<template>
  <div class="oa-page">
    <h3 class="oa-page-title">文档中心</h3>
    <el-row :gutter="16">
      <!-- 目录树 -->
      <el-col :md="7" :sm="24">
        <div class="oa-page-card">
          <div class="card-head">
            <h4 class="card-title">目录</h4>
            <el-button size="small" type="primary" @click="newFolderVisible = true">新建目录</el-button>
          </div>
          <el-tree :data="tree" node-key="folderId" highlight-current :props="{ label: 'name', children: 'children' }"
                   @node-click="selectFolder">
            <template #default="{ node, data }">
              <span class="folder-node">
                <el-icon><Folder /></el-icon>
                {{ node.label }}
                <span v-if="!data.canEdit" class="readonly-tag">只读</span>
              </span>
            </template>
          </el-tree>
        </div>
      </el-col>

      <!-- 文件列表 -->
      <el-col :md="17" :sm="24">
        <div class="oa-page-card">
          <div class="card-head">
            <h4 class="card-title">{{ searching ? '检索结果' : (currentFolder ? currentFolder.name : '选择目录') }}</h4>
            <div style="display: flex; gap: 8px">
              <el-input v-model="searchName" placeholder="按文件名搜索" style="width: 200px" clearable
                        @keyup.enter="doSearch" @clear="clearSearch">
                <template #suffix><el-icon @click="doSearch"><Search /></el-icon></template>
              </el-input>
              <el-button @click="doSearch">搜索</el-button>
              <el-button type="primary" :disabled="!currentFolder || !currentFolder.canEdit" @click="uploadVisible = true">
                上传文件
              </el-button>
              <el-button :disabled="!currentFolder || !currentFolder.canEdit" @click="showPerms">权限</el-button>
            </div>
          </div>

          <el-table v-loading="loading" :data="files" border stripe>
            <el-table-column prop="name" label="文件名" min-width="220">
              <template #default="{ row }">
                <div class="file-name">
                  <el-icon style="color: var(--oa-primary)"><Document /></el-icon>
                  <span>{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="大小" width="90">
              <template #default="{ row }"><span class="oa-mono">{{ fmtSize(row.size) }}</span></template>
            </el-table-column>
            <el-table-column label="版本" width="70">
              <template #default="{ row }"><span class="oa-mono">v{{ row.versionNum }}</span></template>
            </el-table-column>
            <el-table-column prop="uploaderName" label="上传人" width="90" />
            <el-table-column label="操作" width="240" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="previewFile(row)">预览</el-button>
                <el-button link type="primary" @click="downloadFile(row)">下载</el-button>
                <el-button link type="primary" @click="showVersions(row)">版本</el-button>
                <el-button v-if="currentFolder?.canEdit" link type="primary" @click="renameFile(row)">重命名</el-button>
                <el-button v-if="currentFolder?.canEdit" link type="danger" @click="removeFile(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && !files.length" description="目录为空或暂无匹配文件" />
        </div>
      </el-col>
    </el-row>

    <!-- 新建目录 -->
    <el-dialog v-model="newFolderVisible" title="新建目录" width="380px">
      <el-input v-model="newFolderName" placeholder="目录名称" maxlength="60" />
      <template #footer>
        <el-button @click="newFolderVisible = false">取消</el-button>
        <el-button type="primary" @click="createFolder">创建</el-button>
      </template>
    </el-dialog>

    <!-- 上传 -->
    <el-dialog v-model="uploadVisible" title="上传文件到「当前目录」" width="460px">
      <input type="file" multiple @change="chooseUpload" />
      <div style="margin-top: 12px">
        <el-checkbox v-model="overwrite">同名文件覆盖上传（保留历史版本）</el-checkbox>
      </div>
      <div v-if="uploadList.length" style="margin-top: 8px; color: var(--oa-ink-light); font-size: 13px">
        已选 {{ uploadList.length }} 个文件
      </div>
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" :disabled="!uploadList.length" @click="doUpload(currentFolder.folderId)">上传</el-button>
      </template>
    </el-dialog>

    <!-- 权限 -->
    <el-dialog v-model="permVisible" title="目录权限" width="480px">
      <div class="perm-form">
        <el-select v-model="permForm.permType" style="width: 110px">
          <el-option label="可读" value="READ" />
          <el-option label="可编辑" value="EDIT" />
        </el-select>
        <el-select v-model="permForm.grantType" style="width: 110px">
          <el-option label="部门" value="DEPT" />
          <el-option label="人员" value="USER" />
        </el-select>
        <el-input-number v-model="permForm.grantId" :min="1" placeholder="对象ID" style="width: 130px" />
        <el-button type="primary" @click="addPerm">添加</el-button>
      </div>
      <el-table :data="perms" border size="small" style="margin-top: 12px">
        <el-table-column label="权限" width="90">
          <template #default="{ row }">{{ row.permType === 'READ' ? '可读' : '可编辑' }}</template>
        </el-table-column>
        <el-table-column label="授权对象" width="90">
          <template #default="{ row }">{{ row.grantType === 'DEPT' ? '部门' : '人员' }}</template>
        </el-table-column>
        <el-table-column prop="grantId" label="对象ID" width="100" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button link type="danger" size="small" @click="removePerm(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="perm-tip">说明：未授权目录继承上级目录；根目录默认全员可读、仅管理员/建者可编辑。</div>
    </el-dialog>

    <!-- 版本 -->
    <el-dialog v-model="versionVisible" title="文件版本（含当前版本）" width="560px">
      <el-table :data="versions" border size="small">
        <el-table-column prop="versionNo" label="版本" width="80">
          <template #default="{ row }"><span class="oa-mono">v{{ row.versionNo }}</span></template>
        </el-table-column>
        <el-table-column prop="originalName" label="文件名" min-width="200" />
        <el-table-column prop="uploaderName" label="操作人" width="90" />
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="downloadVersion(row)">下载</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 预览 -->
    <el-dialog v-model="previewVisible" :title="previewName" width="800px" top="4vh">
      <iframe v-if="previewUrl" :src="previewUrl" style="width: 100%; height: 70vh; border: none"></iframe>
    </el-dialog>
  </div>
</template>

<style scoped>
.card-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; flex-wrap: wrap; gap: 8px; }
.card-title { margin: 0; font-size: 14px; color: var(--oa-navy); }
.folder-node { display: flex; align-items: center; gap: 5px; }
.readonly-tag { color: var(--oa-ink-light); font-size: 11px; }
.file-name { display: flex; align-items: center; gap: 6px; }
.perm-form { display: flex; gap: 8px; align-items: center; }
.perm-tip { color: var(--oa-ink-light); font-size: 12px; margin-top: 10px; }
</style>