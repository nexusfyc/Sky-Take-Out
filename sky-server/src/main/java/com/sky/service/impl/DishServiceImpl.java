package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品
     * @param dishDTO
     */
    @Override
    @Transactional
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

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     */
    @Override
    public PageResult getDishPage(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.getDishPage(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 删除菜品
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //  1.如果菜品状态为启用，则不能删除（抛出异常）
        List<Dish> dishOnList = dishMapper.getDishByIds(ids);
        if (dishOnList != null && dishOnList.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }
        //  2.如果菜品关联了套餐，则不能删除（抛出异常）
        List<SetmealDish> setmealList = setmealDishMapper.getSetmealByIds(ids);
        if (setmealList != null && setmealList.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //  3.删除菜品
        dishMapper.deleteBatch(ids);
        //  4.删除菜品对应的口味
        dishFlavorMapper.deleteBatch(ids);
    }

    /**
     * 菜品详情回显
     * @param id
     * @return
     */
    @Override
    public DishVO getDishDetail(Long id) {
        Dish dishDetail = dishMapper.getDishDetail(id);
        List<DishFlavor> flavorList = dishMapper.getDishDetailOnlyFlavors(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dishDetail, dishVO);
        dishVO.setFlavors(flavorList);
        return dishVO;
    }

    /**
     * 修改菜品信息
     * @param dishDTO
     */
    @Override
    @Transactional
    public void updateDishDetail(DishDTO dishDTO) {
        //  更新菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        //  先删掉所有相关口味，再添加新口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach((item) -> {
                item.setDishId(dishDTO.getId());
            });
            //  拿到主键id即菜品id后且修改（新增）了口味，则进行dish_flavor表插入口味数据
            dishFlavorMapper.insert(flavors);
        }


    }

    @Override
    public List<Dish> getDishByCategoryId(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.listDishByCategoryId(dish);
    }
}
