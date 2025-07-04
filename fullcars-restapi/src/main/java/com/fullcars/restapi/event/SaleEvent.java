package com.fullcars.restapi.event;

import com.fullcars.restapi.enums.EventType;
import com.fullcars.restapi.model.Sale;

public class SaleEvent extends BaseEntityEvent<Sale> {
    private static final long serialVersionUID = 1L;
    
    public SaleEvent(Object source, Sale entity, EventType eventType) {
        super(source, entity, eventType);
    }
}
