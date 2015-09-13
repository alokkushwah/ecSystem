package com.alok.ecsystem.core.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.alok.ecsystem.core.ElevatorControlInterface;
import com.alok.ecsystem.core.FloorControlInterface;
import com.alok.ecsystem.core.impl.BaseFloorControl;
import com.alok.ecsystem.core.impl.ElevatorSystemControl;

/**
 * Interactive App to use Elevator System.
 * 
 */
public class App {
	
	private FloorControlInterface floorControl;
	private ElevatorControlInterface elevatorControl;
	private ElevatorSystemControl control = new ElevatorSystemControl();
	private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	public App() {
		floorControl = control.getFloorControl(0);
	}

	public static void main(String[] args) throws InterruptedException {
		App app = new App();
		app.askQuestion();
	}

	private void askQuestion() {
		while (true) {
			printMenu();
			String command = readCommand();
			if (command.isEmpty()) {
				System.err.println("Please select a command from menu.");
			} else if ("X".equalsIgnoreCase(command)) {
				System.out.println("Good bye.");
				return;
			}

			try {
				execute(command);
			} catch (Exception e) {
				System.err.println("Inavlid command or condition.");
			}
		}

	}

	private void execute(String command) {
		command = command.toUpperCase();
		// request elevator from any floor
		if (command.startsWith("R")) {
			int index = parseCommand(command);
			BaseFloorControl fControl = control.getFloorControl(index);
			fControl.setElevatorRequest(true);
			System.out.println("Pressed button at:" + index);
			return;
		}
		// go to any floor of building
		if (command.startsWith("G")) {
			int index = parseCommand(command);
			floorControl = control.getFloorControl(index);
			System.out.println("You appeared at floor:" + index);
			return;
		}

		// press up button on floor
		if ("P".equalsIgnoreCase(command)) {
			System.out.println("Pressed request button.");
			floorControl.setElevatorRequest(true);
			return;
		}

		if ("I".equalsIgnoreCase(command)) {
			elevatorControl = floorControl.getElevatorControl();
			if (elevatorControl != null) {
				System.out.println("Hopped in elevetor at floor:" + floorControl.getId());
				floorControl = null;
			} else {
				System.err.println("Invalid command.");
			}
			return;
		}

		if (elevatorControl == null) {
			System.err.println("Invalid command.");
			return;
		}

		if ("O".equalsIgnoreCase(command)) {
			if (elevatorControl.openDoorRequest()) {
				int index = elevatorControl.getCurrentFloor();
				elevatorControl = null;
				System.out.println("Hopped out at " + index + " floor.");
				floorControl = control.getFloorControl(index);
			} else {
				System.err.println("Unable to get out try again.");
			}
			return;
		}

		int index = Integer.parseInt(command);
		elevatorControl.addFloorRequest(index);
		System.out.println("Pressed:" + index);
	}

	private void printMenu() {

		System.out.println("Menu:");
		if (floorControl != null) {
			System.out.println("At Floor:" + floorControl.getId());
			System.out.println("P - Call elevator to your floor.");
			if (floorControl.getElevatorControl() != null) {
				System.out.println("I - Hope in elevator.");
			}
		} else if (elevatorControl != null) {
			System.out.println("Choose Floor:");
			for (int i : elevatorControl.getAllowedFloorList()) {
				System.out.print(i + ",");
			}
			System.out.println();
			System.out.println("O - Open door and hope out");

		}
		System.out.println("R <floorIndex> - request to move elevotor at <floorIndex>");
		System.out.println("G <floorIndex> - apear at <floorIndex>");
		System.out.println("X - Quit the program");
	}

	private String readCommand() {
		try {
			return reader.readLine().trim();
		} catch (IOException e) {
			throw new RuntimeException("Unable to read command.", e);
		}
	}

	private int parseCommand(String command) {
		String[] parts = command.split(" ");
		return Integer.parseInt(parts[1]);
	}
}
