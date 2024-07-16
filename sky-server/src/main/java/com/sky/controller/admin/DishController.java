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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@Slf4j
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 新增菜品：插入dish、插入dish_flavor
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result<?> insert(@RequestBody DishDTO dishDTO) {
        dishService.insert(dishDTO);
        //  新增菜品后，会影响到用户端展示内存中的数据，清空掉相应分类的所有菜品
        StringBuilder key = new StringBuilder("dish_");
        redisTemplate.delete(key.append(dishDTO.getCategoryId()).toString());
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
        //  批量删除菜品可能会影响到用户端的多个分类，则全部删除即可
        clearCache("dish_*");
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
        //  新增菜品后，会影响到用户端展示内存中的数据，清空掉相应分类的所有菜品
        StringBuilder key = new StringBuilder("dish_");
        redisTemplate.delete(key.append(dishDTO.getCategoryId()).toString());
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> getDish(Long categoryId) {
        List<Dish> dishList = dishService.getDishByCategoryId(categoryId);
        return Result.success(dishList);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("更改菜品状态")
    public Result<?> changeStatus(@PathVariable Integer status, Long id) {
        dishService.updateDish(status, id);
        //  批量删除菜品可能会影响到用户端的多个分类，则全部删除即可
        clearCache("dish_*");
        return Result.success();
    }

    private void clearCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
