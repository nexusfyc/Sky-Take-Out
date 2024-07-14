package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {


    /**
     * 批量插入口味数据（一个菜品可以添加多组口味）
     * @param flavors
     */
    void insert(List<DishFlavor> flavors);

    void deleteBatch(List<Long> ids);

    @Delete("delete from dish_flavor where dish_id = #{id}")
    void deleteByDishId(Long id);

}
