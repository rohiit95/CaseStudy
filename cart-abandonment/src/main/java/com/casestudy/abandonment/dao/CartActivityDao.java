package com.casestudy.abandonment.dao;

import com.casestudy.abandonment.model.CartActivity;

import java.util.Optional;

public interface CartActivityDao {

    Optional<CartActivity> findByCartId(String cartId);

    CartActivity save(CartActivity cartActivity);
}
