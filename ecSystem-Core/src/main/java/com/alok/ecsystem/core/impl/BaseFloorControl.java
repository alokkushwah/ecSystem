package com.alok.ecsystem.core.impl;

import java.util.Observable;

import com.alok.ecsystem.core.ElevatorControlInterface;
import com.alok.ecsystem.core.FloorControlInterface;

public class BaseFloorControl extends Observable implements FloorControlInterface {

	private int id;
	private boolean requested;
	private ElevatorControlInterface elevatorControl;

	public BaseFloorControl(int id) {
		this.id = id;
	}

	public boolean getElevatorRequest() {
		return requested;
	}

	public void setElevatorRequest(boolean upRequested) {
		boolean notify = upRequested != this.requested;
		this.requested = upRequested;
		if (notify) {
			setChanged();
			notifyObservers(upRequested ? FLOOR_EVENT.BUTTON_PRESSED : FLOOR_EVENT.BUTTON_OFF);
		}
	}

	public void elevatorArrived(ElevatorControlInterface elevator) {
		requested = false;
		elevatorControl = elevator;
		setChanged();
		notifyObservers(FLOOR_EVENT.ELEVATOR_ARRIVED);
	}

	public void elevatorLeft(ElevatorControlInterface elevator) {
		if (elevatorControl == elevator) {
			elevatorControl = null;
			setChanged();
			notifyObservers(FLOOR_EVENT.ELEVATOR_LEFT);
		}
	}

	public int getId() {
		return id;
	}

	public ElevatorControlInterface getElevatorControl() {
		return elevatorControl;
	}

}
