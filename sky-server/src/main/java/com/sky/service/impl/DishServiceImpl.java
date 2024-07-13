package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品
     * @param dishDTO
     */
    @Override
    public void insert(DishDTO dishDTO) {
        //  DishDTO接收的数据中包括口味数据，而dish表中没有该字段，需要实例化Dish对象并复制属性并去除口味字段
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //  获取口味字段
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //  新菜品数据先插入dish表，再由DishMapper返回主键id供插入dish_flavor时使用，将口味与菜品关联
        dishMapper.insert(dish);
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach((item) -> {
                item.setDishId(dish.getId());
            });
        }
        //  拿到主键id即菜品id后进行dish_flavor表插入口味数据
        dishFlavorMapper.insert(flavors);

    }
}
