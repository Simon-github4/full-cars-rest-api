package com.fullcars.restapi.event;

import com.fullcars.restapi.enums.EventType;
import com.fullcars.restapi.model.PurchaseDetail;

public class PurchaseDetailEvent extends BaseEntityEvent<PurchaseDetail> {
    private static final long serialVersionUID = 1L;
    
    public PurchaseDetailEvent(Object source, PurchaseDetail entity, EventType eventType) {
        super(source, entity, eventType);
    }
}
