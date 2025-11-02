# Load Balancing Study

A comprehensive study of different load balancing strategies implemented in a single Spring Boot application.

## Overview

This project simulates multiple virtual workers in a single application to study and compare different load balancing strategies across various scenarios. The application exposes per-strategy REST endpoints (no central controller) and returns a structured JSON result for each run.

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
├── controller/                           # Per-strategy controllers
│   ├── RoundRobinController.java
│   ├── LeastRequestController.java
│   └── ConsistentHashController.java
├── strategy/
│   ├── LoadBalancerStrategy.java         # Strategy interface
│   ├── RoundRobinStrategy.java
│   ├── LeastRequestStrategy.java
│   └── ConsistentHashStrategy.java
├── service/
│   ├── RoundRobinService.java            # Round-robin scenario runs
│   ├── LeastRequestService.java          # Least-request scenario runs
│   ├── ConsistentHashService.java        # Consistent-hash scenario runs
│   │
│   ├── WorkerService.java                # Simulates backend work
│   ├── LoadGeneratorService.java         # Concurrent load generation
│   └── TestExecutor.java                 # Shared request execution + aggregation
├── scenario/
│   ├── Scenario.java                     # Scenario interface
│   ├── HeterogeneousNodesScenario.java
│   ├── HotKeyScenario.java
│   └── PartialFailureScenario.java
├── metrics/
│   └── MetricsCollector.java             # In-memory metrics collection
├── util/
│   └── LoadTestUtils.java                # Percentiles + worker distribution helpers
├── vo/
│   ├── TestResult.java                   # Response payload for a test run
│   └── LatencyStats.java                 # Aggregate latency stats
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

## API Endpoints (Per Strategy)

Each endpoint runs a scenario and returns a `vo.TestResult` JSON.

### Round Robin
```bash
GET /api/round-robin/heterogeneous-nodes
GET /api/round-robin/hot-key
GET /api/round-robin/partial-failure
```

### Least Request
```bash
GET /api/least-request/heterogeneous-nodes
GET /api/least-request/hot-key
GET /api/least-request/partial-failure
```

### Consistent Hash
```bash
GET /api/consistent-hash/heterogeneous-nodes
GET /api/consistent-hash/hot-key
GET /api/consistent-hash/partial-failure
```

Note: until `LeastRequestStrategy` and `ConsistentHashStrategy` are implemented, their endpoints return HTTP 501.

## Example Usage

Run Round Robin scenarios:
```bash
curl "http://localhost:8080/api/round-robin/heterogeneous-nodes"
curl "http://localhost:8080/api/round-robin/hot-key"
curl "http://localhost:8080/api/round-robin/partial-failure"
```

Response payload (`vo.TestResult`):
```json
{
  "scenario": "hot-key",
  "strategy": "round-robin",
  "totalRequests": 300,
  "successfulRequests": 300,
  "failedRequests": 0,
  "durationMs": 12345,
  "distribution": { "worker1": 100, "worker2": 100, "worker3": 100 },
  "latency": { "min": 10, "max": 220, "average": 105.3, "p50": 100, "p95": 200, "p99": 210 },
  "hotKeyRequests": 240,
  "hotKeyDistribution": { "worker1": 90, "worker2": 80, "worker3": 70 }
}
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

## Notes

- The previous central `MainController` has been removed in favor of per-strategy controllers and services.
- Scenarios (in `scenario/`) own environment setup and key-generation patterns; services delegate execution to `service.TestExecutor`.
- Common helpers for percentiles and worker distributions live in `util/LoadTestUtils`.

