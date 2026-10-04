package com.casestudy.abandonment.dao.memory;

import com.casestudy.abandonment.dao.CartActivityDao;
import com.casestudy.abandonment.model.CartActivity;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryCartActivityDao implements CartActivityDao {

    private final Map<String, CartActivity> cartActivityStoreMap = new ConcurrentHashMap<>();

    @Override
    public Optional<CartActivity> findByCartId(String cartId) {
        return Optional.ofNullable(CartActivity.copyOf(cartActivityStoreMap.get(cartId)));
    }

    @Override
    public CartActivity save(CartActivity cartActivity) {
        CartActivity persisted = CartActivity.copyOf(cartActivity);
        cartActivityStoreMap.put(persisted.getCartId(), persisted);
        return CartActivity.copyOf(persisted);
    }
}
