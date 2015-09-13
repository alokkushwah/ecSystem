package com.alok.ecsystem.core;

public interface FloorControlInterface {
	
	public enum FLOOR_EVENT{BUTTON_PRESSED, BUTTON_OFF, ELEVATOR_ARRIVED, ELEVATOR_LEFT};

	public int getId();
	public void setElevatorRequest(boolean requsted);
	public boolean getElevatorRequest();
	public ElevatorControlInterface getElevatorControl();
	public void elevatorArrived(ElevatorControlInterface elevator);
	
}
