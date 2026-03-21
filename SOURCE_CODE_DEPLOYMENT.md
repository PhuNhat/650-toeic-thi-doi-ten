# SOURCE CODE STRUCTURE & DEPLOYMENT OVERVIEW
## (Repository Structure + Deployment Flow)

---

## PHẦN 1: SOURCE CODE STRUCTURE

### GitHub Repository Layout

```
DistributedTokenRing/
│
├── src/                              ← Source code (Java files)
│   ├── server/                       ← 11 server classes
│   │   ├── Main.java                 (Entry point - reads env vars)
│   │   ├── TokenRing.java            (Token passing logic)
│   │   ├── NodeHandler.java          (Client request handler)
│   │   ├── Database.java             (MySQL JDBC connection)
│   │   ├── RoutingTable.java         (Parse PEERS, find next node)
│   │   ├── VirtualCircle.java        (Ring node data)
│   │   ├── MessageProcess.java       (Parse @$...$@ format messages)
│   │   ├── ProcessData.java          (Extract id|content|timestamp|status)
│   │   ├── Connect.java              (Socket inter-node communication)
│   │   ├── GetState.java             (Read/write state files)
│   │   └── InitialServer.java        (Initialize topology files)
│   │
│   └── client/                       ← 1 universal client
│       └── Client.java               (GUI - select nodes, send INSERT/DELETE/QUERY)
│
├── build/                            ← Compiled .class files
│   ├── server/
│   │   ├── Main.class
│   │   ├── TokenRing.class
│   │   ├── NodeHandler.class
│   │   ├── Database.class
│   │   ├── RoutingTable.class
│   │   ├── VirtualCircle.class
│   │   ├── MessageProcess.class
│   │   ├── ProcessData.class
│   │   ├── Connect.class
│   │   ├── GetState.class
│   │   └── InitialServer.class
│   │
│   └── client/
│       └── Client.class
│
├── setup.sql                         ← Database schema (6 DBs, 6 tables)
├── config.properties                 ← Config template
├── railway.toml                      ← Railway deployment config
├── README.md                         ← Project overview
├── DEPLOYMENT_GUIDE.md               ← Full guide (with local testing)
├── RAILWAY_DEPLOYMENT_DIRECT.md      ← This guide (Railway-only)
├── INDIVIDUAL_DEPLOYMENT_CHECKLIST.md ← For each person
├── PM_RAILWAY_DIRECT.md              ← For project manager
├── DEPLOYMENT_CONFIG.json            ← Configuration reference
├── QUICK_REFERENCE.md                ← One-page cheat sheet
└── .gitignore                        ← Git ignore patterns
```

---

## PHẦN 2: SOURCE CODE MODULES EXPLAINED

### Core Server Classes

#### `Main.java`
```
Purpose: Entry point
Input: Environment variables (NODE_ID, PORT, MYSQL_URL, PEERS)
Output: Running server on PORT listening for clients
Logic:
  1. Read env vars
  2. Create Database connection
  3. Initialize RoutingTable (parse PEERS)
  4. Initialize TokenRing (Node1 has token)
  5. Start NodeHandler server
  6. Listen for client connections
```

#### `TokenRing.java`
```
Purpose: Manage token passing and Lamport clock
Core Methods:
  - receiveToken() - when token arrives from prev node
  - processRequest() - handle client request (if have token)
  - passToken() - send token to next node
  - updateLamportClock() - increment on each message
Data: AtomicInteger(lamportClock), boolean(hasToken)
Flow: Token → Process → Pass → Next Node
```

#### `NodeHandler.java`
```
Purpose: Accept client connections and route requests
Input: Client socket connections
Routes:
  - INSERT| → TokenRing.processRequest() → Database.insertData()
  - DELETE| → TokenRing.processRequest() → Database.delData()
  - QUERY → TokenRing.processRequest() → Database.getAllData()
  - TOKEN| → TokenRing.receiveToken() → pass to PEERS
Output: Response back to client
```

#### `Database.java`
```
Purpose: MySQL connection and CRUD
Connection: MYSQL_URL env var (jdbc:mysql://...)
Table: server[NODE_ID] in db[NODE_ID]
Schema: id|content|timestamp|status
Methods:
  - insertData(int id, String content)
  - delData(int id)
  - getAllData() returns List<Data>
  - queryData(int id)
```

#### `RoutingTable.java`
```
Purpose: Parse PEERS and maintain ring topology
Input: PEERS env var (comma-separated URLs)
Format: https://node1-xxx:8080,https://node2-xxx:8080,...
Output: Array of VirtualCircle (6 nodes)
Usage: TokenRing uses this to know where to send token
```

