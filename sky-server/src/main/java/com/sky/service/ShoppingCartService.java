package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    List<ShoppingCart> list();

    void add(ShoppingCartDTO shoppingCartDTO);

    void delete();

    void sub(ShoppingCartDTO shoppingCartDTO);
}
