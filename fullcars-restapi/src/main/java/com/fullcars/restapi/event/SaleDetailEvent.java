package com.fullcars.restapi.event;

import com.fullcars.restapi.enums.EventType;
import com.fullcars.restapi.model.Sale;
import com.fullcars.restapi.model.SaleDetail;

public class SaleDetailEvent extends BaseEntityEvent<SaleDetail> {
    private static final long serialVersionUID = 1L;
    
    public SaleDetailEvent(Object source, SaleDetail entity, EventType eventType) {
        super(source, entity, eventType);
    }
}