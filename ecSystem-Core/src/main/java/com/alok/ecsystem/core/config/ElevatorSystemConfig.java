package com.alok.ecsystem.core.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.alok.ecsystem.core.impl.AbstractBaseElevatorControl;
import com.alok.ecsystem.core.impl.BaseFloorControl;
import com.alok.ecsystem.core.util.PropertyUtil;

public final class ElevatorSystemConfig  {
	
	private static ElevatorSystemConfig instance =  new ElevatorSystemConfig();
	private final List<BaseFloorControl> floorInputBoards =  new ArrayList<BaseFloorControl>();
	private final List<AbstractBaseElevatorControl> elevetors = new ArrayList<AbstractBaseElevatorControl>();
	
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
	
	public static ElevatorSystemConfig getConfig(){
		return instance;
	}
	
	public List<AbstractBaseElevatorControl> getElevetors(){
		return elevetors;
	}
	
	public BaseFloorControl getFloorInterface(int index){
		return floorInputBoards.get(index);
	}

	public int topFloorIndex(){
		return floorInputBoards.size()-1;
	}

}
