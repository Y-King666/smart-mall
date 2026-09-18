package com.yking.malladmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yking.malladmin.dto.DashboardVO;
import com.yking.malladmin.service.DashboardService;
import com.yking.mallcommon.entity.OmsOrder;
import com.yking.mallcommon.entity.PmsProduct;
import com.yking.mallcommon.mapper.OmsOrderMapper;
import com.yking.mallcommon.mapper.PmsProductMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    /** 趋势图统计天数 */
    private static final int TREND_DAYS = 7;

    /** 支付状态：已支付。销售额只统计已支付订单，见 sumAmount 的说明 */
    private static final int PAY_STATUS_PAID = 1;

    /** 趋势图横轴日期格式 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    private final PmsProductMapper pmsProductMapper;
    private final OmsOrderMapper omsOrderMapper;

    public DashboardServiceImpl(PmsProductMapper pmsProductMapper, OmsOrderMapper omsOrderMapper) {
        this.pmsProductMapper = pmsProductMapper;
        this.omsOrderMapper = omsOrderMapper;
    }

    @Override
    public DashboardVO getDashboard() {
        DashboardVO dashboard = new DashboardVO();

        // 商品总数、订单总数：逻辑删除条件由 MyBatis-Plus 全局配置自动追加
        dashboard.setProductCount(pmsProductMapper.selectCount(null));
        dashboard.setOrderCount(omsOrderMapper.selectCount(null));

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(TREND_DAYS - 1L);

        // 近 7 天的订单一次查出，在内存中按日期汇总，避免逐日查询数据库
        List<OmsOrder> recentOrders = omsOrderMapper.selectList(
                new LambdaQueryWrapper<OmsOrder>()
                        .eq(OmsOrder::getPayStatus, PAY_STATUS_PAID)
                        .ge(OmsOrder::getCreateTime, startDate.atStartOfDay()));

        /*
          累计销售额按「商品累计销量 × 现价」估算，与 mall-analytics 的商品/分类维度同一口径：
          商品带有初始化销量、并没有对应订单，只从订单算会让累计销售额与销量对不上。
          今日销售额与近 7 天趋势是按时间切的，没有对应的历史数据，只能来自真实订单。
        */
        dashboard.setTotalSales(sumEstimatedAmount());
        dashboard.setTodaySales(sumAmount(today.atStartOfDay()));
        dashboard.setRecent7Days(buildTrend(recentOrders, startDate, today));
        return dashboard;
    }

    /**
     * 累计销售额（估算）
     *
     * 按「商品累计销量 × 现价」汇总，与 mall-analytics 的商品/分类维度同一口径。
     * 商品取消订单时销量会回退，这里随之回退。
     */
    private BigDecimal sumEstimatedAmount() {
        QueryWrapper<PmsProduct> wrapper = new QueryWrapper<>();
        wrapper.select("COALESCE(SUM(sales_count * price), 0) AS total");
        Map<String, Object> row = pmsProductMapper.selectMaps(wrapper).get(0);
        return new BigDecimal(row.get("total").toString());
    }

    /**
     * 汇总某时间点之后的销售额（真实订单）
     *
     * 只统计「已支付」订单：待支付还没成交，已取消的（含支付后又取消）钱要退回去，
     * 两者都不能算进销售额。取消一笔已支付订单后 pay_status 变为已取消，
     * 这里自然就不再计入，销售额随之回退。mall-analytics 用同一口径。
     *
     * @param startTime 统计起始时间，为 null 时统计全部已支付订单
     */
    private BigDecimal sumAmount(LocalDateTime startTime) {
        QueryWrapper<OmsOrder> wrapper = new QueryWrapper<>();
        wrapper.select("COALESCE(SUM(total_amount), 0) AS total")
                .eq("pay_status", PAY_STATUS_PAID);
        if (startTime != null) {
            wrapper.ge("create_time", startTime);
        }
        // 不含 GROUP BY 的聚合查询恒返回一行，可安全取首行
        Map<String, Object> row = omsOrderMapper.selectMaps(wrapper).get(0);
        return new BigDecimal(row.get("total").toString());
    }

    /** 按日期汇总订单数与销售额，无订单的日期补 0 */
    private List<DashboardVO.DailySales> buildTrend(List<OmsOrder> orders, LocalDate startDate, LocalDate today) {
        Map<LocalDate, List<OmsOrder>> ordersByDate = orders.stream()
                .collect(Collectors.groupingBy(order -> order.getCreateTime().toLocalDate()));

        List<DashboardVO.DailySales> trend = new ArrayList<>(TREND_DAYS);
        for (LocalDate date = startDate; !date.isAfter(today); date = date.plusDays(1)) {
            List<OmsOrder> dailyOrders = ordersByDate.getOrDefault(date, List.of());
            BigDecimal sales = dailyOrders.stream()
                    .map(OmsOrder::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            trend.add(new DashboardVO.DailySales(date.format(DATE_FORMATTER), sales, (long) dailyOrders.size()));
        }
        return trend;
    }
}
