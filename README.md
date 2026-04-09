# Bus Route Simulator - Java

A Java bus route simulator built as a Parallel and Distributed Programming project.  
The system uses a client-server architecture, TCP socket communication, multithreading, and a Swing-based graphical interface to simulate multiple buses moving along a predefined route.

## Overview

This project simulates the movement of buses across a route in real time.  
The server manages the simulation state, bus positions, and synchronization rules, while the client connects to the server to receive updates and follow a selected bus.

The application also includes a graphical interface that displays the route, bus positions, a simulation clock, and event logs during execution.

## Main Features

- Client-server architecture
- TCP socket communication
- Multithreaded bus simulation
- Swing-based graphical interface
- Bus tracking from the client side
- Simulation clock
- Route and stop visualization
- Event log updates during execution
- Basic synchronization between buses

## Architecture

### Server
The server is responsible for:
- starting the simulation
- managing the buses
- updating bus states and positions
- accepting client connections
- sending updates to connected clients

### Client
The client is responsible for:
- connecting to the server
- selecting a bus to follow
- receiving server messages
- displaying tracking information in a separate window

## Technologies Used

- Java
- Java Swing
- TCP sockets
- Multithreading
- Client-server architecture
- Object-oriented programming

## Requirements

### Minimum
- Java 17 or higher
- Windows, Linux, or macOS
- 4 GB RAM

## How to Run

Compile the project from the repository root.

### Windows


rmdir /s /q out
mkdir out
javac -d out src\Servidor\*.java src\Cliente\*.java


### Run the server first:


java -cp "out;src" Servidor.Servidor


### Then run the client in a second terminal:

java -cp "out;src" Cliente.Cliente
