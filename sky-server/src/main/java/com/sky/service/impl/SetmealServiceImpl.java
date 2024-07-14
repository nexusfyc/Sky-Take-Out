package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishFlavorService;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    @Transactional
    public void insert(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //  1.存入套餐数据（setmeal表）
        setmealMapper.insert(setmeal);
        //  2.存入套餐关联菜品数据（setmeal_dish表）
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmeal.getId());
        }
        setmealDishMapper.insertBatch(setmealDishes);
    }

    @Override
    public PageResult getPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        //  分页查询套餐数据
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.getPage(setmealPageQueryDTO);
        //  多表联查
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        //  计算这些id中哪些状态为起售
        Long onStatusCount = setmealMapper.countOnStatus(ids);
        if (onStatusCount > 0) {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        //  选中套餐均为停售状态即可删除
        setmealMapper.deleteByIds(ids);
        //  删除与这些套餐关联的菜品信息
        setmealDishMapper.deleteWithSetmeal(ids);
    }

    @Override
    @Transactional
    public SetmealVO getSetmealById(Long id) {
        SetmealVO setmealVO = setmealMapper.getSetmealById(id);
        List<SetmealDish> dishListWithSetmeal = setmealDishMapper.getDishesBySetmealId(id);
        setmealVO.setSetmealDishes(dishListWithSetmeal);
        return setmealVO;
    }

    @Override
    @Transactional
    public void updateSetmeal(SetmealDTO setmealDTO) {
        //  更新菜品信息，包括更新时间、更新用户
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.updateSetmeal(setmeal);
        //  删掉所有的菜品关联
        List<Long> ids = new ArrayList<>();
        ids.add(setmealDTO.getId());
        setmealDishMapper.deleteWithSetmeal(ids);
        //  添加新的菜品关联
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealDTO.getId());
        }
        setmealDishMapper.insertBatch(setmealDishes);
    }

    @Override
    public void updateSetmealStatus(Integer status, Long id) {
        //  更改套餐状态
        //  起售套餐时，如果关联菜品中有停售菜品，则不能起售
        if (status == StatusConstant.ENABLE) {
            //  套餐要变更为起售状态，需要进行关联菜品的状态判断
            List<SetmealDish> dishesBySetmealId = setmealDishMapper.getDishesBySetmealId(id);
            List<Long> ids = new ArrayList<>();
            for (SetmealDish setmealDish : dishesBySetmealId) {
                ids.add(setmealDish.getDishId());
            }
            List<Dish> dishByIds = dishMapper.getDishByIds(ids);
            for (Dish dishById : dishByIds) {
                //  包含停售菜品，抛出异常
                if (dishById.getStatus() == 0) {
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }
        }

        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        //  条件判断通过，可以改变状态
        setmealMapper.updateSetmeal(setmeal);

    }
}
