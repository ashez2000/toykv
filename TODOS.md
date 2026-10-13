# TODOS

### 1. Concurrency & Core Robustness _(High Priority)_

- **Thread-Safe Store (`ConcurrentHashMap`)**:
  - _Current state_: [`Store.java`] uses a standard `HashMap`. Since `HttpServer` serves requests across threads, concurrent writes can cause race conditions or memory corruption.
  - _Fix_: Switch to `ConcurrentHashMap` and use atomic operations (e.g. `entries.remove(key, item)`).
- **Virtual Threads in `HttpServer`**:
  - _Current state_: [`App.java`]doesn't set an executor on `HttpServer`, leaving it single-threaded by default.
  - _Fix_: Add `server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());` to leverage Java 21 virtual threads for non-blocking concurrency.
- **String Parsing with Spaces**:
  - _Current state_: `body.split(" ")` breaks if values contain spaces (e.g., `SET bio John Doe`).
  - _Fix_: Split with a limit (e.g., `body.split(" ", 3)`) so values can contain arbitrary text.

---

### 2. Replication & Distributed Consistency _(Resume Highlight)_

- **Replicate `DEL` Operations**:
  - _Current state_: Only `SET` is replicated across peers. If a key is deleted on Node 8080, Node 8081 still retains it.
  - _Enhancement_: Propagate `DEL` to peers with the `X-Replicated` header just like `SET`.
- **Asynchronous Non-Blocking Replication**:
  - _Current state_: [`KvHandler.java`](file:///home/aswin/Documents/github/toykv/src/main/java/io/github/ashez2000/toykv/KvHandler.java#L57) synchronously blocks on each peer one by one using `http.send()`. If a peer is slow or down, the entire write hangs.
  - _Enhancement_: Use `http.sendAsync()` with `CompletableFuture.allOf(...)` and configure timeouts (`.timeout(Duration.ofSeconds(...))`).
- **Conflict Resolution (Last-Write-Wins / Vector Clocks)**:
  - _Problem_: If two nodes receive different writes for the same key at the same time, the cluster enters an inconsistent state.
  - _Enhancement_: Attach a timestamp or monotonic sequence number to every write (e.g., `Item(value, expiresAt, version)`). Replicas only overwrite local data if the incoming write has a newer version.
- **Quorum Reads/Writes ($W + R > N$)**:
  - Allow clients to specify write consistency (e.g. wait for acknowledgments from a majority of nodes before returning `OK`).

---

### 3. TTL & Memory Management

- **Dynamic User-Specified TTL**:
  - _Current state_: TTL is hardcoded to 30 seconds for all keys.
  - _Enhancement_: Support optional TTL in the command: `SET <key> <value> [ttl_seconds]` or add Redis-style `EXPIRE <key> <seconds>` and `TTL <key>` commands.
- **Active Background Eviction (Sweeper)**:
  - _Current state_: Eviction is purely passive (lazy eviction only happens on `GET`). Keys that are written and never read again will leak memory permanently.
  - _Enhancement_: Add a background daemon thread (`ScheduledExecutorService`) that periodically samples or sweeps expired keys from the store.
- **Memory Limits & Eviction Policies (LRU / LFU)**:
  - Enforce a maximum memory limit or maximum key count. When the store is full, evict the Least Recently Used (LRU) keys using `LinkedHashMap` or an eviction queue.

---

### 4. Storage Engine & Persistence

- **Append-Only Write-Ahead Log (WAL)**:
  - Persist operations (`SET`, `DEL`) sequentially to a disk file (`append.log`) before updating memory.
  - On server restart, replay the WAL to reconstruct state, ensuring data durability.
- **Log Compaction / Snapshotting**:
  - Prevent the WAL from growing infinitely by taking periodic in-memory snapshots and truncating older log entries.
- **Bitcask / LSM-Tree Storage**:
  - Implement an append-only disk storage engine with an in-memory key directory (like Bitcask or LevelDB).

---

### 5. Partitioning & High Availability

- **Consistent Hashing / Sharding**:
  - Instead of replicating 100% of the data to every node, partition keys across nodes using a consistent hashing ring with virtual nodes (similar to Amazon DynamoDB or Cassandra).
- **Dynamic Node Discovery & Heartbeats**:
  - Instead of static CLI arguments (`args`), implement a simple gossip protocol or heartbeat mechanism so nodes can detect when peers join or crash.

---

### 6. Protocol & API Extensions

- **Data Types Beyond Strings**:
  - Support Redis-like structures: `INCR`/`DECR` for counters, `LPUSH`/`RPOP` for queues, or `HSET`/`HGET` for hashes.
- **RESP Protocol Compatibility**:
  - Implement the Redis Serialization Protocol (RESP) over raw TCP sockets (`ServerSocket`), allowing standard tools like `redis-cli` or language Redis drivers to talk directly to ToyKV.

---

### 7. Testing & Production Readiness

- **Multi-Node Integration Tests**:
  - Write test cases using JUnit 5 that spin up 2–3 ephemeral servers on random ports, perform writes, and assert cluster replication and failover.
- **Chaos / Partition Tests**:
  - Simulate a network split or a dead peer and verify that the remaining nodes continue to function gracefully.
- **Metrics & Observability**:
  - Expose an endpoint (`/metrics` or `/stats`) tracking uptime, total keys, cache hit/miss ratio, and replication latency.
