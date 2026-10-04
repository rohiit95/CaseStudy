package com.casestudy.abandonment.detect;

import com.casestudy.abandonment.model.CartEvent;
import com.casestudy.abandonment.model.CartProcessResult;

public interface CartEventProcessor {

    CartProcessResult process(CartEvent event);
}
