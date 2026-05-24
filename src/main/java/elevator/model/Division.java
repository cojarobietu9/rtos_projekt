package elevator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
// import java.util.Collections;

public class Division {

    private final String name;
    private final List<Floor> floors;
    private final Elevator elevator;

    public Division(String name, int startFloor, int endFloor, Elevator elevator) {
        if (endFloor < startFloor) {
            throw new IllegalArgumentException("startFloor musi być mniejsze niż endFloor.");
        }

        this.name = name;
        this.elevator = elevator;
        this.floors = new ArrayList<>(endFloor - startFloor+1);

        for (int i = startFloor; i <= endFloor; i++) {
            floors.add(new Floor(i));
        }
    }

    public Floor getFloor(int floorNumber) {
        if (floorNumber < getMinFloor() || floorNumber > getMaxFloor()) {
            throw new IndexOutOfBoundsException("Piętro " + floorNumber + " nie istnieje w pionie '" + name + "'.");
        }

        for(int i = 0; i<floors.size(); i++){
            if(floors.get(i).getFloorNumber() == floorNumber)
                return floors.get(i);
        }
        // default, so that java doesn't shit itself
        return floors.get(0);
    }

    public List<Floor> getFloors() {
        return Collections.unmodifiableList(floors);
    }

    public int getFloorCount() {
        return floors.size();
    }

    public int getMinFloor() {
        return Collections.min(this.floors).getFloorNumber();
    }

    public int getMaxFloor() {
        return Collections.max(this.floors).getFloorNumber();
    }


    public Elevator getElevator() {
        return elevator;
    }

    public Entity callElevator(int originFloor, int destinationFloor) {
        validateFloor(originFloor, "origin");
        validateFloor(destinationFloor, "destination");

        System.out.printf("Przyzwanie: piętro %d do piętra %d%n", originFloor, destinationFloor);

        Floor floor  = getFloor(originFloor);
        Entity entity = floor.spawnEntity(destinationFloor);

        elevator.enqueueDestination(originFloor);
        return entity;
    }

    public void printStatus() {
        System.out.println("\n========== Pion '" + name + "' Status ==========");
        for (int i = floors.size() - 1; i >= 0; i--) {
            Floor f = floors.get(i);
            String marker = (elevator.getCurrentFloor() == f.getFloorNumber()) ? " ◄ ELEVATOR" : "";
            System.out.printf("  Piętro %d | L. osób czekających: %d%s%n", f.getFloorNumber(), f.getWaitingCount(), marker);
        }
        elevator.printStatus();
        System.out.println("=====================================================");
    }

    private void validateFloor(int floor, String label) {
        if (floor < getMinFloor() || floor > getMaxFloor()) {
            throw new IllegalArgumentException("Złe piętro " + label + ": " + floor + " (valid range " + getMinFloor() + " - " + getMaxFloor() + (floors.size() - 1) + ")");
        }
    }

}
