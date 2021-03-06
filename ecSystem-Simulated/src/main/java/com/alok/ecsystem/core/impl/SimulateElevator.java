package com.alok.ecsystem.core.impl;

import com.alok.ecsystem.core.impl.AbstractBaseElevatorControl;

/**
 * Simulated elevator which actually sleeps and does nothing and take credit of doing crucial things.
 *  
 * @author Alok Kushwah (akushwah)
 *
 */
public class SimulateElevator extends AbstractBaseElevatorControl {

	public SimulateElevator(int id, int minFloor, int maxFloor) {
		super(id, minFloor, maxFloor);
	}

	private Thread doorCloseThread;

	@Override
	protected void startDoorClosing() {
		doorCloseThread = new Thread(new Runnable() {

			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// interrupted
					return;
				}
				doorClosed();
			}
		});
		doorCloseThread.setName("DoorClosingThread");
		doorCloseThread.start();
	}

	@Override
	protected void startDoorOpening() {
		if (doorCloseThread != null && doorCloseThread.isAlive()) {
			doorCloseThread.interrupt();
		}
		Thread t = new Thread(new Runnable() {

			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new RuntimeException(Thread.currentThread().getName() + ":InterruptedException");
				}
				doorOpened();
				startDoorClosing();
			}
		});
		t.setName("DoorOpeningThread");
		t.start();
	}

	@Override
	protected void startMoving() {
		Thread t = new Thread(new Runnable() {

			public void run() {
				while (true) {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						throw new RuntimeException(Thread.currentThread().getName() + ":InterruptedException");
					}
					if (!movedToNewFloor())
						return;
				}
			}
		});
		t.setName("MovingThread");
		t.start();
	}
}
