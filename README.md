# 一站式解决方案，让万物互联触手可及

IoTLink是一个基于 SpringBoot、Vue、Mybatis、RabbitMq、Mysql、Redis 等开发的物联网平台，支持对物联网卡、物联网模组以及卡+模组的融合管理。平台可同时接入中国移动、中国电信、中国联通、第三方物联网卡进行统一管理。提供卡状态、资费、客户、进销存、合同、订单、续费、充值、诊断、账单等功能，逐步完善平台，助您快速接入物联网，让万物互联更简单。

## 核心功能

状态查询：实时查看物联卡的状态信息，确保设备正常运行。

资费管理：灵活设置资费策略，满足不同用户的需求。

客户信息：管理客户资料，维护良好的客户关系。

进销存管理：全面掌控物联卡的库存、进货和销售情况。

合同管理：轻松管理合同信息，避免合同纠纷。

订单处理：快速处理用户订单，提高客户满意度。

续费充值：在线续费充值，方便快捷。

诊断及账单：对物联卡进行智能诊断，并提供详细的账单信息。

## 平台优势

多网络支持：支持中国移动、中国电信、中国联通及第三方的物联网卡，实现统一管理。

通信管理：提供物联卡的综合信息查询及功能配置管理，让您对通信情况了如指掌。

资费管理：灵活设置资费策略，轻松掌握资费情况。

生命周期管理：重新定义物联卡的使用生命周期，帮助您更好地管理和跟踪使用状态。

## 特色亮点

业务与系统分离：优化用户体验，确保系统稳定高效运行。

灵活配置上游通道：一次对接，终生实用，支持二次开发、拓展。

轮询进度查看：实时查看通道下的用量、生命周期、激活时间等类别轮询进度。

数据安全保障：采用私钥加密保障关键数据安全，全程加密传输，防止爬虫获取数据。

首页数据概览：一手掌控核心数据，助您做出明智决策。

ERP功能拓展：即将上线ERP企业常用功能，满足您更多业务需求。

## 部署优势

我们为您提供物联卡云端SaaS部署和本地私有部署解决方案，具有以下优势：

快速部署：降低项目时间和成本。

云端与本地灵活选择：随时随地访问和管理物联卡数据，确保数据安全性和隐私保护。

数据采集与整合：实时收集并整合物联卡产生的各种数据，为您的业务提供有力支持。

可扩展性：根据业务需求进行定制和拓展，适应不断变化的市场需求和技术发展。

高性能与稳定性：经过优化和测试，确保大量物联卡同时在线和高并发场景下业务正常运行。

安全保障：采用先进的加密技术和安全措施，保护您的物联卡数据和业务信息。

IoTLink物联网平台致力于为您提供便捷、高效、安全的物联网解决方案。我们将不断优化平台功能，满足您的不同需求，让物联网管理变得更加轻松。系统全部开源，版本持续更新，毫无保留给个人及企业免费使用，IoTLink都能为您提供专业、周到的服务，让您享受智能化的便利。


