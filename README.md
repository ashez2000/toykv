# ToyKV

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A minimalist, toy distributed in-memory key-value store in Java 21 featuring peer-to-peer replication and TTL (Time-To-Live).

---

## Features

- **Zero External Dependencies**: Built entirely with Java 21 standard library features (`com.sun.net.httpserver`, `java.net.http.HttpClient`, and Records).
- **Peer-to-Peer Replication**: Writes (`SET`) automatically replicate across cluster peers.
- **Replication Loop Prevention**: Protects against distributed deadlocks and ping-pong forwarding loops via replication headers (`X-Replicated`).
- **Time-To-Live (TTL)**: Key expiration with passive/lazy eviction on retrieval.
- **Simple HTTP Interface**: Clean text-based command protocol over HTTP POST, easily queryable with `curl` or any HTTP client.

---

## Architecture & How It Works

### Replication Flow

When a node receives a write request from an external client:

1. It applies the write to its local store.
2. It forwards the request to all configured peers with an `X-Replicated: true` header.
3. Peer nodes apply the write locally but do not propagate it further, avoiding infinite replication loops.

```
 Client
   |
   | (POST "SET key val")
   v
[Node 8080] ----(POST with X-Replicated: true)----> [Node 8081]
     |
     +----------(POST with X-Replicated: true)----> [Node 8082]
```

### Expiration (TTL)

Items are stored with an expiration timestamp (30 seconds by default). When a `GET` operation is performed, the store verifies whether the key has expired:

- If valid, the value is returned.
- If expired, the item is removed from memory and an empty response is returned.

---

## Prerequisites

- **Java 21** or later
- **Maven 3.8+**

---

## Building

Build the executable JAR using Maven:

```bash
mvn clean package
```

The compiled JAR will be located at `target/toykv-1.0-SNAPSHOT.jar`.

---

## Running

### Single Node

```bash
java -cp target/toykv-1.0-SNAPSHOT.jar io.github.ashez2000.toykv.App 8080
```

### Multi-Node Cluster

Each node takes its port as the first argument, followed by URLs of its peer nodes.

#### Option 1: Using `start.sh` (Convenience Script)

Run a 3-node local cluster (`8080`, `8081`, `8082`):

```bash
chmod +x start.sh
./start.sh
```

#### Option 2: Running nodes manually

```bash
# Node 1
java -cp target/toykv-1.0-SNAPSHOT.jar io.github.ashez2000.toykv.App 8080 http://localhost:8081 http://localhost:8082

# Node 2
java -cp target/toykv-1.0-SNAPSHOT.jar io.github.ashez2000.toykv.App 8081 http://localhost:8080 http://localhost:8082

# Node 3
java -cp target/toykv-1.0-SNAPSHOT.jar io.github.ashez2000.toykv.App 8082 http://localhost:8080 http://localhost:8081
```

### Stopping the Cluster

```bash
pkill -f 'io.github.ashez2000.toykv.App'
```

---

## Command Reference

ToyKV accepts plain-text commands sent as the body of an HTTP POST request.

| Command | Format              | Description                                          | Response                             |
| :------ | :------------------ | :--------------------------------------------------- | :----------------------------------- |
| `PING`  | `PING`              | Health check / connectivity test                     | `PONG`                               |
| `SET`   | `SET <key> <value>` | Stores key-value pair and replicates to peers        | `OK`                                 |
| `GET`   | `GET <key>`         | Fetches value; returns empty if not found or expired | `<value>` (or empty)                 |
| `DEL`   | `DEL <key>`         | Deletes a key from the local store                   | `TRUE` if deleted, `FALSE` if absent |

---

## Usage Examples

Send commands using `curl`:

### 1. Ping a node

```bash
curl -d "PING" http://localhost:8080
# Output: PONG
```

### 2. Set a key on Node 8080

```bash
curl -d "SET user alice" http://localhost:8080
# Output: OK
```

### 3. Read the replicated key from Node 8081 or 8082

```bash
curl -d "GET user" http://localhost:8081
# Output: alice

curl -d "GET user" http://localhost:8082
# Output: alice
```

### 4. Delete a key

```bash
curl -d "DEL user" http://localhost:8080
# Output: TRUE
```

---

## License

This project is licensed under the [MIT License](LICENSE).
