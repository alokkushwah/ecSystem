## ecSystem - Elevator Control System 

Simple implementation of Elevator Control System. This project consists of four modules.

1. ecSystem-Core
	Core classes and interfaces with base implementation for elevator control system. 
2. ecSystem-Simulated
	A dummy implementation elevator to test the system with.
3. ecSystem-App
	A simulated stand alone application that displays menu and accepts command to interact with Elevator Control System.
4. ecSystem-App 
	A simulated stand alone web application that exposes RESTful services to interact with Elevator Control System.

## Faster way to get things running

Pre-requirement: You will need Java 1.7+ on your system.

You can run either of the application. Command line application has more functionality than Web application.

# Command line application

Download Jar: https://github.com/alokkushwah/ecSystem/raw/master/bin/ecSystemApp.jar

Run command: java -jar ecSystemApp.jar

Follow on-screen menu to interect with system.

Default Config: Building has 5 floors (0-4). There are 2 elevators serving the building. 

# Web application

Download Jar: https://github.com/alokkushwah/ecSystem/raw/master/bin/ecSystemWeb.jar

Run command: java -jar ecSystemWeb.jar

Check elevator status
	URL:http://localhost:8080/elevator
	Method: GET

Make floor request- 
	URL: http://localhost:8080/elevator
	Method: POST
	Content-Type:application/json
	request body: {"floorIndex":2}

Default Config: Building has 5 floors (0-4). There is only 1 elevator serving the building. 





