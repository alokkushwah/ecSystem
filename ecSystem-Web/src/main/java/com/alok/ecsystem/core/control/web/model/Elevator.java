package com.alok.ecsystem.core.control.web.model;

import java.util.Set;

/**
 * Elevator bean to represent elevator state.
 * 
 * @author Alok Kushwah (akushwah)
 */
public class Elevator {
	private int id;
	private int currentFloorIndex;
	private Set<Integer> pendingRequests;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getCurrentFloorIndex() {
		return currentFloorIndex;
	}
	public void setCurrentFloorIndex(int currentFloorIndex) {
		this.currentFloorIndex = currentFloorIndex;
	}
	public Set<Integer> getPendingRequests() {
		return pendingRequests;
	}
	public void setPendingRequests(Set<Integer> set) {
		this.pendingRequests = set;
	}
}
