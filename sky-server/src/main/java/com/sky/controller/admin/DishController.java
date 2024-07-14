package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishFlavorService;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;


    /**
     * 新增菜品：插入dish、插入dish_flavor
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result<?> insert(@RequestBody DishDTO dishDTO) {
        dishService.insert(dishDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询菜品")
    public Result<PageResult> getDishPage(DishPageQueryDTO dishPageQueryDTO) {
        PageResult pageResult = dishService.getDishPage(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result<?> delete(@RequestParam List<Long> ids) {
        log.info("查看传入参数：", ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("菜品数据修改（回显）")
    public Result<DishVO> getDishDetail(@PathVariable Long id) {
        DishVO dishVO = dishService.getDishDetail(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("菜品数据修改（提交修改）")
    public Result<?> updateDish(@RequestBody DishDTO dishDTO) {
        dishService.updateDishDetail(dishDTO);
        return Result.success();
    }
}
