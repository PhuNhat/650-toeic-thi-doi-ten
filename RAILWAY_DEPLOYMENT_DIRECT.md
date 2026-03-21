# TRIỂN KHAI TRỰC TIẾP RAILWAY - Token Ring 6 Servers
## (Direct Railway Deployment - Full Source Code to Production)

**Thời gian:** 3-4 ngày (skip local testing)  
**Mục đích:** Deploy trực tiếp lên Railway, không test local  
**Kết quả:** 6 servers chạy trên Railway, kết nối được với nhau

---

## BƯỚC 0: CẤU TRÚC SOURCE CODE CHÍNH

### Repository chính (PM tạo 1 lần):
```
DistributedTokenRing/
├── src/
│   ├── client/
│   │   └── Client.java (Universal client GUI)
│   └── server/
│       ├── Main.java (Entry point)
│       ├── TokenRing.java (Token logic)
│       ├── NodeHandler.java (Request handler)
│       ├── Database.java (MySQL)
│       ├── RoutingTable.java (Peer discovery)
│       ├── VirtualCircle.java (Ring node)
│       ├── MessageProcess.java (Message parser)
│       ├── ProcessData.java (Data extract)
│       ├── Connect.java (Inter-node comm)
│       ├── GetState.java (State management)
│       └── InitialServer.java (Init logic)
├── build/
│   ├── server/ (compiled .class files)
│   └── client/ (compiled .class files)
├── setup.sql (MySQL schema)
├── config.properties (Template)
├── railway.toml (Railway config)
├── README.md
├── DEPLOYMENT_GUIDE.md (this file)
└── DEPLOYMENT_CONFIG.json (Config reference)
```

**Toàn bộ files đã sẵn sàng để fork và deploy!**

---

## PHẦN 1: GITHUB MAIN REPO (Người PM - Thực hiện 1 lần)

### Bước 1.1: Tạo Main Repository

```bash
# 1. Trên GitHub, tạo repo mới
# Repository name: DistributedTokenRing
# Public (để mọi người fork được)

# 2. Clone về máy
git clone https://github.com/YOUR_USERNAME/DistributedTokenRing.git
cd DistributedTokenRing

# 3. Copy toàn bộ source code từ BaiDoXe/DistributedSystemProject
# Vào build folder, có toàn bộ .class files compiled sẵn
# Vào src folder, có toàn bộ .java sources sẵn

# 4. Push code lên GitHub
git add .
git commit -m "[SETUP] Token Ring - 6 servers ready to deploy"
git push origin main
```

**✓ Main repo ready - all sources compiled and ready!**

---

## PHẦN 2: VỊ TRÍ GIAO VIỆC CỦA MỖI NGƯỜI

**PM giao như sau:**

| Node | Người | GitHub Username | Nhiệm vụ |
|------|--------|---|---|
| 1 | (Tên) | user1 | Fork + Deploy Node 1 |
| 2 | (Tên) | user2 | Fork + Deploy Node 2 |
| 3 | (Tên) | user3 | Fork + Deploy Node 3 |
| 4 | (Tên) | user4 | Fork + Deploy Node 4 |
| 5 | (Tên) | user5 | Fork + Deploy Node 5 |
| 6 | (Tên) | user6 | Fork + Deploy Node 6 |

---

## PHẦN 3: QUY TRÌNH TRIỂN KHAI CỦA MỖI NGƯỜI (3-4 ngày)

### Ngày 1: Fork & Clone

**Mỗi người**

```bash
# 1. Trên GitHub, vào: https://github.com/PM_USERNAME/DistributedTokenRing
# Click Fork (góc phải trên)
# → Sẽ được: https://github.com/YOUR_USERNAME/DistributedTokenRing

# 2. Clone fork về máy
git clone https://github.com/YOUR_USERNAME/DistributedTokenRing.git
cd DistributedTokenRing

# Verify
ls -la
# Nên thấy:
# src/          (toàn bộ source code)
# build/        (compiled .class files)
# setup.sql     (MySQL schema)
# railway.toml  (Railway config)
```

**✓ Mỗi người đã có source code cài sẵn**

---

### Ngày 1-2: Tạo Railway Account & Project

#### Bước 1: Tạo Railway Account

```
1. Vào https://railway.app
2. Click "Sign Up"
3. Chọn "Continue with GitHub"
4. Authorize Railway app
5. Verify email
6. ✓ Done
```

