# HƯỚNG DẪN TRIỂN KHAI - NODE [X]
## (Railway Direct Deployment - Không Test Local)

**Người:** ___________________  
**Node ID:** [1-6]  
**Thời gian:** 3-4 ngày  
**Mục đích:** Deploy Node lên Railway và kết nối với 5 nodes khác

---

## CHUẨN BỊ

### Node Assignment (Ghi nhớ)

| Node | Port (Railway) | MySQL DB | Initial Token? | Next Node |
|------|---|---|---|---|
| 1 | 8080 | db1 | **YES** | Node 2 |
| 2 | 8080 | db2 | NO | Node 3 |
| 3 | 8080 | db3 | NO | Node 4 |
| 4 | 8080 | db4 | NO | Node 5 |
| 5 | 8080 | db5 | NO | Node 6 |
| 6 | 8080 | db6 | NO | Node 1 |

**Tìm Node số của bạn từ PM**

---

## NGÀY 1: FORK REPO

### Bước 1.1: Fork GitHub Repo

```
1. Vào: https://github.com/PM_USERNAME/DistributedTokenRing
2. Bấm nút "Fork" (góc phải trên)
3. Đợi GitHub create fork
4. Sẽ có: https://github.com/YOUR_USERNAME/DistributedTokenRing
```

✅ **Bạn đã có repo source code**

### Bước 1.2: Clone Repo

```bash
git clone https://github.com/YOUR_USERNAME/DistributedTokenRing.git
cd DistributedTokenRing

# Kiểm tra
ls -la
# Nên thấy: src/, build/, setup.sql, railway.toml (đã compile sẵn!)
```

✅ **Source code sẵn sàng trên máy của bạn**

---

## NGÀY 1-2: RAILWAY ACCOUNT & PROJECT

### Bước 2.1: Tạo Railway Account

```
1. Vào https://railway.app
2. Click "Sign Up"
3. Chọn "Continue with GitHub"
4. Authorize Railway
5. Verify email
6. ✓ Done - bạn đã có Railway account
```

### Bước 2.2: Tạo Railway Project

```
1. Vào Railway Dashboard
2. Click "Create a new project"
3. Chọn "Deploy from GitHub repo"
4. Chọn YOUR fork: DistributedTokenRing
5. Click "Deploy Now"
6. Đợi 1-2 phút - Railway sẽ build code
```

✅ **Railway project created**

---

## NGÀY 2: MYSQL PLUGIN & CONFIGURATION

### Bước 3.1: Add MySQL Plugin

```
Trong Railway project của bạn:

1. Click "Plugins" tab (hoặc + icon)
2. Chọn "MySQL"
3. Đợi 1-2 phút MySQL initialize
4. Railway sẽ auto-generate:
   - MYSQL_HOST
   - MYSQL_PORT
   - MYSQL_USER
   - MYSQL_PASSWORD
```

✅ **MySQL plugin added**

### Bước 3.2: Set Environment Variables

```
Trong Railway project:
Tab "Variables" → Add these:

NODE_ID = [X]
PORT = 8080

MYSQL_DATABASE = db[X]
MYSQL_URL = jdbc:mysql://mysql.railway.internal:3306/db[X]

PEERS = (PM sẽ gửi sau - để trống lúc này)
```

**Ví dụ nếu bạn là Node 1:**
```
NODE_ID = 1
PORT = 8080
MYSQL_DATABASE = db1
MYSQL_URL = jdbc:mysql://mysql.railway.internal:3306/db1
PEERS = (empty)
```

**Ví dụ nếu bạn là Node 3:**
```
NODE_ID = 3
PORT = 8080
MYSQL_DATABASE = db3
MYSQL_URL = jdbc:mysql://mysql.railway.internal:3306/db3
PEERS = (empty)
```

✅ **Environment variables configured**

---

## NGÀY 2-3: DEPLOY RAILWAY

### Bước 4.1: Trigger Deploy

```
Trong Railway project:

1. Tab "Deployments"
2. Click "Deploy Now"
3. Đợi 3-5 phút build
4. Nên thấy: Success ✓ (green)

Nếu error:
- Check Railway logs
- Verify NODE_ID unique (1-6)
- Verify MYSQL_DATABASE format (db1, db2, etc)
- Retry deploy
```

✅ **Railway deployment success**

### Bước 4.2: Check Logs

```
Trong Railway project:
Tab "Logs" → Nên thấy:

[INIT] Node [X] initializing...
[SERVER] Server started on port 8080
[ROUTING] RoutingTable initialized with 6 nodes
[READY] Waiting for token...

Hoặc nếu có PEERS sẽ thấy:
[CONNECT] Connected to next node
```

