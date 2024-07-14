package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {


    void insert(SetmealDTO setmealDTO);

    PageResult getPage(SetmealPageQueryDTO setmealPageQueryDTO);

    void deleteByIds(List<Long> ids);

    SetmealVO getSetmealById(Long id);

    void updateSetmeal(SetmealDTO setmealDTO);

    void updateSetmealStatus(Integer status, Long id);
}
