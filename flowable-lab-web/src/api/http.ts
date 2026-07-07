import axios from 'axios'

export const http = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
})

export type ApiResponse<T> = {
  success: boolean
  data: T
  message: string
}
