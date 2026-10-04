# Smart Logistics Route Optimization System

DAA mini-project using TSP Branch & Bound, Greedy (Nearest Neighbor), and Dijkstra's Algorithm.

## Stack
- Java 21
- Spring Boot
- PostgreSQL
- React + Vite
- Plain CSS

## Run
### Backend
1. Create PostgreSQL database `smart_logistics`.
2. Update `backend/src/main/resources/application.properties`.
3. Run:
   `cd backend && ./mvnw spring-boot:run`
   or `mvn spring-boot:run`

Backend: http://localhost:8080

### Frontend
`cd frontend`
`npm install`
`npm run dev`

Frontend: http://localhost:5173

The backend seeds sample locations and distances automatically on an empty database.
