# Load & Booking Management System

A robust Spring Boot backend system for managing Load & Booking operations efficiently. The system is optimized for performance, security, and scalability with comprehensive test coverage.

## 🚀 Features

- **Normalized Database Schema** with foreign key relationships and constraints
- **RESTful APIs** with comprehensive CRUD operations
- **Input Validation** using Bean Validation
- **Status Transitions** with business logic enforcement
- **Pagination and Filtering** for efficient data retrieval
- **Exception Handling** with consistent error responses
- **High Test Coverage** (60%+) using JUnit and Mockito
- **PostgreSQL** database with JPA/Hibernate

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Docker (optional, for containerized database)

## 🛠️ Setup Instructions

### 1. Database Setup

#### Option A: Local PostgreSQL
```sql
CREATE DATABASE cargopro;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE cargopro TO postgres;
```

#### Option B: Docker PostgreSQL
```bash
docker run --name cargopro-postgres \
  -e POSTGRES_DB=cargopro \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:15
```

### 2. Application Setup

1. **Clone the repository**
```bash
git clone <repository-url>
cd cargopro
```

2. **Update database configuration** (if needed)
Edit `src/main/resources/application.yml` to match your database settings.

3. **Build the project**
```bash
mvn clean install
```

4. **Run the application**
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

## 📊 Database Schema

### Load Entity
```sql
CREATE TABLE loads (
    id UUID PRIMARY KEY,
    shipper_id VARCHAR(255) NOT NULL,
    loading_point VARCHAR(255) NOT NULL,
    unloading_point VARCHAR(255) NOT NULL,
    loading_date TIMESTAMP NOT NULL,
    unloading_date TIMESTAMP NOT NULL,
    product_type VARCHAR(255) NOT NULL,
    truck_type VARCHAR(255) NOT NULL,
    no_of_trucks INTEGER NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    comment TEXT,
    date_posted TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'POSTED'
);
```

### Booking Entity
```sql
CREATE TABLE bookings (
    id UUID PRIMARY KEY,
    load_id UUID NOT NULL,
    transporter_id VARCHAR(255) NOT NULL,
    proposed_rate DOUBLE PRECISION NOT NULL,
    comment TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP NOT NULL,
    FOREIGN KEY (load_id) REFERENCES loads(id)
);
```

## 🔗 API Endpoints

### Load Management

#### Create Load
```http
POST /api/load
Content-Type: application/json

{
  "shipperId": "SHIPPER001",
  "facility": {
    "loadingPoint": "Mumbai",
    "unloadingPoint": "Delhi",
    "loadingDate": "2024-01-15T10:00:00",
    "unloadingDate": "2024-01-16T18:00:00"
  },
  "productType": "Electronics",
  "truckType": "Container",
  "noOfTrucks": 2,
  "weight": 5000.0,
  "comment": "Fragile items"
}
```

#### Get Loads with Pagination
```http
GET /api/load?shipperId=SHIPPER001&truckType=Container&status=POSTED&page=0&size=10
```

#### Get Load by ID
```http
GET /api/load/{loadId}
```

#### Update Load
```http
PUT /api/load/{loadId}
Content-Type: application/json

{
  "shipperId": "SHIPPER002",
  "facility": {
    "loadingPoint": "Mumbai",
    "unloadingPoint": "Delhi",
    "loadingDate": "2024-01-15T10:00:00",
    "unloadingDate": "2024-01-16T18:00:00"
  },
  "productType": "Furniture",
  "truckType": "Flatbed",
  "noOfTrucks": 3,
  "weight": 3000.0,
  "comment": "Updated load"
}
```

#### Delete Load
```http
DELETE /api/load/{loadId}
```

### Booking Management

#### Create Booking
```http
POST /api/booking
Content-Type: application/json

{
  "loadId": "uuid-of-load",
  "transporterId": "TRANSPORTER001",
  "proposedRate": 5000.0,
  "comment": "Available for immediate pickup"
}
```

#### Get Bookings with Pagination
```http
GET /api/booking?loadId=uuid&transporterId=TRANSPORTER001&status=PENDING&page=0&size=10
```

#### Get Booking by ID
```http
GET /api/booking/{bookingId}
```

#### Update Booking
```http
PUT /api/booking/{bookingId}
Content-Type: application/json

{
  "loadId": "uuid-of-load",
  "transporterId": "TRANSPORTER002",
  "proposedRate": 6000.0,
  "comment": "Updated booking"
}
```

#### Delete Booking
```http
DELETE /api/booking/{bookingId}
```

#### Accept Booking
```http
POST /api/booking/{bookingId}/accept
```

#### Reject Booking
```http
POST /api/booking/{bookingId}/reject
```

#### Get Bookings by Load
```http
GET /api/booking/load/{loadId}
```

#### Get Active Bookings by Load
```http
GET /api/booking/load/{loadId}/active
```

## 🔄 Status Transitions

### Load Status Flow
- **POSTED** → **BOOKED** (when a booking is accepted)
- **POSTED** → **CANCELLED** (when all bookings are deleted)
- **BOOKED** → **POSTED** (when all bookings are rejected/deleted)

### Booking Status Flow
- **PENDING** → **ACCEPTED** (when booking is accepted)
- **PENDING** → **REJECTED** (when booking is rejected)
- **ACCEPTED** → **REJECTED** (when another booking is accepted)

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Tests with Coverage Report
```bash
mvn test jacoco:report
```

### Test Coverage
- **Unit Tests**: Service layer with Mockito
- **Integration Tests**: Controller layer with MockMvc
- **Coverage Target**: 60%+ (currently exceeds target)

### Test Structure
```
src/test/java/com/cargopro/
├── service/
│   ├── LoadServiceTest.java
│   └── BookingServiceTest.java
└── controller/
    └── LoadControllerIntegrationTest.java
```

## 📈 Performance Optimizations

1. **Database Indexing**: Automatic indexes on foreign keys and frequently queried columns
2. **Pagination**: Efficient data retrieval with configurable page sizes
3. **Lazy Loading**: JPA relationships configured for optimal performance
4. **Connection Pooling**: HikariCP for database connection management
5. **Query Optimization**: Custom repository methods with optimized JPQL queries

## 🔒 Security Features

1. **Input Validation**: Comprehensive Bean Validation annotations
2. **SQL Injection Prevention**: Parameterized queries via JPA
3. **Error Handling**: Secure error responses without sensitive information
4. **Transaction Management**: ACID compliance for data integrity

## 🚀 Deployment

### Docker Deployment
```bash
# Build Docker image
docker build -t cargopro-load-booking .

# Run with Docker Compose
docker-compose up -d
```

### Traditional Deployment
```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/load-booking-system-1.0.0.jar
```

## 📝 API Response Examples

### Success Response
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "shipperId": "SHIPPER001",
  "facility": {
    "loadingPoint": "Mumbai",
    "unloadingPoint": "Delhi",
    "loadingDate": "2024-01-15T10:00:00",
    "unloadingDate": "2024-01-16T18:00:00"
  },
  "productType": "Electronics",
  "truckType": "Container",
  "noOfTrucks": 2,
  "weight": 5000.0,
  "comment": "Fragile items",
  "datePosted": "2024-01-10T09:00:00",
  "status": "POSTED"
}
```

### Error Response
```json
{
  "timestamp": "2024-01-10T09:00:00",
  "status": 400,
  "error": "Validation Error",
  "message": "Invalid input parameters",
  "path": "/api/load",
  "details": [
    "Shipper ID is required",
    "Product type is required"
  ]
}
```

### Paginated Response
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3,
  "hasNext": true,
  "hasPrevious": false
}
```
