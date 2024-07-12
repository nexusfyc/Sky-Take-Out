package com.sky.Aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;

@Component
@Aspect
public class AutoFillAspect {

    //  抽取公共切入点表达式，但是匹配范围仍然过大，需要通过@AutoFill标记具体的方法
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    private void autoFillPointCut(){};
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        //  Signature类不包含getMethod方法，需强转为MethodSignature类型
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        //  通过反射拿到注解对象
        AutoFill autoFill = methodSignature.getMethod().getAnnotation(AutoFill.class);
        //  获取注解中的参数
        OperationType operationType = autoFill.value();
        //  获取被标记方法的参数
        Object[] args = joinPoint.getArgs();
        //  避免异常
        if(args == null || args.length == 0){
            return;
        }

        Class<?> clazz = args[0].getClass();
        if (operationType == OperationType.INSERT) {
            //  连接点为insert类型的方法，需要统一填充字段：create_time、create_user、update_time、update_user
            //  通过反射拿到相应方法并调用
            try {
                Method setCreateTime = clazz.getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = clazz.getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = clazz.getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = clazz.getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setCreateTime.invoke(args[0], LocalDateTime.now());
                setCreateUser.invoke(args[0], BaseContext.getCurrentId());
                setUpdateTime.invoke(args[0], LocalDateTime.now());
                setUpdateUser.invoke(args[0], BaseContext.getCurrentId());
            } catch (Exception e) {
                e.printStackTrace();
            }


        }else if(operationType == OperationType.UPDATE) {
            //  连接点为update类型的方法，需要统一填充（更新）字段：update_time、update_user
            try {
                Method setUpdateTime = clazz.getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = clazz.getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(args[0], LocalDateTime.now());
                setUpdateUser.invoke(args[0], BaseContext.getCurrentId());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}
