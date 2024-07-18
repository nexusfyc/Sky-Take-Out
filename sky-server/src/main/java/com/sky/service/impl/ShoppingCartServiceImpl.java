package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 获得购物车列表
     * @return
     */
    public List<ShoppingCart> list() {
        return shoppingCartMapper.list(BaseContext.getCurrentId());
    }

    /**
     * 添加菜品或套餐到购物车
     * @param shoppingCartDTO
     */
    public void add(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //  查看shopping_cart这个菜品或套餐是否已经添加，如果已经添加更改数量即可（根据user_id和dish_id来查菜品数量）
        Integer countNum = shoppingCartMapper.countDishOrSetmeal(shoppingCart);
        if (countNum != null && countNum > 0) {
            //  购物车中已存在菜品或套餐，更新其数量即可
            shoppingCart.setNumber(countNum + 1);
            shoppingCartMapper.updateDishOrSetmeal(shoppingCart);
            return ;
        }else {
            //  购物车中不存在菜品或套餐，需要新加入购物车
            shoppingCart.setNumber(1);
        }
        if (shoppingCartDTO.getDishId()!= null) {
            //  添加的是菜品
            //  获取菜品的信息
            Dish dish = dishMapper.getDishDetail(shoppingCartDTO.getDishId());
            shoppingCart.setAmount(dish.getPrice());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setName(dish.getName());

        }else {
            //  添加的是套餐
            //  获取套餐信息
            SetmealVO setmeal = setmealMapper.getSetmealById(shoppingCartDTO.getSetmealId());
            shoppingCart.setAmount(setmeal.getPrice());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setName(setmeal.getName());
        }
        shoppingCart.setCreateTime(LocalDateTime.now());

        shoppingCartMapper.save(shoppingCart);

    }

    @Override
    public void delete() {
        shoppingCartMapper.deleteAll(BaseContext.getCurrentId());
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        //  当某菜品或套餐多于2个时，调用该方法改变数量；只有一个时则直接从购物车中删除该菜品或套餐
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //  查看shopping_cart这个菜品或套餐是否已经添加，如果已经添加更改数量即可（根据user_id和dish_id来查菜品数量）
        Integer countNum = shoppingCartMapper.countDishOrSetmeal(shoppingCart);
        if (countNum != null && countNum > 1) {
            //  购物车中已存在菜品或套餐，更新其数量即可
            shoppingCart.setNumber(countNum - 1);
            shoppingCartMapper.updateDishOrSetmeal(shoppingCart);
        }else {
            //  购物车只存在一个该菜品或套餐，直接删除
            shoppingCartMapper.deleteDishOrSetmeal(shoppingCart);
        }
    }
}
