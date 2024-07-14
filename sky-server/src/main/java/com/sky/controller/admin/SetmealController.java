package com.sky.controller.admin;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @PostMapping
    @ApiOperation("新增套餐")
    public Result<?> insertSetmeal(@RequestBody SetmealDTO setmealDTO) {
        setmealService.insert(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> getSetmealPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageResult pageResult = setmealService.getPage(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("菜品删除")
    public Result<?> delete(@RequestParam List<Long> ids) {
        setmealService.deleteByIds(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("修改套餐（回显）")
    public Result<SetmealVO> getSetmealById(@PathVariable Long id) {
        SetmealVO setmealVO = setmealService.getSetmealById(id);
        return Result.success(setmealVO);
    }

    @PutMapping
    @ApiOperation("修改套餐（提交）")
    public Result<?> update(@RequestBody SetmealDTO setmealDTO) {
        setmealService.updateSetmeal(setmealDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("更改套餐状态")
    public Result<?> changeStatus(@PathVariable Integer status, Long id) {
        //  这里传入的status为变更的目标值
        setmealService.updateSetmealStatus(status, id);
        return Result.success();
    }
}
