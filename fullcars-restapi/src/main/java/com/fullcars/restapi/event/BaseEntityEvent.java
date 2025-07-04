package com.fullcars.restapi.event;

import org.springframework.context.ApplicationEvent;
import com.fullcars.restapi.enums.EventType;

public abstract class BaseEntityEvent<T> extends ApplicationEvent {
    
    private static final long serialVersionUID = 1L;
	
    private final T entity;
    private final EventType eventType;
    
    public BaseEntityEvent(Object source, T entity, EventType eventType) {
        super(source);
        this.entity = entity;
        this.eventType = eventType;
    }
    
    public T getEntity() {
        return entity;
    }
    
    public EventType getEventType() {
        return eventType;
    }
    
    public boolean isInsert() {
        return eventType == EventType.INSERT;
    }
    
    public boolean isUpdate() {
        return eventType == EventType.UPDATE;
    }
    
    public boolean isDelete() {
        return eventType == EventType.DELETE;
    }
      
}