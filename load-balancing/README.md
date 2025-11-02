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

1. **Heterogeneous Nodes** - Workers with different latencies (50ms, 100ms, 200ms) to test how strategies handle varying worker performance
2. **Hot Key** - Simulates skewed access patterns with 80% of requests targeting the same key (tests cache affinity and key-based routing)
3. **Partial Failure** - Two-phase test: Phase 1 (baseline with all workers healthy) → Phase 2 (one worker has 50% failure rate)

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
│   ├── StrategyService.java              # Interface for strategy services
│   ├── RoundRobinService.java            # Round-robin scenario runs
│   ├── LeastRequestService.java          # Least-request scenario runs
│   ├── ConsistentHashService.java        # Consistent-hash scenario runs
│   │
│   ├── WorkerService.java                # Simulates backend work
│   ├── LoadGeneratorService.java         # Concurrent load generation
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

## Design and Responsibilities

### Strategies (in `strategy/`)
Implement only worker selection via `LoadBalancerStrategy.selectWorker(key, totalWorkers)`:
- **RoundRobinStrategy**: Cycles worker IDs 1..N in sequence. Exposes `reset()` to start from worker-1 for each run. Keys are ignored.
- **LeastRequestStrategy**: Selects the worker with the fewest active requests. Tracks active request counts per worker using thread-safe counters. Exposes `incrementRequestCount(workerId)`, `decrementRequestCount(workerId)`, and `resetCounters()`.
- **ConsistentHashStrategy**: Not yet implemented.

### Services (in `service/`)
All strategy services implement the `StrategyService` interface, which defines three methods: `runHeterogeneousNodes()`, `runHotKey()`, and `runPartialFailure()`.

Services own orchestration: scenario setup, per-request selection, invoking `WorkerService`, and building `vo.TestResult`:
- **RoundRobinService**: Sequential loop execution; calls `roundRobinStrategy.reset()` before each scenario.
- **LeastRequestService**: Sequential loop execution; increments/decrements active request counts around each `workerService.processRequest()` call; calls `leastRequestStrategy.resetCounters()` before each scenario.
- **ConsistentHashService**: Not yet implemented (throws `UnsupportedOperationException`).

### LoadGeneratorService
Provides a generic concurrent runner that services can use to execute requests in parallel while still delegating worker choice to a `LoadBalancerStrategy`.

### Strategy Behavior Details
- **Round Robin**: Worker IDs are 1-indexed. Service assigns requests strictly in 1..N..1 order (sequential execution). Concurrency can be introduced by using `LoadGeneratorService`.
- **Least Request**: Tracks active requests per worker. In sequential execution, this behaves similarly to round-robin since only one request is active at a time. The strategy shows its advantage when used with concurrent execution (e.g., via `LoadGeneratorService`).

## How Scenarios Are Simulated

All scenarios run **in-process** — there are no real separate servers or network calls. Workers are simulated using configuration and timing delays:

### Heterogeneous Nodes Simulation
- **Setup**: Configures 3 workers with different base latencies (50ms, 100ms, 200ms) and 0% failure rate
- **Execution**: 300 requests with unique keys ("key-1" through "key-300")
- **Worker behavior**: `WorkerService.processRequest()` uses `Thread.sleep()` to simulate processing time:
  - Base latency from configuration (e.g., 50ms for worker-1)
  - Random jitter of ±10ms added to each request (simulates real-world variance)
  - Worker-1: actual latency ranges 40-60ms
  - Worker-2: actual latency ranges 90-110ms
  - Worker-3: actual latency ranges 190-210ms
- **Goal**: Test how strategies perform when workers have different speeds

### Hot Key Simulation
- **Setup**: Configures 3 workers with uniform 100ms latency and 0% failure rate
- **Execution**: 300 requests total
  - First 240 requests (80%) use the same key: `"popular"`
  - Remaining 60 requests use random keys: `"key-1"` through `"key-100"`
