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
- Dockerfile for containerized deployment ✅
  - docker build -t country-routing-service .
  - docker run -p 8080:8080 country-routing-service
- GitHub Actions + Terraform ✅

### Run Terraform from local CLI

```shell
terraform init
terraform validate
terraform plan -var-file="dev.tfvars"
terraform apply -var-file="dev.tfvars" -auto-approve

terraform destroy -var-file="dev.tfvars" -auto-approve
```

### Create GitHub actions Terraform lockers

S3 -> Create bucket -> country-routing-tf-state
- Region:
  Same region where Terraform deploys (e.g., eu-north-1)
- Disable public access (default)
- Enable versioning (recommended)
- Create bucket


#### Using CLI
aws s3api create-bucket \
--bucket country-routing-tf-state \
--region eu-north-1 \
--create-bucket-configuration LocationConstraint=eu-north-1

aws s3api put-bucket-versioning \
--bucket country-routing-tf-state \
--versioning-configuration Status=Enabled


DynamoDB -> Create table -> country-routing-tf-locks
- Partition key:
  LockID (String)
- Billing mode: On‑demand
- Create table

#### Using CLI
aws dynamodb create-table \
--table-name country-routing-tf-locks \
--attribute-definitions AttributeName=LockID,AttributeType=S \
--key-schema AttributeName=LockID,KeyType=HASH \
--billing-mode PAY_PER_REQUEST

### Run actions from GitHub Actions
https://github.com/turcoaneo/country-routing-service/actions

CI-CD - build Docker container in GHCR
Deploy to ECS - upload container from GHCR to AWS
Terraform infrastructure - create / apply infrastructure or destroy it

---

## 👨‍💻 Author

Built with care, curiosity, and a bit of over‑engineering fun.
