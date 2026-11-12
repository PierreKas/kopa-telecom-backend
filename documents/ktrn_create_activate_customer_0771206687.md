# KTRN BSS: create and activate a customer (MSISDN 0771206687)

Built from the terminal history of 29–30 Oct 2026.

- Gateway: `http://192.168.21.7:8086/cloud/ktrn/bss` (called from `kopa-telco-server`)
- Retailer: KOPA Telecom, `SO_ID` 20
- Agent: `TRUPOC`, `ORG_ID` 3472
- Customer: Baraka Ntwali, `CUST_ID` 4606529
- SIM: ICCID `892501720000111620`, MSISDN `0771206687`

> **Internal use only.** This file contains the POC agent's credentials, a test customer's ID number and its encrypted form.

---

## 1. At a glance

| # | Step | Opcode | Captured in history | Outcome |
|---|------|--------|---------------------|---------|
| 1 | Encrypt the agent password | HPOP313 | yes (29 Oct) | ciphertext |
| 2 | Agent login | HPOP201 | yes (29 Oct) | `SO_ID`, `ORG_ID`, `DISTANCE` |
| 3 | Encrypt the customer's ID number | HPOP313 | yes (29 Oct) | ciphertext |
| 4 | Count existing subscriptions | HPOP310 | yes (29 Oct) | `-7002` (none found) |
| 5 | Check the number is free | HPOP601 | yes (30 Oct) | `Y` |
| 6 | Create the customer | HPOP501 | **partly: only "already exists"** | `-4003` |
| 7 | Get the customer ID | HPOP502 | yes (30 Oct) | `CUST_ID` 4606529 |
| 8 | Activate the SIM | HPOP302 | yes (30 Oct) | `1001`, empty body |
| 9 | Verify after activation | HPOP601 / 101 / 102 | yes (30 Oct) | `N` / balance record / bundle list |

**Not in the history** (so not documented here with a response):

- A successful first-time **HPOP501** create. The only 501 response is "customer already exists".
- **HPOP312** fingerprint verification, **HPOP311** OTP, **HPOP303** NIDA lookup.
- A successful **HPOP301** price-plan lookup. It returned `-7002` or `-4003` every time.

---

## 2. Conventions used in every call

- Raw XML body with `Content-Type: application/xml`.
  - The gateway also accepts the `REQ_XML` form-field style used earlier for catalogue calls.
  - It echoes the request's tag spelling (`SVCCODE` or `SVCODE`) in the response header.
- Encrypted values (`LOGIN_PW`, `CORP_REG_NO`) are sent exactly as HPOP313 returns them. They are already URL-encoded (`%2B`, `%2F`, `%3D`), so do not encode them again.
- In the encrypted password `HVVfBSzJ5kJTp5%2B`**`l`**`D9VJ8A%3D%3D` the character after `%2B` is a lowercase **L**. With a digit **1** the login fails with `-6002`.

---

## 3. Step by step

### Step 1: Encrypt the agent password (HPOP313)

Request
```bash
curl -sS -X POST 'http://192.168.21.7:8086/cloud/ktrn/bss' -H 'Content-Type: application/xml' --data-raw '<REQUEST><HEADER><SVCCODE>HP00005</SVCCODE><OPCODE>HPOP313</OPCODE><CHCODE>A</CHCODE></HEADER><BODY><PASSWORD>11223344</PASSWORD><OPERATION_TYPE></OPERATION_TYPE></BODY></REQUEST>'
```

