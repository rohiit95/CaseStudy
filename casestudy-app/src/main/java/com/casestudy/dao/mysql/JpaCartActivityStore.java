package com.casestudy.dao.mysql;

import com.casestudy.models.CartActivity;
import com.casestudy.dao.CartActivityStore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
@ConditionalOnExpression("'${casestudy.storage:in-memory}' == 'mysql' || '${casestudy.storage:in-memory}' == 'mysql-redis'")
public class JpaCartActivityStore implements CartActivityStore {

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
