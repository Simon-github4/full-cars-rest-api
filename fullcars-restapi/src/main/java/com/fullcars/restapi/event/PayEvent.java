package com.fullcars.restapi.event;

import com.fullcars.restapi.enums.EventType;
import com.fullcars.restapi.model.Pay;

public class PayEvent extends BaseEntityEvent<Pay> {
    private static final long serialVersionUID = 1L;
    
    public PayEvent(Object source, Pay entity, EventType eventType) {
        super(source, entity, eventType);
    }
}