package com.casestudy.dao.mysql;

import com.casestudy.models.CartActivity;
import com.casestudy.dao.CartActivityStore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.NotSupportedException;
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
        throw new RuntimeException("Not implemented");
    }

    @Override
    public CartActivity save(CartActivity cartActivity) {
        throw new RuntimeException("Not implemented");
    }
}
