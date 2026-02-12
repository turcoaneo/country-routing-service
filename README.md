# Country Routing Service

A Spring Boot application that computes land routes between countries using their border information.

## Features
- REST endpoint `/routing/{origin}/{destination}`
- Shortest land route using BFS
- Country data sourced from: https://raw.githubusercontent.com/mledoze/countries/master/countries.json
- Swagger UI enabled
- Modular architecture (data loader, graph builder, routing engine)

## Build & Run

mvn clean install
mvn spring-boot:run