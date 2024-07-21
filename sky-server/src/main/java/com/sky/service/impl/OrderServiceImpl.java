package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private Orders orders;

    /**
     * 提交订单（未支付）
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        AddressBook address = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        //  未填写地址则抛出异常
        if (address == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        //  设置订单状态：未付款（1）
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(BaseContext.getCurrentId());
        orders.setOrderTime(LocalDateTime.now());
        //  本项目默认使用微信支付
//        orders.setPayMethod(1);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setPhone(address.getPhone());
        orders.setAddress(address.getDetail());
        orders.setConsignee(address.getConsignee());
        this.orders = orders;
        orderMapper.save(orders);

        //  向order_detail表中插入若干条购物车中的数据
        List<ShoppingCart> cartList = shoppingCartMapper.list(orders.getUserId());
        if(cartList == null || cartList.size() == 0) {
            //  如果查找不到数据或购物车为空则抛出异常
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart shoppingCart : cartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        //  批量插入购物车数据
        orderDetailMapper.save(orderDetailList);
        //  构造返回的订单数据对象
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .build();

        //  下单后清空购物车
        shoppingCartMapper.deleteAll(BaseContext.getCurrentId());
        return orderSubmitVO;

    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code","ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        Integer OrderPaidStatus = Orders.PAID;//支付状态，已支付
        Integer OrderStatus = Orders.TO_BE_CONFIRMED;  //订单状态，待接单
        LocalDateTime check_out_time = LocalDateTime.now();//更新支付时间
        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, this.orders.getId());

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 获取历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult getHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<OrderVO> page = orderMapper.getHistoryOrders(ordersPageQueryDTO);

        for (OrderVO orderVO : page.getResult()) {
            List<OrderDetail> orderDetailList = orderMapper.getOrderDetailList(orderVO.getId());
            orderVO.setOrderDetailList(orderDetailList);
        }
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 获取历史订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO getOrderDetail(Long id) {
        OrderVO orderDetail = orderMapper.getOrderDetail(id);
        List<OrderDetail> orderDetailList = orderMapper.getOrderDetailList(orderDetail.getId());
        orderDetail.setOrderDetailList(orderDetailList);
        return orderDetail;
    }

    /**
     * 取消订单（用户端）
     * @param id
     */
    @Override
    public void cancelOrder(Long id) {
        Orders order = Orders.builder()
                .status(Orders.CANCELLED)
                .id(id)
                .build();
        orderMapper.cancelOrder(order);
    }

    /**
     * 取消订单（管理端）
     * @param ordersCancelDTO
     */
    @Override
    public void cancelOrder(OrdersCancelDTO ordersCancelDTO) {
        Orders order = Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.rejectOrder(order);
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void oneMoreOrder(Long id) {
        //  重新提交商品至购物车
        List<OrderDetail> orderDetailList = orderDetailMapper.getDishOrSetmeal(id);
        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCartDTO shoppingCartDTO = new ShoppingCartDTO();
            BeanUtils.copyProperties(orderDetail, shoppingCartDTO);
            shoppingCartService.add(shoppingCartDTO);
        }
    }

    @Override
    public PageResult getHistoryOrdersOnAdmin(OrdersPageQueryDTO ordersPageQueryDTO) {
        Page<OrderVO> orders = orderMapper.searchOrders(ordersPageQueryDTO);
        return new PageResult(orders.getTotal(), orders.getResult());
    }

    @Override
    public OrderStatisticsVO statistics() {
        Orders confirmed = Orders.builder().status(Orders.CONFIRMED).build();
        Integer confirmedNum = orderMapper.countStatus(confirmed);
        Orders deliveryInProgress = Orders.builder().status(Orders.DELIVERY_IN_PROGRESS).build();
        Integer deliveryInProgressNum = orderMapper.countStatus(deliveryInProgress);
        Orders toBeConfirmed = Orders.builder().status(Orders.TO_BE_CONFIRMED).build();
        Integer toBeConfirmedNum = orderMapper.countStatus(toBeConfirmed);
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmedNum);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgressNum);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmedNum);
        return orderStatisticsVO;
    }

    @Override
    public void confirmOrder(OrdersConfirmDTO ordersConfirmDTO) {
        ordersConfirmDTO.setStatus(Orders.CONFIRMED);
        orderMapper.confirmOrder(ordersConfirmDTO);
    }

    @Override
    public void rejectOrder(OrdersRejectionDTO ordersRejectionDTO) {
        OrderVO orderDetail = orderMapper.getOrderDetail(ordersRejectionDTO.getId());
        if (!Objects.equals(orderDetail.getStatus(), Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

//        //支付状态
//        Integer payStatus = orderDetail.getPayStatus();
//        if (payStatus == Orders.PAID) {
//            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    orderDetail.getNumber(),
//                    orderDetail.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
//        }

        Orders order = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.rejectOrder(order);
    }

    @Override
    public void deliveryOrder(Long id) {
        OrderVO orderVO = orderMapper.getOrderDetail(id);
        if (orderVO == null || !orderVO.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orderMapper.deliveryOrder(id);
    }

    @Override
    public void completeOrder(Long id) {
        OrderVO orderVO = orderMapper.getOrderDetail(id);
        if (orderVO == null || !orderVO.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orderMapper.finishOrder(id);
    }
}
