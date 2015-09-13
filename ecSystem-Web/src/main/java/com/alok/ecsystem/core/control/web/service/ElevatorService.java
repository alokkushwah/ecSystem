package com.alok.ecsystem.core.control.web.service;

import org.springframework.stereotype.Component;

import com.alok.ecsystem.core.ElevatorControlInterface;
import com.alok.ecsystem.core.ElevatorSystemControl;
import com.alok.ecsystem.core.FloorControlInterface;
import com.alok.ecsystem.core.control.web.model.Elevator;

/**
 * Initialize the Elevator Control system and expose service for controller. 
 * 
 * @author Alok Kushwah (akushwah)
 *
 */

@Component
public class ElevatorService {

	ElevatorSystemControl control;
	
	/**
	 * Default constructor for ElevatorService
	 */
	public ElevatorService() {
		control = new ElevatorSystemControl(); 
	}
	
	/**
	 * Exposes service to request elevator to a floor.
	 * @param index - floor index
	 */
	public void requestElevotorToFloor(int index) {
		FloorControlInterface fc = control.getFloorControl(index);
		if(fc==null) {
			throw new RuntimeException("Invalid floor index=" + index +"." );
		}
		fc.setElevatorRequest(true);
	}

	/**
	 * Returns state of elevator
	 * @return
	 */
	public Elevator getElevatorStatus() {
		ElevatorControlInterface elevatorInterface = control.getElevetorControl(0);
		Elevator elevator = new Elevator();
		elevator.setId(elevatorInterface.getId());
		elevator.setCurrentFloorIndex(elevatorInterface.getCurrentFloor());
		elevator.setPendingRequests(elevatorInterface.getFloorRequests());
		return elevator;
	}

}
