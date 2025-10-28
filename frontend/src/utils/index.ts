/**
 * 工具函数集合
 */

/**
 * 生成唯一的玩家ID
 */
export function generatePlayerId(): string {
  return `player_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

/**
 * 格式化时间戳
 */
export function formatTimestamp(timestamp: number): string {
  return new Date(timestamp).toLocaleString('zh-CN')
}

/**
 * 检查是否为空值
 */
export function isEmpty(value: any): boolean {
  return value === null || value === undefined || value === ''
}

/**
 * 深拷贝对象
 */
export function deepClone<T>(obj: T): T {
  return JSON.parse(JSON.stringify(obj))
}

/**
 * 防抖函数
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: number
  
  return (...args: Parameters<T>) => {
    clearTimeout(timeout)
    timeout = setTimeout(() => func(...args), wait)
  }
}

/**
 * 节流函数
 */
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  limit: number
): (...args: Parameters<T>) => void {
  let inThrottle: boolean
  
  return (...args: Parameters<T>) => {
    if (!inThrottle) {
      func(...args)
      inThrottle = true
      setTimeout(() => (inThrottle = false), limit)
    }
  }
}

/**
 * 随机选择数组中的一个元素
 */
export function randomChoice<T>(array: T[]): T {
  return array[Math.floor(Math.random() * array.length)]
}

/**
 * 计算字符串的字符数（支持中文）
 */
export function getStringLength(str: string): number {
  return str.replace(/[\u0391-\uFFE5]/g, 'aa').length
}

/**
 * 截取字符串（支持中文）
 */
export function truncateString(str: string, length: number): string {
  if (getStringLength(str) <= length) return str
  
  let result = ''
  let count = 0
  
  for (const char of str) {
    const charLength = /[\u0391-\uFFE5]/.test(char) ? 2 : 1
    if (count + charLength > length) break
    result += char
    count += charLength
  }
  
  return result + '...'
}

/**
 * 检查是否为移动设备
 */
export function isMobile(): boolean {
  return window.innerWidth <= 768
}

/**
 * 检查是否支持触摸
 */
export function isTouchDevice(): boolean {
  return 'ontouchstart' in window || navigator.maxTouchPoints > 0
}

/**
 * 存储管理
 */
export class StorageManager {
  private static readonly PREFIX = 'infinite_narrative_'

  static setItem(key: string, value: any): void {
    try {
      localStorage.setItem(
        this.PREFIX + key,
        JSON.stringify(value)
      )
    } catch (error) {
      console.error('存储失败:', error)
    }
  }

  static getItem<T>(key: string, defaultValue?: T): T | null {
    try {
      const item = localStorage.getItem(this.PREFIX + key)
      return item ? JSON.parse(item) : defaultValue || null
    } catch (error) {
      console.error('读取存储失败:', error)
      return defaultValue || null
    }
  }

  static removeItem(key: string): void {
    try {
      localStorage.removeItem(this.PREFIX + key)
    } catch (error) {
      console.error('删除存储失败:', error)
    }
  }

  static clear(): void {
    try {
      const keys = Object.keys(localStorage)
      keys.forEach(key => {
        if (key.startsWith(this.PREFIX)) {
          localStorage.removeItem(key)
        }
      })
    } catch (error) {
      console.error('清除存储失败:', error)
    }
  }
}

/**
 * 错误处理
 */
export class ErrorHandler {
  static handle(error: any, context: string = '未知操作'): void {
    console.error(`${context}发生错误:`, error)
    
    // 可以在这里添加错误上报逻辑
    if (process.env.NODE_ENV === 'development') {
      alert(`操作失败: ${error.message || '未知错误'}`)
    }
  }

  static async handleAsync<T>(
    promise: Promise<T>,
    context: string = '异步操作'
  ): Promise<T | null> {
    try {
      return await promise
    } catch (error) {
      this.handle(error, context)
      return null
    }
  }
}

/**
 * 动画工具
 */
export class AnimationUtils {
  /**
   * 文字逐字显示动画
   */
  static async typeWriter(text: string, callback: (displayed: string) => void, speed: number = 50): Promise<void> {
    let index = 0
    const timer = setInterval(() => {
      if (index <= text.length) {
        callback(text.substring(0, index))
        index++
      } else {
        clearInterval(timer)
      }
    }, speed)
  }

  /**
   * 缓动函数
   */
  static easeInOut(t: number): number {
    return t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t
  }

  /**
   * 颜色插值
   */
  static interpolateColor(color1: string, color2: string, factor: number): string {
    const hex = (color: string) => parseInt(color.replace('#', ''), 16)
    const r1 = (hex(color1) >> 16) & 0xff
    const g1 = (hex(color1) >> 8) & 0xff
    const b1 = hex(color1) & 0xff
    
    const r2 = (hex(color2) >> 16) & 0xff
    const g2 = (hex(color2) >> 8) & 0xff
    const b2 = hex(color2) & 0xff
    
    const r = Math.round(r1 + factor * (r2 - r1))
    const g = Math.round(g1 + factor * (g2 - g1))
    const b = Math.round(b1 + factor * (b2 - b1))
    
    return `#${((r << 16) + (g << 8) + b).toString(16).padStart(6, '0')}`
  }
}