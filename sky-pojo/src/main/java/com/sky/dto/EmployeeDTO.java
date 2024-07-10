package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用于接收前端（管理端）传入的新增员工的数据
 */
@Data
public class EmployeeDTO implements Serializable {

    private Long id;

    private String username;

    private String name;

    private String phone;

    private String sex;

    private String idNumber;

}
