package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;

public interface DishService {

    public void insert(DishDTO dishDTO);

    PageResult getDishPage(DishPageQueryDTO dishPageQueryDTO);
}