✅ **Server running**

---

## NGÀY 2-3: LẤY RAILWAY URL

### Bước 5.1: Find Your Railway URL

```
Trong Railway project:
Tab "Settings" → Public Networking

Sẽ thấy URL: 
https://nodeX-production-xxxxxx.up.railway.app

Copy lại → gửi cho PM (format: link:8080)
```

**Gửi PM:**
```
Node X: https://nodeX-production-xxxxxx.up.railway.app:8080
```

✅ **URL sent to PM**

---

## NGÀY 3: CHỜ PM UPDATE PEERS

### Bước 6.1: Chờ PM

```
PM sẽ:
1. Thu thập 6 URLs từ 6 người
2. Tạo PEERS string
3. Gửi lại cho tất cả 6 người

Khi PM gửi PEERS string:
```

### Bước 6.2: Update PEERS

```
Khi nhận PEERS từ PM:

1. Vào Railway project
2. Tab "Variables"
3. Find PEERS (hoặc create)
4. Paste: (toàn bộ string PM gửi)
5. Save

Ví dụ:
PEERS = https://node1-xxx.up.railway.app:8080,https://node2-xxx.up.railway.app:8080,https://node3-xxx.up.railway.app:8080,https://node4-xxx.up.railway.app:8080,https://node5-xxx.up.railway.app:8080,https://node6-xxx.up.railway.app:8080

6. Railway auto-redeploy (watch Deployments tab)
7. Đợi green ✓ Success
```

✅ **PEERS updated, redeploy complete**

---

## NGÀY 3-4: VERIFY CONNECTION

### Bước 7.1: Check Logs After PEERS Update

```
Tab "Logs" → Nên thấy:

[ROUTING] Parsing PEERS...
[CONNECT] Connecting to Node X+1...
[CONNECT] Connected to https://nodeX+1-xxx...
[TOKEN] Ready to receive/pass token
[SYNC] Synchronized with ring

Không nên thấy:
❌ Connection refused
❌ Unknown host
❌ Timeout
```

✅ **Node connected to ring**

### Bước 7.2: Token Flow (Optional Monitor)

```
Nếu PM gửi INSERT request từ client:

Bạn nên thấy logs:
[TOKEN] Received token from Node X-1
[PROCESS] Processing request (nếu là assigned request)
[TOKEN] Passing token to Node X+1

Điều này chứng tỏ token đang flow qua node bạn!
```

✅ **System working end-to-end**

---

## GHI NHỚ

### Environment Variables - MỘT LẦN NỮA

```
Node 1:
  NODE_ID = 1
  MYSQL_DATABASE = db1

Node 2:
  NODE_ID = 2
  MYSQL_DATABASE = db2

Node 3:
  NODE_ID = 3
  MYSQL_DATABASE = db3

... etc

TOÀN BỘ:
  PORT = 8080
  MYSQL_URL = jdbc:mysql://mysql.railway.internal:3306/db[NODE_ID]
  PEERS = (PM gửi sau)
```

### Troubleshooting

| Lỗi | Giải pháp |
|-----|----------|
| Build fails | Check logs, verify NODE_ID 1-6, NODE_ID unique |
| MySQL connection error | Verify MYSQL_DATABASE = db[NODE_ID], MYSQL_URL format |
| "PEERS not found" | PM chưa update, chờ thêm hoặc contact PM |
| No logs | Check Railway app running (Deployments tab) |
| "Connection refused" | PEERS outdated, wait for PM update, redeploy |

---

## CHECKLIST

- [ ] Fork GitHub repo
- [ ] Clone repo locally (verify src/, build/ exists)
- [ ] Create Railway account
- [ ] Create Railway project
- [ ] Add MySQL plugin (wait 1-2 min)
- [ ] Set NODE_ID = [my number]
- [ ] Set MYSQL_DATABASE = db[my number]
- [ ] Set MYSQL_URL = jdbc:mysql://mysql.railway.internal:3306/db[my number]
- [ ] Deploy Railway (green ✓)
- [ ] Check logs - no errors
- [ ] Get Railway URL
- [ ] Send URL to PM
- [ ] Wait for PM to send PEERS string
- [ ] Update PEERS in Railway Variables
- [ ] Redeploy (green ✓)
- [ ] Check logs - connected to ring
- [ ] Status: ✅ READY

---

**Current Status:** _______________  
**Deployed Date:** _______________  
**Railway URL:** _______________  

---

**Questions?** Check the logs first. Contact PM if needed.

**You're done! Your node is live! 🚀**
