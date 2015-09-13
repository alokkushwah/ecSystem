package com.alok.ecsystem.core;


import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

import com.alok.ecsystem.core.config.ElevatorSystemConfig;
import com.alok.ecsystem.core.impl.AbstractBaseElevatorControl;
import com.alok.ecsystem.core.impl.BaseFloorControl;

public class ElevatorSystemControl implements Observer {

	private static final Logger logger = Logger.getLogger(ElevatorSystemControl.class);
	private ElevatorSystemConfig config;
	
	
	public ElevatorSystemControl() {
		config =  ElevatorSystemConfig.getConfig();
		int total  = config.topFloorIndex();
		for (int i = 0; i <= total; i++) {
			((Observable)config.getFloorInterface(i)).addObserver(this);
		}
	}
	
	public void update(Observable observable, Object arg) {

		if(FloorControlInterface.FLOOR_EVENT.BUTTON_PRESSED != arg) {
			return;
		}

		FloorControlInterface floorControl = (FloorControlInterface)observable;
		int minCost = Integer.MAX_VALUE;
		AbstractBaseElevatorControl selected = null;
		for (AbstractBaseElevatorControl elevertor: config.getElevetors()) {
			int cost = elevertor.cost(floorControl.getId());
			if(cost<minCost){
				minCost = cost;
				selected = elevertor;
			}
		}

		if(selected==null) {
			logger.error("No elevetor is available to serve the request.");
			floorControl.setElevatorRequest(false); 
			return;
		}
		logger.debug("Assigning request to elevetor=" +  selected + " to floor=" + floorControl.getId());
		selected.addFloorRequest(floorControl.getId());
	}

	public FloorControlInterface getFloorControl(int index){
		return config.getFloorInterface(index);
	}

	public ElevatorControlInterface getElevetorControl(int index){
		return config.getElevetors().get(index);
	}

}
