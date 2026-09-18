-- 初始数据
USE mall_db;

-- ============================================================================
-- 本文件是「最小可跑种子」：只含参考数据与商品目录，
-- 不含订单、订单项、AI 对话记录等运行时才产生的数据（那些见 mall_db.sql 完整快照）。
--
-- 内容从 mall_db.sql 抽取，两者在抽取时刻一致；库有变动时重新抽取即可，不必手抄。
--
-- 商品封面 cover_image 指向 /api/uploads/images/cover-1.jpg ~ cover-21.jpg，
-- 对应项目根目录 uploads/images/ 下的 21 个文件。
-- ============================================================================

-- 账号：admin（管理员）、user（普通用户）

INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$Dt9ARpfwM14a/9KHNHwlz.378JohUOgmyqoGUk7Eaw1L/qlXnpxZa', '系统管理员', NULL, 'ADMIN', 1, 0, '2026-08-24 16:20:22', '2026-08-24 17:45:15');
INSERT INTO `sys_user` VALUES (2, 'user', '$2a$10$EmsBciVAsoQpWVBYf6Skzu1kLKoauaJuD.CfLqAKJZftg4jKzq2Z2', '测试用户', NULL, 'USER', 1, 0, '2026-08-24 16:20:22', '2026-09-17 14:04:25');

-- 商品分类（10 个）
INSERT INTO `pms_category` VALUES (1, '数字课程', 0, 1, 'Reading', 0, '2026-08-24 16:20:22');
INSERT INTO `pms_category` VALUES (2, '软件工具', 0, 2, 'SetUp', 0, '2026-08-24 16:20:22');
INSERT INTO `pms_category` VALUES (3, '电子书', 0, 3, 'Notebook', 0, '2026-08-24 16:20:22');
INSERT INTO `pms_category` VALUES (4, '会员服务', 0, 4, 'StarFilled', 0, '2026-08-24 16:20:22');
INSERT INTO `pms_category` VALUES (5, '编程开发', 1, 1, 'Headset', 0, '2026-08-24 16:20:22');
INSERT INTO `pms_category` VALUES (6, '设计创意', 1, 2, 'Collection', 0, '2026-08-24 16:20:22');
INSERT INTO `pms_category` VALUES (7, '办公软件', 2, 1, 'Monitor', 0, '2026-08-24 16:20:22');
INSERT INTO `pms_category` VALUES (8, '开发工具', 2, 2, 'Trophy', 0, '2026-08-24 16:20:22');
INSERT INTO `pms_category` VALUES (9, '技术书籍', 3, 1, 'Collection', 0, '2026-08-24 16:20:22');
INSERT INTO `pms_category` VALUES (10, '小说文学', 3, 2, 'Notebook', 0, '2026-08-24 16:20:22');