#### Bước 2: Tạo Railway Project từ Fork của Bạn

```
1. Vào Railway Dashboard
2. Click "Create a new project"
3. Chọn "Deploy from GitHub repo"
4. Chọn YOUR fork: DistributedTokenRing
5. Xác nhận deploy
6. Đợi Railway tạo project (1-2 min)
```

**✓ Railway project created, building code...**

---

### Ngày 2: Thêm MySQL Plugin & Cấu Hình

#### Bước 1: Thêm MySQL Plugin

```
Trong Railway project:
1. Tab "Plugins" (hoặc bấm + icon)
2. Chọn "MySQL"
3. Đợi MySQL initialize (1-2 phút)
4. Railway sẽ tự động tạo:
   - MYSQL_HOST
   - MYSQL_PORT
   - MYSQL_USER
   - MYSQL_PASSWORD
```

#### Bước 2: Tạo Database & Table

**IMPORTANT:** Railway MySQL sẽ tự tạo database, nhưng bạn cần tạo table.

Lấy connection info từ Railway MySQL plugin, rồi chạy SQL:

```bash
# Lấy MySQL URL từ Railway (MySQL plugin section)
# Format: mysql://user:password@host:port

# Kết nối từ máy của bạn (optional, hoặc thực hiện trong app startup)
# hoặc Railway sẽ auto-execute setup.sql nếu có
```

**Hoặc: Để Main.java tự tạo table startup**

Sửa Main.java để auto-create table:
```java
// Trong Main.java, thêm vào đầu
InitialServer init = new InitialServer(nodeId, 6);
init.initializeDatabase();  // Auto create table
```

**✓ MySQL configured**

---

### Bước 3: Set Environment Variables

**Trong Railway Project:**

Vào tab **Variables**, thêm:

```
NODE_ID = 1 (for Node 1 person)
        = 2 (for Node 2 person)
        = ... (etc)
        = 6 (for Node 6 person)

PORT = 8080 (all same)

MYSQL_DATABASE = db1 (for Node 1)
               = db2 (for Node 2)
               = ... (etc)

MYSQL_URL = (Railway auto-fills từ MySQL plugin)
            = jdbc:mysql://[MYSQL_HOST]:[MYSQL_PORT]/[MYSQL_DATABASE]

PEERS = (PM sẽ gửi sau khi toàn bộ deploy)
        = (để trống lúc này)
```

**Ví dụ cho Node 1:**
```
NODE_ID = 1
PORT = 8080
MYSQL_DATABASE = db1
MYSQL_URL = jdbc:mysql://mysql.railway.internal:3306/db1
PEERS = (empty for now)
```

**✓ Environment variables set**

---

### Ngày 2-3: Deploy Railway

#### Bước 1: Trigger Deploy

```
Trong Railway project:
1. Tab "Deployments"
2. Bấm "Deploy Now"
3. Đợi build hoàn tất (3-5 phút)
4. Nên thấy output:
   - Compiling Java files...
   - Building project...
   - ✓ Success
```

#### Bước 2: Kiểm tra Logs

```
Tab "Logs" → Nên thấy:
[NODE_1] Server started on port 8080
Node 1 initialized
Connected to RoutingTable...
Waiting for token...
```

**Nếu error**, kiểm tra:
- Environment variables đúng không
- MYSQL_DATABASE trùng với NODE_ID (db1 for Node1, etc)
- MYSQL_URL format đúng không

#### Bước 3: Lấy Railway URL

```
Trong Railway project:
Tab "Settings" → tìm "Public Networking" hoặc "Railway URL"

Sẽ thấy: https://node1-production-xxxx.up.railway.app

Sao chép lại → gửi cho PM
```

**Format gửi PM:**
```
Node 1: https://node1-production-xxxx.up.railway.app:8080
Node 2: https://node2-production-xxxx.up.railway.app:8080
... etc
```

**✓ Node deployed và có Railway URL**

---

## PHẦN 4: PM - HOÀN THIỆN VIỆC INTEGRATE (Khi 6 node đã online)

### Bước 1: Thu Thập 6 Railway URLs

Từ 6 người, PM nhận:
```
Node 1: https://node1-xxx.up.railway.app
Node 2: https://node2-xxx.up.railway.app
Node 3: https://node3-xxx.up.railway.app
Node 4: https://node4-xxx.up.railway.app
Node 5: https://node5-xxx.up.railway.app
Node 6: https://node6-xxx.up.railway.app
```