Response
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?><RESPONSE><HEADER><MESSAGE>It has been successfully processed.</MESSAGE><MSGCODE>1001</MSGCODE><SVCCODE>HP00005</SVCCODE><CHCODE>A</CHCODE><OPCODE>HPOP313</OPCODE></HEADER><BODY><RESULT>HVVfBSzJ5kJTp5%2BlD9VJ8A%3D%3D</RESULT></BODY></RESPONSE>
```

### Step 2: Agent login (HPOP201)

Request
```bash
curl -sS -X POST 'http://192.168.21.7:8086/cloud/ktrn/bss' -H 'Content-Type: application/xml' --data-raw '<REQUEST><HEADER><SVCCODE>HP00005</SVCCODE><OPCODE>HPOP201</OPCODE><CHCODE>A</CHCODE></HEADER><BODY><LOGIN_ID>TRUPOC</LOGIN_ID><LOGIN_PW>HVVfBSzJ5kJTp5%2BlD9VJ8A%3D%3D</LOGIN_PW><USER_TYPE>02</USER_TYPE><REQ_SYS_ID>01</REQ_SYS_ID></BODY></REQUEST>'
```

Response
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?><RESPONSE><HEADER><MESSAGE>It has been successfully processed.</MESSAGE><MSGCODE>1001</MSGCODE><SVCCODE>HP00005</SVCCODE><CHCODE>A</CHCODE><OPCODE>HPOP201</OPCODE></HEADER><BODY><DISTANCE>30</DISTANCE><ORG_ID>3472</ORG_ID><SO_NM>KOPA Telecom</SO_NM><USER_TYPE>02</USER_TYPE><SO_ID>20</SO_ID><SEC_PW/><APP_USE_YN>Y</APP_USE_YN></BODY></RESPONSE>
```

Values to keep from this response: `SO_ID` 20, `ORG_ID` 3472, `SO_NM` KOPA Telecom, `DISTANCE` 30, `APP_USE_YN` Y.
The response contains **no** `LONGITUDE` / `LATITUDE` and an empty `SEC_PW`.

Failed login (digit `1` instead of lowercase `l` in the ciphertext)
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?><RESPONSE><HEADER><MESSAGE>Undefined value/incorrect value LOGIN_PW was entered.</MESSAGE><MSGCODE>-6002</MSGCODE><SVCCODE>HP00005</SVCCODE><CHCODE>A</CHCODE><OPCODE>HPOP201</OPCODE></HEADER><BODY/></RESPONSE>
```

### Step 3: Encrypt the customer's ID number (HPOP313)

Request
```bash
curl -sS -X POST 'http://192.168.21.7:8086/cloud/ktrn/bss' -H 'Content-Type: application/xml' --data-raw '<REQUEST><HEADER><SVCCODE>HP00005</SVCCODE><OPCODE>HPOP313</OPCODE><CHCODE>A</CHCODE></HEADER><BODY><PASSWORD>1198904041239999</PASSWORD><OPERATION_TYPE></OPERATION_TYPE></BODY></REQUEST>'
```

Response
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?><RESPONSE><HEADER><MESSAGE>It has been successfully processed.</MESSAGE><MSGCODE>1001</MSGCODE><SVCCODE>HP00005</SVCCODE><CHCODE>A</CHCODE><OPCODE>HPOP313</OPCODE></HEADER><BODY><RESULT>NBwY5U%2Fa23M%2FtEbUWWPxk%2BJ9Uqjpjd3Vy0JbXmXdask%3D</RESULT></BODY></RESPONSE>
```

This ciphertext is the `CORP_REG_NO` used in steps 4, 6 and 7.

### Step 4: Count existing subscriptions under this ID (HPOP310)

Run on 29 Oct, before the customer existed.

Request
```bash
curl -sS -X POST 'http://192.168.21.7:8086/cloud/ktrn/bss' -H 'Content-Type: application/xml' --data-raw '<REQUEST><HEADER><SVCCODE>HP00005</SVCCODE><OPCODE>HPOP310</OPCODE><CHCODE>A</CHCODE></HEADER><BODY><SO_ID>20</SO_ID><CUST_TP>A</CUST_TP><CUST_NM>Baraka Ntwali</CUST_NM><CORP_REG_NO>NBwY5U%2Fa23M%2FtEbUWWPxk%2BJ9Uqjpjd3Vy0JbXmXdask%3D</CORP_REG_NO></BODY></REQUEST>'
```