特别鸣谢：[RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue)，[element](https://github.com/ElemeFE/element)，[vue-element-admin](https://github.com/PanJiaChen/vue-element-admin)，[eladmin-web](https://github.com/elunez/eladmin-web)

# 一、关于云则

山东云则信息技术有限公司（[官网](http://https://www.5iot.com)）是一家成立于2016年的高新技术企业，专注于物联网平台、物联网模组、物联网终端的研究、研发、生产及销售。我们以“规范、创新、专业、高效”为经营理念，致力于为客户提供专业的物联网解决方案，实现为客户持续创造价值的目标。我们的服务标准是“安全、准时、诚信、责任”，合作原则是“诚信、合作、共赢、发展”。

公司被评为科技型中小企业、国家高新技术企业、山东省专精特新中小企业、山东省软件企业，通过了ISO 9001质量管理体系认证和ISO27001信息安全管理体系认证。我们拥有8项专利、1项作品著作权和36项软件著作权，以创新和应用为动力，为政企客户提供专业的物联网解决方案。

我们的主营业务包括物联网模组（如2G模组、NB-IOT模组、4G模组、5G模组和定位模组）、物联网资费（流量套餐定制、流量管理平台、eSIM卡、VPDN专网）、物联网终端（信号测试仪、4GDTU、4G/5G路由器、4G/5G智能网关）以及针对各行业的物联网解决方案（如智慧农业、智慧社区、智慧园区、公共事业和工业物联网）。此外，我们还推出了物联网综合业务支撑平台(IoTLink V1.0)，支持物联网卡、物联网模组以及卡+模组的融合管理，并开放源代码。

山东云则信息技术有限公司致力于成为客户身边的物联网专家，助您快速接入物联网，让万物互联更加便捷、高效。携手上下游合作伙伴共同抢占物联网市场先机，打造更高效、更安全、更节能、更可持续发展的物联网产业。


# 二、系统功能介绍
## 2.1 系统架构
系统运行框架图：
![输入图片说明](/doc/flie/systemStructure.png)


系统共分为七大模块：

![输入图片说明](/doc/flie/FunctionList.jpg)


## 2.2 系统演示

演示站点:
[http://demo.5iot.com](http://demo.5iot.com)

账号:5iot 密码:123456

文档地址
[http://doc.5iot.com
](http://doc.5iot.com)

视频教程
[https://www.bilibili.com/video/BV1ZK411Q7Vk/?spm_id_from=333.999.0.0](https://www.bilibili.com/video/BV1ZK411Q7Vk/?spm_id_from=333.999.0.0)

## 2.3 系统技术栈

本项目基于 [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue?_from=gitee_search) 后台开发框架，感谢 [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue?_from=gitee_search) 的开源。


| 技术栈 | 介绍 | 地址 |
|:-:|:-:|:-:|
| Vue | 渐进式 JavaScript 框架  |  https://cn.vuejs.org/  |
|  Vuex	|专为 Vue.js 应用程序开发的状态管理模式	|https://vuex.vuejs.org/zh/  |
|Vue Router|	Vue.js 官方的路由管理器|	https://router.vuejs.org/zh/
|Vue CLI	|基于 Vue.js 进行快速开发的完整系统	|https://cli.vuejs.org/zh/guide/
|Vant|	轻量、可靠的移动端 Vue 组件库	|https://vant-contrib.gitee.io/vant/#/zh-CN/
|Element-UI	|基于 Vue 2.0 的桌面端组件库|	https://element.eleme.io/#/zh-CN
|ES6|	JavaScript 语言的下一代标准|	https://es6.ruanyifeng.com/


 **后端技术栈** 
| 技术栈 | 介绍 | 地址 |
|:-:|:-:|:-:|
|Spring Boot	|快捷创建基于 Spring 的生产级应用程序|	https://spring.io/projects/spring-boot
|MyBatis-Plus|	MyBatis 增强工具	|https://mp.baomidou.com/
|MyBatis|	MyBatis 持久层框架	|https://mybatis.org/mybatis-3/zh/index.html
|JWT|	轻量级身份认证规范|	https://jwt.io/introduction
|RabbitMq|	基于AMQP协议的消息中间件 |	https://www.rabbitmq.com/
|Spring Security	|基于 Spring 的强大且高度可定制的身份验证和访问控制框架	|https://spring.io/projects/spring-security/


## 2.4 系统代码结构

**后端结构** 

```
com.yunze     
├── common            // 工具类
│       └── annotation                    // 自定义注解
│       └── config                        // 全局配置
│       └── constant                      // 通用常量
│       └── core                          // 核心控制
│       └── enums                         // 通用枚举
│       └── exception                     // 通用异常
│       └── filter                        // 过滤器处理
│       └── mapper                        // 数据持久化
│       └── utils                         // 通用类处理
├── framework         // 框架核心
│       └── aspectj                       // 注解实现
│       └── config                        // 系统配置
│       └── datasource                    // 数据权限
│       └── interceptor                   // 拦截器
│       └── manager                       // 异步处理
│       └── security                      // 权限控制
│       └── web                           // 前端控制
├── yunze-consumption-admin               // 平台业务分离执行监听
│       └── system                       // 监听yunze-admin业务执行
├── yunze-consumption-car-activatedate   // 轮询 激活时间 执行同步
├── yunze-consumption-car-disconnected   // 未订购停机 消费者
├── yunze-consumption-car-flow           // 轮询 用量 执行同步
├── yunze-consumption-car-status         // 轮询 生命周期 执行同步
├── yunze-consumption-car-stop           // 达量停机 消费者
├── yunze-consumption-order              // 订单充值 消费者
├── yunze-consumption-update             // yz_card_info 表修改 消费者
├── yunze-generator                      // 代码生成
├── yunze-quartz                         // 定时任务
├── yunze-system                         // 系统代码
├── yunze-admin                          // 后台服务
├── yunze-ui                             // 页面前端代码
├── yunze-timed-task                     // 定时任务执行

```

**前端结构** 

```
├── build                      // 构建相关  
├── bin                        // 执行脚本
├── public                     // 公共文件
│   ├── favicon.ico            // favicon图标
│   └── index.html             // html模板
├── src                        // 源代码
│   ├── api                    // 所有请求
│   ├── assets                 // 主题 字体等静态资源
│   ├── components             // 全局公用组件
│   ├── directive              // 全局指令
│   ├── layout                 // 布局
│   ├── router                 // 路由
│   ├── store                  // 全局 store管理
│   ├── utils                  // 全局公用方法
│   ├── views                  // view
│   ├── App.vue                // 入口页面
│   ├── main.js                // 入口 加载组件 初始化等
│   ├── permission.js          // 权限管理
│   └── settings.js            // 系统配置
├── .editorconfig              // 编码格式
├── .env.development           // 开发环境配置
├── .env.production            // 生产环境配置
├── .env.staging               // 测试环境配置
├── .eslintignore              // 忽略语法检查
├── .eslintrc.js               // eslint 配置项
├── .gitignore                 // git 忽略项
├── babel.config.js            // babel.config.js
├── package.json               // package.json
└── vue.config.js              // vue.config.js
```


## 2.5 系统部署

点击下方链接进入官方语雀帮助手册查看项目部署方式：

[如何快速部署 IoTLink](https://www.5iot.com/doc/?target=/md/deploy)

建议服务器最低配置：

| 类型 | 配置 |
|:-:|:-:|
|操作系统|CentOS Stream  8 64位|
|CPU|4核|
|内存|8G|
|带宽|5M|
|硬盘|100G|

# 三、常见问题


**常见问题列表** 

- [如何快速部署 IoTLink](https://www.5iot.com/doc/?target=/md/deploy)
- [目前支持哪些上游接口](https://www.5iot.com/doc/?target=/md/upstreamApi)
- [为什么项目启动时报错](https://www.5iot.com/doc/?target=/md/startError)
- [数据库使用 Group By 查询报错](https://www.5iot.com/doc/?target=/md/sqlError)
- [项目每个分支的作用是什么](https://www.5iot.com/doc/?target=/md/branchDescription)

# 四、最近规划

补充中


# 五、系统截图


![首页](/doc/flie/pageShow/pageShow1-1.png)

![物联网卡管理](/doc/flie/pageShow/pageShow2-1.png)

![卡详情](/doc/flie/pageShow/pageShow2.1.png)

![公司所属查询](/doc/flie/pageShow/pageShow2.2.png)

![更新基础信息](/doc/flie/pageShow/pageShow2.3.png)

![物联网卡设置](/doc/flie/pageShow/pageShow3.png)

![平台资费](/doc/flie/pageShow/pageShow4.png)

![资费订购](/doc/flie/pageShow/pageShow5.png)

![订购历史](/doc/flie/pageShow/pageShow6.png)

![用量详情](/doc/flie/pageShow/pageShow7.png)

![上游通道](/doc/flie/pageShow/pageShow8.png)

![上游通道详情](/doc/flie/pageShow/pageShow8.1.png)

![通道进度](/doc/flie/pageShow/pageShow9.png)

![执行任务管理](/doc/flie/pageShow/pageShow10.png)

![执行任务下载](/doc/flie/pageShow/pageShow10.1.png)

![内部管理](/doc/flie/pageShow/pageShow11.png)

![企业管理](/doc/flie/pageShow/pageShow12.png)

![角色管理](/doc/flie/pageShow/pageShow13.png)

![全部订单](/doc/flie/pageShow/pageShow14.png)

![定时任务](/doc/flie/pageShow/pageShow15.png)

![菜单管理](/doc/flie/pageShow/pageShow16.png)

![岗位管理](/doc/flie/pageShow/pageShow17.png)

![字典管理](/doc/flie/pageShow/pageShow18.png)

![参数设置](/doc/flie/pageShow/pageShow19.png)

![日志执行](/doc/flie/pageShow/pageShow20.png)

![规则管理](/doc/flie/pageShow/pageShow21.png)

![推送配置](/doc/flie/pageShow/pageShow22.png)

![推送记录](/doc/flie/pageShow/pageShow23.png)

![绑定卡号](/doc/flie/pageShow/wxpageShow1.png)

![主页](/doc/flie/pageShow/wxpageShow2.png)

![资费订购](/doc/flie/pageShow/wxpageShow3.png)

![流量查询](/doc/flie/pageShow/wxpageShow4.png)

![充值记录](/doc/flie/pageShow/wxpageShow5.png)

![购物下单](/doc/flie/pageShow/wxpageShow6.png)



---

 **<p align="center">如果您觉得我们的开源项目很有帮助，请点击 :star: Star 支持 IoTLink 开源团队  :heart: </p>** 

---

# 六、联系我们
    
如果你有任何 IoTLink 产品上的想法、意见或建议，或商务上的合作需求，请扫码添加 IoTLink 项目团队进一步沟通：


![IoTLinkQunLiao20220119](/doc/flie/IoTLinkQunLiao20220119.png)


愿所有的物联网公司、从业者及开发者都能感受到开源的力量，体验到其带来的无限可能。让我们的共同努力使物联网在不久的将来更加工具化，从而为各行各业注入创新活力，实现价值创造。让物联网技术深入千行百业，为全球的经济增长和社会进步贡献力量。