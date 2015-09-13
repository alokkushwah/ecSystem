package com.alok.ecsystem.core.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.alok.ecsystem.core.ElevatorControlInterface;
import com.alok.ecsystem.core.FloorControlInterface;

public abstract class AbstractBaseElevatorControl implements ElevatorControlInterface {

	private static final Logger logger = Logger.getLogger(AbstractBaseElevatorControl.class);

	private enum STATE {
		IDLE, MOVING, DOOR_OPENING, DOOR_OPEN, DOOR_CLOSING, OUT_OF_ORDER
	};

	private enum Direction {
		UP, DOWN;
	}

	private int id;
	private int maxFloor;
	private int minFloor;
	private List<Integer> validFloorList;

	private Direction movingDirection = Direction.UP;
	protected STATE state = STATE.IDLE;
	private int currentFloorIndex = 0;
	private int nextFloorStop;

	private SortedSet<Integer> requestedFloorIndexes = Collections.synchronizedSortedSet(new TreeSet<Integer>());

	public AbstractBaseElevatorControl(int id, int minFloor, int maxFloor) {
		this.id = id;
		this.maxFloor = maxFloor;
		this.minFloor = minFloor;
		validFloorList = new ArrayList<Integer>();
		for (int i = minFloor; i <= maxFloor; i++) {
			validFloorList.add(i);
		}
	}

	public int cost(int requestedFloorIndex) {
		logger.debug("Enter cost() requestedFloorIndex=" + requestedFloorIndex);
		if (requestedFloorIndex < minFloor && requestedFloorIndex > maxFloor) {
			throw new RuntimeException("Invalid floor index request. index=" + requestedFloorIndex + " (" + minFloor + "," + maxFloor + ")");
		}
		int cost = Math.abs(currentFloorIndex - requestedFloorIndex);
		if (state == STATE.MOVING) {
			if (movingDirection == Direction.UP) {
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

	public synchronized void addFloorRequest(int requestedFloorIndex) {
		logger.debug("Enter floorRequest() requestedFloorIndex=" + requestedFloorIndex);
		if (requestedFloorIndex >= minFloor && requestedFloorIndex <= maxFloor) {
			if (!requestedFloorIndexes.contains(requestedFloorIndex)) {
				logger.debug("added requestedFloorIndex=" + requestedFloorIndex);
				requestedFloorIndexes.add(requestedFloorIndex);
				if (state == STATE.IDLE)
					calculateNextState();
			}
		} else {
			String msg = "Invalid floor index request. index=" + requestedFloorIndex + " (" + minFloor + "," + maxFloor + ")";
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		logger.debug("Exit floorRequest() requestedFloorIndex=" + requestedFloorIndex);
	}

	public synchronized boolean openDoorRequest() {
		logger.info(id + ":Door open request recieved");
		if (state == STATE.IDLE || state == STATE.DOOR_CLOSING) {
			state = STATE.DOOR_OPENING;
			startDoorOpening();
			return true;
		} else if(state == STATE.DOOR_OPEN || state == STATE.DOOR_OPENING){
			return true;
		}
		return false;
	}

	public synchronized boolean closeDoorRequest() {
		logger.info(id + ":Door Close request recieved");
		if (state == STATE.DOOR_OPEN) {
			state = STATE.DOOR_CLOSING;
			startDoorClosing();
			return true;
		}
		return false;
	}

	public synchronized void doorOpened() {
		logger.info(id + ":Door Open at floor=" + currentFloorIndex);
		state = STATE.DOOR_OPEN;
	}

	public synchronized void doorClosed() {
		logger.info(id + ":Door Closed at floor=" + currentFloorIndex);
		state = STATE.IDLE;
		calculateNextState();
	}

	public int getCurrentFloor(){
		return currentFloorIndex;
	}
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

		if (Direction.UP == movingDirection) {
			if (upNext == -1) {
				movingDirection = Direction.DOWN;
				nextFloorStop = downNext;
			} else {
				nextFloorStop = upNext;
			}
		} else if (Direction.DOWN == movingDirection) {
			if (downNext == -1) {
				movingDirection = Direction.UP;
				nextFloorStop = upNext;
			} else {
				nextFloorStop = downNext;
			}
		}

		logger.debug("Start moving nextFloorStop=" + nextFloorStop + " currentFloorIndex=" + currentFloorIndex + " movingDirection=" + movingDirection + " requestedFloorIndexes=" + requestedFloorIndexes);
		state = STATE.MOVING;
		startMoving();
	}

	protected synchronized boolean movedToNewFloor() {
		
		logger.debug("Enter movedToNewFloor() currentFloorIndex=" + currentFloorIndex + " movingDirection=" + movingDirection + " nextFloorStop=" + nextFloorStop);
		
		ElevatorSystemConfig.getFloorInterface(currentFloorIndex).elevatorLeft(this);
		
		if (Direction.UP.equals(movingDirection)) {
			currentFloorIndex++;
			if (currentFloorIndex == maxFloor) {
				movingDirection = Direction.DOWN;
			}
		} else if (Direction.DOWN.equals(movingDirection)) {
			currentFloorIndex--;
			if (currentFloorIndex == minFloor) {
				movingDirection = Direction.UP;
			}
		}

		logger.info("Now elevator " +id+ " at floor" + currentFloorIndex + " moving " + movingDirection);

		

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

	private void openDoorAndNotify() {
		logger.debug("Opening door floor currentFloorIndex=" + currentFloorIndex + " movingDirection" + movingDirection + "nextFloorStop" + nextFloorStop);
		BaseFloorControl floorInputBoard = ElevatorSystemConfig.getFloorInterface(currentFloorIndex);
		state = STATE.DOOR_OPENING;
		requestedFloorIndexes.remove(currentFloorIndex);
		nextFloorStop = -1;
		startDoorOpening();
		floorInputBoard.elevatorArrived(this);
	}

	public List<Integer> getAllowedFloorList() {
		return validFloorList;
	}

	protected abstract void startDoorClosing();

	protected abstract void startDoorOpening();

	protected abstract void startMoving();

	@Override
	public String toString() {
		return ""  + id ;
	}
    
	public int getId() {
	
		return id;
	}

	public Set<Integer> getFloorRequests() {
		return requestedFloorIndexes;
	}

}
