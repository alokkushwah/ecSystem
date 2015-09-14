package com.alok.ecsystem.core.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.alok.ecsystem.core.ElevatorControlInterface;
import com.alok.ecsystem.core.FloorControlInterface;
import com.alok.ecsystem.core.impl.AbstractBaseElevatorControl;
import com.alok.ecsystem.core.impl.BaseFloorControl;
import com.alok.ecsystem.core.util.PropertyUtil;

/**
 * It load and make elevator configuration from properties files.
 * 
 * It initialize the system based on classpath resource "ecSystem.properties". 
 * Properties can be override by external property file. Path for can be defined in "ecsPropFile" system properties.
 * 
 * It instantiate all floor control interfaces and elevators based on configuration.
 * 
 * It is a singleton class to keep the configuration centralized and easily accessible.
 * 
 * @author Alok Kushwah (akushwah)
 */
public final class ElevatorSystemConfig  {
	
	private static ElevatorSystemConfig instance =  new ElevatorSystemConfig();
	private final List<FloorControlInterface> floorInputBoards =  new ArrayList<FloorControlInterface>();
	private final List<ElevatorControlInterface> elevetors = new ArrayList<ElevatorControlInterface>();
	
	/**
	 * Private constructor to create singleton instance.
	 * It loads the properties in initialize the system. 
	 */
	private ElevatorSystemConfig()  {
		PropertyUtil.load("ecSystem.properties");
		String externalPropertyFile = System.getProperty("ecsPropFile");
		if(externalPropertyFile!=null){
			PropertyUtil.load(new File(externalPropertyFile));
		}
		
		int floorCount =  Integer.parseInt(PropertyUtil.getProperty("floor.count"));
		
		for (int i = 0; i < floorCount; i++) {
			BaseFloorControl baseFloorControl = new BaseFloorControl(i);
			floorInputBoards.add(baseFloorControl);
		}
		
		int elevatorCount =  Integer.parseInt(PropertyUtil.getProperty("elevator.count"));
		
		for (int i = 0; i < elevatorCount; i++) {
			String className =  PropertyUtil.getProperty("elevator." + i + ".className");
			int minIndex =  Integer.parseInt(PropertyUtil.getProperty("elevator." + i + ".minIndex"));
			int maxIndex =  Integer.parseInt(PropertyUtil.getProperty("elevator." + i + ".maxIndex"));
			Class classObject;
			try {
				classObject = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Unable to find class=" +  className, e);
			}
			AbstractBaseElevatorControl elevator;
			try {
				elevator = (AbstractBaseElevatorControl) classObject.getConstructor(int.class,int.class,int.class).newInstance(i,minIndex,maxIndex);
			} catch (Exception e) {
				throw new RuntimeException("Unable to instantiate class=" +  className, e);
			}
			elevetors.add(elevator);
		}
		
	}
	
	/**
	 * getter for singleton config instance.
	 * @return
	 */
	public static ElevatorSystemConfig getConfig(){
		return instance;
	}
	
	/**
	 * getter for list of elevators.
	 * @return List<AbstractBaseElevatorControl>
	 */
	public List<ElevatorControlInterface> getElevetors(){
		return elevetors;
	}
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	public FloorControlInterface getFloorInterface(int index){
		return floorInputBoards.get(index);
	}

	public int topFloorIndex(){
		return floorInputBoards.size()-1;
	}

}
