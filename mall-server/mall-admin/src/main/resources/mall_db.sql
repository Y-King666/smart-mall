-- ============================================================================
-- 开发库全量导出，含真实演示数据：
--   4 个用户、10 个分类、21 个商品、14 笔订单、21 条订单项、60 条 AI 对话。
--
-- 【本文件不会被应用自动执行】
--   application.yml 的 spring.sql.init 指向 schema.sql + data.sql，且 mode: never。
--   本文件仅供手工导入，用于恢复带演示数据的完整环境。
--   若只需最小可跑数据集（2 个账号 + 10 个分类 + 8 个商品），用 schema.sql + data.sql。
--
-- 【导入前需先选库】本文件不含 CREATE DATABASE / USE 语句，
--   导入时请先连到 mall_db：
--     mysql -u root -p mall_db < mall_db.sql
--
-- 【商品图片】cover_image 指向 /api/uploads/images/cover-1.jpg ~ cover-21.jpg，
--   与项目根目录 uploads/images/ 下的 21 个文件一一对应，导入后图片可正常显示。
-- ============================================================================

/*
 Navicat Premium Dump SQL

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : mall_db

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 18/09/2026 15:29:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_chat_message
-- ----------------------------
DROP TABLE IF EXISTS `ai_chat_message`;
CREATE TABLE `ai_chat_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_session`(`user_id` ASC, `session_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 164 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_chat_message
-- ----------------------------
INSERT INTO `ai_chat_message` VALUES (1, 1, 'e6719930-a3bb-4d70-84b9-c02b1b345f85', 'user', 'hello, what products do you have?', '2026-08-26 10:04:24');
INSERT INTO `ai_chat_message` VALUES (2, 1, 'e6719930-a3bb-4d70-84b9-c02b1b345f85', 'assistant', '抱歉，AI服务暂时不可用，请稍后再试。', '2026-08-26 10:04:25');
INSERT INTO `ai_chat_message` VALUES (3, 1, '47979cb2-e830-4cff-8aa4-af8bd55b4448', 'user', 'hello, what products do you have?', '2026-08-26 10:07:50');
INSERT INTO `ai_chat_message` VALUES (4, 1, '47979cb2-e830-4cff-8aa4-af8bd55b4448', 'assistant', '抱歉，AI服务暂时不可用，请稍后再试。', '2026-08-26 10:07:50');
INSERT INTO `ai_chat_message` VALUES (5, 1, '63480d06-6e11-452e-a26d-786ab6eeabfd', 'user', 'hello', '2026-08-26 10:10:24');
INSERT INTO `ai_chat_message` VALUES (6, 1, '63480d06-6e11-452e-a26d-786ab6eeabfd', 'assistant', 'Hello! 😊 How can I help you today? Whether you have questions about products, orders, or anything else—I\'m here to assist!', '2026-08-26 10:10:26');
INSERT INTO `ai_chat_message` VALUES (7, 1, '29b76979-f31d-4835-8867-53d176fc96f8', 'user', 'What products do you have?', '2026-08-26 10:10:36');
INSERT INTO `ai_chat_message` VALUES (8, 1, '29b76979-f31d-4835-8867-53d176fc96f8', 'assistant', 'I offer a variety of virtual goods — such as digital gift cards, software licenses, and online course access codes. To help you better, could you tell me what type of product you\'re interested in? For example: “Steam gift card”, “Adobe Creative Cloud license”, or “Coursera course code”. I can then check real-time availability, price, and stock for you! 😊', '2026-08-26 10:10:38');
INSERT INTO `ai_chat_message` VALUES (9, 1, '6cb30543-0666-4197-aa12-35b9253f556b', 'user', 'hello', '2026-08-26 10:13:49');
INSERT INTO `ai_chat_message` VALUES (10, 1, '6cb30543-0666-4197-aa12-35b9253f556b', 'assistant', 'Hello! 😊 How can I help you today? Whether you have questions about products, orders, or anything else—I\'m here to assist!', '2026-08-26 10:13:50');
INSERT INTO `ai_chat_message` VALUES (11, 2, '21489182-5e79-4926-a65c-85dc08cbc5e5', 'user', '你好', '2026-08-26 10:31:08');
INSERT INTO `ai_chat_message` VALUES (12, 2, '21489182-5e79-4926-a65c-85dc08cbc5e5', 'assistant', '你好！很高兴为您服务～😊  \n请问有什么可以帮您的？比如想了解某款商品，还是需要查询订单状态？', '2026-08-26 10:31:10');
INSERT INTO `ai_chat_message` VALUES (15, 1, 'dce115c9-9186-4b25-b071-0433a13dcf56', 'user', 'hello', '2026-08-26 10:37:11');
INSERT INTO `ai_chat_message` VALUES (16, 1, 'dce115c9-9186-4b25-b071-0433a13dcf56', 'assistant', '你好！很高兴为你服务～😊  \n有什么我可以帮你的吗？比如查询商品信息、了解订单状态，或者其他问题？', '2026-08-26 10:37:12');
INSERT INTO `ai_chat_message` VALUES (23, 1, '2c5c3976-9498-4f2f-907c-47f7bfc07c69', 'user', 'hello', '2026-08-26 10:43:50');
INSERT INTO `ai_chat_message` VALUES (24, 1, '2c5c3976-9498-4f2f-907c-47f7bfc07c69', 'assistant', '你好！很高兴为你服务～😊  \n有什么我可以帮你的吗？比如查询商品信息、了解订单状态，或者其他问题？', '2026-08-26 10:43:51');
INSERT INTO `ai_chat_message` VALUES (25, 1, '89b6e500-0af1-4f38-a623-f0430b13fc0c', 'user', 'hi', '2026-08-26 10:43:51');
INSERT INTO `ai_chat_message` VALUES (26, 1, '89b6e500-0af1-4f38-a623-f0430b13fc0c', 'assistant', '你好！很高兴为你服务～😊  \n有什么我可以帮你的吗？比如查询商品信息、了解订单状态，或者其他问题？', '2026-08-26 10:43:52');
INSERT INTO `ai_chat_message` VALUES (27, 1, '5967b63f-e777-4b9e-b463-d7716204bc07', 'user', 'hi', '2026-08-26 10:44:44');
INSERT INTO `ai_chat_message` VALUES (28, 1, '5967b63f-e777-4b9e-b463-d7716204bc07', 'assistant', '你好！很高兴为你服务～😊  \n有什么我可以帮你的吗？比如查询商品信息、了解订单状态，或者其他问题？', '2026-08-26 10:44:45');
INSERT INTO `ai_chat_message` VALUES (33, 2, '67b95185-30ac-4a57-a4de-50b5c21dee6b', 'user', 'java课程多少钱', '2026-08-26 10:52:46');
INSERT INTO `ai_chat_message` VALUES (34, 2, '67b95185-30ac-4a57-a4de-50b5c21dee6b', 'assistant', '抱歉，暂未找到名称中包含“java课程”的商品。您是否记错了名称？或者需要我帮您查找其他类型的课程（如编程入门、Python、前端等）？欢迎随时告诉我 😊', '2026-08-26 10:52:49');
INSERT INTO `ai_chat_message` VALUES (35, 2, '67b95185-30ac-4a57-a4de-50b5c21dee6b', 'user', 'Spring Boot 微服务开发 价格多少', '2026-08-26 10:53:09');
INSERT INTO `ai_chat_message` VALUES (36, 2, '67b95185-30ac-4a57-a4de-50b5c21dee6b', 'assistant', '“Spring Boot 微服务开发”课程价格为 **129.0 元**，目前库存充足（剩余 997 份），课程内容涵盖从零搭建 Spring Boot 微服务架构。需要我帮您下单或了解详情吗？😊', '2026-08-26 10:53:11');
INSERT INTO `ai_chat_message` VALUES (65, 2, '718f2333-e6e5-4914-95e4-0a2bd6764247', 'user', '你好', '2026-08-27 08:50:07');
INSERT INTO `ai_chat_message` VALUES (66, 2, '718f2333-e6e5-4914-95e4-0a2bd6764247', 'assistant', '你好！很高兴为您服务～😊  \n请问有什么可以帮您的？比如想了解某款商品，还是需要查询订单状态？', '2026-08-27 08:50:10');
INSERT INTO `ai_chat_message` VALUES (67, 2, '718f2333-e6e5-4914-95e4-0a2bd6764247', 'user', '查询我买过的商品', '2026-08-27 08:50:21');
INSERT INTO `ai_chat_message` VALUES (68, 2, '718f2333-e6e5-4914-95e4-0a2bd6764247', 'assistant', '您好！为了帮您查询购买过的商品，需要您提供订单号哦～  \n如果您记得订单号，可以直接告诉我；如果不确定，也可以查看下单时的短信、邮件或App订单列表。😊', '2026-08-27 08:50:22');
INSERT INTO `ai_chat_message` VALUES (69, 2, '718f2333-e6e5-4914-95e4-0a2bd6764247', 'user', '你是使用的什么模型', '2026-08-27 08:51:23');
INSERT INTO `ai_chat_message` VALUES (70, 2, '718f2333-e6e5-4914-95e4-0a2bd6764247', 'assistant', '我是通义千问（Qwen），由通义实验室研发的超大规模语言模型。我擅长回答问题、创作文字，比如写故事、写公文、写邮件、写剧本、逻辑推理、编程等等，还能表达观点，玩游戏等。如果您有任何问题或需要帮助，欢迎随时告诉我！😊', '2026-08-27 08:51:24');
INSERT INTO `ai_chat_message` VALUES (71, 2, '718f2333-e6e5-4914-95e4-0a2bd6764247', 'user', '查询下java类的商品价格', '2026-08-27 09:01:59');
INSERT INTO `ai_chat_message` VALUES (72, 2, '718f2333-e6e5-4914-95e4-0a2bd6764247', 'assistant', '抱歉，未找到名称中包含“java类”的商品。可能是关键词不匹配，或者该商品暂未上架～  \n您可以尝试提供更具体的商品名（如“Java编程入门”“Java开发实战”等），或告诉我您想了解的类别（如图书、课程、工具等），我来帮您进一步查找！😊', '2026-08-27 09:02:01');
INSERT INTO `ai_chat_message` VALUES (73, 2, '718f2333-e6e5-4914-95e4-0a2bd6764247', 'user', 'Java编程入门 商品详情', '2026-08-27 09:06:47');
INSERT INTO `ai_chat_message` VALUES (74, 2, '718f2333-e6e5-4914-95e4-0a2bd6764247', 'assistant', '抱歉，当前系统中暂未找到名称包含“Java编程入门”的商品。  \n可能是商品已下架、名称略有差异，或尚未录入系统。  \n您也可以提供更准确的全名、ISBN号，或截图商品页面，我来帮您进一步确认～ 😊', '2026-08-27 09:06:50');
INSERT INTO `ai_chat_message` VALUES (75, 2, '718f2333-e6e5-4914-95e4-0a2bd6764247', 'user', 'Spring Boot 微服务开发 商品详情', '2026-08-27 09:07:23');
INSERT INTO `ai_chat_message` VALUES (76, 2, '718f2333-e6e5-4914-95e4-0a2bd6764247', 'assistant', '已为您查到商品信息：\n\n✅ 商品名称：Spring Boot 微服务开发  \n💰 价格：129.0 元  \n📦 库存：997 件（充足）  \n📝 描述：从零搭建Spring Boot微服务架构  \n\n需要帮您下单、了解配套资源，或查询其他商品吗？😊', '2026-08-27 09:07:27');
INSERT INTO `ai_chat_message` VALUES (77, 2, '718f2333-e6e5-4914-95e4-0a2bd6764247', 'user', 'Spring Boot 微服务开发 商品详情', '2026-08-27 09:13:26');
INSERT INTO `ai_chat_message` VALUES (78, 2, '718f2333-e6e5-4914-95e4-0a2bd6764247', 'assistant', '✅ 商品名称：Spring Boot 微服务开发  \n💰 价格：129.0 元  \n📦 库存：997 件（充足）  \n📝 描述：从零搭建Spring Boot微服务架构  \n\n如需下单、了解配套源码/视频，或查询其他商品，欢迎随时告诉我～ 😊', '2026-08-27 09:13:31');
INSERT INTO `ai_chat_message` VALUES (83, 2, 'bb3f145e-d9c4-43bc-8d9b-9f3fcaadba8c', 'user', 'Vue3 从入门到实战多少钱？还有库存吗？', '2026-09-17 16:08:09');
INSERT INTO `ai_chat_message` VALUES (84, 2, 'bb3f145e-d9c4-43bc-8d9b-9f3fcaadba8c', 'assistant', '您好！关于您咨询的《Vue3 从入门到实战》课程，目前商城内的信息如下：\n\n*   **商品价格**：99 元\n*   **当前库存**：充足（996 件）\n*   **课程简介**：内容全面讲解了 Vue3 的核心技术，包括 Composition API、Pinia 状态管理以及 Vue Router 路由管理等。\n\n如果您感兴趣，欢迎随时下单学习！还有其他疑问吗？', '2026-09-17 16:08:16');
INSERT INTO `ai_chat_message` VALUES (134, 2, '825f2e27-2df5-4b24-97b2-05c67fa7ce50', 'user', '热销好物推荐', '2026-09-17 19:37:07');
INSERT INTO `ai_chat_message` VALUES (135, 2, '825f2e27-2df5-4b24-97b2-05c67fa7ce50', 'assistant', '为您精选了以下商城热销好物，按销量排序供您参考：\n\n1. **网站 VIP 年卡**\n   价格：¥88.0\n   描述：全站资源免费下载一年\n\n2. **VS Code Pro 插件包**\n   价格：¥29.0\n   描述：精选 20 款高效 VS Code 插件\n\n3. **Java 编程思想（电子版）**\n   价格：¥39.0\n   描述：经典 Java 学习书籍电子版\n\n4. **Office 365 年度会员**\n   价格：¥198.0\n   描述：正版 Office 365 一年使用权，含 Word/Excel/PPT\n\n5. **Photoshop CC 教程**\n   价格：¥59.0\n   描述：PS 入门到精通，涵盖海报设计、照片修图等\n\n以上商品均为虚拟商品，下单后无需物流配送，支付成功即可使用。如果您对这些商品感兴趣或有其他疑问，欢迎随时咨询！', '2026-09-17 19:37:18');
INSERT INTO `ai_chat_message` VALUES (136, 2, '825f2e27-2df5-4b24-97b2-05c67fa7ce50', 'user', 'VIP年卡还有多少件？', '2026-09-17 19:37:58');
INSERT INTO `ai_chat_message` VALUES (137, 2, '825f2e27-2df5-4b24-97b2-05c67fa7ce50', 'assistant', '为您查询到**网站 VIP 年卡**目前的库存情况如下：\n\n*   **当前库存**：999 件\n\n该商品支持虚拟发货，下单后无需等待物流，支付成功后即可立即使用全站资源。如有需要欢迎随时选购！', '2026-09-17 19:38:03');
INSERT INTO `ai_chat_message` VALUES (138, 2, '14fe0825-7c26-48b3-b36c-893937184c65', 'user', '查询我的历史订单', '2026-09-17 19:58:17');
INSERT INTO `ai_chat_message` VALUES (139, 2, '14fe0825-7c26-48b3-b36c-893937184c65', 'assistant', '您好，您共有 8 笔历史订单，以下是最近 5 笔订单详情：\n\n订单号：20260917193343692519\n下单时间：2026-09-17 19:33:43\n订单金额：168.0 元\n支付状态：已取消\n商品明细：WPS 365 会员年卡 x1\n\n订单号：20260917193303501554\n下单时间：2026-09-17 19:33:03\n订单金额：88.0 元\n支付状态：已取消\n商品明细：网站VIP年卡 x1\n\n订单号：20260917193227532646\n下单时间：2026-09-17 19:32:28\n订单金额：99.0 元\n支付状态：已取消\n商品明细：Vue3 从入门到实战 x1\n\n订单号：20260917155043708211\n下单时间：2026-09-17 15:50:43\n订单金额：525.0 元\n支付状态：已取消\n商品明细：Spring Boot 微服务开发 x1, Office 365 年度会员 x2\n\n订单号：20260917155006497564\n下单时间：2026-09-17 15:50:07\n订单金额：198.0 元\n支付状态：已取消\n商品明细：Vue3 从入门到实战 x2\n\n您可以在「我的订单」页面查看、支付或取消订单。', '2026-09-17 19:58:32');
INSERT INTO `ai_chat_message` VALUES (140, 2, '781e27ec-0377-4758-9185-aa41e5d8dc06', 'user', '你可以帮我下单云盘会员年卡么？', '2026-09-17 19:59:24');
INSERT INTO `ai_chat_message` VALUES (141, 2, '781e27ec-0377-4758-9185-aa41e5d8dc06', 'assistant', '已为您核实该商品信息：\n商品名称：云盘会员年卡\n售价：128元\n库存：500件\n商品描述：1TB 云空间一年，支持多端同步与在线预览\n\n由于系统暂不支持客服直接代下单，您可以按以下流程自行操作：\n1. 前往「购物车」页面，添加该商品并填写所需数量\n2. 勾选已加入的商品，点击提交订单\n3. 在「我的订单」页面选择支付方式完成付款\n\n本商品为虚拟商品，支付成功后即可立即开通使用，无需等待配送。如需我帮您查看购物车当前状态，或查询其他商品，请随时告知。', '2026-09-17 19:59:43');
INSERT INTO `ai_chat_message` VALUES (142, 2, '3573944d-eba0-4e4d-b229-7e1c0dca6a6d', 'user', '20260917193343692519能查到这单信息么？', '2026-09-17 20:31:08');
INSERT INTO `ai_chat_message` VALUES (143, 2, '3573944d-eba0-4e4d-b229-7e1c0dca6a6d', 'assistant', '已为您查到该订单信息，详情如下：\n\n订单号：20260917193343692519\n下单时间：2026-09-17 19:33:43\n订单金额：168.0元\n当前状态：已取消\n商品明细：WPS 365 会员年卡 x1\n\n如需查看其他历史订单或重新下单，请随时告诉我。', '2026-09-17 20:31:28');
INSERT INTO `ai_chat_message` VALUES (152, 2, 'd7f2a1cf-d5cb-4089-b619-e0dd35622dee', 'user', '订单可以取消么？', '2026-09-18 09:03:41');
INSERT INTO `ai_chat_message` VALUES (153, 2, 'd7f2a1cf-d5cb-4089-b619-e0dd35622dee', 'assistant', '是的，订单可以取消。\n\n无论是待支付还是已支付的订单，您都可以申请取消。\n取消成功后，对应商品的库存会恢复。\n\n如果您现在就需要取消某笔订单，请告诉我具体的**订单号**，我来帮您办理。', '2026-09-18 09:03:47');
INSERT INTO `ai_chat_message` VALUES (154, 2, 'd7f2a1cf-d5cb-4089-b619-e0dd35622dee', 'user', '20260917213121866527', '2026-09-18 09:03:52');
INSERT INTO `ai_chat_message` VALUES (155, 2, 'd7f2a1cf-d5cb-4089-b619-e0dd35622dee', 'assistant', '您的订单「20260917213121866527」已成功取消，对应商品的库存已恢复。', '2026-09-18 09:03:55');
INSERT INTO `ai_chat_message` VALUES (158, 2, '30532203-c78b-4ea4-bee8-6a74fe19364f', 'user', '我的这单信息20260918095912298703', '2026-09-18 09:59:55');
INSERT INTO `ai_chat_message` VALUES (159, 2, '30532203-c78b-4ea4-bee8-6a74fe19364f', 'assistant', '您好，为您查询到订单 **20260918095912298703** 的详细信息如下：\n\n*   **下单时间**：2026-09-18 09:59:13\n*   **订单金额**：297.0 元\n*   **支付状态**：已支付\n*   **购买商品**：Docker 容器化实战 x3\n\n请问您还需要了解关于此订单的其他信息吗？', '2026-09-18 10:00:13');
INSERT INTO `ai_chat_message` VALUES (160, 2, '30532203-c78b-4ea4-bee8-6a74fe19364f', 'user', '有什么会员商品？', '2026-09-18 10:00:34');
INSERT INTO `ai_chat_message` VALUES (161, 2, '30532203-c78b-4ea4-bee8-6a74fe19364f', 'assistant', '商城目前有以下几款会员商品供您选择：\n\n*   **Office 365 年度会员**：价格 198.0 元。正版 Office 365 一年使用权，包含 Word、Excel、PPT。\n*   **WPS 365 会员年卡**：价格 168.0 元。正版 WPS 365 一年使用权，包含云文档、PDF 转换与海量办公模板。\n*   **设计素材会员年卡**：价格 158.0 元。全站素材一年内免费下载，包含商用授权。\n*   **云盘会员年卡**：价格 128.0 元。1TB 云空间一年，支持多端同步与在线预览。\n\n请问您对哪一款感兴趣？', '2026-09-18 10:00:42');
INSERT INTO `ai_chat_message` VALUES (162, 2, 'eafc5260-6a79-4fdc-b2e0-afccd93dd295', 'user', '你知道思诚科技么？', '2026-09-18 15:04:08');
INSERT INTO `ai_chat_message` VALUES (163, 2, 'eafc5260-6a79-4fdc-b2e0-afccd93dd295', 'assistant', '是的，我了解思诚科技。\n\n根据相关资料，**江西思诚科技有限公司**（简称思诚科技）成立于2014年，总部位于江西南昌东部高新区，是一家面向企业、高校和个人提供软件产品研发、信息技术咨询及产教融合服务的高新技术企业。\n\n以下是关于该公司的主要信息：\n\n*   **核心业务**：\n    *   **软件产品**：自主研发了成客CRM客户关系管理系统、在线考试系统、在线评测系统等。\n    *   **企业服务**：为江西邮政集团、南昌市政公用集团等企事业单位提供信息系统项目咨询、团队梯队建设等服务。\n    *   **教育培训**：依托“思诚New学堂”品牌和“南昌思诚IT职业培训学校”，开展软件人才培训业务。\n*   **行业资质与合作**：\n    *   是中国软件行业协会会员单位、教育部就业育人项目成员单位。\n    *   与华为、Oracle、阿里云、RedHat、Adobe等国际知名IT企业合作，拥有鸿蒙、Java、Linux、UI设计等多个方向的官方授权培训中心和考试中心。\n*   **产教融合**：\n    *   联合高校开展专业共建、师资共享、共同举办软件设计大赛、建立认证考试中心，并提供公益训练营和实习就业平台。\n\n请问您是想了解他们的培训课程、软件解决方案，还是其他具体业务吗？', '2026-09-18 15:04:26');

-- ----------------------------
-- Table structure for oms_cart_item
-- ----------------------------
DROP TABLE IF EXISTS `oms_cart_item`;
CREATE TABLE `oms_cart_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `quantity` int NOT NULL DEFAULT 1,
  `checked` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_cart_item
-- ----------------------------

-- ----------------------------
-- Table structure for oms_order
-- ----------------------------
DROP TABLE IF EXISTS `oms_order`;
CREATE TABLE `oms_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` bigint NOT NULL,
  `total_amount` decimal(10, 2) NOT NULL,
  `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '0待支付/1已支付/2已取消',
  `pay_time` datetime NULL DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_order
-- ----------------------------
INSERT INTO `oms_order` VALUES (1, '20260825112958893997', 2, 357.00, 1, '2026-08-25 11:29:58', 0, '2026-08-25 11:29:58', '2026-08-25 11:29:58');
INSERT INTO `oms_order` VALUES (2, '20260901100616301265', 2, 327.00, 1, '2026-09-01 10:06:16', 0, '2026-09-01 10:06:16', '2026-09-01 10:06:16');
INSERT INTO `oms_order` VALUES (3, '20260901143143990987', 2, 118.00, 1, '2026-09-01 14:31:43', 0, '2026-09-01 14:31:43', '2026-09-01 14:31:43');
INSERT INTO `oms_order` VALUES (4, '20260901153202485541', 5, 637.00, 1, '2026-09-01 15:32:02', 0, '2026-09-01 15:32:02', '2026-09-01 15:32:02');
INSERT INTO `oms_order` VALUES (5, '20260901154124139211', 5, 129.00, 1, '2026-09-01 15:41:25', 0, '2026-09-01 15:41:25', '2026-09-01 15:41:25');
INSERT INTO `oms_order` VALUES (8, '20260917155006497564', 2, 198.00, 2, '2026-09-17 15:54:44', 0, '2026-09-17 15:50:07', '2026-09-17 15:55:32');
INSERT INTO `oms_order` VALUES (9, '20260917155043708211', 2, 525.00, 2, '2026-09-17 15:54:46', 0, '2026-09-17 15:50:43', '2026-09-17 15:55:12');
INSERT INTO `oms_order` VALUES (15, '20260917193227532646', 2, 99.00, 2, '2026-09-17 19:32:39', 0, '2026-09-17 19:32:28', '2026-09-17 19:32:42');
INSERT INTO `oms_order` VALUES (16, '20260917193303501554', 2, 88.00, 2, NULL, 0, '2026-09-17 19:33:03', '2026-09-17 19:33:38');
INSERT INTO `oms_order` VALUES (17, '20260917193343692519', 2, 168.00, 2, '2026-09-17 19:33:43', 0, '2026-09-17 19:33:43', '2026-09-17 19:33:54');
INSERT INTO `oms_order` VALUES (21, '20260917213121866527', 2, 45.00, 2, '2026-09-17 21:31:24', 0, '2026-09-17 21:31:21', '2026-09-18 09:03:54');
INSERT INTO `oms_order` VALUES (22, '20260917213132141237', 2, 990.00, 1, '2026-09-17 21:31:33', 0, '2026-09-17 21:31:33', '2026-09-17 21:31:32');
INSERT INTO `oms_order` VALUES (26, '20260918095912298703', 2, 297.00, 1, '2026-09-18 09:59:13', 0, '2026-09-18 09:59:13', '2026-09-18 09:59:13');
INSERT INTO `oms_order` VALUES (27, '20260918100211157632', 2, 823.00, 1, '2026-09-18 10:02:15', 0, '2026-09-18 10:02:11', '2026-09-18 10:02:15');

-- ----------------------------
-- Table structure for oms_order_item
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_item`;
CREATE TABLE `oms_order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `product_price` decimal(10, 2) NOT NULL,
  `quantity` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_order_item
-- ----------------------------
INSERT INTO `oms_order_item` VALUES (1, 1, 1, 'Vue3 从入门到实战', 99.00, 1);
INSERT INTO `oms_order_item` VALUES (2, 1, 2, 'Spring Boot 微服务开发', 129.00, 2);
INSERT INTO `oms_order_item` VALUES (3, 2, 2, 'Spring Boot 微服务开发', 129.00, 1);
INSERT INTO `oms_order_item` VALUES (4, 2, 1, 'Vue3 从入门到实战', 99.00, 2);
INSERT INTO `oms_order_item` VALUES (5, 3, 3, 'Photoshop CC 教程', 59.00, 2);
INSERT INTO `oms_order_item` VALUES (6, 4, 6, 'Java编程思想（电子版）', 39.00, 1);
INSERT INTO `oms_order_item` VALUES (7, 4, 7, 'AI智能客服系统源码', 299.00, 2);
INSERT INTO `oms_order_item` VALUES (8, 5, 2, 'Spring Boot 微服务开发', 129.00, 1);
INSERT INTO `oms_order_item` VALUES (12, 8, 1, 'Vue3 从入门到实战', 99.00, 2);
INSERT INTO `oms_order_item` VALUES (13, 9, 2, 'Spring Boot 微服务开发', 129.00, 1);
INSERT INTO `oms_order_item` VALUES (14, 9, 4, 'Office 365 年度会员', 198.00, 2);
INSERT INTO `oms_order_item` VALUES (20, 15, 1, 'Vue3 从入门到实战', 99.00, 1);
INSERT INTO `oms_order_item` VALUES (21, 16, 8, '网站VIP年卡', 88.00, 1);
INSERT INTO `oms_order_item` VALUES (22, 17, 10, 'WPS 365 会员年卡', 168.00, 1);
INSERT INTO `oms_order_item` VALUES (26, 21, 19, '数据结构与算法（电子版）', 45.00, 1);
INSERT INTO `oms_order_item` VALUES (27, 22, 4, 'Office 365 年度会员', 198.00, 5);
INSERT INTO `oms_order_item` VALUES (31, 26, 17, 'Docker 容器化实战', 99.00, 3);
INSERT INTO `oms_order_item` VALUES (32, 27, 21, '云盘会员年卡', 128.00, 5);
INSERT INTO `oms_order_item` VALUES (33, 27, 13, '草木人间（电子版）', 25.00, 1);
INSERT INTO `oms_order_item` VALUES (34, 27, 2, 'Spring Boot 微服务开发', 129.00, 1);
INSERT INTO `oms_order_item` VALUES (35, 27, 5, 'VS Code Pro 插件包', 29.00, 1);

-- ----------------------------
-- Table structure for pms_category
-- ----------------------------
DROP TABLE IF EXISTS `pms_category`;
CREATE TABLE `pms_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `parent_id` bigint NOT NULL DEFAULT 0,
  `sort` int NOT NULL DEFAULT 0,
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pms_category
-- ----------------------------
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

-- ----------------------------
-- Table structure for pms_product
-- ----------------------------
DROP TABLE IF EXISTS `pms_product`;
CREATE TABLE `pms_product`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `category_id` bigint NULL DEFAULT NULL,
  `price` decimal(10, 2) NOT NULL,
  `original_price` decimal(10, 2) NULL DEFAULT NULL,
  `stock` int NOT NULL DEFAULT 0,
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `sales_count` int NOT NULL DEFAULT 0,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of pms_product
-- ----------------------------
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

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'USER',
  `status` tinyint NOT NULL DEFAULT 1,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$Dt9ARpfwM14a/9KHNHwlz.378JohUOgmyqoGUk7Eaw1L/qlXnpxZa', '系统管理员', NULL, 'ADMIN', 1, 0, '2026-08-24 16:20:22', '2026-08-24 17:45:15');
INSERT INTO `sys_user` VALUES (2, 'user', '$2a$10$EmsBciVAsoQpWVBYf6Skzu1kLKoauaJuD.CfLqAKJZftg4jKzq2Z2', '测试用户', NULL, 'USER', 1, 0, '2026-08-24 16:20:22', '2026-09-17 14:04:25');
INSERT INTO `sys_user` VALUES (5, '18600000000', '$2a$10$fulWBhevU4wXK9pw4UbJ9O3jOxUNIgRgTWa4UvnPKYf2ottkT.1gW', '刘哥哥', NULL, 'USER', 1, 0, '2026-09-01 15:31:38', '2026-09-01 15:31:38');
INSERT INTO `sys_user` VALUES (14, 'user2', '$2a$10$gyDONT3Qk47APBS/3s/gh.fhskIaVb2ZHoJFzfLkcmMo52needmqu', 'user2', NULL, 'USER', 1, 0, '2026-09-17 21:10:35', '2026-09-18 10:03:18');

SET FOREIGN_KEY_CHECKS = 1;