### Bước 2: Tạo PEERS String

```
PEERS = node1-xxx.up.railway.app:8080,node2-xxx.up.railway.app:8080,node3-xxx.up.railway.app:8080,node4-xxx.up.railway.app:8080,node5-xxx.up.railway.app:8080,node6-xxx.up.railway.app:8080
```

### Bước 3: Update PEERS ở Toàn Bộ 6 Nodes

**Với mỗi node:**

```
1. Vào Railway project
2. Tab "Variables"
3. Find PEERS variable (hoặc create mới)
4. Paste PEERS string từ bước 2
5. Save
6. Railway tự động redeploy (xem Deployments tab)
7. Đợi green ✓ Success
```

**Repeat cho 6 nodes**

### Bước 4: Verify Connection

After all 6 redeploy, kiểm tra logs:

```
Mỗi node nên thấy log:
[NODE_1] Connected to Node 2: https://node2-xxx
[NODE_2] Connected to Node 3: https://node3-xxx
... etc
[NODE_6] Connected to Node 1: https://node1-xxx

Không được thấy: "Connection refused" hoặc "Unknown host"
```

**✓ Toàn bộ 6 nodes kết nối được với nhau**

---

## PHẦN 5: KIỂM TRA HỆ THỐNG (Sau khi toàn bộ deploy)

### Bước 1: Test Token Passing

**PM hoặc ai đó chạy client từ máy:**

```bash
cd DistributedTokenRing/build
java -cp . client.Client
```

**Trong Client GUI:**
1. Click "Connect to Node 1": `https://node1-xxx.up.railway.app:8080`
2. Nhập: ID = 1, Content = "Test"
3. Click "INSERT"
4. Xem kết quả: nên thấy "Success"

### Bước 2: Monitor Token Flow

**Mỗi person check logs của node của mình:**

```
Railway Project Tab → "Logs"

Nên thấy:
[TOKEN] Received token from Node X
[PROCESS] Processing INSERT request
[DATA] Storing to db1 table server1
[TOKEN] Passing token to Node X+1
```

**Token nên di chuyển:**
Node1 → Node2 → Node3 → Node4 → Node5 → Node6 → Node1

### Bước 3: Verify Data Storage

**Check database của Node 1:**

```
INSERT command lưu data vào db1.server1

Query từ Node 1:
SELECT * FROM server1;

Nên thấy:
| id | content | timestamp | status  |
|----|---------|-----------|---------|
| 1  | Test    | 1234567   | SUCCESS |
```

### Bước 4: Long-Running Test (5 phút)

```
Gửi 10-20 INSERT commands từ client
Quan sát:
- Token thường xuyên chuyển đổi qua 6 nodes
- Không có error
- Data lưu vào databases tương ứng
- Lamport clock tăng liên tục
```

**✓ Hệ thống hoạt động bình thường**

---

## PHẦN 6: QUICK REFERENCE - CHO MỖI NGƯỜI

### Node Assignment

```
Ghi nhớ:
- NODE_ID: 1-6 (unique per person)
- LOCAL: Node 1 has initial token (HAS_TOKEN=true)
- Others: HAS_TOKEN=false
- NEXT_NODE: 1→2, 2→3, ... 6→1
- PREV_NODE: 1←6, 2←1, ... 6←5
```

### Environment Variables

```
Phải set đúng trong Railway Variables:

NODE_ID = [1-6, unique]
PORT = 8080 (all same)
MYSQL_DATABASE = db[1-6] (match NODE_ID)
MYSQL_URL = jdbc:mysql://[HOST]:3306/db[NODE_ID]
PEERS = (PM gửi sau)

Ví dụ Node 1:
NODE_ID = 1
MYSQL_DATABASE = db1
MYSQL_URL = jdbc:mysql://mysql.railway.internal:3306/db1

Ví dụ Node 2:
NODE_ID = 2
MYSQL_DATABASE = db2
MYSQL_URL = jdbc:mysql://mysql.railway.internal:3306/db2
```

### Troubleshooting

