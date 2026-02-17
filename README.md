# 🌍 Country Routing Service

A Spring Boot application that computes the shortest **land route** between two countries using BFS over a graph derived from `borders.json`.

The service exposes a REST API and includes full Swagger/OpenAPI documentation.

---

## 🚀 How to Run the Application

### Prerequisites
- Java 21
- Maven 3.9+

### Build & Start

    mvn clean install
    mvn spring-boot:run

The application starts at:

    http://localhost:8080

---

## 📘 API Documentation (Swagger UI)

Once the application is running, open:

    http://localhost:8080/swagger-ui.html

Swagger UI will display all available endpoints, including the routing API.

---

## 🌐 Routing Endpoint

### GET `/routing/{origin}/{destination}`

Computes the shortest land route between two countries using their **CCA3** codes.

### Example Request

    GET /routing/CZE/ITA

### Successful Response (200)

    {
      "route": ["CZE", "AUT", "ITA"]
    }

### Error: Unknown Country (400)

    {
      "error": "Unknown country code"
    }

### Error: No Land Route (400)

    {
      "error": "No land route found"
    }

---

## 🗺️ Data Source

The application uses:

    src/main/resources/data/borders.json

This file is generated from the official `countries.json` dataset and contains:

- `cca2`
- `ccn3`
- `cca3`
- `cioc`
- `borders` (sorted alphabetically)

Example entry:

    "ITA": {
      "cca2": "IT",
      "ccn3": "380",
      "cca3": "ITA",
      "cioc": "ITA",
      "borders": ["AUT", "CHE", "FRA", "SMR", "SVN", "VAT"]
    }

---

## 🧠 How Routing Works

1. `borders.json` is loaded at startup.
2. A graph is built using an adjacency list.
3. A **Breadth‑First Search (BFS)** is executed to find the shortest path.
4. The route is returned as a list of CCA3 country codes.

This guarantees the minimal number of border crossings.

---

## 🧪 Running Tests

Execute the full test suite:

    mvn test

The project includes:

- JSON transformation tests
- Graph construction tests
- BFS routing tests
- Controller tests (positive & negative)

---

## 🧩 Future Enhancements

- Fuzzy matching for mistyped country codes ✅ (/routing/fuzzy/ROM/SPN)
- “All possible routes” mode plus benchmark - iterative / recursive ✅ (dfs-benchmark.csv)
  - (/routing/all/iterative/ROU/ESP?maxDepth=10&maxRoutes=10)
  - (/routing/all/ROU/ESP?maxDepth=10&maxRoutes=10)
- Caching for repeated queries ✅
  - (/routing/fuzzy/all/ESP/ITA?maxDepth=2&maxRoutes=3)
  - (/routing/fuzzy/all/ITA/ESP?maxDepth=2&maxRoutes=3) - reversed from cache and appended to cache
- Dockerfile for containerized deployment
  - docker build -t country-routing-service .
  - docker run -p 8080:8080 country-routing-service

---

## 👨‍💻 Author

Built with care, curiosity, and a bit of over‑engineering fun.
