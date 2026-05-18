package elevator.simulation;

import elevator.model.Division;
import elevator.model.Elevator;

// claude-generated simulation in terminal
public class ElevatorSimulation {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   Elevator RTOS Simulation – Java 8+     ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        Elevator elevator = new Elevator(4, 1000, 3000, 5);
        Division division = new Division("Pion losowy, nazwa i tak nie wykorzystana nigdzie lol", -1, 7, elevator);
        division.printStatus();


        // deklaracja osób
        division.callElevator(0, 5);
        division.callElevator(3, 7);
        division.callElevator(6, 1);
        division.callElevator(2, 4);

        division.printStatus();

        System.out.println("\n--- Winda rusza ---");

        elevator.handleFloorStop(division, elevator.getCurrentFloor());

        elevator.processQueue(division);

        division.printStatus();
        System.out.println("\nSimulation complete.");
    }
}
