package com.casestudy.dao.impl;

import com.casestudy.dao.CartActivityDao;
import com.casestudy.models.CartActivity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CartActivityDaoImpl implements CartActivityDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<CartActivity> findByCartId(String cartId) {
        return Optional.ofNullable(entityManager.find(CartActivity.class, cartId));
    }

    @Override
    public CartActivity save(CartActivity cartActivity) {
        CartActivity persisted = entityManager.merge(cartActivity);
        entityManager.flush();
        return persisted;
    }
}
