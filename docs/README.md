# 个人投资记账系统（stock-picker 改造版）

在原有「智能选股」小工具基础上升级成的全栈后台系统：保留选股/行情能力，新增 RBAC 权限、个人投资记账（含股票买卖手续费）、数据看板与操作日志。

## 1. 技术栈

| 层 | 选型 |
|---|---|
| 后端 | Spring Boot 3.3.5、Java 21、Spring Data JPA / Hibernate |
| 数据库 | MySQL 8.4（`assess_finance`，utf8mb4）；`dev` profile 可切 H2 文件库 |
| 安全 | JWT（jjwt 0.12.6）、自定义拦截器 + `@RequirePerm` 按钮级权限、BCrypt 密码哈希 |
| 报表 | EasyExcel 3.3.4 |
| 前端 | Vue 3 + Vite 6 + Element Plus + Pinia + Vue Router（hash 模式） |
| 可视化 | ECharts 5.5 |
| 部署形态 | 前端构建产物输出到 `src/main/resources/static`，后端打成单 jar 部署 |

## 2. 目录结构

```
stock-picker/
├── pom.xml
├── docs/                          # 改造方案与工程文档
├── frontend/                      # Vue 3 前端工程
│   ├── src/api/                   # 接口封装（request / finance / system / stock / menu / auth）
│   ├── src/layout/                # 侧边栏 + 顶栏布局
│   ├── src/router/index.js        # 静态路由 + 按菜单动态注册 + 路由守卫
│   └── src/views/                 # login / dashboard / record / stock / system ...
├── src/main/java/com/example/stockpicker/
│   ├── common/                    # R 统一返回体、PageResult、BizException、BaseEntity
│   ├── config/                    # WebConfig（拦截器、静态资源）
│   ├── security/                  # JwtUtils、JwtInterceptor、UserContext、@RequirePerm、切面
│   ├── system/                    # 登录、用户、角色、菜单、操作日志、数据初始化
│   ├── finance/                   # 收支分类、收支记录、统计、导出、图表
│   └── stock/                     # 选股、行情、策略对比、个股分析（原有能力）
├── src/main/resources/
│   ├── application.yml            # dev(H2) / mysql 双 profile
│   └── static/                    # 前端构建产物
└── logs/                          # 运行日志（已加入 .gitignore）
```

## 3. 启动方式

### 3.1 数据库

MySQL 8 需先启动并创建库（表由 JPA `ddl-auto=update` 自动创建）：

```sql
CREATE DATABASE assess_finance DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

连接参数全部走环境变量，不在代码里硬编码：

| 变量 | 默认值 | 说明 |
|---|---|---|
| `DB_HOST` | localhost | 数据库地址 |
| `DB_PORT` | 3306 | 端口 |
| `DB_NAME` | assess_finance | 库名 |
| `DB_USERNAME` | root | 账号 |
| `DB_PASSWORD` | （无默认） | 密码，必须通过环境变量注入 |
| `JWT_SECRET` | 内置示例值 | 生产环境务必覆盖 |

### 3.2 后端

```powershell
$env:DB_PASSWORD="你的密码"
.\tools\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run   # 默认 mysql profile，端口 8082
```

- 端口固定 8082（本机 8080 被代理占用），可在 `application.yml` 调整。
- 首次启动会自动写入菜单/权限种子、ADMIN 角色与管理员账号 `admin / admin123`，并初始化 10 条收支分类。
- 无 MySQL 环境时可 `--spring.profiles.active=dev` 退回 H2 文件库。

### 3.3 前端

```powershell
cd frontend
npm install
npm run build        # 产物输出到 ../src/main/resources/static
npm run dev          # 本地开发，/api 代理到 8082
```

注意：Vite 6 要求 Node 18+，本机若默认 Node 为 14，请切换到 Node 20 再构建。

## 4. 日志目录

- 运行日志：`logs/app.log`（由 `application.yml` 的 `logging.file.name` 指定），按天滚动。
- 操作日志：落库表 `sys_oper_log`，页面入口「系统管理 → 操作日志」，可按关键字、日期筛选并展开查看请求参数与失败原因。

## 5. 功能与接口

### 5.1 认证与权限

- `POST /api/auth/login`：登录换取 JWT；`GET /api/auth/me`：当前用户信息与权限码。
- `GET /api/menus/routes`：按角色返回可见菜单树，前端据此动态注册路由。
- 后端 `@RequirePerm("system:user:add")` 控制接口；前端 `v-permission` 控制按钮显隐。

### 5.2 系统管理

| 模块 | 接口前缀 | 说明 |
|---|---|---|
| 用户 | `/api/system/users` | 分页/新增/编辑/删除/启停/重置密码，禁止操作当前登录人 |
| 角色 | `/api/system/roles` | CRUD + `GET/PUT /{id}/menus` 分配菜单权限；ADMIN 角色受保护 |
| 菜单 | `/api/menus` | `/routes` 动态路由、`/admin-tree` 全量树、CRUD（含类型/路径/权限标识校验） |
| 操作日志 | `/api/system/logs` | 分页查询，支持关键字与日期区间 |

### 5.3 财务记账

- 分类：`/api/finance/categories` CRUD，收入/支出两类，删除被引用分类会被拦截。
- 记录：`/api/finance/records` 分页筛选（时间、类型、分类、关键词）、新增/编辑/删除、`/batch-delete` 批量删除、`/export` 导出 Excel。
- 统计：`/stat` 总收入/总支出/结余 + 分类汇总，金额全程 `BigDecimal`。
- 股票买卖自动计算：
  - 买入金额 = 成交额 + 佣金 + 过户费（印花税强制为 0）
  - 卖出金额 = 成交额 − 佣金 − 印花税 − 过户费
- 看板：`/chart/monthly-trend`、`/chart/weekly-bar`、`/chart/category-pie`。

### 5.4 选股

- `/api/stocks/pick`、`/screen`、`/compare`、`/analyze` 保持原有契约（直接返回数据、不包 `R`，前端请求层已做兼容）。
- 新前端只迁移「行情查询」页：`/stock/query`，权限码 `stock:query`。

## 6. 验收要点速查

| 验收点 | 验证方式 |
|---|---|
| 密码非明文 | 查 `sys_user.password` 以 `$2a$` 开头 |
| 动态菜单 | 给新角色只勾部分菜单，新用户登录仅看到这部分 |
| 按钮权限 | 同上，无权限用户看不到增删改按钮 |
| 金额精度 | 录入 0.1 + 0.2 类数据，结余精确 |
| 软删除 | 删除记录后列表不显示，库中 `is_deleted=1` |
| Excel 无乱码 | 导出文件中文表头正常 |
| 操作日志 | 新增一条记录后查 `sys_oper_log` 或日志页 |
| 图表联动 | 新增记录后回看板数字变化 |
