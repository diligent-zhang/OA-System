import { get, post, put } from './request'

/* 即时通讯 */
export const chatConversations = () => get('/chat/conversations')
export const chatOpen = (data) => post('/chat/conversations', data)
export const chatMessages = (id, params) => get(`/chat/conversations/${id}/messages`, params)
export const chatMarkRead = (id) => put(`/chat/conversations/${id}/read`)
export const chatSend = (data) => post('/chat/messages', data)
