# 无限叙事游戏 - 前端项目

## 项目简介

这是一个基于AI的个性化无限流叙事游戏前端项目，使用React 18 + TypeScript + Vite + Ant Design 5.x开发。游戏允许玩家在不同的故事世界中进行选择，每个选择都会影响故事的发展方向。

## 技术栈

- **前端框架**: React 18 + TypeScript
- **构建工具**: Vite 5.x
- **UI组件库**: Ant Design 5.x
- **状态管理**: Zustand
- **路由管理**: React Router DOM
- **动画库**: Framer Motion
- **测试框架**: Jest + React Testing Library
- **部署平台**: Vercel

## 项目结构

```
frontend/
├── public/                    # 静态资源
├── src/
│   ├── components/           # 通用组件
│   │   ├── StoryDisplay.tsx  # 故事展示组件
│   │   └── OptionSelector.tsx # 选项选择组件
│   ├── pages/               # 页面组件
│   │   ├── WorldSelectionPage.tsx # 世界选择页面
│   │   └── GamePage.tsx     # 游戏主页面
│   ├── services/            # 服务层
│   │   ├── api.ts           # API服务
│   │   ├── contentFilter.ts # 内容过滤器
│   │   ├── aiClient.ts      # AI客户端
│   │   └── narrativeEngine.ts # 叙事引擎
│   ├── store/               # 状态管理
│   │   └── index.ts         # Zustand store
│   ├── types/               # 类型定义
│   │   └── index.ts         # 核心数据模型
│   ├── utils/               # 工具函数
│   │   └── index.ts         # 工具集合
│   ├── styles/              # 样式文件
│   │   ├── global.css       # 全局样式
│   │   └── game.css         # 游戏专用样式
│   ├── __tests__/           # 测试文件
│   │   └── utils.test.ts    # 工具函数测试
│   ├── setupTests.ts        # 测试配置
│   ├── App.tsx              # 根组件
│   └── main.tsx             # 入口文件
├── .env.example             # 环境变量示例
├── package.json             # 依赖配置
├── tsconfig.json            # TypeScript配置
├── vite.config.ts           # Vite配置
├── jest.config.js           # Jest配置
├── vercel.json              # Vercel部署配置
└── README.md                # 项目说明
```

## 核心功能

### 1. 世界选择系统
- AI根据玩家属性推荐适合的世界
- 支持三个初始世界：赛博蓬莱、法典迷城、衰败王座
- 属性要求检查和世界解锁机制

### 2. 叙事生成系统
- 基于结构化提示的AI内容生成
- 维护世界状态和故事连贯性
- 实时内容安全过滤

### 3. 交互系统
- 文字逐字显示动画
- 选项选择和故事分支
- 玩家属性成长系统

### 4. 状态管理
- 本地存储游戏进度
- 玩家属性和世界状态持久化
- 故事历史记录

## 安装和运行

### 1. 安装依赖

```bash
npm install
```

### 2. 配置环境变量

复制 `.env.example` 为 `.env.local` 并填写实际配置：

```bash
cp .env.example .env.local
```

编辑 `.env.local` 文件：

```env
# API配置
VITE_API_BASE_URL=https://api.katcoder.pro/v1
VITE_API_KEY=your_api_key_here

# 应用配置
VITE_APP_TITLE=无限叙事游戏
VITE_APP_DESCRIPTION=个性化无限流叙事游戏
```

### 3. 开发模式

```bash
npm run dev
```

### 4. 构建项目

```bash
npm run build
```

### 5. 运行测试

```bash
npm run test
```

## 部署

### Vercel部署

1. 安装Vercel CLI：

```bash
npm install -g vercel
```

2. 登录并部署：

```bash
vercel login
vercel
```

### 环境变量配置

在Vercel中配置以下环境变量：

- `VITE_API_BASE_URL`: AI API基础URL
- `VITE_API_KEY`: AI API密钥

## API接口

### 叙事生成接口

```typescript
POST /generate
Content-Type: application/json

{
  "system_instruction": "你将是《赛博蓬莱》世界的叙述者...",
  "world_state": {
    "current_world": "赛博蓬莱",
    "key_characters": {},
    "unlocked_locations": [],
    "core_puzzles": {},
    "story_threads": []
  },
  "recent_story": "故事片段...",
  "player_attributes": {
    "insight": 10,
    "persuasion": 10,
    "魄力": 10
  },
  "available_worlds": ["赛博蓬莱", "法典迷城", "衰败王座"],
  "active_quests": [],
  "inventory_items": [],
  "faction_relations": {}
}
```

响应格式：

```typescript
{
  "story_segment": "生成的故事片段...",
  "generated_options": [
    {
      "option_id": "option_1",
      "text": "选择1",
      "description": "描述...",
      "required_attributes": { "insight": 8 },
      "potential_consequences": ["可能的后果"],
      "estimated_time_cost": 5
    }
  ],
  "new_world_state": { /* 更新的世界状态 */ },
  "confidence_score": 0.95
}
```

## 开发指南

### 添加新世界

1. 在 `WorldSelectionPage.tsx` 中添加世界配置
2. 在 `narrativeEngine.ts` 中添加世界特定的系统指令
3. 更新类型定义以支持新世界

### 添加新属性

1. 在 `PlayerAttributes` 接口中添加新属性
2. 更新默认属性值
3. 在UI中显示新属性

### 自定义内容过滤

1. 在 `ContentFilter.ts` 中添加新的过滤规则
2. 更新风险关键词库
3. 调整过滤策略

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 Issue
- 发送邮件至项目维护者
- 在 Discord 社区中讨论

## 致谢

- DeepSeek 团队提供强大的AI模型支持
- Ant Design 团队提供优秀的UI组件库
- React 社区提供丰富的生态系统