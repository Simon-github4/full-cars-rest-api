package com.fullcars.restapi.event;

import com.fullcars.restapi.enums.EventType;
import com.fullcars.restapi.model.Purchase;

public class PurchaseEvent extends BaseEntityEvent<Purchase> {
    private static final long serialVersionUID = 1L;
    
    public PurchaseEvent(Object source, Purchase entity, EventType eventType) {
        super(source, entity, eventType);
    }
}
