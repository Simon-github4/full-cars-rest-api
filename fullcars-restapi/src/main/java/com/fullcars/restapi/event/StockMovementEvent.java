package com.fullcars.restapi.event;

import com.fullcars.restapi.enums.EventType;
import com.fullcars.restapi.model.StockMovement;

public class StockMovementEvent extends BaseEntityEvent<StockMovement>{
	private static final long serialVersionUID = -8412806922049082878L;
	
	public StockMovementEvent(Object source, StockMovement movement, EventType et) {
		super(source, movement, et);
	}

}