Response
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?><RESPONSE><HEADER><MESSAGE>Data is not found..</MESSAGE><MSGCODE>-7002</MSGCODE><SVCCODE>HP00005</SVCCODE><CHCODE>A</CHCODE><OPCODE>HPOP310</OPCODE></HEADER><BODY/></RESPONSE>
```

On this gateway `-7002` means no subscriptions exist for this customer, so no limit problem and no OTP step. The spec describes a `SUBSCOUNT` response, which was not seen here.

### Step 5: Check the number is free (HPOP601)

Request
```bash
curl -sS -X POST 'http://192.168.21.7:8086/cloud/ktrn/bss' -H 'Content-Type: application/xml' --data-raw '<REQUEST><HEADER><SVCCODE>HP00005</SVCCODE><OPCODE>HPOP601</OPCODE><CHCODE>A</CHCODE></HEADER><BODY><MSISDN>0771206687</MSISDN><SO_ID>20</SO_ID></BODY></REQUEST>'
```

Response
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?><RESPONSE><HEADER><MESSAGE>It has been successfully processed.</MESSAGE><MSGCODE>1001</MSGCODE><SVCCODE>HP00005</SVCCODE><CHCODE>A</CHCODE><OPCODE>HPOP601</OPCODE></HEADER><BODY><RESULT>Y</RESULT></BODY></RESPONSE>
```

### Step 6: Create the customer (HPOP501)

This step is only partly captured.

6a. First attempt, with the placeholder `YYYYMMDD` as birth date: the terminal printed nothing and was stopped with Ctrl+C.
```bash
curl -sS -X POST 'http://192.168.21.7:8086/cloud/ktrn/bss' -H 'Content-Type: application/xml' --data-raw '<REQUEST><HEADER><SVCCODE>HP00005</SVCCODE><OPCODE>HPOP501</OPCODE><CHCODE>A</CHCODE></HEADER><BODY><SO_ID>20</SO_ID><LOGIN_ID>TRUPOC</LOGIN_ID><ORG_ID>3472</ORG_ID><CUST_TP>A</CUST_TP><CUST_NM>Baraka Ntwali</CUST_NM><CORP_REG_NO>NBwY5U%2Fa23M%2FtEbUWWPxk%2BJ9Uqjpjd3Vy0JbXmXdask%3D</CORP_REG_NO><BIRTH_DAY>YYYYMMDD</BIRTH_DAY><ADDR_DTL>KIGALI</ADDR_DTL></BODY></REQUEST>'
```
Response: none.

6b. Second attempt, with a valid birth date.

Request
```bash
curl -sS -X POST 'http://192.168.21.7:8086/cloud/ktrn/bss' -H 'Content-Type: application/xml' --data-raw '<REQUEST><HEADER><SVCCODE>HP00005</SVCCODE><OPCODE>HPOP501</OPCODE><CHCODE>A</CHCODE></HEADER><BODY><SO_ID>20</SO_ID><LOGIN_ID>TRUPOC</LOGIN_ID><ORG_ID>3472</ORG_ID><CUST_TP>A</CUST_TP><CUST_NM>Baraka Ntwali</CUST_NM><CORP_REG_NO>NBwY5U%2Fa23M%2FtEbUWWPxk%2BJ9Uqjpjd3Vy0JbXmXdask%3D</CORP_REG_NO><BIRTH_DAY>20000312</BIRTH_DAY><ADDR_DTL>KIGALI</ADDR_DTL></BODY></REQUEST>'
```

