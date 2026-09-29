package com.casestudy.services;

import com.casestudy.models.CartEvent;
import com.casestudy.models.CartEventResult;

public interface CartEventService {

    CartEventResult process(CartEvent event);
}
