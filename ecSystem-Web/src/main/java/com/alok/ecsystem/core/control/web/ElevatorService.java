package com.alok.ecsystem.core.control.web;

import org.springframework.stereotype.Component;

import com.alok.ecsystem.core.ElevatorControlInterface;
import com.alok.ecsystem.core.ElevatorSystemControl;
import com.alok.ecsystem.core.FloorControlInterface;

@Component
public class ElevatorService {

	ElevatorSystemControl control;
	
	public ElevatorService() {
		control = new ElevatorSystemControl(); 
	}
	
	public void requestElevotorToFloor(int index) {
		FloorControlInterface fc = control.getFloorControl(index);
		if(fc==null) {
			throw new RuntimeException("Invalid floor index=" + index +"." );
		}
		fc.setElevatorRequest(true);
	}

	public Elevator getElevatorStatus() {
		ElevatorControlInterface elevatorInterface = control.getElevetorControl(0);
		Elevator elevator = new Elevator();
		elevator.setId(elevatorInterface.getId());
		elevator.setCurrentFloorIndex(elevatorInterface.getCurrentFloor());
		elevator.setPendingRequests(elevatorInterface.getFloorRequests());
		return elevator;
	}

}
