# DistBench - Module Comparison

## Overview

DistBench contains two modules for studying distributed system patterns:

1. **Load-Balancing** - Studies different load balancing strategies
2. **Caching** - Studies different caching strategies

Both modules follow similar architectural patterns for consistency and reusability.

## Side-by-Side Comparison

| Aspect | Load-Balancing Module | Caching Module |
|--------|----------------------|----------------|
| **Port** | 8081 | 8082 |
| **Main Class** | `LoadBalancingStudyApplication` | `CachingStudyApplication` |
| **Package** | `com.example` | `com.example` |
| **Status** | ✅ 2/3 Strategies Working | ⏳ Structure Complete, Implementation Pending |

## Strategies

### Load-Balancing Strategies

1. **Round-Robin** ✅
   - Rotates requests evenly across nodes
   - Ignores node capacity and health

2. **Least-Request** ✅
   - Directs traffic to node with fewest active requests
   - Adapts to node performance

3. **Consistent Hashing** 🚧
   - Picks node by hash(key) for cache affinity
   - Not yet implemented

### Caching Strategies

1. **LRU with TTL** ⏳
   - Evicts least recently used items
   - Time-to-live per item with optional reset on access

2. **Request Coalescing** ⏳
   - Combines concurrent requests for same key
   - Single backend fetch, shared result

3. **Stale-While-Revalidate** ⏳
   - Serves expired items immediately
   - Asynchronously fetches fresh data

## Scenarios

### Load-Balancing Scenarios

1. **Heterogeneous Nodes**
   - Nodes with different capacities (50ms, 100ms, 200ms)
   - Tests: How strategies handle varying performance

2. **Hot Key**
   - 80% of requests target same key
   - Tests: Cache affinity and key-based routing

3. **Partial Failure**
   - One node has 50% failure rate
   - Tests: Resilience and failure handling

### Caching Scenarios

1. **Different Cache Sizes**
   - Varying capacities (50, 100, 200 items)
   - Tests: Memory constraints impact

2. **Freshness Requirements**
   - Different TTL values (1s, 5s, 10s)
   - Tests: Freshness vs. performance trade-offs

3. **Network Delays**
   - Backend latencies (50ms, 200ms, 500ms)
   - Tests: Caching benefits with slow backends

## Architecture Patterns

Both modules share the same architectural patterns:

```
┌─────────────────────────────────────────────────┐
│           Common Architecture Pattern            │
├─────────────────────────────────────────────────┤
│                                                  │
│  REST Controllers (/api/{strategy}/{scenario})  │
│                    ↓                            │
│  Service Layer (IStrategyService interface)     │
│                    ↓                            │
│  Strategy Pattern (Strategy interface)          │
│                    ↓                            │
│  Infrastructure (LoadGenerator)                 │
│                    ↓                            │
│  Backend Simulation (Worker / BackendService)   │
│                                                  │
└─────────────────────────────────────────────────┘
```

### Common Components

| Component | Load-Balancing | Caching |
|-----------|----------------|---------|
| **Strategy Interface** | `LoadBalancerStrategy` | `CacheStrategy` |
| **Service Interface** | `IStrategyService` | `IStrategyService` |
| **Backend Simulation** | `Worker` | `Worker` |
| **Load Generation** | `LoadGenerator` | `LoadGenerator` |
| **Scenario Interface** | `Scenario` | `Scenario` |
| **Result VO** | `TestResult` | `TestResult` |
| **Latency VO** | `LatencyStats` | `LatencyStats` |
| **Utility Class** | `LoadTestUtils` | `CacheTestUtils` |
| **Configuration** | `WorkerConfig` | `CacheConfig` |

## Infrastructure Components

### Load-Balancing Infrastructure

- **Worker**
  - Simulates backend server nodes
  - Configurable latency and failure rates
  - Uses `Thread.sleep()` for simulation

- **LoadGenerator**
  - 100-thread pool for concurrent requests
  - Tracks active requests per worker (for Least-Request)
  - Returns request records for analysis

### Caching Infrastructure

- **Worker**
  - Simulates backend database/service
  - Configurable latency with jitter
  - Uses `Thread.sleep()` for simulation

- **LoadGenerator**
  - 100-thread pool for concurrent requests
  - Supports different key generation patterns
  - Tracks cache hits/misses per request

## Simulation Approach

Both modules use **in-process simulation**:

| Aspect | Implementation |
|--------|---------------|
| **Network Latency** | `Thread.sleep(latencyMs + jitter)` |
| **Concurrency** | 100-thread ExecutorService pool |
| **Failures** | Random boolean based on failure rate |
| **Metrics** | Collected per request in memory |
| **Thread Safety** | AtomicInteger, ConcurrentHashMap |
| **Real Networks** | ❌ None - all in-process |
| **Real Servers** | ❌ None - simulated with delays |

## API Endpoints

### Load-Balancing (Port 8081)

```bash
# Round Robin (✅ Working)
GET /api/round-robin/heterogeneous-nodes
GET /api/round-robin/hot-key
GET /api/round-robin/partial-failure

# Least Request (✅ Working)
GET /api/least-request/heterogeneous-nodes
GET /api/least-request/hot-key
GET /api/least-request/partial-failure

# Consistent Hash (🚧 Not Implemented)
GET /api/consistent-hash/heterogeneous-nodes
GET /api/consistent-hash/hot-key
GET /api/consistent-hash/partial-failure
```

### Caching (Port 8082)

