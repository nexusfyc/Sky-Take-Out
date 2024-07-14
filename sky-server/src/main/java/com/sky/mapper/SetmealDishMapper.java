package com.sky.mapper;

import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    List<SetmealDish> getSetmealByIds(List<Long> ids);

    void insertBatch(List<SetmealDish> setmealDishes);

    void deleteWithSetmeal(List<Long> ids);

    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getDishesBySetmealId(Long id);
}
