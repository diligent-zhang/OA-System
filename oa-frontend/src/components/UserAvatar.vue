<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { loadAvatar, avatarVersion } from '@/api/avatar'

const props = defineProps({
  userId: { type: [Number, String], default: null },
  name: { type: String, default: '' },
  /** 是否设置了头像；为 false 时直接显示姓名首字，不发起请求 */
  hasAvatar: { type: Boolean, default: true },
  size: { type: Number, default: 40 }
})

const src = ref('')
const initial = computed(() => {
  const n = (props.name || 'U').trim()
  return n ? n.charAt(0).toUpperCase() : 'U'
})

async function refresh() {
  if (!props.userId || !props.hasAvatar) {
    src.value = ''
    return
  }
  src.value = await loadAvatar(props.userId)
}

watch(() => [props.userId, props.hasAvatar, avatarVersion.value], refresh)
onMounted(refresh)
</script>

<template>
  <el-avatar :size="size" :src="src || undefined">
    {{ initial }}
  </el-avatar>
</template>
