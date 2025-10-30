# Load Balancing Study

A comprehensive study of different load balancing strategies implemented in a single Spring Boot application.

## Overview

This project simulates three virtual workers in a single application to study and compare different load balancing strategies across various scenarios. The application provides REST APIs to run experiments and collect metrics.

## Features

### Load Balancing Strategies

1. **Round Robin** - Distributes requests evenly across all workers in sequence
2. **Least Request** - Routes to the worker with the fewest active requests
3. **Consistent Hash** - Uses consistent hashing based on request keys for deterministic routing

### Test Scenarios

1. **Heterogeneous Nodes** - Workers with different latencies (50ms, 100ms, 150ms)
2. **Hot Key** - Simulates skewed access patterns with frequent requests to the same key
3. **Partial Failure** - Tests resilience when one worker has a failure rate

### Metrics Collected

- Request count
- Success count
- Failure count
- Average response time
- Success rate
- Worker load distribution

## Project Structure

```
src/main/java/com/example/
├── LoadBalancingStudyApplication.java    # Main application class
├── controller/
│   └── MainController.java               # All REST endpoints
├── strategy/
│   ├── LoadBalancerStrategy.java         # Strategy interface
│   ├── RoundRobinStrategy.java
│   ├── LeastRequestStrategy.java
│   └── ConsistentHashStrategy.java
├── service/
│   ├── WorkerService.java                # Simulates backend work
│   └── LoadGeneratorService.java         # Generates test traffic
├── scenario/
│   ├── Scenario.java                     # Scenario interface
│   ├── HeterogeneousNodesScenario.java
│   ├── HotKeyScenario.java
│   └── PartialFailureScenario.java
├── metrics/
│   └── MetricsCollector.java             # Metrics collection
└── config/
    └── WorkerConfig.java                 # Worker configurations
```

## Getting Started

### Prerequisites

- Java 21
- Maven 3.6+

### Building the Project

```bash
mvn clean install
```

### Running the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### 1. Handle Request (Load Balancer)
```bash
GET /api/request?key=test123&strategy=round-robin
```

### 2. Run a Specific Scenario
```bash
POST /api/scenario/run?scenarioName=heterogeneous&strategy=round-robin
```

### 3. Run All Tests (Complete Test Suite)
```bash
POST /api/test/run-all
```

### 4. Get Metrics
```bash
GET /api/metrics?scenarioName=heterogeneous&strategy=round-robin
```

### 5. Configure Worker
```bash
POST /api/worker/configure?workerId=1&latency=100&failureRate=0.1
```

## Example Usage

### Run Complete Test Suite

```bash
curl -X POST http://localhost:8080/api/test/run-all
```

This will run all scenarios against all strategies and return a comprehensive metrics matrix.

### Run a Single Test

```bash
curl -X POST "http://localhost:8080/api/scenario/run?scenarioName=heterogeneous&strategy=least-request"
```

### Get Results

```bash
curl "http://localhost:8080/api/metrics?scenarioName=heterogeneous&strategy=round-robin"
```

## Configuration

Worker configurations can be adjusted in `src/main/resources/application.yml`:

```yaml
workers:
  count: 3
  default-latency: 100
  worker1:
    latency: 50
    failure-rate: 0.0
  worker2:
    latency: 100
    failure-rate: 0.0
  worker3:
    latency: 150
    failure-rate: 0.0
```

## Study Goals

1. Compare load distribution across strategies
2. Analyze performance under heterogeneous worker conditions
3. Test consistency with hot keys
4. Evaluate fault tolerance and resilience
5. Measure response time differences between strategies

## License

This project is created for educational purposes as part of the Enterprise Distributed Systems (CMPE 273) course.