#### `VirtualCircle.java`
```
Purpose: Data structure for single node in ring
Fields:
  - int nodeId (1-6)
  - String destination (URL)
  - int port (8080)
  - String name (Server1-Server6)
```

#### `MessageProcess.java`
```
Purpose: Parse inter-node messages
Format: @$nodeId|token|lamportClock|serverName|action|data$@
Example: @$1|100000|10|Server1|INSERT|id=1|content=hello$@
Output: Parsed fields (nodeId, token, clock, action, data)
```

#### `ProcessData.java`
```
Purpose: Extract data fields from messages
Format: id|content|timestamp|status
Methods:
  - parseData(String dataStr)
  - getters: getId(), getContent(), getTimestamp(), getStatus()
```

#### `Connect.java`
```
Purpose: Socket-based inter-node communication
Method: sendMessage(String host, int port, String message)
Flow: Create Socket → Send message → Close
Used by: TokenRing to send token to next node
```

#### `GetState.java`
```
Purpose: Read/write node state to disk
Files: Server[X]state.txt (token ownership)
Methods:
  - getCurrentState()
  - setCurrentState(int state)
  - getTokenState() - binary representation
```

#### `InitialServer.java`
```
Purpose: Initialize node topology at startup
Methods:
  - initializeCircle() - write ServerXcircle.txt
  - initializeDatabase() - create table schema
  - initializePeers() - load routing info
Called by: Main.java at startup
```

### Client Class

#### `Client.java`
```
Purpose: Universal test client (GUI)
Features:
  - Dropdown: Select any Node (1-6)
  - Input fields: NODE_URL, ID, Content
  - Buttons: INSERT, DELETE, QUERY
  - Send to selected node via HTTP/Socket
  - Display results/errors
Usage: javac + java -cp build client.Client
```

---

## PHẦN 3: TOKEN RING ALGORITHM

### Flow Diagram

```
┌─────────────────────────────────────────────────────┐
│                    NODE RING TOPOLOGY                │
└─────────────────────────────────────────────────────┘

         Client sends request
                 │
                 ▼
         ┌────────────────┐
         │    Node 1      │ ◄────┐ (has initial jeton)
         │   Server1      │      │
         └────────────────┘      │
                 │               │
                 │ Pass jeton    │ Receive
                 ▼               │
         ┌────────────────┐      │
         │    Node 2      │      │
         │   Server2      │      │
         └────────────────┘      │
                 │               │
                 │ Pass jeton    │ Receive
                 ▼               │
         ┌────────────────┐      │
         │    Node 3      │      │
         │   Server3      │      │
         └────────────────┘      │
                 │               │
                 │ Pass jeton    │ Receive
                 ▼               │
         ┌────────────────┐      │
         │    Node 4      │      │
         │   Server4      │      │
         └────────────────┘      │
                 │               │
                 │ Pass jeton    │ Receive
                 ▼               │
         ┌────────────────┐      │
         │    Node 5      │      │
         │   Server5      │      │
         └────────────────┘      │
                 │               │
                 │ Pass jeton    │ Receive
                 ▼               │
         ┌────────────────┐      │
         │    Node 6      │      │
         │   Server6      │      │
         └────────────────┘      │
                 │               │
                 │ Pass jeton────┘ Receive
                 │ (back to Node 1)
                 ▼
         [Cycle repeats every 6 nodes]
```

### Token Processing Logic

```
1. NODE RECEIVES TOKEN
   ├─ Set hasToken = true
   ├─ Lamport clock += 1
   └─ Listen for client requests

2. CLIENT SENDS REQUEST (while node has token)
   ├─ NodeHandler receives request
   ├─ TokenRing processes:
   │   ├─ INSERT: Database.insertData()
   │   ├─ DELETE: Database.delData()
   │   └─ QUERY: Database.getAllData()
   ├─ Update Lamport clock += 1
   └─ Send response to client

3. PASS TOKEN TO NEXT NODE
   ├─ Create message: @$nodeId|token|clock|...$@
   ├─ Connect.sendMessage() to next node
   ├─ Next node receives
   └─ hasToken = false (current node)

4. NEXT NODE RECEIVES TOKEN
   └─ Repeat from step 1
```

### Lamport Clock

