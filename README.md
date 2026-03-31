<p align="center">
  <h1 align="center">JY-Study</h1>
  <h4 align="center">多模态交互的教育辅导系统</h4>
  <p align="center">
    <a href="#项目简介">项目简介</a> |
    <a href="#系统功能">系统功能</a> |
    <a href="#技术栈">技术栈</a> |
    <a href="#快速开始">快速开始</a> |
    <a href="#项目结构">项目结构</a> |
    <a href="#论文信息">论文信息</a>
  </p>
</p>

## 写在前面
此项目是本人的毕业设计项目，主要思路为借用若依的框架来完成用户和角色管理。再自己添加了文章、课程管理和ai服务功能；并借助ai生成了前端界面，最后根据此项目写出了论文。

希望抛砖引玉，让需要写毕业设计的大学生可以参考思路。
此外，建议直接从论文.txt来了解本项目，此readme为ai生成，只供大致了解。 论文word版中有非常详细的模块介绍、演示图片、代码节选和解释。

**项目基于若依框架开发**：本项目基于 [若依框架（RuoYi）](https://gitee.com/y_project/RuoYi) 进行开发，若依是一个基于SpringBoot的Java快速开发框架，提供了完善的用户、角色、权限等基础功能。

再次感谢若依以及给我毕设思路的xiaofeng。

tag：AI课堂、AI课程、JAVA开发、后端、毕业设计、毕业论文
## 项目简介

JY-Study 是一个基于多模态交互的教育辅导系统，融合了人工智能大模型技术和多模态交互技术，整合了文本、语音、图像等多种模态信息，为用户提供智能化的教育辅导服务。

系统主要包括课程学习、文章学习、课程管理、文章管理、AI学习等功能模块，并提供了AI朗读、AI生图、AI对话、AI视频、AI试题等智能交互功能。通过多模态信息的融合处理和智能化的交互设计，有效提升学习效果，增强用户体验。

### 主要特点

- 🤖 **AI智能交互**：集成多个主流AI大模型，提供智能对话、语音朗读、图像生成、视频生成等功能
- 📚 **多模态学习**：支持文本、音频、视频等多种形式的学习资源
- 🎯 **个性化学习**：基于用户学习行为提供个性化推荐和智能总结
- 🏗️ **模块化设计**：采用模块化架构，代码清晰，易于维护和扩展
- 🔒 **安全可靠**：基于Shiro安全框架，完善的权限管理和安全防护

## 系统功能
截图预览
![截图1.png](doc/readmePic/%E6%88%AA%E5%9B%BE1.png)
![img.png](doc/readmePic/img.png)
![img_1.png](doc/readmePic/img_1.png)
![img_2.png](doc/readmePic/img_2.png)
### 用户端

#### 1. 课程学习
- 课程浏览：查看课程列表和详情
- 多模态学习：支持教案浏览、音频播放、视频播放
- 课程操作：点赞、收藏和智能推荐
- AI辅助：AI总结、AI对话

#### 2. 文章学习
- 文章浏览：阅读文章内容
- 文章操作：点赞、收藏和智能推荐
- AI功能：AI朗读、AI总结、AI生图、AI对话

#### 3. AI学习
- 数字人对话：与虚拟数字人进行互动对话
- 大模型对话：与大型AI模型进行深入对话

#### 4. 用户中心
- 个人信息管理
- 学习记录查看
- 学习数据统计
- 智能总结查看

### 管理员端

#### 1. 系统管理
- 用户管理：用户信息和权限管理
- 角色管理：角色权限分配
- 菜单管理：系统菜单配置
- 系统日志：操作日志记录和查询

#### 2. 课程管理
- 课程上传：上传课程内容和多媒体资源
- 课程编辑：编辑课程信息
- 课程删除：移除课程

#### 3. 文章管理
- 文章上传：上传文章内容
- 文章编辑：编辑文章信息
- 文章删除：移除文章
- 文章查询：搜索和查看文章

#### 4. AI管理
- 知识库管理：维护知识库内容
- 模型管理：管理AI模型配置
- 记录管理：管理AI使用记录

#### 5. 数据报表
- 课程数据统计
- 学习数据分析
- AI使用数据统计

## 技术栈

### 后端技术
- **框架**：Spring Boot 2.5.15
- **安全**：Apache Shiro 1.13.0
- **持久层**：MyBatis 3.5.x
- **数据库连接池**：Alibaba Druid 1.2.23
- **数据验证**：Hibernate Validation 6.0.x
- **缓存**：Redis
- **数据库**：MySQL 5.7+

### 前端技术
- **UI框架**：Bootstrap 3.3.7
- **模板引擎**：Thymeleaf 3.0.x
- **JavaScript库**：jQuery, Layui
- **Ajax**：异步数据交互

### AI技术
- **大语言模型**：通义千问（阿里云）
- **语音技术**：通义听悟（阿里云）
- **图像生成**：通义万相（阿里云）
- **视频生成**：VideoRetalk
- **其他AI服务**：硅基流动、扣子（字节跳动）

### 开发环境
- **JDK**：1.8+
- **Maven**：3.6+
- **IDE**：IntelliJ IDEA / Eclipse
- **数据库**：MySQL 5.7+
- **缓存**：Redis 3.0+

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+
- Redis 3.0+（可选，用于缓存）

### 数据库初始化

1. 首先执行sql文件夹下的ry_20240601.sql，这是若依的基础数据库，主要包括用户、角色、菜单、部门、字典、定时任务、参数配置、操作日志、登录日志、定时任务日志等。
2. 然后执行jy_study.sql，这是JY-Study的数据库,主要关于课程、文章和ai功能等。

### 配置文件

修改 `jy-study-admin/src/main/resources/application.yml` 配置文件：

1. **数据库配置**
这里要改为你自己的数据库连接，一般是本地数据库。我用了云端服务器的。
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/jy_study?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: root
    password: your_password
```

2. **AI服务配置**（重要）

这一步是为了让项目中的ai服务能够调用成功，主要使用的是通义千问的api，硅基流动的其实最终没有用到。coze的是用给自动出题的服务，如何做自动出题的工作流，请自己额外学习了（或许我有空把工作流的文件也放进来）。
```yaml
# 通义千问 API Key
modelTongyi:
  apiKey: your_api_key

# 硅基流动 API Key
siliconCouldAI:
  apiKey: your_api_key

# 扣子 Token
cozeAI:
  token: your_token
```

### 运行项目

 此处就不赘述了，个人开发和测试的时候使用idea来运行就好了。
 Linux生产环境使用maven来打包，然后云服务器上启动tomcat，将war包放进webapps后会自动解压部署。
### 访问系统

- 用户端：http://localhost:8080/web/index
- 管理端：http://localhost:8080/login
- 默认管理员账号：admin / admin123（请首次登录后修改密码）

## 项目结构

```
jy-study
├── jy-study-admin          # 管理模块（启动模块）
│   ├── src/main/java       # Java代码
│   ├── src/main/resources  # 配置文件、静态资源、模板
│   └── pom.xml
├── jy-study-common         # 通用模块
│   ├── src/main/java       # 公共类、工具类
│   └── pom.xml
├── jy-study-framework      # 核心框架模块
│   ├── src/main/java       # 框架核心代码
│   └── pom.xml
├── jy-study-system         # 系统模块
│   ├── src/main/java       # 系统管理相关代码
│   └── pom.xml
├── jy-study-lesson         # 课程模块
│   ├── src/main/java       # 课程、文章相关代码
│   └── pom.xml
├── jy-study-generator      # 代码生成模块
│   └── pom.xml
├── jy-study-quartz         # 定时任务模块
│   └── pom.xml
├── sql                     # SQL脚本目录
│   ├── jy_study.sql        # 数据库初始化脚本
│   └── quartz.sql          # 定时任务表脚本
├── 论文                     # 论文文档
│   ├── 论文.txt            # 论文文本
│   └── 多模态交互的教育辅导系统设计与实现.docx  # 论文Word文档
├── pom.xml                 # 父POM文件
└── README.md               # 项目说明文档
```

## 论文信息

### 论文题目
**多模态交互的教育辅导系统设计与实现**

### 摘要
本文设计并实现了一个基于多模态交互的教育辅导系统。本系统对人工智能大模型技术和多模态交互技术加以融合，整合了文本、语音、图像等多种模态信息，为用户提供智能化的教育辅导服务。系统主要包括课程学习、文章学习、课程管理、文章管理、AI学习等功能模块，并提供了AI朗读、AI生图、AI对话、AI视频、AI试题等智能交互功能。

### 关键词
教育辅导系统、多模态交互、人工智能、大语言模型、Spring Boot


### 论文文档
论文文档位于 `论文/` 目录下：
- `论文.txt` - 论文文本版本
- `多模态交互的教育辅导系统设计与实现.docx` - 论文Word版本

论文章节截图：
![img.png](doc/readmePic/论文结构1.png)
![img.png](doc/readmePic/论文结构2.png)

## 特别说明

### 关于AI服务配置
本项目集成了多个AI服务接口，包括：
- 通义千问（阿里云）
- 通义听悟（阿里云）
- 通义万相（阿里云）
- 硅基流动
- 扣子（字节跳动）

如需使用相关AI功能，请配置相应的API密钥。配置方法请参考配置文件中的注释说明。

### 关于若依框架
本项目基于 [若依框架（RuoYi）](https://gitee.com/y_project/RuoYi) 进行开发，在此特别感谢若依框架团队提供的优秀基础框架。

若依框架相关文档：
- [若依框架总体手册](http://doc.ruoyi.vip/ruoyi/)
- [若依框架后台手册](http://doc.ruoyi.vip/ruoyi/document/htsc.html)
- [若依框架前台手册](http://doc.ruoyi.vip/ruoyi/document/qdsc.html)

## 许可证

本项目采用 [MIT License](LICENSE) 开源许可证。

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

如有问题或建议，需要帮助配置部署/私下一对一解答疑问/写作帮助，欢迎通过以下方式联系：

- **Gitee**: [https://gitee.com/jily0526/jy-study](https://gitee.com/jily0526/jy-study)
- **Issue**: [提交Issue](https://gitee.com/jily0526/jy-study/issues)
- 邮箱: jily1218@163.com
---

<p align="center">
  <sub>Built with ❤️ by jily</sub>
</p>
