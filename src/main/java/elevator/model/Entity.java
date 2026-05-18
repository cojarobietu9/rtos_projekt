package elevator.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Entity {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger(1);

    public enum State {
        WAITING_ON_FLOOR,
        IN_ELEVATOR,
        ARRIVED
    }

    private final int id;
    private final int originFloor;
    private final int destinationFloor;
    private State state;

    /**
     * Creates a new entity.
     *
     * @param originFloor      the floor number the entity starts on
     * @param destinationFloor the floor number the entity wants to reach
     */
    public Entity(int originFloor, int destinationFloor) {
        this.id = ID_COUNTER.getAndIncrement();
        this.originFloor = originFloor;
        this.destinationFloor = destinationFloor;
        this.state = State.WAITING_ON_FLOOR;
    }

    public void boardElevator() {
        if (state != State.WAITING_ON_FLOOR) {
            throw new IllegalStateException("Osoba " + id + " nie może wejść do windy.");
        }
        state = State.IN_ELEVATOR;
        System.out.printf(" Osoba %d weszła do windy na %d piętrze. Jedzie do %d piętra.%n", id, originFloor, destinationFloor);
    }

    public void exitElevator() {
        if (state != State.IN_ELEVATOR) {
            throw new IllegalStateException("Entity " + id + " cannot exit – current state: " + state);
        }
        state = State.ARRIVED;
        System.out.printf("  Osoba %d wyszła z windy. %n", id);
    }

    public int getId(){
        return id;
    }
    
    public int getOriginFloor(){
        return originFloor;
    }
    
    public int getDestinationFloor(){
        return destinationFloor;
    }
    
    public State getState(){
        return state;
    }
}
