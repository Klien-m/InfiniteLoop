import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { NarrativeContext, NarrativeResponse, AIClientConfig } from '@/types'

class ApiService {
  private client: AxiosInstance
  private config: AIClientConfig

  constructor(config: AIClientConfig) {
    this.config = config
    this.client = axios.create({
      baseURL: config.baseUrl,
      timeout: config.timeoutSeconds * 1000,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${config.apiKey}`,
      },
    })

    this.setupInterceptors()
  }

  private setupInterceptors() {
    // 请求拦截器
    this.client.interceptors.request.use(
      (config) => {
        console.log('API Request:', config.method?.toUpperCase(), config.url)
        return config
      },
      (error) => {
        console.error('API Request Error:', error)
        return Promise.reject(error)
      }
    )

    // 响应拦截器
    this.client.interceptors.response.use(
      (response: AxiosResponse) => {
        console.log('API Response:', response.status, response.data)
        return response
      },
      (error) => {
        console.error('API Response Error:', error)
        return Promise.reject(error)
      }
    )
  }

  // 生成叙事内容
  async generateNarrative(context: NarrativeContext): Promise<NarrativeResponse> {
    try {
      const response = await this.client.post<NarrativeResponse>('/generate', context)
      return response.data
    } catch (error) {
      console.error('Failed to generate narrative:', error)
      throw new Error('AI生成失败，请稍后重试')
    }
  }

  // 健康检查
  async healthCheck(): Promise<boolean> {
    try {
      const response = await this.client.post('/health')
      return response.data.status === 'healthy'
    } catch (error) {
      console.error('Health check failed:', error)
      return false
    }
  }

  // 更新配置
  updateConfig(config: Partial<AIClientConfig>) {
    this.config = { ...this.config, ...config }
    this.client.defaults.baseURL = this.config.baseUrl
    this.client.defaults.headers['Authorization'] = `Bearer ${this.config.apiKey}`
  }
}

export default ApiService