- **Goal**: Test cache affinity and key-based routing (consistent hash should route all "popular" requests to the same worker; round-robin ignores keys)

### Partial Failure Simulation
- **Phase 1 (baseline)**: 100 requests, all workers at 100ms latency with 0% failure rate
- **Phase 2 (failure injection)**: 200 requests, worker-2 latency remains 100ms but failure rate set to 50%
  - On each request to worker-2, `random.nextDouble() < 0.5` determines if it throws an exception
  - Failed requests have 0ms latency (recorded as failure immediately)
- **No strategy reset between phases**: Round-robin pointer and least-request counters continue from Phase 1 to simulate mid-run failure
- **Goal**: Test resilience and failure handling (round-robin continues routing to failing worker; least-request in concurrent mode should avoid it)

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

**Note**: After making code changes, you must rebuild and restart the application for changes to take effect:
```bash
# Stop the running application (Ctrl+C if running in terminal)
mvn clean compile
mvn spring-boot:run
```

Or if using an IDE like IntelliJ IDEA:
1. Stop the running application
2. Build → Rebuild Project
3. Run the application again

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

## Example Usage

Run Round Robin scenarios:
```bash
curl "http://localhost:8080/api/round-robin/heterogeneous-nodes"
curl "http://localhost:8080/api/round-robin/hot-key"
curl "http://localhost:8080/api/round-robin/partial-failure"
```

Run Least Request scenarios:
```bash
curl "http://localhost:8080/api/least-request/heterogeneous-nodes"
curl "http://localhost:8080/api/least-request/hot-key"
curl "http://localhost:8080/api/least-request/partial-failure"
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

1. **Compare load distribution**: Measure how evenly each strategy distributes requests across workers
2. **Analyze performance under heterogeneous conditions**: Test how strategies handle workers with different latencies (50ms vs 200ms)
3. **Test cache affinity with hot keys**: Evaluate consistent hashing for routing repeated keys to the same worker vs. round-robin spreading hot keys across all workers
4. **Evaluate fault tolerance**: Measure how strategies respond to partial failures (50% error rate on one worker)
5. **Measure response time differences**: Compare latency percentiles (p50, p95, p99) and average response times across strategies and scenarios

## Expected Outcomes

### Heterogeneous Nodes
- **Round Robin**: Perfect distribution (100/100/100) but higher average latency due to slow workers
- **Least Request** (sequential): Similar to round-robin since only one request is active at a time
- **Least Request** (concurrent): Should favor faster workers, resulting in better average latency

### Hot Key
- **Round Robin**: Spreads hot key across all workers (no cache affinity); even distribution
- **Consistent Hash**: Routes all "popular" key requests to the same worker (cache affinity); uneven distribution but predictable

### Partial Failure
- **Round Robin**: Continues routing to failing worker; ~33% of requests fail (worker-2 gets 33% of traffic × 50% failure rate)
- **Least Request** (sequential): Similar to round-robin
- **Least Request** (concurrent): Should naturally avoid the failing worker as it accumulates active requests from retries/slow failures

## Implementation Notes

- **Worker simulation**: All workers run in-process; no real servers or network calls. Latency is simulated via `Thread.sleep()` with random jitter (±10ms).
- **Sequential vs concurrent execution**: Current services (`RoundRobinService`, `LeastRequestService`) execute requests sequentially. For concurrent execution, use or adapt `LoadGeneratorService`.
- **Least-request limitation**: In sequential mode, least-request behaves like round-robin since only one request is active at a time. Its advantage appears only with concurrent execution.
- **Strategy reset**: `RoundRobinStrategy.reset()` and `LeastRequestStrategy.resetCounters()` are called before each scenario run to ensure clean state.
- **Partial failure phases**: The two-phase design (baseline → failure injection) allows clear attribution of performance degradation to the failure, and simulates mid-run outages without resetting strategy state.

## License

This project is created for educational purposes as part of the Enterprise Distributed Systems (CMPE 273) course.