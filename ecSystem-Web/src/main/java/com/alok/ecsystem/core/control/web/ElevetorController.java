package com.alok.ecsystem.core.control.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ElevetorController {

    @Autowired
    private ElevatorService elevatorService;

    @RequestMapping(value ="/elevator", method = RequestMethod.POST)
    public Response elevatorPost(@RequestBody ElevatorRequest request) {
    	
    	try{
    		elevatorService.requestElevotorToFloor(request.getFloorIndex());
    		return new Response(true,"Request to floor index "+ request.getFloorIndex() +"submitted successfully.");
    	}catch(RuntimeException exp){
       		return new Response(false,exp.getMessage());
    	}
    }

    @RequestMapping(value ="/elevator", method = RequestMethod.GET)
    public Elevator elevatorGet() {
        return elevatorService.getElevatorStatus();
    }

}