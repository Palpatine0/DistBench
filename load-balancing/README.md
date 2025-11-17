# Load Balancing Study

> A comprehensive study of different load balancing strategies implemented in a single Spring Boot application for distributed systems research.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.6-green)
![Maven](https://img.shields.io/badge/Maven-3.6+-blue)
![Status](https://img.shields.io/badge/Status-Active%20Development-yellow)

## Overview

This project simulates multiple virtual workers in a single application to study and compare different load balancing strategies across various scenarios. The application exposes per-strategy REST endpoints (no central controller) and returns structured JSON results for each run.

**Key Highlights:**
- 🔄 **2 Working Strategies**: Round Robin and Least Request (Consistent Hash pending)
- 🧪 **3 Test Scenarios**: Heterogeneous Nodes, Hot Key, Partial Failure
- ⚡ **Concurrent Load Testing**: 100-thread pool for realistic load simulation
- 📊 **Rich Metrics**: Latency percentiles (p50, p95, p99), distribution, success rates
- 🎯 **In-Process Simulation**: No external servers needed - all workers simulated via Thread.sleep()
- 🔧 **Configurable**: YAML-based worker configuration with runtime scenario adjustments

## Features

### Load Balancing Strategies

1. **Round Robin** ✅ - Distributes requests evenly across all workers in sequence
2. **Least Request** ✅ - Routes to the worker with the fewest active requests
3. **Consistent Hash** 🚧 - Uses consistent hashing based on request keys for deterministic routing (NOT IMPLEMENTED YET)

### Test Scenarios

1. **Heterogeneous Nodes** - Workers with different latencies (50ms, 100ms, 200ms) to test how strategies handle varying worker performance
2. **Hot Key** - Simulates skewed access patterns with 80% of requests targeting the same key (tests cache affinity and key-based routing)
3. **Partial Failure** - Tests resilience when one worker (worker-2) has a 50% failure rate while others remain healthy

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
│   ├── IStrategyService.java             # Interface for strategy services
│   └── impl/
│       ├── RoundRobinServiceImpl.java    # Round-robin scenario runs
│       ├── LeastRequestServiceImpl.java  # Least-request scenario runs
│       └── ConsistentHashServiceImpl.java # Consistent-hash scenario runs
├── infrastructure/                       # Core infrastructure components
│   ├── Worker.java                       # Simulates backend workers
│   └── LoadGenerator.java                # Concurrent load generation
├── scenario/
│   ├── Scenario.java                     # Scenario interface
│   ├── HeterogeneousNodesScenario.java
│   ├── HotKeyScenario.java
│   └── PartialFailureScenario.java
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
- **ConsistentHashStrategy**: Uses consistent hashing to map request keys to workers deterministically, ensuring the same key always routes to the same worker for cache affinity.

### Services (in `service/`)
All strategy services implement the `IStrategyService` interface, which defines three methods: `runHeterogeneousNodes()`, `runHotKey()`, and `runPartialFailure()`.

Services own orchestration: scenario setup, concurrent request execution via `LoadGenerator`, and building `vo.TestResult`:
- **RoundRobinServiceImpl**: Uses `LoadGenerator` for concurrent request execution; calls `roundRobinStrategy.reset()` before each scenario.
- **LeastRequestServiceImpl**: Uses `LoadGenerator` with pre/post-request callbacks to track active request counts; increments count before processing and decrements after; calls `leastRequestStrategy.resetCounters()` before each scenario.
- **ConsistentHashServiceImpl**: Uses `LoadGenerator` for concurrent request execution with consistent hash-based worker selection.

### Infrastructure (in `infrastructure/`)
Core infrastructure components that provide foundational capabilities:
- **Worker**: Simulates individual backend worker nodes with configurable latency, jitter (±10ms), and failure rates. Uses `Thread.sleep()` to simulate processing time.
- **LoadGenerator**: Provides concurrent request execution using a thread pool (100 threads). All strategy services use this for parallel load generation while delegating worker selection to any `LoadBalancerStrategy`. Supports optional pre/post-request callbacks for strategies that need to track active requests.

### Utilities (in `util/`)
Helper utilities for test execution and result processing:
- **LoadTestUtils**: Static utility methods for calculating latency percentiles (p50, p95, p99) from sorted latency lists and converting worker ID-based counts to string-keyed distribution maps.

### Strategy Behavior Details
- **Round Robin**: Worker IDs are 1-indexed. Cycles through workers 1..N..1 in order. Uses concurrent execution via `LoadGenerator` with 100 threads.
- **Least Request**: Tracks active requests per worker using thread-safe atomic counters. With concurrent execution via `LoadGenerator`, this strategy shows its advantage by routing new requests to workers with fewer active requests, naturally avoiding slow or failing workers.
- **Consistent Hash**: Maps request keys to workers using consistent hashing. The same key always routes to the same worker, providing cache affinity benefits for hot keys.

## How Scenarios Are Simulated

All scenarios run **in-process** — there are no real separate servers or network calls. Workers are simulated using configuration and timing delays:

### Heterogeneous Nodes Simulation
- **Setup**: Configures 3 workers with different base latencies (50ms, 100ms, 200ms) and 0% failure rate
- **Execution**: 300 concurrent requests with unique keys ("key-1" through "key-300") executed via `LoadGenerator` thread pool
- **Worker behavior**: `Worker.processRequest()` uses `Thread.sleep()` to simulate processing time:
  - Base latency from configuration (e.g., 50ms for worker-1)
  - Random jitter of ±10ms added to each request (simulates real-world variance)
  - Worker-1: actual latency ranges 40-60ms
  - Worker-2: actual latency ranges 90-110ms
  - Worker-3: actual latency ranges 190-210ms
- **Goal**: Test how strategies perform when workers have different speeds under concurrent load

### Hot Key Simulation
- **Setup**: Configures 3 workers with uniform 100ms latency and 0% failure rate
- **Execution**: 300 concurrent requests total
  - First 240 requests (80%) use the same key: `"popular"`
  - Remaining 60 requests use random keys: `"key-1"` through `"key-100"`
- **Goal**: Test cache affinity and key-based routing (consistent hash should route all "popular" requests to the same worker; round-robin ignores keys and spreads them across all workers)

### Partial Failure Simulation
- **Setup**: Configures 3 workers with uniform 100ms latency but worker-2 has 50% failure rate
- **Execution**: 300 concurrent requests with unique keys ("key-1" through "key-300") executed via `LoadGenerator` thread pool
- **Worker behavior**:
  - Worker-1: 100ms latency, 0% failure rate (always succeeds)
  - Worker-2: 100ms latency, 50% failure rate (fails half the time)
  - Worker-3: 100ms latency, 0% failure rate (always succeeds)
  - On each request to worker-2, `random.nextDouble() < 0.5` determines if it throws an exception
  - Failed requests have 0ms latency (recorded as failure immediately)
- **Goal**: Test resilience and failure handling (round-robin continues routing to failing worker evenly; least-request with concurrent execution should naturally avoid it as failed requests accumulate)

## Technology Stack

- **Java**: 21
- **Spring Boot**: 3.2.6
- **Maven**: 3.6+ (for build and dependency management)
- **Spring Retry**: 1.3.1
- **Spring Boot DevTools**: For hot reload during development

## Getting Started

### Quick Start

Get the application running in 3 steps:

```bash
# 1. Build the project
mvn clean install

# 2. Run the application
mvn spring-boot:run

# 3. Test an endpoint (in a new terminal)
curl http://localhost:8081/api/round-robin/heterogeneous-nodes
```

The application will start on port 8081 and you can immediately test the load balancing strategies.

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

The application will start on `http://localhost:8081`

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
GET /api/consistent-hash/heterogeneous-nodes  # Returns UnsupportedOperationException
GET /api/consistent-hash/hot-key              # Returns UnsupportedOperationException
GET /api/consistent-hash/partial-failure      # Returns UnsupportedOperationException
```

## Example Usage

Run Round Robin scenarios:
```bash
curl "http://localhost:8081/api/round-robin/heterogeneous-nodes"
curl "http://localhost:8081/api/round-robin/hot-key"
curl "http://localhost:8081/api/round-robin/partial-failure"
```

Run Least Request scenarios:
```bash
curl "http://localhost:8081/api/least-request/heterogeneous-nodes"
curl "http://localhost:8081/api/least-request/hot-key"
curl "http://localhost:8081/api/least-request/partial-failure"
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

**Note**: Worker configurations in `application.yml` are the default/baseline values. Each scenario dynamically reconfigures workers via `WorkerConfig` at runtime before execution (e.g., `HeterogeneousNodesScenario.setup()` sets specific latencies, `PartialFailureScenario.setup()` configures failure rates).


## Study Goals

1. **Compare load distribution**: Measure how evenly each strategy distributes requests across workers
2. **Analyze performance under heterogeneous conditions**: Test how strategies handle workers with different latencies (50ms vs 200ms)
3. **Test cache affinity with hot keys**: Evaluate consistent hashing for routing repeated keys to the same worker vs. round-robin spreading hot keys across all workers
4. **Evaluate fault tolerance**: Measure how strategies respond to partial failures (50% error rate on one worker)
5. **Measure response time differences**: Compare latency percentiles (p50, p95, p99) and average response times across strategies and scenarios

## Expected Outcomes

### Heterogeneous Nodes
- **Round Robin**: Even distribution (approximately 100/100/100) but higher average latency due to slow workers getting equal share of requests
- **Least Request**: Should favor faster workers as they complete requests more quickly and have fewer active requests, resulting in better average latency and uneven distribution favoring faster workers
- **Consistent Hash**: Distribution depends on hash function; latency varies based on which worker handles most requests

### Hot Key
- **Round Robin**: Spreads hot key across all workers (no cache affinity); even distribution
- **Consistent Hash**: Routes all "popular" key requests to the same worker (cache affinity); uneven distribution but predictable

### Partial Failure
- **Round Robin**: Continues routing to failing worker evenly (approximately 100 requests to each worker); will experience approximately 50 failures (from the 100 requests sent to worker-2)
- **Least Request**: Should naturally avoid the failing worker as it completes requests slower and accumulates active requests; will route fewer requests to worker-2, resulting in fewer overall failures
- **Consistent Hash**: Failure rate depends on hash distribution; keys mapped to worker-2 will experience 50% failure rate

## Implementation Notes

### Architecture & Design Patterns

- **Strategy Pattern**: Load balancing algorithms are encapsulated in separate strategy classes (`RoundRobinStrategy`, `LeastRequestStrategy`, `ConsistentHashStrategy`) implementing the `LoadBalancerStrategy` interface. This allows runtime selection and easy addition of new strategies.
- **Dependency Injection**: Spring's `@Autowired` annotation is used throughout for loose coupling and testability.
- **Interface-based Design**: 
  - `IStrategyService` defines the contract for strategy service implementations
  - `Scenario` interface enables polymorphic scenario execution
  - `LoadBalancerStrategy` interface allows pluggable load balancing algorithms
- **Separation of Concerns**:
  - **Controllers** (`controller/`) - Handle HTTP requests and responses
  - **Services** (`service/impl/`) - Orchestrate scenario execution and build results
  - **Strategies** (`strategy/`) - Implement worker selection logic only
  - **Infrastructure** (`infrastructure/`) - Provide core capabilities (worker simulation, load generation)
  - **Scenarios** (`scenario/`) - Define test configurations and key generation
  - **Value Objects** (`vo/`) - Represent data structures for results

### Implementation Details

- **Package organization**: Strategy services are in `service/impl/`, infrastructure components in `infrastructure/`, following separation of concerns.
- **Naming convention**: Interface `IStrategyService` (with `I` prefix), implementations with `Impl` suffix (e.g., `RoundRobinServiceImpl`). Infrastructure components use simple, descriptive names (`Worker`, `LoadGenerator`).
- **Worker simulation**: All workers run in-process; no real servers or network calls. Latency is simulated via `Thread.sleep()` in `Worker` with random jitter (±10ms).
- **Worker IDs**: Workers are 1-indexed (worker-1, worker-2, worker-3) throughout the codebase, not 0-indexed.
- **Concurrent execution**: All service implementations use `LoadGenerator` for concurrent request execution with a thread pool of 100 threads. This provides realistic load testing and allows strategies like Least Request to demonstrate their benefits under concurrent load.
- **LoadGenerator callbacks**: `LoadGenerator` supports optional pre/post-request callbacks, used by `LeastRequestServiceImpl` to track active request counts via `incrementRequestCount()` and `decrementRequestCount()`.
- **Strategy reset**: `RoundRobinStrategy.reset()` and `LeastRequestStrategy.resetCounters()` are called before each scenario run to ensure clean state.
- **Partial failure handling**: Worker-2 is configured with a 50% failure rate from the start, allowing direct comparison of how strategies handle failing workers under concurrent load.
- **Thread safety**: `RoundRobinStrategy` uses `AtomicInteger` for thread-safe worker selection. `LeastRequestStrategy` uses `ConcurrentHashMap` with `AtomicInteger` values for thread-safe request count tracking.
- **Error handling**: Failed requests are caught and recorded with 0ms latency. The `TestResult` tracks both successful and failed request counts, plus per-worker failure distributions.
- **Jitter simulation**: Each worker request has ±10ms random jitter added to the base latency to simulate real-world variance.

### Spring Boot Features Used

- **@SpringBootApplication**: Main application class with component scanning
- **@RestController & @RequestMapping**: REST API endpoints
- **@Component & @Service**: Bean registration and dependency injection
- **@ConfigurationProperties**: Type-safe configuration binding for `WorkerConfig`
- **@Autowired**: Dependency injection throughout
- **Spring Boot DevTools**: Hot reload support during development
- **Actuator Endpoints**: Health, info, and metrics endpoints available

## Troubleshooting

### Common Issues

**Issue**: Port 8081 already in use
```
Error: Web server failed to start. Port 8081 was already in use.
```
**Solution**: Either stop the process using port 8081 or change the port in `application.yml`:
```yaml
server:
  port: 8082  # Use a different port
```

**Issue**: Application doesn't reflect code changes
```
Changes to Java files aren't showing up when I test
```
**Solution**: Rebuild the project before running:
```bash
mvn clean compile
mvn spring-boot:run
```

**Issue**: Consistent Hash endpoints return errors
```
UnsupportedOperationException: Not implemented yet
```
**Solution**: This is expected. Consistent Hash strategy is not implemented yet. Only Round Robin and Least Request are available.

**Issue**: Long response times on first request
```
First API call takes much longer than subsequent calls
```
**Solution**: This is normal JVM warmup and Spring context initialization. The first request initializes all beans and strategies. Subsequent requests will be faster.

### Verifying Application Status

Check if the application is running:
```bash
curl http://localhost:8081/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

Check available actuator endpoints:
```bash
curl http://localhost:8081/actuator
```

## Advanced Usage

### Analyzing Test Results

The `TestResult` JSON response contains rich metrics for analysis:

```json
{
  "scenario": "heterogeneous-nodes",
  "strategy": "least-request",
  "totalRequests": 300,
  "successfulRequests": 300,
  "failedRequests": 0,
  "durationMs": 5432,
  "distribution": {
    "worker1": 180,
    "worker2": 90,
    "worker3": 30
  },
  "latency": {
    "min": 41,
    "max": 210,
    "average": 78.5,
    "p50": 55,
    "p95": 105,
    "p99": 205
  }
}
```

**Key Metrics to Analyze:**

1. **Distribution Balance**: Compare worker request counts. Even distribution (100/100/100) vs. uneven (180/90/30) shows strategy behavior.

2. **Latency Stats**:
   - `average`: Overall performance indicator
   - `p50`: Median latency - typical user experience
   - `p95`: 95th percentile - captures slow requests
   - `p99`: 99th percentile - catches worst-case scenarios

3. **Success Rate**: `successfulRequests / totalRequests` - important for failure scenarios

4. **Duration**: Total test execution time - useful for comparing strategy overhead

### Comparing Strategies

Run both strategies for the same scenario and compare:

```bash
# Run Round Robin
curl http://localhost:8081/api/round-robin/heterogeneous-nodes > rr-hetero.json

# Run Least Request
curl http://localhost:8081/api/least-request/heterogeneous-nodes > lr-hetero.json

# Compare the results
diff rr-hetero.json lr-hetero.json
```

### Testing Partial Failure Scenario

The partial failure scenario tests how strategies handle a failing worker:

```bash
curl http://localhost:8081/api/round-robin/partial-failure | jq '.'
```

Look for:
- `totalRequests`: 300 requests total
- `successfulRequests` vs `failedRequests`: How many succeeded/failed
- `distribution`: How requests were distributed across workers
- `failuresByWorker`: Which workers experienced failures (should be mostly worker-2)

Example output structure:
```json
{
  "scenario": "partial-failure",
  "strategy": "round-robin",
  "totalRequests": 300,
  "successfulRequests": 250,
  "failedRequests": 50,
  "distribution": { "worker1": 100, "worker2": 100, "worker3": 100 },
  "failuresByWorker": { "worker2": 50 },
  "latency": { "min": 0, "max": 110, "average": 75.5, "p50": 100, "p95": 105, "p99": 108 }
}
```

### Hot Key Scenario Analysis

For hot key scenarios, check the `hotKeyDistribution` to see how strategies route the popular key:

```bash
curl http://localhost:8081/api/round-robin/hot-key | jq '.hotKeyDistribution'
```

**Round Robin** should spread "popular" evenly:
```json
{ "worker1": 80, "worker2": 80, "worker3": 80 }
```

**Least Request** may also spread evenly since all workers have the same latency:
```json
{ "worker1": 81, "worker2": 79, "worker3": 80 }
```

**Consistent Hash** (when implemented) should route all to one worker:
```json
{ "worker1": 240, "worker2": 0, "worker3": 0 }
```

## Project Metrics

- **Total Lines of Code**: ~1,500 lines (excluding tests and generated files)
- **Number of Classes**: 23
- **API Endpoints**: 9 (6 working, 3 not implemented)
- **Test Scenarios**: 3 (Heterogeneous Nodes, Hot Key, Partial Failure)
- **Load Balancing Strategies**: 3 (2 implemented, 1 pending)
- **Concurrent Threads**: 100 (LoadGenerator thread pool)
- **Simulated Workers**: 3 (configurable)

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      Spring Boot Application                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                   REST Controllers                        │  │
│  │  /api/round-robin/*  /api/least-request/*  /api/c-hash/* │  │
│  └────────────────────┬─────────────────────────────────────┘  │
│                       │                                          │
│  ┌────────────────────▼─────────────────────────────────────┐  │
│  │              Strategy Services (IStrategyService)        │  │
│  │  RoundRobinServiceImpl  LeastRequestServiceImpl  C-Hash  │  │
│  └────┬───────────────┬──────────────────┬─────────────────┘  │
│       │               │                  │                     │
│  ┌────▼───────┐  ┌───▼────────┐  ┌──────▼──────┐            │
│  │ Round Robin│  │ Least Req  │  │ Consistent  │            │
│  │  Strategy  │  │  Strategy  │  │    Hash     │            │
│  └────┬───────┘  └────┬───────┘  └──────┬──────┘            │
│       │               │                  │                     │
│       └───────────────┴──────────────────┘                     │
│                       │                                          │
│  ┌────────────────────▼─────────────────────────────────────┐  │
│  │               LoadGenerator (100 threads)                │  │
│  │           Concurrent Request Execution                   │  │
│  └────────────────────┬─────────────────────────────────────┘  │
│                       │                                          │
│  ┌────────────────────▼─────────────────────────────────────┐  │
│  │                   Worker (Simulator)                     │  │
│  │   Worker-1 (50ms)  Worker-2 (100ms)  Worker-3 (200ms)   │  │
│  │         Thread.sleep() + Jitter ± 10ms                   │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                      Scenarios                           │  │
│  │  Heterogeneous │ Hot Key │ Partial Failure              │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## Request Flow

```
1. HTTP Request → Controller (e.g., /api/round-robin/heterogeneous-nodes)
                      │
2. Controller calls → ServiceImpl.runScenario()
                      │
3. Service sets up  → Scenario.setup() (configure workers)
                      │
4. Service uses     → LoadGenerator.generateLoad(strategy, keyGen)
                      │
5. LoadGenerator    → Creates 100 concurrent threads
   │                  │
   ├─────────────────┼─────────────────┐
   │                 │                 │
   Thread 1       Thread 2         Thread 100
   │                 │                 │
   ├→ Strategy.selectWorker()          │
   │    │                              │
   ├→ Worker.processRequest()          │
   │    (Thread.sleep(latency))        │
   │                                   │
   └─────────────────┴─────────────────┘
                      │
6. Collect results  → RequestRecord(latency, workerId, success)
                      │
7. Build TestResult → Calculate stats, distribution, percentiles
                      │
8. Return JSON      ← TestResult (metrics + latency stats)
```
