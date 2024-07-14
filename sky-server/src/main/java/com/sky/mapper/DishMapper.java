package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);


    /**
     * 新增菜品
     * @param dish
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    Page<DishVO> getDishPage(DishPageQueryDTO dishPageQueryDTO);

    void deleteBatch(List<Long> ids);

    List<Dish> getDishByIds(List<Long> ids);

    @Select("select dish.* from dish where dish.id = #{id}")
    Dish getDishDetail(Long id);

    @Select("select * from dish_flavor where dish_id = #{id}")
    List<DishFlavor> getDishDetailOnlyFlavors(Long id);

    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);



    List<Dish> listDishByCategoryId(Dish dish);
}
