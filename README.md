# Simple Flight Reservation (Facade Pattern)

Small Java 17 + Maven example for Design Patterns Assignment 4.

## Workflow
- A: reserve a seat.
- B: issue an e-ticket and validate baggage (maximum 23 kg).
- C: save meal preference and loyalty points.
- `FlightReservationFacade` coordinates A → B → C.
- If B fails, A is cancelled. If C fails, B is cancelled first and then A.
- JSON files are saved in `demo-data/`; state remains after restarting.

## Run
Install JDK 17 and Maven, then from this folder:

```powershell
mvn test
mvn compile exec:java
```

Choose menu option 2 to demonstrate B failure, option 3 to demonstrate C failure, option 6 to inspect data, and option 8 to clear the data folder.

## Least Knowledge
Before: a client would need to call the seat service, ticket service, and loyalty service in the right order and perform compensation itself.
After: the client calls only `FlightReservationFacade.book(request)`. The facade hides the subsystem sequence.

## Facade vs Adapter
Facade provides one simple entry point to several subsystems. Adapter changes one interface into another compatible interface. This project uses Facade because it coordinates multiple reservation steps rather than adapting an incompatible API.

## Design note
The three subsystems have separate entities, status enums, services, and JSON repositories. Workflow progress is also stored, so unfinished workflows can be compensated on startup.
