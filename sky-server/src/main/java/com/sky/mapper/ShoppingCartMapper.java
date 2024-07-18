package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {


    @Select("select * from shopping_cart where user_id = #{id}")
    List<ShoppingCart> list(Long id);

    @Insert("insert into shopping_cart (name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time) " +
            " values (#{name},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{image},#{createTime})")
    void save(ShoppingCart shoppingCartDish);


    Integer countDishOrSetmeal(ShoppingCart shoppingCart);


    void updateDishOrSetmeal(ShoppingCart shoppingCart);

    @Delete("delete from shopping_cart where user_id = #{currentId}")
    void deleteAll(Long currentId);

    void deleteDishOrSetmeal(ShoppingCart shoppingCart);
}
