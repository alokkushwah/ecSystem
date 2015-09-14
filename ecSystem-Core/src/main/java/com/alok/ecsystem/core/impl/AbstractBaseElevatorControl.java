package com.alok.ecsystem.core.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.alok.ecsystem.core.ElevatorControlInterface;
import com.alok.ecsystem.core.ElevatorSystemControl;
import com.alok.ecsystem.core.FloorControlInterface;
import com.alok.ecsystem.core.ElevatorControlInterface.DIRECTION;
import com.alok.ecsystem.core.ElevatorControlInterface.STATE;
import com.alok.ecsystem.core.config.ElevatorSystemConfig;

/**
 * Abstract base implementation of {@link ElevatorControlInterface}. This class controls the elevator.
 * This class must be implemented by all elevator class to be used within system.
 * This class implement defines methods to help {@link ElevatorSystemControl} to assign requests to individual elevators.
 * 
 * This class keeps the queue of requests and complete them one by one.
 * 
 * Elevator keeps the state of elevator. Elevator can be following state 
 * 
 * IDLE, MOVING, DOOR_OPENING, DOOR_OPEN, DOOR_CLOSING, OUT_OF_ORDER
 * 
 * By default elevator remains in "IDLE" state. Elevator reacts to initial request and decide the direction to go and change state to "MOVING" 
 * and completes all task in that direction before changing direction.
 * 
 * After completion of all the request elevator goes in "IDLE" state.
 * 
 * @author Alok Kushwah (akushwah)
 */
public abstract class AbstractBaseElevatorControl implements ElevatorControlInterface {

	private static final Logger logger = Logger.getLogger(AbstractBaseElevatorControl.class);

	

	private int id;
	private int maxFloor;
	private int minFloor;
	private List<Integer> validFloorList;

	private DIRECTION movingDirection = DIRECTION.UP;
	protected STATE state = STATE.IDLE;
	private int currentFloorIndex = 0;
	private int nextFloorStop;

	private SortedSet<Integer> requestedFloorIndexes = Collections.synchronizedSortedSet(new TreeSet<Integer>());
	private boolean dirtyRequestedFloorIndexes = false;

	/**
	 * Creates a new elevator and initialize valid floor list.
	 * @param id - unique id
	 * @param minFloor - minimum floor index this elevator can go. 
	 * @param maxFloor - maximum floor index this elevator can go.
	 */
	public AbstractBaseElevatorControl(int id, int minFloor, int maxFloor) {
		this.id = id;
		this.maxFloor = maxFloor;
		this.minFloor = minFloor;
		validFloorList = new ArrayList<Integer>();
		for (int i = minFloor; i <= maxFloor; i++) {
			validFloorList.add(i);
		}
	}
	
	/**
	 * Unique identification of elevator.
	 * @return {@link Integer}
	 */
	public int getId() {
		return id;
	}

	/**
	 * Return list of floor index this elevator can serve. 
	 * @return {@link List}
	 */
	public List<Integer> getAllowedFloorList() {
		return validFloorList;
	}
	
	/**
	 * Returns state of elevator.
	 * @return {@link STATE}
	 */
	public STATE getState(){
		return state;
	}

	/**
	 * Returns direction of movement elevator.
	 * @return {@link DIRECTION}
	 */
	public DIRECTION getDirection(){
		return movingDirection;
	}


	/**
	 * Current floor of elevator.
	 * @return int - floor index
	 */
	public int getCurrentFloor() {
		return currentFloorIndex;
	}

	/**
	 * Get the list of pending requests.
	 * @return Set<Integer>
	 */	
	public Set<Integer> getFloorRequests() {
		return requestedFloorIndexes;
	}
	