Response
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?><RESPONSE><HEADER><MESSAGE>The current customer already exists.</MESSAGE><MSGCODE>-4003</MSGCODE><SVCCODE>HP00005</SVCCODE><CHCODE>A</CHCODE><OPCODE>HPOP501</OPCODE></HEADER><BODY/></RESPONSE>
```

What this shows:

- These fields are enough for the gateway to process the request: `SO_ID`, `LOGIN_ID`, `ORG_ID`, `CUST_TP`, `CUST_NM`, `CORP_REG_NO`, `BIRTH_DAY`, `ADDR_DTL`.
- The history cannot show whether 6a or an earlier call created the customer.
- **To capture a real success response, run HPOP501 with a new test ID number** (encrypt it first, as in step 3).

### Step 7: Get the customer ID (HPOP502)

Request
```bash
curl -sS -X POST 'http://192.168.21.7:8086/cloud/ktrn/bss' -H 'Content-Type: application/xml' --data-raw '<REQUEST><HEADER><SVCCODE>HP00005</SVCCODE><OPCODE>HPOP502</OPCODE><CHCODE>A</CHCODE></HEADER><BODY><SO_ID>20</SO_ID><CUST_TP>A</CUST_TP><CUST_NM>Baraka Ntwali</CUST_NM><CORP_REG_NO>NBwY5U%2Fa23M%2FtEbUWWPxk%2BJ9Uqjpjd3Vy0JbXmXdask%3D</CORP_REG_NO></BODY></REQUEST>'
```

Response
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?><RESPONSE><HEADER><MESSAGE>It has been successfully processed.</MESSAGE><MSGCODE>1001</MSGCODE><SVCCODE>HP00005</SVCCODE><CHCODE>A</CHCODE><OPCODE>HPOP502</OPCODE></HEADER><BODY><CUST_ID>4606529</CUST_ID><CUST_NAME>Baraka Ntwali</CUST_NAME></BODY></RESPONSE>
```

`CUST_ID` 4606529 is what step 8 needs. The spec calls the name field `CUST_NM`; this gateway returns `CUST_NAME`.

### Step 8: Activate the SIM (HPOP302)

Request
```bash
curl -sS -X POST 'http://192.168.21.7:8086/cloud/ktrn/bss' -H 'Content-Type: application/xml' --data-raw '<REQUEST><HEADER><SVCCODE>HP00005</SVCCODE><OPCODE>HPOP302</OPCODE><CHCODE>A</CHCODE></HEADER><BODY><SO_ID>20</SO_ID><MSISDN>0771206687</MSISDN><PROD_CD>KOPA TelecomMainProduct</PROD_CD><LOGIN_ID>TRUPOC</LOGIN_ID><ORG_ID>3472</ORG_ID><ICCID>892501720000111620</ICCID><CUST_ID>4606529</CUST_ID><REQ_SYS_ID>01</REQ_SYS_ID></BODY></REQUEST>'
```

Response
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?><RESPONSE><HEADER><MESSAGE>It has been successfully processed.</MESSAGE><MSGCODE>1001</MSGCODE><SVCCODE>HP00005</SVCCODE><CHCODE>A</CHCODE><OPCODE>HPOP302</OPCODE></HEADER><BODY/></RESPONSE>
```

Notes:

- Success is `MSGCODE 1001` with an empty `<BODY/>`. The spec expects `RESULT` and `PROD_RATE` in the body.
- This call carried no `FINGERPRINT`, `PHOTO`, `HAND`, `LONGITUDE`, `LATITUDE`, `RNUMBER` or `OTP`, and the gateway still accepted it.
- `PROD_CD` is the literal `KOPA TelecomMainProduct`, not a `PDxxxxxxxx` code. Where this value came from is not in the history.
- The ICCID that worked is the 18-digit form `892501720000111620`.

### Step 9: Verify after activation

**9a. HPOP601 again: the number is no longer free**

Request
```bash
curl -sS -X POST 'http://192.168.21.7:8086/cloud/ktrn/bss' -H 'Content-Type: application/xml' --data-raw '<REQUEST><HEADER><SVCCODE>HP00005</SVCCODE><OPCODE>HPOP601</OPCODE><CHCODE>A</CHCODE></HEADER><BODY><MSISDN>0771206687</MSISDN><SO_ID>20</SO_ID></BODY></REQUEST>'
```
Response
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?><RESPONSE><HEADER><MESSAGE>It has been successfully processed.</MESSAGE><MSGCODE>1001</MSGCODE><SVCCODE>HP00005</SVCCODE><CHCODE>A</CHCODE><OPCODE>HPOP601</OPCODE></HEADER><BODY><RESULT>N</RESULT></BODY></RESPONSE>
```