-- 商品（21 个）
INSERT INTO `pms_product` VALUES (1, 'Vue3 从入门到实战', 5, 99.00, 199.00, 996, '/api/uploads/images/cover-1.jpg', '全面讲解Vue3Composition API、Pinia、Vue Router等核心技术', '<h2>课程大纲</h2><p>1. Vue3基础</p><p>2. Composition API</p><p>3. Pinia状态管理</p><p>4. Vue Router</p><p>5. 项目实战</p>', 1, 131, 0, '2026-08-24 16:20:22', '2026-09-17 21:01:40');
INSERT INTO `pms_product` VALUES (2, 'Spring Boot 微服务开发', 5, 129.00, 259.00, 994, '/api/uploads/images/cover-2.jpg', '从零搭建Spring Boot微服务架构', '<h2>课程内容</h2><p>Spring Boot核心</p><p>MyBatis Plus</p><p>Spring Security</p><p>微服务架构</p>', 1, 91, 0, '2026-08-24 16:20:22', '2026-09-18 10:02:15');
INSERT INTO `pms_product` VALUES (3, 'Photoshop CC 教程', 6, 59.00, 119.00, 997, '/api/uploads/images/cover-3.jpg', 'PS入门到精通，涵盖海报设计、照片修图等', '<h2>课程内容</h2><p>基础工具</p><p>图层管理</p><p>蒙版与通道</p><p>实战案例</p>', 1, 258, 0, '2026-08-24 16:20:22', '2026-09-18 09:01:16');
INSERT INTO `pms_product` VALUES (4, 'Office 365 年度会员', 7, 198.00, 398.00, 495, '/api/uploads/images/cover-4.jpg', '正版Office 365一年使用权，含Word/Excel/PPT', '<h2>会员权益</h2><p>正版授权</p><p>1TB OneDrive</p><p>多设备使用</p>', 1, 517, 0, '2026-08-24 16:20:22', '2026-09-17 21:31:32');
INSERT INTO `pms_product` VALUES (5, 'VS Code Pro 插件包', 8, 29.00, 49.00, 998, '/api/uploads/images/cover-5.jpg', '精选20款高效VS Code插件', '<h2>插件列表</h2><p>代码格式化</p><p>Git增强</p><p>主题美化</p>', 1, 1025, 0, '2026-08-24 16:20:22', '2026-09-18 10:02:15');
INSERT INTO `pms_product` VALUES (6, 'Java编程思想（电子版）', 9, 39.00, 79.00, 998, '/api/uploads/images/cover-6.jpg', '经典Java学习书籍电子版', '<h2>书籍介绍</h2><p>Java编程经典之作</p><p>涵盖Java核心技术</p>', 1, 731, 0, '2026-08-24 16:20:22', '2026-09-18 09:02:13');
INSERT INTO `pms_product` VALUES (7, 'AI智能客服系统源码', 5, 299.00, 599.00, 98, '/api/uploads/images/cover-7.jpg', '完整的AI智能客服系统源码，含Spring AI集成', '<h2>项目内容</h2><p>Spring AI集成</p><p>Function Calling</p><p>SSE流式对话</p>', 1, 34, 0, '2026-08-24 16:20:22', '2026-09-17 21:01:40');
INSERT INTO `pms_product` VALUES (8, '网站VIP年卡', 4, 88.00, 188.00, 999, '/api/uploads/images/cover-8.jpg', '全站资源免费下载一年', '<h2>VIP权益</h2><p>全站资源免费下载</p><p>专属客服</p><p>优先更新</p>', 1, 2048, 0, '2026-08-24 16:20:22', '2026-09-17 21:01:40');
INSERT INTO `pms_product` VALUES (9, 'WPS 365 会员年卡', 7, 168.00, 268.00, 500, '/api/uploads/images/cover-9.jpg', '正版 WPS 365 一年使用权，含云文档、PDF 转换与海量办公模板', '<h2>会员权益</h2><p>正版授权，支持多端登录</p><p>100GB 云文档空间</p><p>PDF 转换与合并</p><p>海量办公模板免费使用</p>', 1, 0, 0, '2026-09-17 15:07:01', '2026-09-18 15:21:37');
INSERT INTO `pms_product` VALUES (10, '长夜将明（电子版）', 10, 29.00, 59.00, 999, '/api/uploads/images/cover-10.jpg', '悬疑长篇，一桩封存三十年的旧案因一封匿名信重开', '<h2>内容简介</h2><p>一桩封存了三十年的旧案，因为一封匿名信重新被翻起。</p><p>全书约 26 万字，共四十二章。</p><h2>适合读者</h2><p>喜欢社会派推理的读者</p>', 1, 156, 0, '2026-09-17 19:39:06', '2026-09-18 15:21:46');
INSERT INTO `pms_product` VALUES (11, '山海拾遗（电子版）', 10, 35.00, 69.00, 999, '/api/uploads/images/cover-11.jpg', '古典志怪小说集，上下两卷共二十四篇', '<h2>内容简介</h2><p>取《山海经》与六朝志怪的笔法，写山野异闻与人间小事。</p><p>上下两卷，共二十四篇。</p><h2>适合读者</h2><p>喜欢古典志怪与短篇小说的读者</p>', 1, 87, 0, '2026-09-17 19:39:06', '2026-09-18 15:21:49');
INSERT INTO `pms_product` VALUES (12, '草木人间（电子版）', 10, 25.00, 49.00, 998, '/api/uploads/images/cover-12.jpg', '散文随笔集，写草木、四时与故人', '<h2>内容简介</h2><p>记草木荣枯、四时流转，以及在这些景色里出现过的人。</p><p>共三十六篇短文。</p><h2>适合读者</h2><p>喜欢散文与自然写作的读者</p>', 1, 204, 0, '2026-09-17 19:39:06', '2026-09-18 15:21:53');
INSERT INTO `pms_product` VALUES (13, 'Figma 设计系统实战', 6, 89.00, 179.00, 999, '/api/uploads/images/cover-13.jpg', '从组件到设计令牌，搭建可维护的设计系统', '<h2>课程内容</h2><p>组件库搭建、变量与样式、交付与协作</p><p>共 32 节，约 9 小时</p><h2>适合人群</h2><p>有一定设计基础、想把团队协作规范起来的同学</p>', 1, 142, 0, '2026-09-17 19:45:27', '2026-09-18 15:21:59');
INSERT INTO `pms_product` VALUES (14, 'Illustrator 商业插画入门', 6, 79.00, 159.00, 999, '/api/uploads/images/cover-14.jpg', '从线条到配色，完成第一组商业插画', '<h2>课程内容</h2><p>钢笔工具、配色与构图、风格化练习</p><p>共 28 节，约 8 小时</p><h2>适合人群</h2><p>零基础想学插画的同学</p>', 1, 96, 0, '2026-09-17 19:45:27', '2026-09-18 15:22:02');
INSERT INTO `pms_product` VALUES (15, 'Excel 数据分析实战', 7, 69.00, 139.00, 999, '/api/uploads/images/cover-15.jpg', '函数、透视表与图表，把表格变成结论', '<h2>课程内容</h2><p>常用函数与技巧、数据透视表、图表与仪表盘</p><p>共 36 节，约 10 小时</p><h2>适合人群</h2><p>需要做报表与数据汇报的职场人</p>', 1, 187, 0, '2026-09-17 19:45:27', '2026-09-18 15:22:06');
INSERT INTO `pms_product` VALUES (16, 'Docker 容器化实战', 8, 99.00, 199.00, 996, '/api/uploads/images/cover-16.jpg', '从镜像构建到多容器编排，跑通一套部署流程', '<h2>课程内容</h2><p>镜像与容器、数据卷与网络、Compose 编排</p><p>共 30 节，约 9 小时</p><h2>适合人群</h2><p>想掌握容器化部署的后端开发者</p>', 1, 121, 0, '2026-09-17 19:45:27', '2026-09-18 15:22:08');
INSERT INTO `pms_product` VALUES (17, 'Git 版本控制精讲', 8, 49.00, 99.00, 999, '/api/uploads/images/cover-17.jpg', '分支模型、冲突处理与团队协作规范', '<h2>课程内容</h2><p>提交与分支、合并与变基、协作与回滚</p><p>共 22 节，约 6 小时</p><h2>适合人群</h2><p>刚开始参与团队协作的开发者</p>', 1, 231, 0, '2026-09-17 19:45:27', '2026-09-18 15:22:12');
INSERT INTO `pms_product` VALUES (18, '数据结构与算法（电子版）', 9, 45.00, 89.00, 999, '/api/uploads/images/cover-18.jpg', '常用数据结构与算法图解，每章配习题解析', '<h2>内容简介</h2><p>数组、链表、树、图，以及常见的排序与查找算法。</p><p>共 18 章，每章配有习题与解析。</p><h2>适合读者</h2><p>准备面试、想打牢算法基础的读者</p>', 1, 164, 0, '2026-09-17 19:45:27', '2026-09-18 15:22:14');
INSERT INTO `pms_product` VALUES (19, '重构与设计模式（电子版）', 9, 55.00, 109.00, 999, '/api/uploads/images/cover-19.jpg', '从坏味道出发，讲重构手法与常用设计模式', '<h2>内容简介</h2><p>从代码坏味道出发，讲常见重构手法与设计模式。</p><p>共 21 章，含 60 余个代码示例。</p><h2>适合读者</h2><p>有一两年开发经验、想提升代码质量的读者</p>', 1, 103, 0, '2026-09-17 19:45:27', '2026-09-18 15:22:18');
INSERT INTO `pms_product` VALUES (20, '设计素材会员年卡', 4, 158.00, 318.00, 500, '/api/uploads/images/cover-20.jpg', '全站素材一年内免费下载，含商用授权', '<h2>会员权益</h2><p>全站素材免费下载</p><p>含商用授权</p><p>每月新增素材优先获取</p>', 1, 216, 0, '2026-09-17 19:45:27', '2026-09-18 15:22:21');
INSERT INTO `pms_product` VALUES (21, '云盘会员年卡', 4, 128.00, 258.00, 495, '/api/uploads/images/cover-21.jpg', '1TB 云空间一年，支持多端同步与在线预览', '<h2>会员权益</h2><p>1TB 云空间</p><p>多端同步与在线预览</p><p>历史版本保留 30 天</p>', 1, 253, 0, '2026-09-17 19:45:27', '2026-09-18 15:23:39');
