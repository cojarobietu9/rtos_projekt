package elevator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.lang.Comparable;

public class Floor implements Comparable<Floor> {

    private final int floorNumber;

    private final List<Entity> waitingEntities;

    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.waitingEntities = new ArrayList<>();
    }

    public Entity spawnEntity(int destinationFloor) {
        if (destinationFloor == floorNumber) {
            throw new IllegalArgumentException("Piętro 'do' musi się różnić od aktualnego.");
        }
        Entity entity = new Entity(floorNumber, destinationFloor);
        waitingEntities.add(entity);
        System.out.printf("  Osoba %d pojawiła się na piętrze %d.", entity.getId(), floorNumber);
        return entity;
    }

    public void entityEntersElevator(Entity entity) throws InterruptedException {
        if (!waitingEntities.remove(entity)) {
            throw new IllegalArgumentException("Osoba " + entity.getId() + " nie czeka już na piętrze " + floorNumber);
        }
        entity.boardElevator();
    }

    public void entityExitsElevator(Entity entity) throws InterruptedException {
        entity.exitElevator();                           // transitions state → ARRIVED

        // Entity has reached its destination; it disappears from the building.
        System.out.printf("  Osoba %d zniknęła z piętra %d.%n",  entity.getId(), floorNumber);
    }

    public boolean hasWaitingEntities() {
        return !waitingEntities.isEmpty();
    }

    public int getWaitingCount() {
        return waitingEntities.size();
    }

    public List<Entity> getWaitingEntities() {
        return Collections.unmodifiableList(waitingEntities);
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    @Override
    public int compareTo(Floor otherFloor) {
        return Integer.compare(this.floorNumber, otherFloor.getFloorNumber());
    }
}
