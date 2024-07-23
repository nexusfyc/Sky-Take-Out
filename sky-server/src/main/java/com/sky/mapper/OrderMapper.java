package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 保存订单
     * @param orders
     */
    void save(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{check_out_time} where id = #{id}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime check_out_time, Long id);


    Page<OrderVO> getHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    OrderVO getOrderDetail(Long id);

    @Select("select * from order_detail where order_id = #{id}")
    List<OrderDetail> getOrderDetailList(Long id);

    void cancelOrder(Orders order);

    Page<OrderVO> searchOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select count(*) from orders where status = #{status}")
    Integer countStatus(Orders order);

    @Update("update orders set status = #{status} where id = #{id}")
    void confirmOrder(OrdersConfirmDTO ordersConfirmDTO);


    void rejectOrder(Orders order);

    @Update("update orders set status = 4 where id = #{id}")
    void deliveryOrder(Long id);

    @Update("update orders set status = 5 where id = #{id}")
    void finishOrder(Long id);

    @Select("select * from orders where status = #{pendingPayment} and order_time < #{fifteenMinutesAgo}")
    List<Orders> getByStatusAndOrdertimeLT(Integer pendingPayment, LocalDateTime fifteenMinutesAgo);


    Double sumByMap(Map map);

    Integer countByMap(Map map);

    @Select("select sum(amount) sa, name\n" +
            "from (select o.id, o.status, od.amount, od.name from orders o LEFT JOIN order_detail od " +
            "on o.id = od.order_id where o.status = 5 and o.order_time BETWEEN #{beginTime} and #{endTime}) as tb\n" +
            "        GROUP BY name order by sa desc limit 10")
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime beginTime, LocalDateTime endTime);
}
