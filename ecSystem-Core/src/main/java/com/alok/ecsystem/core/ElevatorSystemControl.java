package com.alok.ecsystem.core;


import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

import com.alok.ecsystem.core.config.ElevatorSystemConfig;
import com.alok.ecsystem.core.impl.AbstractBaseElevatorControl;

/**
 * This class is central elevator control system. It keeps eye on all the floor. 
 * It also assign requests to elevators based on minimum costs.
 * 
 * @author Alok Kushwah (akushwah)
 */
public class ElevatorSystemControl implements Observer {

	private static final Logger logger = Logger.getLogger(ElevatorSystemControl.class);
	private ElevatorSystemConfig config;
	
	/**
	 * Constructor to create new ElevatorSystemControl.
	 * 
	 * It initialize the system configuration with help of {@link ElevatorSystemConfig}
	 * 
	 * Add itslef as observer to all floor interface controllers.
	 */
	public ElevatorSystemControl() {
		config =  ElevatorSystemConfig.getConfig();
		int total  = config.topFloorIndex();
		for (int i = 0; i <= total; i++) {
			((Observable)config.getFloorInterface(i)).addObserver(this);
		}
	}
	
	/**
	 * Gets notification from floor interface controllers and react as per the event.
	 * 
	 * It react of FLOOR_EVENT.BUTTON_PRESSED event. On button press it creates and assign 
	 * floor request to and elevator based on minimum cost. 
	 */
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

	/**
	 * Return floor interface controller for a particular floor.
	 * @param index - floor index
	 * @return {@link FloorControlInterface}
	 * @throws RuntimeException in case invalid index
	 */
	public FloorControlInterface getFloorControl(int index){
		return config.getFloorInterface(index);
	}

	/**
	 * Return Elevator Control Interface for a particular elevator.
	 * @param index - elevator id
	 * @return {@link ElevatorControlInterface}
	 * @throws RuntimeException in case invalid index
	 */
	public ElevatorControlInterface getElevetorControl(int index){
		return config.getElevetors().get(index);
	}

}
