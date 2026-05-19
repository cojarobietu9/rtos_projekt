package elevator.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Elevator {

    public interface ElevatorListener {
        void onElevatorUpdate(Elevator elevator);
    }

    private final List<ElevatorListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(ElevatorListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ElevatorListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (ElevatorListener l : listeners) {
            l.onElevatorUpdate(this);
        }
    }

    public enum State {
        UP, DOWN, IDLE
    }

    private int currentFloor;
    private final long speedMs, IOwaitMs;
    private final int maxCapacity;
    private final List<Entity> occupants;
    private State currentState;

    private final LinkedList<Integer> destinationQueue;

    public Elevator(int startFloor, long speedMs, long IOwaitMs, int capacity) {
        if (speedMs <= 0) {
            throw new IllegalArgumentException("speedMs must be > 0");
        }

        if (IOwaitMs <= 0) {
            throw new IllegalArgumentException("IOwaitMs must be > 0");
        }

        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }

        if (startFloor < -1) {
            throw new IllegalArgumentException("starting floor must be > -1");
        }

        this.currentFloor = startFloor;
        this.speedMs = speedMs;
        this.IOwaitMs = IOwaitMs;
        this.maxCapacity = capacity;
        this.currentState = State.IDLE;
        this.occupants = new ArrayList<>();
        this.destinationQueue = new LinkedList<>();
    }

    public void pushDestinationFront(int floor) {
        if (floor == currentFloor)
            return;

        destinationQueue.remove(Integer.valueOf(floor));
        destinationQueue.addFirst(floor);
        System.out.printf("  Winda dodała piętro %d na początek kolejki. Kolejka: %s%n", floor, destinationQueue);
    }

    public void enqueueDestination(int floor) {
        if (floor == currentFloor) return;
        if (!destinationQueue.contains(floor)) {
            destinationQueue.addLast(floor);
            System.out.printf("  Winda dodała %d piętro na koniec kolejki. Kolejka: %s%n", floor, destinationQueue);
        }
    }

    private void moveOneFloor(Division division, int targetFloor) throws InterruptedException, IllegalArgumentException {
        int nextFloor = currentFloor + (this.currentState == State.UP ? 1 : -1);

        if (nextFloor > division.getMaxFloor() || nextFloor < division.getMinFloor()) {
            if (this.currentState == State.UP) setCurrentState(State.DOWN); else setCurrentState(State.UP);
            throw new IllegalArgumentException("Destination floor does not exist in division.");
        }

        System.out.printf("%n Winda rusza się z %d piętra na %d piętro. %n", currentFloor, nextFloor);
        Thread.sleep(speedMs);
        currentFloor = nextFloor;

        notifyListeners(); // odśwież GUI po każdym ruchu
    }

    public void processQueue(Division division) throws InterruptedException {
        while (!destinationQueue.isEmpty()) {
            int target = destinationQueue.peekFirst();

            if (currentFloor == target) {
                destinationQueue.pollFirst();
                handleFloorStop(division, currentFloor);
                continue;
            }

            if (this.currentState == State.IDLE && !destinationQueue.isEmpty()) {
                if (destinationQueue.peekFirst() > this.currentFloor)
                    setCurrentState(State.UP);
                else
                    setCurrentState(State.DOWN);
            }

            moveOneFloor(division, target);

            if (destinationQueue.contains(currentFloor)) {
                destinationQueue.remove(Integer.valueOf(currentFloor));
                handleFloorStop(division, currentFloor);
            } else if (currentFloor == target) {
                destinationQueue.pollFirst();
                handleFloorStop(division, currentFloor);
            }
        }

        System.out.println("Elevator queue empty – elevator idle at floor " + currentFloor);
    }

    public void handleFloorStop(Division division, int floorNum) throws InterruptedException {
        Floor floor = division.getFloor(floorNum);
        System.out.printf("%n--- Winda zatrzymała się na piętrze %d ---%n", floorNum);

        List<Entity> exiting = new ArrayList<>();
        for (Entity e : occupants) {
            if (e.getDestinationFloor() == floorNum) {
                exiting.add(e);
            }
        }
        for (Entity e : exiting) {
            occupants.remove(e);
            floor.entityExitsElevator(e);
        }

        if (floor.hasWaitingEntities() && !isFull()) {
            List<Entity> waiting = new ArrayList<>(floor.getWaitingEntities());
            for (Entity e : waiting) {
                if (isFull()) break;
                floor.entityEntersElevator(e);
                occupants.add(e);
                pushDestinationFront(e.getDestinationFloor());
            }
        }

        Thread.sleep(this.IOwaitMs);

        setCurrentState(State.IDLE);

        printStatus();

        notifyListeners(); // odśwież GUI po postoju
    }

    public void setCurrentState(State state) {
        this.currentState = state;
    }

    public boolean isFull() {
        return occupants.size() >= maxCapacity;
    }

    public boolean isEmpty() {
        return occupants.isEmpty();
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public long getSpeedMs() {
        return speedMs;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getOccupantCount() {
        return occupants.size();
    }

    public List<Entity> getOccupants() {
        return java.util.Collections.unmodifiableList(occupants);
    }

    public LinkedList<Integer> getDestinationQueue() {
        return new LinkedList<>(destinationQueue);
    }

    public State getDirection() {
        if (destinationQueue.isEmpty()) return State.IDLE;
        int next = destinationQueue.peekFirst();
        return next > currentFloor ? State.UP : State.DOWN;
    }

    public void printStatus() {
        System.out.printf(
                "Status windy: Piętro = %d | Ludzie w windzie = %d/%d | Kolejka pięter = %s | Status = %s%n",
                currentFloor, occupants.size(), maxCapacity, destinationQueue, getDirection()
        );
    }
}