# Nie
## naw
### idz
#### ę k
##### się
###### dza

 ```mermaid

 sequenceDiagram
    Chuj->>Ksiądz: Wwchodzi w dupe
    Ksiądz-->>Chuj: aprobuje
    Chuj-)Ksiądz: wychodzi z dupy
 ```


dobra a tak bardziej serio

założenia: 4 klasy
- Entity - osoba która chce skorzystać z windy. Ma source floor, destination floor. Jeżeli winda się zatrzyma i nie jest pełna, wsiada do windy i jego piętro jest wrzucone
na przód kolejki priorytetowej;
- Floor - piętro. Na piętrach spawnują się entity.
- Elevator - winda. Jeździ. Do niej wchodzą entity. Jeden pion ma jedną windę z założenia. ProcessQueue jest ważne.
- Division - pion. Zawiera listę piętr, windę, i by proxy osoby. Główna klasa do wzywania windy.


simulation/ElevatorSimulation wygenerował claude