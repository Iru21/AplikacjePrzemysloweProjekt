# Dating App

A full-stack dating application built with Spring Boot, MariaDB, and Thymeleaf.

## Running with Docker

### Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/Iru21/AplikacjePrzemysloweProjekt
   cd AplikacjePrzemysloweProjekt
   ```

2. **Build and run with Docker Compose**
   ```bash
   docker-compose up --build
   ```

   Or use the provided script:
   ```bash
   ./scripts/docker-run.sh
   ```

3. **Access the application**
   - **Web Application**: http://localhost:8080
   - **Swagger UI**: http://localhost:8080/swagger-ui.html

4. **Stop the application**
   ```bash
   docker-compose down
   ```

### Database Connection Details

- **Host**: localhost
- **Port**: 3307 (external), 3306 (internal)
- **Database**: datingapp
- **Username**: datingapp_user
- **Password**: datingapp_pass
- **Root Password**: rootpassword

## Testing

### Run all tests
```bash
./gradlew test
```

### Code Coverage
```bash
./gradlew test jacocoTestReport
```

View report: `build/reports/jacoco/test/html/index.html`

## API Documentation

### Swagger UI
Once the application is running, access interactive API documentation at:
- http://localhost:8080/swagger-ui.html

### OpenAPI Specification
- JSON: http://localhost:8080/v3/api-docs
- YAML: http://localhost:8080/v3/api-docs.yaml