	/**
	 * Returns cost estimation to go to given floor from the current state.
	 * Helper methdo for {@link ElevatorSystemControl} in deciding which elevator should get the request.
	 * 
	 * This method can be override by implementor in case cost calculation is different.
	 * 
	 * @param requestedFloorIndex
	 * @return
	 */
	public int estimatedFloorRequestCost(int requestedFloorIndex) {
		logger.debug("Enter cost() requestedFloorIndex=" + requestedFloorIndex);
		if (requestedFloorIndex < minFloor && requestedFloorIndex > maxFloor) {
			throw new RuntimeException("Invalid floor index request. index=" + requestedFloorIndex + " (" + minFloor + "," + maxFloor + ")");
		}
		int cost = Math.abs(currentFloorIndex - requestedFloorIndex);
		if (state == STATE.MOVING) {
			if (movingDirection == DIRECTION.UP) {
				if (requestedFloorIndex < currentFloorIndex) {
					cost += Math.abs((currentFloorIndex - requestedFloorIndexes.last()) * 2);
				}
			} else { // moving down
				if (requestedFloorIndex > currentFloorIndex) {
					cost += Math.abs((currentFloorIndex - requestedFloorIndexes.first()) * 2);
				}
			}
		}
		logger.debug("Exit cost() requestedFloorIndex=" + requestedFloorIndex + " currentFloorIndex=" + currentFloorIndex + " movingDirection=" + movingDirection + " cost=" + cost);
		return cost;
	}

	
	/**
	 * Accepts a new floor request. Initialize movement in case elevator is "IDLE".
	 * @param requestedFloorIndex - floor index
	 * @throws RuntimeException - in case invalid floor index
	 */
	public synchronized void addFloorRequest(int requestedFloorIndex) {
		logger.debug("Enter floorRequest() requestedFloorIndex=" + requestedFloorIndex);
		if (requestedFloorIndex >= minFloor && requestedFloorIndex <= maxFloor) {
			if (!requestedFloorIndexes.contains(requestedFloorIndex)) {
				logger.debug("added requestedFloorIndex=" + requestedFloorIndex);
				requestedFloorIndexes.add(requestedFloorIndex);
				dirtyRequestedFloorIndexes = true;
				if (state == STATE.IDLE){
					calculateNextState();
				}		
			}
		} else {
			String msg = "Invalid floor index request. index=" + requestedFloorIndex + " (" + minFloor + "," + maxFloor + ")";
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		logger.debug("Exit floorRequest() requestedFloorIndex=" + requestedFloorIndex);
	}

	/**
	 * Request to open the door. Request may be denied in case system cannot open door due to state. Client should retry. 
	 * @return boolean - true if accepted or false in case denied.
	 */
	public synchronized boolean openDoorRequest() {
		logger.info(id + ":Door open request recieved");
		if (state == STATE.IDLE || state == STATE.DOOR_CLOSING) {
			state = STATE.DOOR_OPENING;
			startDoorOpening();
			return true;
		} else if (state == STATE.DOOR_OPEN || state == STATE.DOOR_OPENING) {
			return true;
		}
		return false;
	}

	/**
	 * Request to close door. Request may be denied in case system cannot close door due to state. Client should retry. 
	 * @return boolean - true if accepted or false in case denied.
	 */
	public synchronized boolean closeDoorRequest() {
		logger.info(id + ":Door Close request recieved");
		if (state == STATE.DOOR_OPEN) {
			state = STATE.DOOR_CLOSING;
			startDoorClosing();
			return true;
		}
		return false;
	}
	

	/**
	 * This method must be called by implementor after door is fully opened. 
	 */
	protected synchronized void doorOpened() {
		logger.info("Now elevator " + id + " at floor " + currentFloorIndex + " door is open.");
		state = STATE.DOOR_OPEN;
	}

	/**
	 * This method must be called by implementor after door is fully closed. 
	 */
	protected synchronized void doorClosed() {
		logger.info("Now elevator " + id + " at floor " + currentFloorIndex + " door is closed.");
		state = STATE.IDLE;
		calculateNextState();
	}

	/**
	 * This method must be called by implementor after elevator reached to new floor.
	 * This method recalculate movement of elevator in case floor requests are changed.  
	 */
	protected synchronized boolean movedToNewFloor() {

		logger.debug("Enter movedToNewFloor() currentFloorIndex=" + currentFloorIndex + " movingDirection=" + movingDirection + " nextFloorStop=" + nextFloorStop);

		ElevatorSystemConfig.getConfig().getFloorInterface(currentFloorIndex).elevatorLeft(this);

		if (DIRECTION.UP.equals(movingDirection)) {
			currentFloorIndex++;
			if (currentFloorIndex == maxFloor) {
				movingDirection = DIRECTION.DOWN;
			}
		} else if (DIRECTION.DOWN.equals(movingDirection)) {
			currentFloorIndex--;
			if (currentFloorIndex == minFloor) {
				movingDirection = DIRECTION.UP;
			}
		}
		logger.info("Now elevator " + id + " at floor " + currentFloorIndex + " moving " + movingDirection);

		if(dirtyRequestedFloorIndexes){
			state = STATE.IDLE;	   
			calculateNextState();
			return false;
		}
	
		if (currentFloorIndex == nextFloorStop) {
			logger.debug("Reached requested floor currentFloorIndex=" + currentFloorIndex + " movingDirection=" + movingDirection + " nextFloorStop=" + nextFloorStop);
			openDoorAndNotify();
			return false;
		}

		if (nextFloorStop == -1) {
			logger.debug("Setting IDLE. currentFloorIndex=" + currentFloorIndex + " movingDirection=" + movingDirection + " nextFloorStop=" + nextFloorStop);
			state = STATE.IDLE;
			return false;
		}

		logger.debug("Exit movedToNewFloor(). currentFloorIndex=" + currentFloorIndex + " movingDirection=" + movingDirection + " nextFloorStop=" + nextFloorStop);
		return true; // keep moving
	}

	/**
	 * This method is called to start closing the door.
	 */
	protected abstract void startDoorClosing();

	/**
	 * This method is called to start opening the door.
	 */
	protected abstract void startDoorOpening();

	/**
	 * This is called to start moving. System already decided where to move before calling this method.
	 */
	protected abstract void startMoving();

	/**
	 * calculate next state based on current state of system.
	 */
	private synchronized void calculateNextState() {
		logger.debug("Enter calculateNextState()");
		if (state != STATE.IDLE) {
			logger.warn("calculateNextState() called while elevetor was not IDLE");
			return; // Elevetor is moving or door is active. Wait for IDLE.
		}

		if (requestedFloorIndexes.isEmpty()) {
			logger.debug("Empty requestedFloorIndexes setting status IDLE");
			state = STATE.IDLE;
			return; // Nothing to do
		}
		dirtyRequestedFloorIndexes = false;
		int downNext = -1;
		int upNext = -1;
		int nextStop = -1;
		for (int i : requestedFloorIndexes) {
			if (i < currentFloorIndex) {
				downNext = i;
			} else if (i == currentFloorIndex) {
				nextStop = currentFloorIndex;
			} else {
				upNext = i;
				break;
			}
		}

		logger.debug("downNext=" + downNext + " nextStop=" + nextStop + " currentFloorIndex=" + currentFloorIndex + " upNext=" + upNext + "requestedFloorIndexes=" + requestedFloorIndexes);

		if (nextStop == currentFloorIndex) {
			openDoorAndNotify();
			return;
		}

		if (DIRECTION.UP == movingDirection) {
			if (upNext == -1) {
				movingDirection = DIRECTION.DOWN;
				nextFloorStop = downNext;
			} else {
				nextFloorStop = upNext;
			}
		} else if (DIRECTION.DOWN == movingDirection) {
			if (downNext == -1) {
				movingDirection = DIRECTION.UP;
				nextFloorStop = upNext;
			} else {
				nextFloorStop = downNext;
			}
		}

		logger.debug("Start moving nextFloorStop=" + nextFloorStop + " currentFloorIndex=" + currentFloorIndex + " movingDirection=" + movingDirection + " requestedFloorIndexes=" + requestedFloorIndexes);
		state = STATE.MOVING;
		startMoving();
	}

	/**
	 * help method to declare elevator is arrived and door is opening.
	 */
	private void openDoorAndNotify() {
		logger.debug("Opening door floor currentFloorIndex=" + currentFloorIndex + " movingDirection" + movingDirection + "nextFloorStop" + nextFloorStop);
		FloorControlInterface floorInputBoard = ElevatorSystemConfig.getConfig().getFloorInterface(currentFloorIndex);
		state = STATE.DOOR_OPENING;
		requestedFloorIndexes.remove(currentFloorIndex);
		nextFloorStop = -1;
		startDoorOpening();
		floorInputBoard.elevatorArrived(this);
	}

	@Override
	public String toString() {
		return "" + id;
	}


}
