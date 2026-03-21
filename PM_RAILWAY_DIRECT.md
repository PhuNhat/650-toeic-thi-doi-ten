# PM GUIDE - RAILWAY DIRECT DEPLOYMENT
## (Không Test Local - Thẳng Tới Production)

**Vai trò:** Quản Lý Dự Án  
**Thời gian:** 3-4 ngày  
**Mục tiêu:** Coordinate 6 nodes từ fork → Railway live

---

## NGÀY 0: SETUP MAIN REPO (PM - 1 lần)

### Bước 1: Tạo Main Repository

```bash
# 1. GitHub - Create new repo
# Name: DistributedTokenRing
# Public (for forking)

# 2. Clone về máy
git clone https://github.com/YOUR_USERNAME/DistributedTokenRing.git
cd DistributedTokenRing

# 3. Copy source code từ BaiDoXe project
# Cần: src/, build/ (compiled classes)
#     setup.sql, config.properties, railway.toml

# 4. Commit & push
git add .
git commit -m "[SETUP] Token Ring - Ready for Railway deployment"
git push origin main
```

✅ **Main repo ready - code compiled and ready**

### Bước 2: Assign Nodes & Send Links

**Tạo file: TEAM_ASSIGNMENT.txt**

```
TEAM ASSIGNMENTS - Token Ring Project

Repository: https://github.com/YOUR_USERNAME/DistributedTokenRing

ASSIGNMENTS:
Node 1 (Token holder): [Person A] (@github_user1)
Node 2:                [Person B] (@github_user2)
Node 3:                [Person C] (@github_user3)
Node 4:                [Person D] (@github_user4)
Node 5:                [Person E] (@github_user5)
Node 6:                [Person F] (@github_user6)

READ FILE: RAILWAY_DEPLOYMENT_DIRECT.md
FOLLOW:    INDIVIDUAL_DEPLOYMENT_CHECKLIST.md

Timeline:
- Day 1: Fork + Railway account
- Day 1-2: Railway project + MySQL
- Day 2: Deploy + get URL
- Day 3: PM updates PEERS + redeploy all
- Day 3-4: Verify + test

Contact PM if stuck!
```

**Gửi link + assignments cho 6 người**

---

## NGÀY 1-2: MONITOR DEPLOYMENTS

### Bước 2: Track Progress

**Tạo tracking spreadsheet:**

```
Node | Person | Fork | Railway Account | Project | MySQL | Deployed | URL | PEERS | Status
-----|--------|------|---|---|---|---|---|---|---
 1   | (Name) | [ ] | [ ]             | [ ]     | [ ]   | [ ]      | --- | [ ] | 
 2   | (Name) | [ ] | [ ]             | [ ]     | [ ]   | [ ]      | --- | [ ] |
 3   | (Name) | [ ] | [ ]             | [ ]     | [ ]   | [ ]      | --- | [ ] |
 4   | (Name) | [ ] | [ ]             | [ ]     | [ ]   | [ ]      | --- | [ ] |
 5   | (Name) | [ ] | [ ]             | [ ]     | [ ]   | [ ]      | --- | [ ] |
 6   | (Name) | [ ] | [ ]             | [ ]     | [ ]   | [ ]      | --- | [ ] |
```

**Daily check:** Ask each person's status

```
"Có deploy được trên Railway chưa? Gặp vấn đề gì không?"
```

**Expected timeline:**
- Day 1: Forks + Railway accounts created
- Day 1-2: Projects created + MySQL plugins added
- Day 2: All deployed + URLs received

---

## NGÀY 2-3: COLLECT URLs & CREATE PEERS

### Bước 3: Collect Railway URLs

**Message to all 6:**

```
Subject: Send me your Railway URL

Please send:
Node [X]: https://yoururl.up.railway.app:8080

Example:
Node 1: https://node1-production-xxxx.up.railway.app:8080
Node 2: https://node2-production-xxxx.up.railway.app:8080

Reply in format:
Node X: [URL]

Thanks!
```

**Wait until all 6 reply**

### Bước 4: Create PEERS String