| Problem | Solution |
|---------|----------|
| Build fails | Check Railway logs, verify NODE_ID is unique, re-trigger deploy |
| Can't connect to MySQL | Verify MYSQL_DATABASE matches NODE_ID, check MYSQL_URL format |
| PEERS error | Wait for all 6 to deploy, then PM updates PEERS |
| No logs showing | Railway app might not be running, check Deployments tab |
| Token stuck | Check if all 6 nodes deployed, verify PEERS format |

---

## PHẦN 7: FILES CẦN BIẾT

**Trong repo:**

1. **src/server/Main.java** - Entry point
   - Reads: NODE_ID, PORT, MYSQL_URL, PEERS from env vars
   - Starts server on PORT
   - Initializes TokenRing

2. **src/server/TokenRing.java** - Token logic
   - Manages token passing
   - Updates Lamport clock
   - Processes requests when holding token

3. **src/server/RoutingTable.java** - Peer routing
   - Parses PEERS env var
   - Stores routes to all 6 nodes

4. **src/server/Database.java** - MySQL connection
   - Uses MYSQL_URL env var
   - CRUD operations on data

5. **src/client/Client.java** - Universal test client
   - Select any node
   - Send INSERT/DELETE/QUERY

6. **setup.sql** - Database schema
   - Creates 6 databases (db1-db6)
   - Creates 6 tables (server1-server6)

---

## PHẦN 8: TIMELINE

```
Ngày 1:
- PM: Tạo main repo, push source code
- Mỗi người: Fork repo

Ngày 1-2:
- Mỗi người: Tạo Railway account, create project, add MySQL

Ngày 2:
- Mỗi người: Set env vars, deploy Railway

Ngày 2-3:
- Mỗi người: Lấy Railway URL, gửi cho PM

Ngày 3:
- PM: Thu thập 6 URLs, tạo PEERS string
- PM: Update PEERS ở toàn bộ 6 nodes
- Toàn bộ 6 redeploy

Day 3-4:
- Mỗi người: Verify logs, kiểm tra connection
- PM: Run client, test INSERT/DELETE/QUERY
- Verify token passing, data storage

✓ DONE - 6 nodes running on Railway!
```

---

## PHẦN 9: SUCCESS CRITERIA

✓ Dự án hoàn thành khi:

- [ ] Mỗi người có Railway project deployed
- [ ] Toàn bộ 6 MYSQL_DATABASE đã tạo (db1-db6)
- [ ] Toàn bộ 6 tables đã tạo (server1-server6)
- [ ] PEERS variable update xong trên toàn bộ 6 nodes
- [ ] Logs thấy token passing: 1→2→3→4→5→6→1
- [ ] Client có thể INSERT/DELETE/QUERY
- [ ] Data lưu vào databases tương ứng
- [ ] Lamport clock tăng liên tục
- [ ] Không error "Connection refused"
- [ ] 5-minute test pass (toàn bộ operation success)

---

## CHECKLISTS

### Checklist Mỗi Người

```
□ Fork repo từ GitHub
□ Tạo Railway account
□ Create Railway project từ fork
□ Add MySQL plugin
□ Set NODE_ID = [my number]
□ Set MYSQL_DATABASE = db[my number]
□ Deploy Railway (green ✓)
□ Get Railway URL
□ Send URL to PM
□ Wait PM updates PEERS
□ Redeploy with new PEERS
□ Check logs + no errors
□ Report "Ready" to PM
```

### Checklist PM

```
□ Create main GitHub repo
□ Push source code
□ Assign 6 nodes to 6 people
□ Monitor 6 deployments
□ Collect 6 Railway URLs
□ Create PEERS string
□ Update PEERS ở 6 projects
□ Wait toàn bộ redeploy
□ Verify 6 logs - connections
□ Run client + test
□ Monitor token passing
□ Verify data storage
□ Document results
```

---

## PHẦN 10: SUPPORT

**Mỗi người gặp vấn đề:**

1. Kiểm tra: Railway Project Logs (Tab "Logs")
2. Tìm: Error message cụ thể
3. Common issues:
   - Build error → Check NODE_ID unique, MYSQL_DATABASE match
   - Connection error → Check PEERS updated by PM
   - Data not showing → Check if token reached your node

4. Contact PM nếu còn vấn đề

---

**VERSION:** 1.0  
**READY FOR:** Direct Railway Deployment  
**NO LOCAL TESTING:** Skip straight to production!  
**TIME:** 3-4 days from fork to live system

**Let's deploy! 🚀**
