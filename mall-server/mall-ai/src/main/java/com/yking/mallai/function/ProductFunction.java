package com.yking.mallai.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yking.mallcommon.entity.PmsProduct;
import com.yking.mallcommon.mapper.PmsProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ProductFunction {

    /** 热销榜最多返回的商品数 */
    private static final int HOT_LIMIT = 5;

    private final PmsProductMapper pmsProductMapper;

    public ProductFunction(PmsProductMapper pmsProductMapper) {
        this.pmsProductMapper = pmsProductMapper;
    }

    @Tool(description = "根据商品名称模糊查询商品信息，返回商品名称、价格、库存和描述")
    public List<ProductInfo> getProductInfo(@ToolParam(description = "商品名称关键字") String productName){
        log.info("【ToolCall】getProductInfo 根据商品名称模糊查询商品信息，参数：{}", productName);

        // 1.构建查询条件
        LambdaQueryWrapper<PmsProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(PmsProduct::getName, productName)  // 模糊匹配商品名称
                .eq(PmsProduct::getStatus, 1)  // 商品状态为上架
                .last("limit 5");  // 最多返回5条记录

        // 2.调用数据库查询商品数据
        List<PmsProduct> productList = pmsProductMapper.selectList(queryWrapper);

        // 3.将查询到的商品数据转换为AI需要的数据结构
        List<ProductInfo> productInfoList = productList.stream().map(product -> new ProductInfo(
                product.getName(),
                product.getPrice().doubleValue(),
                product.getStock(),
                product.getDescription()
        )).toList();
        log.info("【ToolCall】getProductInfo 查询结果：{}", productInfoList);
        return productInfoList;
    }

    @Tool(description = "查询商城的畅销商品，按累计销量从高到低返回最多 5 件商品，用于向用户推荐热销好物")
    public List<ProductInfo> getHotProducts() {
        log.info("【ToolCall】getHotProducts 查询热销商品");

        // 1.构建查询条件：只取上架商品，销量高的排前面
        LambdaQueryWrapper<PmsProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PmsProduct::getStatus, 1)  // 商品状态为上架
                .orderByDesc(PmsProduct::getSalesCount)
                .last("limit " + HOT_LIMIT);  // 最多返回5条记录

        // 2.调用数据库查询商品数据
        List<PmsProduct> productList = pmsProductMapper.selectList(queryWrapper);

        // 3.将查询到的商品数据转换为AI需要的数据结构
        List<ProductInfo> productInfoList = productList.stream().map(product -> new ProductInfo(
                product.getName(),
                product.getPrice().doubleValue(),
                product.getStock(),
                product.getDescription()
        )).toList();
        log.info("【ToolCall】getHotProducts 查询结果：{}", productInfoList);
        return productInfoList;
    }

    /**
     * 商品信息记录(返回给AI的数据结构)
     * @param name
     * @param price
     * @param stock
     * @param description
     */

    public record ProductInfo(String name, double price,Integer stock,String description){ }
}
