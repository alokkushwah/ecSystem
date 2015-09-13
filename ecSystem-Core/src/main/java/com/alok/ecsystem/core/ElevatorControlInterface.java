package com.alok.ecsystem.core;

import java.util.List;
import java.util.Set;

public interface ElevatorControlInterface {

	public int getId();
	public List<Integer> getAllowedFloorList();
	public void addFloorRequest(int requestedFoorId);
	public Set<Integer> getFloorRequests();
	public boolean openDoorRequest();
	public boolean closeDoorRequest();
	public int getCurrentFloor();

}