```
Maintains causal ordering:

Event 1: Node1 receives token → Clock = 1
Event 2: Node1 processes INSERT → Clock = 2
Event 3: Node1 passes token → Clock = 3
Event 4: Node2 receives token, Local = 1, Received = 3 → Max = 4
Event 5: Node2 processes request → Clock = 5
...
Guarantees: clock never decreases, causal ordering maintained
```

---

## PHẦN 4: DEPLOYMENT ARCHITECTURE

### Local Structure (Before Railway)

```
Developer's Machine
├── forked repo
│   ├── src/ (source code)
│   ├── build/ (compiled classes)
│   └── ...
└── Railway (deployed)
    ├── Node X server (running on :8080)
    ├── MySQL database (db[X])
    └── Logs (viewable in Railway portal)
```

### Production Architecture (Railway)

```
┌─────────────────────────────────────────────────────┐
│                    RAILWAY NETWORK                   │
└─────────────────────────────────────────────────────┘

┌──────────────────┐    ┌──────────────────┐
│  Node1 Railway   │    │  Node1 MySQL     │
│  Container       │───▶│  Database: db1   │
│  PORT: 8080      │    │  TABLE: server1  │
└──────────────────┘    └──────────────────┘
        ▲                        ▲
        │ Token Pass             │ Store data
        │ (via PEERS)            │
        ▼                        │
┌──────────────────┐    ┌──────────────────┐
│  Node2 Railway   │    │  Node2 MySQL     │
│  Container       │───▶│  Database: db2   │
│  PORT: 8080      │    │  TABLE: server2  │
└──────────────────┘    └──────────────────┘
        ▲                        ▲
        │ Token Pass             │ Store data
        │                        │
        ▼                        │
    ... Node3-6 ...

┌──────────────────────────────────────────┐
│         Client (Your Machine)            │
│      java -cp build client.Client        │
│      Select Node 1-6, Send Request       │
└──────────────────────────────────────────┘
```

### Network Flow

```
CLIENT REQUEST (from your machine)
    │
    ▼
RAILWAY NODE 1 (https://node1-xxx.up.railway.app:8080)
    ├─ Receive client request
    ├─ NodeHandler parses
    ├─ Check: do I have token?
    │   ├─ YES: Process INSERT/DELETE/QUERY
    │   │   ├─ Database.insertData() → db1
    │   │   └─ Lamport clock += 1
    │   └─ NO: Respond "Wait, token with Node X"
    │
    ├─ Create message: @$...$@
    ├─ Send via PEERS to Node 2
    │
    └─ Pass token to RAILWAY NODE 2

RAILWAY NODE 2 (https://node2-xxx.up.railway.app:8080)
    ├─ Receive token via Connect.receiveToken()
    ├─ hasToken = true
    ├─ Lamport clock += 1
    │
    └─ Wait for next client request or pass token

[... repeats through Node 3-6 ...]

RAILWAY NODE 6 (https://node6-xxx.up.railway.app:8080)
    └─ Pass token back to Node 1 (ring complete)
```

---

## PHẦN 5: ENVIRONMENT VARIABLES - FULL REFERENCE

### Set in Railway Dashboard

```
NODE_ID
├─ Type: Integer
├─ Values: 1-6 (must be unique)
├─ Example: 1
└─ Used in: Main.java, RoutingTable, Database

PORT
├─ Type: Integer
├─ Value: 8080 (always)
├─ Used in: Railway auto-assigns port
└─ Override: java -D PORT=8080

MYSQL_DATABASE
├─ Type: String
├─ Values: db1, db2, db3, db4, db5, db6
├─ Must match: NODE_ID (Node 1 → db1, etc)
└─ Used in: Database.java connection string

MYSQL_URL
├─ Type: Connection String
├─ Format: jdbc:mysql://[HOST]:[PORT]/[DB]
├─ Example: jdbc:mysql://mysql.railway.internal:3306/db1
├─ Railway MySQL plugin auto-generates parts
└─ Used in: Database.java for JDBC connection

PEERS
├─ Type: Comma-separated URLs
├─ Format: url:port,url:port,url:port,...
├─ Example: node1.up.railway.app:8080,node2.up.railway.app:8080,...
├─ Updated by: PM after all 6 nodes deployed
└─ Used in: RoutingTable.java to parse ring topology
```

### Complete Example - Node 1

```
NODE_ID = 1
PORT = 8080
MYSQL_DATABASE = db1
MYSQL_URL = jdbc:mysql://mysql.railway.internal:3306/db1
PEERS = https://node1-production-xxxxx.up.railway.app:8080,https://node2-production-xxxxx.up.railway.app:8080,https://node3-production-xxxxx.up.railway.app:8080,https://node4-production-xxxxx.up.railway.app:8080,https://node5-production-xxxxx.up.railway.app:8080,https://node6-production-xxxxx.up.railway.app:8080
```

