# s-pay-mall-wei（wmall 商城）

Spring Cloud 微服务商城 + Vue 3 前端，支持购物车、订单、支付宝沙箱支付、微信扫码登录、RabbitMQ 延迟关单等。

> 本仓库为进化版（微服务架构）。

## 项目结构

```
├── wmall/              # 后端（Spring Cloud 微服务）
├── wmall-portal/       # 前端（Vue 3 + Vite + Element Plus）
├── mysql/              # 数据库初始化脚本
├── scripts/            # natapp 等辅助脚本（仅示例）
└── README.md
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 11、Spring Boot 2.7、Spring Cloud、Nacos、MyBatis-Plus、RabbitMQ |
| 前端 | Vue 3、Vite、Pinia、Vue Router、Element Plus、Axios |
| 中间件 | MySQL、Redis、Nacos、RabbitMQ |
| 支付 | 支付宝沙箱 |
| 登录 | 用户名密码 + 微信扫码 |

## 后端服务

| 服务 | 端口 | 说明 |
|------|------|------|
| gateway | 8080 | API 网关、JWT 鉴权 |
| item-service | 8081 | 商品 |
| cart-service | 8082 | 购物车 |
| user-service | 8084 | 用户、微信登录 |
| trade-service | 8085 | 订单 |
| pay-service | 8086 | 支付 |

## 快速开始

### 1. 中间件

本地需启动：MySQL、Redis、Nacos、RabbitMQ（端口与 `application.yaml` 中一致）。

### 2. 数据库

在 MySQL 中依次执行 `mysql/` 目录下脚本：

| 脚本 | 说明 |
|------|------|
| `mysql/nacos.sql` | Nacos 配置中心（默认账号 `nacos` / `nacos`） |
| `mysql/wmall-user.sql` | 用户库（含演示账号 Jack / Rose，密码 `123`） |
| `mysql/wmall-item.sql` | 商品库（含 5 条演示商品） |
| `mysql/wmall-cart.sql` | 购物车库 |
| `mysql/wmall-trade.sql` | 订单库 |
| `mysql/wmall-pay.sql` | 支付库 |

### 3. 本地私密配置

各服务提供 `application-local.yaml.example`，复制为 `application-local.yaml` 并填入本地密码、支付宝/微信密钥。**此文件已在 .gitignore，勿提交 Git。**

```bash
# 示例（在对应服务 resources 目录下）
copy application-local.yaml.example application-local.yaml
```

### 4. 启动后端

在 IDEA 中分别运行各服务的 `*Application` 主类，或使用 Maven。

### 5. 启动前端

```bash
cd wmall-portal
npm install
npm run dev
```

浏览器访问 http://localhost:5173 ，API 通过 Vite 代理到 Gateway `http://localhost:8080`。

## 外网回调（natapp，可选）

本地开发时，支付宝异步回调需要公网地址：

1. 注册 [natapp.cn](https://natapp.cn/)，创建隧道，本地端口填 **8080**（Gateway）
2. 复制 `scripts/run-natapp.example.bat` 为 `run-natapp.bat`，填入自己的 authtoken（已在 .gitignore，勿提交）
3. 将 natapp 分配的域名填入 `pay-service` 的 `application-local.yaml` → `alipay.notify_url`

> `natapp.exe` 与真实 token 请勿提交到 Git。

## JWT 密钥库

`user-service` / `gateway` 使用 `wmall.jks` 签发 JWT。请在本地自行生成并放入 `resources/`，**不要提交到 Git**。

## 演示账号

| 用户名 | 密码 | 余额 |
|--------|------|------|
| Jack | 123 | 10000 元（1000000 分） |
| Rose | 123 | 10000 元 |

## License

MIT（可按需修改）