**9b. HPOP101: the number now has a balance record**

Request (note that `SO_NM` comes from the login response)
```bash
curl -sS -X POST 'http://192.168.21.7:8086/cloud/ktrn/bss' -H 'Content-Type: application/xml' --data-raw '<REQUEST><HEADER><SVCCODE>HP00005</SVCCODE><OPCODE>HPOP101</OPCODE><CHCODE>A</CHCODE></HEADER><BODY><MSISDN>0771206687</MSISDN><SO_NM>KOPA Telecom</SO_NM></BODY></REQUEST>'
```
Response
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?><RESPONSE><HEADER><MESSAGE>It has been successfully processed.</MESSAGE><MSGCODE>1001</MSGCODE><SVCCODE>HP00005</SVCCODE><CHCODE>A</CHCODE><OPCODE>HPOP101</OPCODE></HEADER><BODY><MSISDN>0771206687</MSISDN><SO_NM>KOPA Telecom</SO_NM><REMAIN_AMT>0</REMAIN_AMT><SO_ID>20</SO_ID><TOTAL_VOLUME>0</TOTAL_VOLUME><BUNDLE_HIST/><PROD_GRP>a</PROD_GRP><REMAIN_VOLUME>0</REMAIN_VOLUME><TODAY_VOLUME>0</TODAY_VOLUME><AMOUNT_USED>0</AMOUNT_USED><APP_USE_YN>Y</APP_USE_YN></BODY></RESPONSE>
```

**9c. HPOP102: bundles the new subscriber can buy**

Request
```bash
curl -sS -X POST 'http://192.168.21.7:8086/cloud/ktrn/bss' -H 'Content-Type: application/xml' --data-raw '<REQUEST><HEADER><SVCCODE>HP00005</SVCCODE><OPCODE>HPOP102</OPCODE><CHCODE>A</CHCODE></HEADER><BODY><MSISDN>0771206687</MSISDN><SO_ID>20</SO_ID></BODY></REQUEST>'
```
Response
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?><RESPONSE><HEADER><MESSAGE>It has been successfully processed.</MESSAGE><MSGCODE>1001</MSGCODE><SVCCODE>HP00005</SVCCODE><CHCODE>A</CHCODE><OPCODE>HPOP102</OPCODE></HEADER><BODY><PROD_LIST>582-PD00033550,KOPA Telecom SAGE ONE MUP 1GB Basic(PRE),48000|279-PD00054400,KOPA Telecom private MUP 2GB Bundle(PRE),35000|582-PD00054400,KOPA Telecom private MUP 2GB Bundle(PRE),35000|279-PD00018119,Daily 1GB Bundle(PRE),1000|572-PD00067100,KOPA Telecom Agahebuzo 20GB Pack Basic(PRE),13000|572-PD00067150,KOPA Telecom  Agahebuzo 20GB Pack(PRE),13000</PROD_LIST><SO_ID>20</SO_ID><RESULT>OK</RESULT><APP_USE_YN>Y</APP_USE_YN></BODY></RESPONSE>
```

---

## 4. Failed attempts and what they showed

| Call | What was sent | Response | Lesson |
|------|---------------|----------|--------|
| HPOP302 | no `LOGIN_ID` | `-6001` "LOGIN_ID is missing." | `LOGIN_ID` is required |
| HPOP302 | + `LOGIN_ID` | `-6001` "ORG_ID is missing." | `ORG_ID` is required |
| HPOP302 | + `ORG_ID` | `-6001` "CUST_ID is missing." | `CUST_ID` is required |
| HPOP302 | `PROD_CD` `PD00067150` or `KOPA TelecomMainProduct`, `CUST_ID` 4606526, `REQ_SYS_ID` 02, `LONGITUDE`/`LATITUDE` present | `-6002` "incorrect value PROD_CD,SO_ID" | see section 5 |
| HPOP301 | ICCID `892501720000111620` | `-7002` "Data is not found.." | never returned a price plan |
| HPOP301 | ICCID `0892501720000111620` or `8925017200001116208` | `-4003` "The ICCID does not exist or The state of ICCID is invalid." | ICCID format matters |
| HPOP301 | + `DISTINGUISH_NO`, with or without `SO_ID` | `-7002` | no change |
| HPOP201 | digit `1` in the ciphertext | `-6002` "LOGIN_PW" | copy `l` vs `1` exactly |
| HPOP313 decrypt | digit `1` in the ciphertext | `-4003` "Password format error." | same cause |

