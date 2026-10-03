package com.casestudy.dao.memory;

import com.casestudy.models.CartActivity;
import com.casestudy.dao.CartActivityStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(prefix = "casestudy", name = "storage", havingValue = "in-memory")
public class InMemoryCartActivityStore implements CartActivityStore {

    private final Map<String, CartActivity> byCartId = new ConcurrentHashMap<>();

    @Override
    public Optional<CartActivity> findByCartId(String cartId) {
        return Optional.ofNullable(byCartId.get(cartId)).map(InMemoryCartActivityStore::copy);
    }

    @Override
    public CartActivity save(CartActivity cartActivity) {
        CartActivity persisted = copy(cartActivity);
        byCartId.put(persisted.getCartId(), persisted);
        return copy(persisted);
    }

    static CartActivity copy(CartActivity source) {
        CartActivity copy = new CartActivity();
        copy.setCartId(source.getCartId());
        copy.setUserId(source.getUserId());
        copy.setActivityVersion(source.getActivityVersion());
        copy.setLastActivityTime(source.getLastActivityTime());
        copy.setState(source.getState());
        copy.setCreatedAt(source.getCreatedAt());
        copy.setUpdatedAt(source.getUpdatedAt());
        copy.setLastEventId(source.getLastEventId());
        copy.setCartActivityCol(source.getCartActivityCol());
        return copy;
    }
}