```bash
# Collect URLs:
Node1: https://node1-prod-abc123.up.railway.app:8080
Node2: https://node2-prod-def456.up.railway.app:8080
Node3: https://node3-prod-ghi789.up.railway.app:8080
Node4: https://node4-prod-jkl012.up.railway.app:8080
Node5: https://node5-prod-mno345.up.railway.app:8080
Node6: https://node6-prod-pqr678.up.railway.app:8080

# Create PEERS (comma-separated):
PEERS = https://node1-prod-abc123.up.railway.app:8080,https://node2-prod-def456.up.railway.app:8080,https://node3-prod-ghi789.up.railway.app:8080,https://node4-prod-jkl012.up.railway.app:8080,https://node5-prod-mno345.up.railway.app:8080,https://node6-prod-pqr678.up.railway.app:8080
```

✅ **PEERS string ready**

### Bước 5: Send PEERS to All 6

```
Message to all:

Subject: PEERS String - Update Railway Variables Now

Below is the complete PEERS string. 
Paste into your Railway project's Variables:

PEERS = https://node1-prod-abc123.up.railway.app:8080,https://node2-prod-def456.up.railway.app:8080,https://node3-prod-ghi789.up.railway.app:8080,https://node4-prod-jkl012.up.railway.app:8080,https://node5-prod-mno345.up.railway.app:8080,https://node6-prod-pqr678.up.railway.app:8080

STEPS:
1. Go to your Railway project
2. Tab "Variables"
3. Find PEERS (or create new)
4. Paste the string above
5. Save
6. Railway will redeploy automatically
7. Watch Deployments tab for green ✓

Took 1-2 minutes to redeploy.
Let me know when done!
```

---

## NGÀY 3: UPDATE PEERS EVERYWHERE

### Bước 6: Track PEERS Updates

**Message check-in:**

```
DEPLOYMENT CHECKPOINT:

Have you:
□ Pasted PEERS into your Railway Variables?
□ Watched redeploy complete (green ✓)?
□ Checked logs - no "Connection refused"?

Let me know when all 6 done!
```

**Track in spreadsheet:**
```
Node | PEERS Updated | Redeploy Status | Connection OK
-----|---|---|---
 1   | [ ] | [ ] | [ ]
 2   | [ ] | [ ] | [ ]
 3   | [ ] | [ ] | [ ]
 4   | [ ] | [ ] | [ ]
 5   | [ ] | [ ] | [ ]
 6   | [ ] | [ ] | [ ]
```

✅ **All 6 redeploy with PEERS complete**

---

## NGÀY 3-4: VERIFY & TEST

### Bước 7: Check All Logs

**Method 1: Ask each person**

```
"Check your Railway logs for:
[CONNECT] Connected to https://node[X+1]...

Reply: OK if you see it, ERROR if not"
```

**Method 2: PM spot-check (optional)**

```
# If you have Railway access to view other projects:
Check each node's logs for successful connection
```

### Bước 8: Run End-to-End Test

**From your machine:**

```bash
cd DistributedTokenRing/build
java -cp . client.Client
```

**Test sequence:**

```
1. Client window opens
2. Select Node 1 from dropdown
3. Connect to: https://node1-prod-....up.railway.app:8080
4. ID = 1, Content = "Test123"
5. Click INSERT
6. Wait 2-3 seconds
7. Should see: "Success ✓"
8. Watch all 6 nodes' logs - token should cycle
```

### Bước 9: Verify Token Passing

**Ask 6 people to check their logs simultaneously:**

```
Message to all:

"At 2:00 PM, PM is sending INSERT request.
Watch your logs and confirm:

1. Do you see '[TOKEN] Received token from Node[X-1]'?
2. Do you see '[TOKEN] Passing token to Node[X+1]'?

Reply with a timestamp and status!"
```

**Expected flow:**
```
Time: 2:00:01 PM
Node 1: Received request, processing...
         Passing token → Node 2

Time: 2:00:02 PM
Node 2: Received token
         Processing (if needed)
         Passing token → Node 3

... continues ... Node 6

Time: 2:00:06 PM
Node 1: Received token back
         Cycle complete!
```

---

## BƯỚC 10: VERIFY DATA STORAGE

### Test Data Persistence

**Send 6 INSERT requests, each targeting different nodes:**

```
Via client:

1. Node 1: ID=10, Content="Data10" → INSERT
2. Node 1: ID=20, Content="Data20" → INSERT
3. Node 1: ID=30, Content="Data30" → INSERT
... (send to Node 1, but data goes to different databases)

OR directly target via Railway:
- Send to Node 2: should store in db2
- Send to Node 3: should store in db3
- etc
```

