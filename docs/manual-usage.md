# Manual Usage

## Overview

This application simulates the movement of buses along a predefined route.  
The system displays bus positions, route progress, and key simulation events while multiple buses move concurrently.

The project follows a client-server architecture:
- the server manages the simulation logic and bus state
- the client connects to the server to retrieve and display bus information

## Main Features

- Real-time bus route simulation
- Client-server communication
- Multiple buses running concurrently
- Bus tracking from the client side
- Simulation clock
- Route and stop visualization
- Event updates during the simulation

## Requirements

### Minimum
- Windows 10 or Linux
- 4 GB RAM
- Java 17 or higher

### Recommended
- Windows 10/11, macOS, or modern Linux
- 8 GB RAM or more
- JDK 17 or higher

## How to Run

1. Compile the Java source files.
2. Start the server application.
3. Start the client application.
4. Connect the client to the server.
5. Start the simulation.
6. Select a bus if you want to track a specific unit.

## System Behavior

- Each bus runs as an independent execution flow in the simulation.
- The server controls the logic, timing, and current state of the buses.
- The client requests information from the server and displays it to the user.
- Buses move through the route, stop at predefined points, and continue their path.
- The system includes synchronization rules to keep the simulation behavior consistent.

## Main Components

- `Servidor.java`: starts and manages the server-side simulation
- `Bus.java`: represents each bus and its movement logic
- `Reloj.java`: controls the simulation time
- `Interfaz.java`: renders the graphical interface
- `Cliente.java`: connects to the server and retrieves bus information

## Basic Usage

### Start Simulation
Begins the bus movement simulation.

### Stop Simulation
Stops the current simulation.

### Connect
Allows the client to connect to the server.

### Follow Bus
Lets the user choose a specific bus and monitor its position during execution.

## Notes

This project was developed as an academic exercise focused on concurrency, synchronization, client-server communication, and graphical simulation in Java.
