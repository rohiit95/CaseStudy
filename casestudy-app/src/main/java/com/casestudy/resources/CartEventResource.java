package com.casestudy.resources;

import com.casestudy.models.CartEvent;
import com.casestudy.models.CartEventResult;
import com.casestudy.services.CartEventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
public class CartEventResource {
    private final CartEventService cartEventService;

    public CartEventResource(CartEventService cartEventService) {
        this.cartEventService = cartEventService;
    }

    @PostMapping("/api/cart/events")
    @ResponseStatus(HttpStatus.CREATED)
    public CartEventResult createCart(@Valid @RequestBody(required = true) CartEvent event) {
        return cartEventService.process(event);
    }
}