**Verify with each person:**

```
Message:

"Check your database for the data sent to your node.
Run query (if you have access):
SELECT COUNT(*) FROM server[X];

Should show at least 1 row.
Reply count: ___"
```

---

## NGÀY 4: FINALIZATION

### Bước 11: Document Results

**Create file: DEPLOYMENT_RESULTS.md**

```markdown
# Deployment Results - Token Ring System

** Deployment Date:** March 21, 2026
**PM:** (Your Name)

## Node Status
- [✓] Node 1: Live on Railway
- [✓] Node 2: Live on Railway
- [✓] Node 3: Live on Railway
- [✓] Node 4: Live on Railway
- [✓] Node 5: Live on Railway
- [✓] Node 6: Live on Railway

## Tests Performed

### Test 1: Token Passing
- [✓] INSERT sent to Node 1
- [✓] Token passed through all 6 nodes
- [✓] Elapsed time: X seconds
- Result: **PASS**

### Test 2: Data Storage
- [✓] Data stored in corresponding databases
- [✓] All 6 databases have data
- [✓] Lamport clock incrementing
- Result: **PASS**

### Test 3: Long-Running (5 min)
- [✓] Multiple requests processed
- [✓] No errors in logs
- [✓] Token continued cycling
- Result: **PASS**

## Overall Status
✅ **SYSTEM OPERATIONAL**
All 6 nodes deployed and functioning correctly.
Token ring active, data persistent.

---
Signed: (PM Name)
Date: March 21, 2026
```

---

## TROUBLESHOOTING (If Issues Arise)

### Issue: One node deployment failed

**Solution:**
```
1. Contact that person
2. Ask them to check Railway logs for error
3. Have them delete project and redeploy
4. Wait for new Railway URL
5. Update PEERS string and re-send to all 6
```

### Issue: Token stuck / not passing

**Solution:**
```
1. Check all 6 nodes deployed (Deployments: green ✓)
2. Verify PEERS string format (comma-separated, no spaces)
3. Check each person updated PEERS (ask them to confirm)
4. Trigger redeploy on stuck node
5. Monitor logs again
```

### Issue: MySQL connection error

**Solution:**
```
1. Verify MYSQL_DATABASE = db[NODE_ID] (not mismatched)
2. Verify MYSQL_URL format correct
3. Check Railway MySQL plugin still running
4. Redeploy that node
```

---

## QUICK CHECKLIST - PM

```
SETUP PHASE:
[ ] Create main GitHub repo
[ ] Push source code (src/, build/)
[ ] Assign 6 nodes to 6 people
[ ] Send repo link + assignments

DEPLOYMENT PHASE (TRACK):
[ ] All 6 forks received
[ ] All 6 Railway accounts created
[ ] All 6 projects deployed (green ✓)
[ ] All 6 MySQL plugins added
[ ] All 6 URLs collected from people

INTEGRATION PHASE:
[ ] Create PEERS string (6 URLs comma-separated)
[ ] Send PEERS to all 6 people
[ ] Track all 6 update variables
[ ] Track all 6 redeploy (green ✓)
[ ] Verify all 6 logs show connections

TESTING PHASE:
[ ] Run client INSERT test
[ ] Monitor token passing through all 6
[ ] Verify data storage in databases
[ ] Check Lamport clock incrementing
[ ] Run 5-minute continuous test
[ ] Document results

SIGN-OFF:
[ ] All tests pass
[ ] System operational
[ ] Send final report to team
[ ] Deployment complete! 🎉
```

---

## TIMELINE - RAPID DEPLOYMENT

```
Day 1:
- Morning: PM creates main repo, assigns nodes
- Afternoon: 6 people fork, create Railway accounts

Day 1-2:
- Morning: 6 people create projects + add MySQL
- Afternoon: 6 people deploy Railway

Day 2:
- Morning: 6 people send URLs to PM
- Afternoon: PM creates PEERS string, sends to all 6

Day 3:
- Morning: 6 people update PEERS + redeploy
- Afternoon: PM verifies connections

Day 3-4:
- Morning: PM runs tests
- Afternoon: System verified operational

✓ TOTAL: 3-4 days to live system!
```

---

**VERSION:** 1.0  
**MODE:** Rail way Direct (No Local Testing)  
**DURATION:** 3-4 days  
**READY:** Yes!

**Let's go live! 🚀**
