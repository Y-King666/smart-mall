package com.yking.mallai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

/**
 * RAG配置类，用于配置向量存储和嵌入模型
 */
@Slf4j
@Configuration
public class RagConfig {

    /** 每次检索返回的片段数：太多会挤占上下文预算，太少可能漏掉答案 */
    private static final int TOP_K = 4;

    /**
     * 相似度阈值：低于该值的片段不注入 prompt。
     * 客服的多数提问（查订单、取消订单、购物车）与知识库无关，
     * 不过滤会让这些无关片段混进上下文干扰回答。
     */
    private static final double SIMILARITY_THRESHOLD = 0.5;

    @Value("classpath:Consultation2025.pdf")
    private Resource pdfResource;

    /**
     * 向量库持久化文件路径。
     *
     * SimpleVectorStore 是内存版，不落盘的话每次启动都要把整份 PDF 重新向量化，
     * 持续消耗嵌入模型的调用额度。这里把向量连同文本一起存到文件，
     * 之后启动直接 load（纯反序列化，不调用嵌入模型）。
     */
    @Value("${rag.vector-store-path:./data/vector-store.json}")
    private String vectorStorePath;

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // 1.创建向量存储
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        try {
            // 2.优先复用本地向量文件，避免重复调用嵌入模型
            if (loadFromCache(vectorStore)) {
                return vectorStore;
            }
            // 3.无可用缓存（首次运行，或 PDF 内容已变化），重新构建并落盘
            buildFromPdf(vectorStore);
            saveToCache(vectorStore);
        } catch (Exception e) {
            // 不在启动阶段抛出：知识库属于增强能力，不该拖垮整个应用。
            // 但必须留下明确的告警，否则失败是静默的——向量库空着，
            // 问答退化成普通对话，从外部完全看不出异常。
            log.warn("知识库初始化失败，RAG 未生效，问答将退化为普通对话。"
                    + "请检查 AI_API_KEY 是否已配置、Consultation2025.pdf 是否可读。", e);
        }
        return vectorStore;
    }

    /**
     * 检索增强 advisor：每次对话自动检索知识库，把相关片段注入 prompt。
     * 这里只给检索模板（数量与阈值），真正的查询语句由 advisor 在每次请求时填入。
     */
    @Bean
    public QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(TOP_K)
                        .similarityThreshold(SIMILARITY_THRESHOLD)
                        .build())
                .build();
    }

    /**
     * 向量文件存在、且记录的 PDF 指纹与当前 PDF 一致时，直接读取本地向量库。
     * 指纹不一致说明知识库换过内容，必须重建，否则会继续拿旧知识回答。
     */
    private boolean loadFromCache(SimpleVectorStore vectorStore) {
        File cacheFile = new File(vectorStorePath);
        if (!cacheFile.isFile()) {
            return false;
        }
        String cached = readFingerprint();
        String current = currentPdfFingerprint();
        if (cached.isEmpty() || current.isEmpty() || !cached.equals(current)) {
            log.info("本地向量库无法复用（知识库内容已变化或指纹缺失），重新构建");
            return false;
        }
        try {
            vectorStore.load(cacheFile);
            log.info("知识库已从本地向量文件加载，本次未调用嵌入模型：{}", cacheFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            log.warn("本地向量文件不可用，改为重新构建：{}", cacheFile.getAbsolutePath(), e);
            return false;
        }
    }

    /** 读取 PDF、切分并向量化入库 */
    private void buildFromPdf(SimpleVectorStore vectorStore) {
        log.info("================== 开始加载 PDF 知识库文档 ==================");
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(pdfResource);
        List<Document> documents = tikaDocumentReader.get();
        log.info("PDF 文档加载完成，文档数量：{}", documents.size());
        // 将文档分割成较小的块
        TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder().build();
        List<Document> chunks = tokenTextSplitter.apply(documents);  // 分割文档成块，生成块列表
        log.info("文档分割完成，块数量：{}", chunks.size());
        // 将块向量化并存储在向量存储中
        vectorStore.add(chunks);  // 向量数据库存储初始化
        log.info("向量数据库存储初始化完成，已入库 {} 个片段", chunks.size());
    }

    /** 把向量库连同 PDF 指纹落盘，下次启动即可跳过向量化 */
    private void saveToCache(SimpleVectorStore vectorStore) throws Exception {
        File cacheFile = new File(vectorStorePath);
        File parent = cacheFile.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        vectorStore.save(cacheFile);
        Files.writeString(fingerprintFile().toPath(), currentPdfFingerprint(), StandardCharsets.UTF_8);
        log.info("向量库已持久化到本地，后续启动将直接复用：{}", cacheFile.getAbsolutePath());
    }

    private File fingerprintFile() {
        return new File(vectorStorePath + ".sha256");
    }

    /** 上次构建向量库时 PDF 的指纹，读不到就返回空串（视为不可复用） */
    private String readFingerprint() {
        try {
            return Files.readString(fingerprintFile().toPath(), StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            return "";
        }
    }

    /** 当前 PDF 内容的 SHA-256，用于判断向量库是否需要重建 */
    private String currentPdfFingerprint() {
        try (InputStream in = pdfResource.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception e) {
            log.warn("计算知识库文档指纹失败，将重新构建向量库", e);
            return "";
        }
    }
}
