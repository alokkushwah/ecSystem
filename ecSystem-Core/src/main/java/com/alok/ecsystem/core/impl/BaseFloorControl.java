package com.alok.ecsystem.core.impl;

import java.util.Observable;

import com.alok.ecsystem.core.ElevatorControlInterface;
import com.alok.ecsystem.core.ElevatorSystemControl;
import com.alok.ecsystem.core.FloorControlInterface;

/**
 * Base implementation for {@link FloorControlInterface}.
 * It also implements {@link Observable} so {@link ElevatorSystemControl} can observe it.
 * 
 * @author Alok Kushwah (akushwah)
 *
 */
public class BaseFloorControl extends Observable implements FloorControlInterface {

	private int id;
	private boolean requested;
	private ElevatorControlInterface elevatorControl;

	/**
	 * Constructor to create new BaseFloorControl.
	 * @param id - unique floor index/id
	 */
	public BaseFloorControl(int id) {
		this.id = id;
	}

	/**
	 * Unique floor index/id
	 * @return int
	 */
	public int getId() {
		return id;
	}

	/**
	 * Return status of request 
	 * @return boolean - true if elevator request is pending otherwise false.
	 */
	public boolean getElevatorRequest() {
		return requested;
	}

	/**
	 * Should be called when button is pressed to call elevator.
	 * @param requsted - boolean true if button is pressed or false to reset.
	 */
	public void setElevatorRequest(boolean upRequested) {
		boolean notify = upRequested != this.requested;
		this.requested = upRequested;
		if (notify) {
			setChanged();
			notifyObservers(upRequested ? FLOOR_EVENT.BUTTON_PRESSED : FLOOR_EVENT.BUTTON_OFF);
		}
	}

	/**
	 * Returns Elevator interface in case elevator is serving the floor. 
	 * @return {@link ElevatorControlInterface}
	 */
	public ElevatorControlInterface getElevatorControl() {
		return elevatorControl;
	}

	/**
	 * Receives notification when elevator arrives. 
	 */
	public void elevatorArrived(ElevatorControlInterface elevator) {
		requested = false;
		elevatorControl = elevator;
		setChanged();
		notifyObservers(FLOOR_EVENT.ELEVATOR_ARRIVED);
	}

	/**
	 * Receives notification when elevator left or pass through this floor. 
	 */
	public void elevatorLeft(ElevatorControlInterface elevator) {
		if (elevatorControl == elevator) {
			elevatorControl = null;
			setChanged();
			notifyObservers(FLOOR_EVENT.ELEVATOR_LEFT);
		}
	}


}