---

## 5. What differs between the failed and the working HPOP302

| Field | Failed (29 Oct) | Worked (30 Oct) |
|-------|-----------------|-----------------|
| `CUST_ID` | 4606526 | 4606529 (from HPOP502) |
| `REQ_SYS_ID` | 02 (app access) | 01 (pivot access) |
| `LONGITUDE` / `LATITUDE` | sent | not sent |

The history does not show which of these three caused the `-6002`. Change one at a time on a test number to find out.

---

## 6. Where the agent login (HPOP201) matters

A customer cannot register or activate themselves. Every call that changes data is made by an agent, and the customer is only data in the request: `CUST_ID`, the encrypted ID number, MSISDN and ICCID. The agent's identity travels in `LOGIN_ID` and `ORG_ID`.

HPOP201 is the first step because:

1. **It checks the agent.** Wrong password gives `-6002`. `USER_TYPE` 02 is the retailer (agent) role, and `APP_USE_YN` = `Y` means this staff account may use the app. If it is `N`, stop.
2. **It supplies values the later steps require.**

   | From HPOP201 | Needed by | Evidence |
   |--------------|-----------|----------|
   | `ORG_ID` 3472 | HPOP501, HPOP302 | `-6001` "ORG_ID is missing." without it |
   | `LOGIN_ID` TRUPOC | HPOP501, HPOP302 | `-6001` "LOGIN_ID is missing." without it |
   | `SO_ID` 20 | HPOP310, 502, 501, 302, 601, 102 | used in every call |
   | `SO_NM` KOPA Telecom | HPOP101 | field in the request |
   | `DISTANCE` 30 | geo check by the caller | see below |

3. **It tells the app where the agent may work.** The spec (section 2.7.1) says the third-party app must store the returned longitude, latitude and `DISTANCE`, compare them with the agent's current GPS position before registering or activating, and reject the operation when the agent is outside the radius.

What the history shows about how the login behaves:

- **It is not a session.** HPOP201 returns no token. The only successful login is from 29 Oct (12:10 UTC session), and the activation was on 30 Oct with no login in between. The agent's identity is just fields in later requests.
- **So the Kopa app must enforce the login itself.** It should keep the HPOP201 result for the agent's session and block the register and activate screens without it.
- **The geo check is also Kopa's job.** HPOP302 succeeded with no coordinates, and the login response for TRUPOC contains `DISTANCE` but no `LONGITUDE` / `LATITUDE` to compare against.
- **Untested:** whether the BSS checks that the `LOGIN_ID` / `ORG_ID` in HPOP501 and HPOP302 really belong to a logged-in agent.

---

## 7. Open points to raise with KTRN

1. HPOP501: a call with an invalid birth date (`YYYYMMDD`) got no response at all instead of a validation error. A first-time success response is still to be captured.
2. HPOP310 after activation returns `-4002` ("data type that can not be converted at the gateway") instead of a `SUBSCOUNT`.
3. HPOP313 returns `-4002` for the ID number `1198904041239888` (twice), while `1198904041239999` encrypted fine.
4. HPOP301 never returns a price plan for ICCID `892501720000111620`, so the origin of `PROD_CD` `KOPA TelecomMainProduct` is unclear.
5. HPOP201 returns no `LONGITUDE` / `LATITUDE` and an empty `SEC_PW`, although the spec lists them as mandatory.
6. HPOP302 succeeds without fingerprint, photo, OTP or coordinates, and returns an empty body instead of `RESULT` / `PROD_RATE`.
7. HPOP502 returns `CUST_NAME` where the spec says `CUST_NM`.
