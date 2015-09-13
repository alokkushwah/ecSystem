package com.alok.ecsystem.core.control.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alok.ecsystem.core.control.web.model.Elevator;
import com.alok.ecsystem.core.control.web.model.ElevatorRequest;
import com.alok.ecsystem.core.control.web.model.Response;
import com.alok.ecsystem.core.control.web.service.ElevatorService;

/**
 * Controller to expose RESTful services to interact with Elevator System
 *  1. Post following json request to "http://<host:port>/elevator" to make a new request
 *  	{"floorIndex":<int>} 
 *    
 *  2. To get the status of elevator "Get" request to "http://<host:port>/elevator"
 * 
 * @author Alok Kushwah (akushwah)
 *
 */
@RestController
public class ElevetorController {

    @Autowired
    private ElevatorService elevatorService;

    /**
     * Makes a new request to elevator to visit a floor
     * @param request
     * @return {@link Response} 
     */
    @RequestMapping(value ="/elevator", method = RequestMethod.POST)
    public Response elevatorPost(@RequestBody ElevatorRequest request) {
    	
    	try{
    		elevatorService.requestElevotorToFloor(request.getFloorIndex());
    		return new Response(true,"Request to floor index "+ request.getFloorIndex() +"submitted successfully.");
    	}catch(RuntimeException exp){
       		return new Response(false,exp.getMessage());
    	}
    }

    /**
     * Returns status of elevator
     * @return {@link Elevator}
     */
    @RequestMapping(value ="/elevator", method = RequestMethod.GET)
    public Elevator elevatorGet() {
        return elevatorService.getElevatorStatus();
    }

}