### Complete Example - Node 2

```
NODE_ID = 2
PORT = 8080
MYSQL_DATABASE = db2
MYSQL_URL = jdbc:mysql://mysql.railway.internal:3306/db2
PEERS = https://node1-production-xxxxx.up.railway.app:8080,https://node2-production-xxxxx.up.railway.app:8080,https://node3-production-xxxxx.up.railway.app:8080,https://node4-production-xxxxx.up.railway.app:8080,https://node5-production-xxxxx.up.railway.app:8080,https://node6-production-xxxxx.up.railway.app:8080
```

(Same PEERS for all 6 nodes! Only NODE_ID and MYSQL_DATABASE differ)

---

## PHẦN 6: DATABASE SCHEMA

### 6 Databases, 6 Tables

```sql
-- Run by each node or Railway auto-execute setup.sql

CREATE DATABASE db1;
USE db1;
CREATE TABLE server1 (
    id INT PRIMARY KEY,
    content VARCHAR(255) NOT NULL,
    timestamp BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE DATABASE db2;
USE db2;
CREATE TABLE server2 (
    id INT PRIMARY KEY,
    content VARCHAR(255) NOT NULL,
    timestamp BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL
);

... (db3, db4, db5, db6 similarly)
```

### Data Flow

```
Client sends: INSERT id=1, content="Hello"
                ↓
Node 1 receives request (has token)
                ↓
TokenRing processes request
                ↓
Database.insertData(1, "Hello")
                ↓
INSERT INTO db1.server1 VALUES (1, 'Hello', <timestamp>, 'SUCCESS')
                ↓
Response: "Data inserted successfully"
```

---

## PHẦN 7: COMPILATION & EXECUTION

### Compile (Already Done in build/)

```bash
javac -cp . -d build src/server/*.java src/client/*.java
```

**Result: All .class files in build/server/ and build/client/**

### Execute - Server (in Railway)

```bash
# Railway auto-runs this based on railway.toml
NODE_ID=1 PORT=8080 MYSQL_URL=... java -cp build server.Main

# Output:
# [INIT] Node 1 initializing...
# [SERVER] Server started on port 8080
# [READY] Waiting for token...
```

### Execute - Client (from your machine)

```bash
java -cp build client.Client

# Output: GUI window with dropdowns and buttons
```

---

## PHẦN 8: FILE PURPOSES AT A GLANCE

| File | Purpose | Modified By | When |
|------|---------|---|---|
| src/server/*.java | Core logic | Dev team | Before deployment |
| src/client/*.java | Test client | Dev team | Before deployment |
| build/ | Compiled classes | build process | Auto during git push |
| setup.sql | DB schema | Railway (auto) | On first deploy |
| config.properties | Config template | Reference only | - |
| railway.toml | Railway config | Auto | - |
| RAILWAY_DEPLOYMENT_DIRECT.md | **Deployment guide** | **Read this** | **Before deploying** |
| INDIVIDUAL_DEPLOYMENT_CHECKLIST.md | **Your checklist** | **Follow this** | **While deploying** |
| PM_RAILWAY_DIRECT.md | **PM workflow** | **PM reads** | **For coordination** |

---

## PHẦN 9: QUICK EXECUTION REFERENCE

### What Happens When PM Sends INSERT

```
TIME: T+0s
Client clicks INSERT (Node 1)
→ Request sent to Node 1 Railway

TIME: T+0.5s
Node 1 receives request
→ TokenRing checks: hasToken? YES
→ Database.insertData()
→ Data stored in db1.server1
→ Response: "Success"

TIME: T+1s
Node 1: Lamport clock += 1
→ Creates message: @$1|100000|11|Server1|...$@
→ Sends to Node 2

TIME: T+1.5s
Node 2 receives token
→ hasToken = true
→ Lamport clock = max(1, 11) + 1 = 12

... pattern repeats ...

TIME: T+6s
Node 6 receives token
→ Passes back to Node 1
→ Cycle complete!

TOKEN CYCLE TIME: ~6 seconds for all 6 nodes
```

---

**Version:** 1.0  
**Complete Reference:** All source code + deployment architecture  
**Ready for:** Direct Railway deployment (3-4 days)

🚀 **Let's build the Token Ring system!**