```bash
# LRU with TTL (⏳ Skeleton)
GET /api/lru-ttl/different-cache-sizes
GET /api/lru-ttl/freshness-requirements
GET /api/lru-ttl/network-delays

# Request Coalescing (⏳ Skeleton)
GET /api/request-coalescing/different-cache-sizes
GET /api/request-coalescing/freshness-requirements
GET /api/request-coalescing/network-delays

# Stale-While-Revalidate (⏳ Skeleton)
GET /api/stale-while-revalidate/different-cache-sizes
GET /api/stale-while-revalidate/freshness-requirements
GET /api/stale-while-revalidate/network-delays
```

## Response Format

Both modules return similar structured JSON:

### Load-Balancing Response

```json
{
  "scenario": "heterogeneous-nodes",
  "strategy": "round-robin",
  "totalRequests": 300,
  "successfulRequests": 300,
  "failedRequests": 0,
  "durationMs": 12345,
  "distribution": {
    "worker1": 100,
    "worker2": 100,
    "worker3": 100
  },
  "latency": {
    "min": 41,
    "max": 210,
    "average": 118.5,
    "p50": 100,
    "p95": 200,
    "p99": 205
  }
}
```

### Caching Response

```json
{
  "scenario": "network-delays",
  "strategy": "lru-ttl",
  "totalRequests": 300,
  "cacheHits": 180,
  "cacheMisses": 120,
  "backendFetches": 120,
  "durationMs": 12345,
  "cacheStats": {
    "currentSize": 100,
    "maxSize": 100,
    "evictions": 20,
    "hitRate": 60.0,
    "expiredItems": 15
  },
  "latency": {
    "min": 5,
    "max": 220,
    "average": 85.3,
    "p50": 50,
    "p95": 200,
    "p99": 210
  }
}
```

## File Structure Comparison

### Load-Balancing Module

```
load-balancing/
├── controller/           (3 files)
├── strategy/            (4 files: 3 strategies + interface)
├── service/impl/        (3 files)
├── infrastructure/      (2 files: Worker, LoadGenerator)
├── scenario/           (4 files: 3 scenarios + interface)
├── vo/                 (2 files: TestResult, LatencyStats)
├── util/               (1 file: LoadTestUtils)
└── config/             (1 file: WorkerConfig)
Total: ~23 Java files
```

### Caching Module

```
caching/
├── controller/           (3 files)
├── strategy/            (4 files: 3 strategies + interface)
├── service/impl/        (3 files)
├── infrastructure/      (2 files: BackendService, LoadGenerator)
├── scenario/           (4 files: 3 scenarios + interface)
├── vo/                 (4 files: TestResult, CacheStats, LatencyStats, RequestRecord)
├── util/               (1 file: CacheTestUtils)
└── config/             (1 file: CacheConfig)
Total: 24 Java files
```

## Metrics Collected

### Load-Balancing Metrics

- Total requests / Successful / Failed
- Worker distribution (requests per worker)
- Failure distribution (failures per worker)
- Latency stats (min, max, avg, p50, p95, p99)
- Hot key distribution (for hot-key scenario)
- Test duration

### Caching Metrics

- Total requests / Cache hits / Cache misses
- Backend fetch count
- Cache size / Max size / Evictions
- Hit rate percentage
- Expired items count
- Latency stats (min, max, avg, p50, p95, p99)
- Strategy-specific: stale hits, coalesced requests
- Test duration

## Technology Stack

Both modules use identical technology:

- **Java**: 21
- **Spring Boot**: 3.2.6
- **Maven**: 3.6+
- **Spring Retry**: 1.3.1
- **Spring Boot Actuator**: Health and metrics
- **Jackson**: JSON serialization

## Running Both Modules

```bash
# Terminal 1: Load-Balancing
cd load-balancing
mvn spring-boot:run
# Runs on http://localhost:8081

# Terminal 2: Caching
cd caching
mvn spring-boot:run
# Runs on http://localhost:8082

# Terminal 3: Test both
curl http://localhost:8081/api/round-robin/heterogeneous-nodes
curl http://localhost:8082/api/lru-ttl/network-delays
```

## Study Goals

### Load-Balancing Study Goals

1. Compare load distribution across strategies
2. Analyze performance with heterogeneous nodes
3. Test cache affinity with hot keys
4. Evaluate fault tolerance during failures
5. Measure response time differences

### Caching Study Goals

1. Compare cache hit rates across strategies
2. Analyze latency with different cache sizes
3. Test eviction policies under various patterns
4. Measure request coalescing benefits
5. Evaluate stale-while-revalidate latency improvements
6. Understand memory vs. freshness trade-offs

## Design Philosophy

Both modules follow these principles:

✅ **In-process simulation** - No Docker, no network, easy to run
✅ **Realistic concurrency** - 100 threads create real contention
✅ **Strategy pattern** - Pluggable algorithms
✅ **Separation of concerns** - Clean layered architecture
✅ **Comprehensive metrics** - Rich data for analysis
✅ **REST API** - Easy to test and compare
✅ **Scenario-based** - Controlled test environments
✅ **Thread-safe** - Proper concurrent data structures

## Differences

| Aspect | Load-Balancing | Caching |
|--------|----------------|---------|
| **Focus** | Request routing | Data storage & retrieval |
| **Key Metric** | Request distribution | Cache hit rate |
| **Backend** | Multiple workers | Single backend service |
| **State** | Stateless (request counts) | Stateful (cached data) |
| **Complexity** | Worker selection logic | Eviction + TTL + async logic |
| **Special VO** | - | `CacheStats`, `RequestRecord` |

## Summary

- **Load-Balancing**: 6/9 endpoints working (66%)
- **Caching**: 0/9 endpoints working (0%) - structure complete, ready for implementation

Both modules demonstrate distributed system patterns through in-process simulation, making them excellent teaching and research tools without the complexity of real distributed infrastructure.

---

**Architecture**: Consistent patterns across both modules for easy learning
**Technology**: Identical stack for easy maintenance
**Purpose**: Study and compare distributed system strategies

