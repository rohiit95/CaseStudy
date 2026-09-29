package com.casestudy.dao;

import com.casestudy.models.CartActivity;

import java.util.Optional;

public interface CartActivityDao {

    Optional<CartActivity> findByCartId(String cartId);

    CartActivity save(CartActivity cartActivity);
}
