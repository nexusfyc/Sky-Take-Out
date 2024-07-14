package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    public void insert(DishDTO dishDTO);

    PageResult getDishPage(DishPageQueryDTO dishPageQueryDTO);

    void deleteBatch(List<Long> ids);

    DishVO getDishDetail(Long id);

    void updateDishDetail(DishDTO dishDTO);

    List<Dish> getDishByCategoryId(Long categoryId);
}
