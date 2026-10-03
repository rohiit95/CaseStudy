package com.casestudy.dao;

import com.casestudy.models.CartActivity;

import java.util.Optional;

public interface CartActivityStore {

    Optional<CartActivity> findByCartId(String cartId);

    CartActivity save(CartActivity cartActivity);
}
