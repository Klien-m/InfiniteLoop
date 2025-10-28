// 工具函数测试

import { 
  generatePlayerId, 
  formatTimestamp, 
  isEmpty, 
  deepClone,
  randomChoice,
  getStringLength,
  truncateString
} from '../utils'

describe('工具函数测试', () => {
  describe('generatePlayerId', () => {
    it('应该生成唯一的玩家ID', () => {
      const id1 = generatePlayerId()
      const id2 = generatePlayerId()
      
      expect(id1).toMatch(/^player_\d+_[a-z0-9]+$/)
      expect(id2).toMatch(/^player_\d+_[a-z0-9]+$/)
      expect(id1).not.toBe(id2)
    })

    it('生成的ID应该包含时间戳', () => {
      const id = generatePlayerId()
      const parts = id.split('_')
      
      expect(parts).toHaveLength(3)
      expect(parts[0]).toBe('player')
      expect(Number(parts[1])).toBeGreaterThan(1000000000000)
    })
  })

  describe('formatTimestamp', () => {
    it('应该正确格式化时间戳', () => {
      const timestamp = 1698400000000 // 2023-10-27 00:00:00
      const formatted = formatTimestamp(timestamp)
      
      expect(formatted).toContain('2023')
      expect(formatted).toContain('10')
      expect(formatted).toContain('27')
    })
  })

  describe('isEmpty', () => {
    it('应该正确识别空值', () => {
      expect(isEmpty(null)).toBe(true)
      expect(isEmpty(undefined)).toBe(true)
      expect(isEmpty('')).toBe(true)
      expect(isEmpty([])).toBe(true)
      expect(isEmpty({})).toBe(true)
    })

    it('应该正确识别非空值', () => {
      expect(isEmpty('hello')).toBe(false)
      expect(isEmpty(0)).toBe(false)
      expect(isEmpty(false)).toBe(false)
      expect(isEmpty([1, 2, 3])).toBe(false)
      expect(isEmpty({ a: 1 })).toBe(false)
    })
  })

  describe('deepClone', () => {
    it('应该深度克隆对象', () => {
      const original = {
        a: 1,
        b: {
          c: 2,
          d: [3, 4, 5]
        }
      }
      
      const cloned = deepClone(original)
      
      expect(cloned).toEqual(original)
      expect(cloned).not.toBe(original)
      expect(cloned.b).not.toBe(original.b)
      expect(cloned.b.d).not.toBe(original.b.d)
    })

    it('应该深度克隆数组', () => {
      const original = [1, 2, [3, 4, [5, 6]]]
      const cloned = deepClone(original)
      
      expect(cloned).toEqual(original)
      expect(cloned).not.toBe(original)
      expect(cloned[2]).not.toBe(original[2])
      expect(cloned[2][2]).not.toBe(original[2][2])
    })
  })

  describe('randomChoice', () => {
    it('应该从数组中随机选择一个元素', () => {
      const array = [1, 2, 3, 4, 5]
      const choice = randomChoice(array)
      
      expect(array).toContain(choice)
      expect(typeof choice).toBe('number')
    })

    it('应该处理单元素数组', () => {
      const array = ['only']
      const choice = randomChoice(array)
      
      expect(choice).toBe('only')
    })
  })

  describe('getStringLength', () => {
    it('应该正确计算字符串长度（支持中文）', () => {
      expect(getStringLength('hello')).toBe(5)
      expect(getStringLength('你好')).toBe(4) // 中文字符每个算2个长度
      expect(getStringLength('hello你好')).toBe(9) // 5 + 4
      expect(getStringLength('')).toBe(0)
    })
  })

  describe('truncateString', () => {
    it('应该正确截取字符串', () => {
      expect(truncateString('hello world', 5)).toBe('hello...')
      expect(truncateString('你好世界', 5)).toBe('你好...')
      expect(truncateString('short', 10)).toBe('short')
      expect(truncateString('', 5)).toBe('')
    })
  })
})