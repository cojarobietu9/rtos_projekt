package GUI;

import elevator.model.Division;
import elevator.model.Elevator;
import elevator.model.Floor;
import elevator.model.Elevator.State;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.Random;


public class ElevatorGUI extends JFrame {

    private Division division;
    private Elevator elevator;

    private final JLabel simulationState = new JLabel();
    private final JLabel elevatorFloorLabel = new JLabel();
    private final JLabel elevatorSpeedLabel = new JLabel();
    private final JLabel elevatorStateLabel = new JLabel();
    private final JLabel elevatorPassengersLabel = new JLabel();
    private final JLabel elevatorQueueLabel = new JLabel();
    private final JLabel totalFloorPassengersLabel = new JLabel();

    private final DefaultListModel<String> floorsModel = new DefaultListModel<>();
    private final JList<String> floorsList = new JList<>(floorsModel);

    private final JSpinner startFloorInput = new JSpinner(new SpinnerNumberModel(4, -1, 100, 1));
    private final JSpinner minFloorInput = new JSpinner(new SpinnerNumberModel(-1, -10, 100, 1));
    private final JSpinner maxFloorInput = new JSpinner(new SpinnerNumberModel(7, -1, 100, 1));
    private final JSpinner speedMsInput = new JSpinner(new SpinnerNumberModel(1000, 100, 10000, 100));
    private final JSpinner ioWaitMsInput = new JSpinner(new SpinnerNumberModel(3000, 100, 10000, 100));
    private final JSpinner capacityInput = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));

    private final JSpinner fromFloorInput = new JSpinner(new SpinnerNumberModel(0, -1, 100, 1));
    private final JSpinner toFloorInput = new JSpinner(new SpinnerNumberModel(1, -1, 100, 1));

    private Thread simulationThread;
    private volatile boolean stopRequested = true;

    public ElevatorGUI() {
        super("Winda");

        initSimulationFromInputs();

        setLayout(new BorderLayout(10, 10));

        JPanel main = new JPanel(new GridLayout(1, 2, 10, 10));

        JPanel floorsPanel = new JPanel(new BorderLayout());
        floorsPanel.setBorder(new TitledBorder("Piętra"));
        floorsList.setVisibleRowCount(10);
        floorsPanel.add(new JScrollPane(floorsList), BorderLayout.CENTER);
        floorsPanel.add(totalFloorPassengersLabel, BorderLayout.SOUTH);

        JPanel elevatorPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        elevatorPanel.setBorder(new TitledBorder("Winda"));
        elevatorPanel.add(simulationState);
        elevatorPanel.add(elevatorFloorLabel);
        elevatorPanel.add(elevatorSpeedLabel);
        elevatorPanel.add(elevatorStateLabel);
        elevatorPanel.add(elevatorPassengersLabel);
        elevatorPanel.add(elevatorQueueLabel);

        main.add(floorsPanel);
        main.add(elevatorPanel);

        JPanel controls = new JPanel(new GridLayout(4, 4, 5, 5));
        controls.setBorder(new TitledBorder("Parametry symulacji"));

        controls.add(new JLabel("Najniższe piętro:"));
        controls.add(minFloorInput);
        controls.add(new JLabel("Najwyższe piętro:"));
        controls.add(maxFloorInput);
        
        controls.add(new JLabel("Piętro startowe:"));
        controls.add(startFloorInput);
        controls.add(new JLabel("Pojemność windy:"));
        controls.add(capacityInput);
        
        controls.add(new JLabel("Czas przejazdu między piętrami (ms):"));
        controls.add(speedMsInput);
        controls.add(new JLabel("Czas postoju przy wsiadaniu (ms):"));
        controls.add(ioWaitMsInput);

        JButton apply = new JButton("Zastosuj i restartuj");
        // empty labels for alignment
        controls.add(new JLabel(""));
        controls.add(new JLabel(""));
        controls.add(new JLabel(""));
        controls.add(apply);

        JPanel passengerPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        passengerPanel.setBorder(new TitledBorder("Dodaj pasażera"));
        passengerPanel.add(new JLabel("Z piętra:"));
        passengerPanel.add(fromFloorInput);

        passengerPanel.add(new JLabel("Na piętro:"));
        passengerPanel.add(toFloorInput);
        JButton addManualPassenger = new JButton("Dodaj");
        passengerPanel.add(new JLabel(""));
        passengerPanel.add(addManualPassenger);

        JPanel buttons = new JPanel(new GridLayout(1,3,5,5));

        JButton addRandomPassenger = new JButton("<html><body style='text-align:center'>Dodaj losowego<br>pasażera</body></html>");
        JButton runSimulation = new JButton("Start symulacji");
        JButton stopSimulation = new JButton("Stop symulacji");

        apply.addActionListener(e -> {
            stopSimulation();
            initSimulationFromInputs();
            updateUIState();
        });
        addRandomPassenger.addActionListener(e -> spawnRandomPassenger());
        addManualPassenger.addActionListener(e -> addManualPassenger());
        runSimulation.addActionListener(e -> startSimulationThread());
        stopSimulation.addActionListener(e -> stopSimulation());

        buttons.add(addRandomPassenger);
        buttons.add(runSimulation);
        buttons.add(stopSimulation);

        JPanel bottom = new JPanel(new GridLayout(1, 2, 5,5));
        bottom.add(passengerPanel);
        bottom.add(buttons);
        add(main, BorderLayout.CENTER);
        add(controls, BorderLayout.NORTH);
        add(bottom, BorderLayout.SOUTH);

        updateUIState();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 640);
        setLocationRelativeTo(null);
    }

    private void initSimulationFromInputs() {
        int start = (int) startFloorInput.getValue();
        int min = (int) minFloorInput.getValue();
        int max = (int) maxFloorInput.getValue();
        int speed = (int) speedMsInput.getValue();
        int ioWait = (int) ioWaitMsInput.getValue();
        int cap = (int) capacityInput.getValue();

        if (max < min) {
            JOptionPane.showMessageDialog(this, "Max piętro musi być >= min piętro.");
            return;
        }

        elevator = new Elevator(start, speed, ioWait, cap);
        division = new Division("GUI Division", min, max, elevator);

        elevator.addListener(e -> updateUIState());

        if (0 >= min && 5 <= max) division.callElevator(0, 5);
        if (3 >= min && 7 <= max) division.callElevator(3, 7);
        if (6 >= min && 1 <= max) division.callElevator(6, 1);
        if (2 >= min && 4 <= max) division.callElevator(2, 4);

        updateManualInputsRange(min, max);
    }

    private void updateManualInputsRange(int min, int max) {
        ((SpinnerNumberModel) fromFloorInput.getModel()).setMinimum(min);
        ((SpinnerNumberModel) fromFloorInput.getModel()).setMaximum(max);
        ((SpinnerNumberModel) toFloorInput.getModel()).setMinimum(min);
        ((SpinnerNumberModel) toFloorInput.getModel()).setMaximum(max);

        if ((int) fromFloorInput.getValue() < min) fromFloorInput.setValue(min);
        if ((int) toFloorInput.getValue() < min) toFloorInput.setValue(min);
        if ((int) fromFloorInput.getValue() > max) fromFloorInput.setValue(max);
        if ((int) toFloorInput.getValue() > max) toFloorInput.setValue(max);
    }

    private void spawnRandomPassenger() {
        Random r = new Random();
        int from, to;
        do {
            from = r.nextInt(division.getFloorCount()) + division.getMinFloor(); // bo możemy mieć piętra ujemne albo zaczynać na >0 piętrze
            to = r.nextInt(division.getFloorCount()) + division.getMinFloor();
        } while (from == to);

        division.callElevator(from, to);
        if(elevator.getCurrentState() == State.IDLE && !stopRequested)
            startSimulationThread();
        updateUIState();
    }

    private void addManualPassenger() {
        int from = (int) fromFloorInput.getValue();
        int to = (int) toFloorInput.getValue();

        if (from == to) {
            JOptionPane.showMessageDialog(this, "Piętro początkowe i docelowe muszą być różne.");
            return;
        }

        division.callElevator(from, to);
        updateUIState();
    }

    private void startSimulationThread() {
        if (simulationThread != null && simulationThread.isAlive()) return;

        stopRequested = false;
        simulationThread = new Thread(() -> {
            try {
                elevator.handleFloorStop(division, elevator.getCurrentFloor());
                elevator.processQueue(division);
            } catch (InterruptedException ex) {
                if (!stopRequested) ex.printStackTrace();
            }
        });
        simulationThread.start();
    }

    private void stopSimulation() {
        stopRequested = true;
        if (simulationThread != null) {
            simulationThread.interrupt();
        }
        updateUIState();
    }

    private void updateUIState() {
        SwingUtilities.invokeLater(() -> {
            if (stopRequested) {
                simulationState.setText("Stan symulacji: Zatrzymana");
                simulationState.setForeground(Color.RED);
            }
            else {
                simulationState.setText("Stan symulacji: Aktywna");
                simulationState.setForeground(new Color(28,163,31));
            }
            elevatorFloorLabel.setText("Aktualne piętro: " + elevator.getCurrentFloor());
            elevatorSpeedLabel.setText("Prędkość (ms/piętro): " + elevator.getSpeedMs());
            elevatorStateLabel.setText("Stan: " + elevator.getDirection());
            switch(elevator.getDirection()) {
                case UP:
                    elevatorStateLabel.setForeground(new Color(107, 105, 255));
                    break;
                case IDLE:
                    elevatorStateLabel.setForeground(new Color(255, 166, 0)); 
                    break;
                case DOWN:
                    elevatorStateLabel.setForeground(new Color(213, 105, 255));
                    break;
            }
            
            elevatorPassengersLabel.setText("Pasażerowie w windzie: " +
                    elevator.getOccupantCount() + "/" + elevator.getMaxCapacity());
            elevatorQueueLabel.setText("Kolejka: " + elevator.getDestinationQueue());

            floorsModel.clear();
            int totalWaiting = 0;
            List<Floor> floors = division.getFloors();
            for (int i = floors.size() - 1; i >= 0; i--) {
                Floor f = floors.get(i);
                String marker = (elevator.getCurrentFloor() == f.getFloorNumber()) ? " ◄ Winda" : "";
                floorsModel.addElement("Piętro " + f.getFloorNumber() +
                        " | Pasażerowie na piętrze: " + f.getWaitingCount() + marker);
                totalWaiting += f.getWaitingCount();
            }

            totalFloorPassengersLabel.setText("Łącznie na piętrach: " + totalWaiting);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ElevatorGUI().setVisible(true));
    }
}