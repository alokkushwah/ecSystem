package com.alok.ecsystem.core;

import java.util.List;
import java.util.Set;

/**
 * Interface to define user and control interface for an elevator.
 * This interface is use by client of system to interact with individual elevator. 
 * This also use by {@link ElevatorSystemControl} to send floor requests.
 * 
 * @author Alok Kushwah (akushwah)
 */
public interface ElevatorControlInterface {
	
	/**
	 * States of Elevator
	 */
	public enum STATE {
		IDLE, MOVING, DOOR_OPENING, DOOR_OPEN, DOOR_CLOSING, OUT_OF_ORDER
	};

	/**
	 * Direction of elevator 
	 */
	public enum DIRECTION {
		UP, DOWN;
	}

	/**
	 * Unique identification of elevator.
	 * @return {@link Integer}
	 */
	public int getId();
	
	/**
	 * Return list of floor index this elevator can serve. 
	 * @return {@link List}
	 */
	public List<Integer> getAllowedFloorList();

	/**
	 * Returns state of elevator.
	 * @return {@link STATE}
	 */
	public STATE getState();

	/**
	 * Returns direction of movement elevator.
	 * @return {@link DIRECTION}
	 */
	public DIRECTION getDirection();

	/**
	 * Current floor of elevator.
	 * @return int - floor index
	 */
	public int getCurrentFloor();
	
	/**
	 * Returns cost estimation to go to given floor from the current state.
	 * @param requestedFloorIndex 
	 * @return int - cost
	 */
	public int estimatedFloorRequestCost(int requestedFloorIndex);

	/**
	 * Request to go to particular floor. It may throw {@link RuntimeException} in case floor index is invalid.
	 * @param requestedFoorId - floor index
	 */
	public void addFloorRequest(int requestedFoorId);
	
	/**
	 * Get the list of pending requests.
	 * @return Set<Integer>
	 */
	public Set<Integer> getFloorRequests();
	
	/**
	 * Request to open the door. Request may be denied in case system cannot open door due to state. Client should retry. 
	 * @return boolean - true if accepted or false in case denied.
	 */
	public boolean openDoorRequest();
	
	/**
	 * Request to close door. Request may be denied in case system cannot close door due to state. Client should retry. 
	 * @return boolean - true if accepted or false in case denied.
	 */
	public boolean closeDoorRequest();
	
}
