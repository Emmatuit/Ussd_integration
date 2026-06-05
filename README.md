# OlivePay USSD Banking Middleware – Complete Project Documentation

This document provides a comprehensive overview of the OlivePay USSD banking middleware, designed to deliver financial services via the USSD shortcode **\*5151#**. The system acts as a bridge between **OlivePay MFB** (using BankOne as its core banking system) and **Interswitch** (payment switching & non‑card payment APIs). The middleware is built with **Spring Boot (Java 17)** and is ready for production deployment after receiving Interswitch OAuth2 credentials.

---

## 1. Project Overview

- **Shortcode**: `*5151#` (registered with NCC, routed via Interswitch)
- **Target Users**: OlivePay customers (existing and new)
- **Core Functions**:
  - Airtime & Data purchase (self & others)
  - Fund transfers (self, other Olive accounts, inter‑bank, e‑wallets)
  - Bill payments (Cable TV, electricity, water, internet)
  - Account management (balance enquiry, open account, loan balance)
  - Security (PIN change/reset, opt‑in/out, token generation)
- **External Integrations**:
  - **BankOne** (Core Banking API) – for customer accounts, balances, internal transfers, PIN validation, account opening.
  - **Interswitch** – for card payments (purchase, OTP, requery), inter‑bank transfers, bill payments, airtime/data, wallet payments, and OAuth2 authentication.
- **Deployment**: Render (cloud) + PostgreSQL + Redis (Upstash)

---

## 2. Technology Stack

| Layer          | Technology                                      |
|----------------|-------------------------------------------------|
| Backend        | Java 17, Spring Boot 3.3                       |
| Web Framework  | Spring MVC, RestClient                         |
| Database       | PostgreSQL (production), H2 (local dev)        |
| Caching        | Redis (Upstash) – bank list cache              |
| Migration      | Flyway                                         |
| Logging        | Async audit logs into `audit_logs` table       |
| Security       | OAuth2 (client credentials) – no HMAC/AES      |
| USSD Simulator | HTML/CSS/JS (static) – mimics phone keypad     |
| Build Tool     | Maven/Gradle                                   |

---

## 3. High‑Level Architecture

```
[Feature Phone] --USSD--> [Mobile Network] --HTTP--> [Interswitch Aggregator] --POST--> [Your Middleware]
                                                                                           |
                                                                                           v
                                                                               +---------------------+
                                                                               |   UssdController    |
                                                                               |  (receives requests) |
                                                                               +---------------------+
                                                                                           |
                                                                                           v
                                                                               +---------------------+
                                                                               |   UssdMenuService   |
                                                                               | (routes menu flows) |
                                                                               +---------------------+
                                                                                           |
                                      +----------------------------------------------------+
                                      |                    |                             |
                                      v                    v                             v
                          +------------------+  +----------------------+  +----------------------+
                          | TransferHandler  |  | AirtimeDataHandler   |  | BillsHandler         |
                          | AccountHandler   |  | SecurityHandler      |  | ...                  |
                          +------------------+  +----------------------+  +----------------------+
                                      |                    |                             |
                                      v                    v                             v
                          +---------------------------------------------------------------+
                          |                    InterswitchPaymentClient                  |
                          |  (OAuth2 token, purchase, transfer, bills, name enquiry)    |
                          +---------------------------------------------------------------+
                                      |
                                      v
                          +-------------------------------+
                          |          BankApiClient         |
                          |  (BankOne: balance, transfer,  |
                          |   account opening, PIN change) |
                          +-------------------------------+
```

---

## 4. Key Packages & Classes

### 4.1 Controller Layer (`com.interswitch.controller`)

| Class               | Purpose                                                                 |
|---------------------|-------------------------------------------------------------------------|
| `UssdController`    | Exposes all USSD endpoints (`/menu`, `/menu-choice`, `/transfer/*`, `/airtime/*`, `/bills/*`, etc.). Delegates to `UssdMenuService`. |

### 4.2 Service Layer (`com.interswitch.service`)

| Class                    | Responsibility                                                                                  |
|--------------------------|-------------------------------------------------------------------------------------------------|
| `UssdMenuService`        | Orchestrates the main menu and delegates to category handlers. Includes audit logging.          |
| `TransferMenuHandler`    | Handles all transfer flows (self, other Olive, inter‑bank, e‑wallet). Stores session data.      |
| `AirtimeDataMenuHandler` | Handles airtime self/others, data purchase, and airtime advance.                                |
| `BillsMenuHandler`       | Handles cable TV payment, general bill payment, and balance enquiry.                            |
| `AccountServicesMenuHandler` | Handles open account, loan balance enquiry, and token generation.                           |
| `SecurityMenuHandler`    | Handles PIN change/reset, opt‑in, opt‑out.                                                      |
| `OAuth2TokenService`     | Fetches and caches OAuth2 bearer tokens from Interswitch Passport (supports test‑mode fake token). |
| `AuditLogService`        | Asynchronously logs every USSD step into PostgreSQL (sessionId, step, input, response, status). |
| `BankCacheService`       | Caches the list of commercial banks in Redis (24h TTL).                                        |

### 4.3 Client Layer (`com.interswitch.client`)

| Class                     | Purpose                                                                                     |
|---------------------------|---------------------------------------------------------------------------------------------|
| `BankApiClient`           | Calls BankOne endpoints (balance, internal transfer, account opening, PIN change) and staging mybankone APIs (name enquiry, bank list). |
| `InterswitchPaymentClient`| Calls Interswitch APIs (purchase, OTP validation, resend OTP, transaction status, name enquiry, single transfer, bill payment). Uses OAuth2 token. |

### 4.4 DTOs (`com.interswitch.dto`)

| Package            | Contents                                                                 |
|--------------------|--------------------------------------------------------------------------|
| `request`          | `UssdRequest` (msisdn, sessionId, input, sessionData, sessionStart)      |
| `response`         | `UssdResponse` (text, callbackUrl, sessionData, sessionEnd)              |
| `bank`             | `AccountDto`, `TransactionResultDto`, `BankDto`, `NameEnquiryResponse`, etc. |

### 4.5 Constants & Enums

| Class                  | Purpose                                                                 |
|------------------------|-------------------------------------------------------------------------|
| `UssdStep` (enum)      | Defines all possible USSD steps (MAIN_MENU, TRANSFER_SELF, etc.) for audit logging. |
| `SessionKeys` (final)  | Constants for session data keys (e.g., `AMOUNT`, `DEST_ACCOUNT`).        |
| `ErrorCode` (enum)     | Error codes for USSD failures (used in exceptions).                     |
| `TransactionStatus`    | SUCCESS, FAILED, PENDING.                                               |

### 4.6 Configuration (`com.interswitch.config`)

| Class                 | Purpose                                                                 |
|-----------------------|-------------------------------------------------------------------------|
| `OAuth2Properties`    | Binds `interswitch.oauth2.*` properties (clientId, secret, tokenUrl).   |
| `RestClientConfig`    | Provides `RestClient.Builder` bean and a preconfigured `bankRestClient`.|
| `BankApiProperties`   | Binds BankOne base URL, timeouts.                                       |
| `MockBankProperties`  | Configures mock accounts (for `h2` profile).                            |
| `RedisConfig`         | Configures RedisTemplate for caching bank list.                         |

### 4.7 Entities & Repository

| Class                | Purpose                                                       |
|----------------------|---------------------------------------------------------------|
| `AuditLog` (entity)  | Maps to `audit_logs` table (sessionId, msisdn, step, input, responseText, status, createdAt). |
| `AuditLogRepository` | Spring Data JPA repository for audit logs.                    |

### 4.8 Exception Handling

| Class                     | Purpose                                                         |
|---------------------------|-----------------------------------------------------------------|
| `GlobalExceptionHandler`  | Converts exceptions into USSD‑friendly error responses (max 140 chars). |
| `UssdFlowException`       | Thrown for invalid steps, missing session data, etc.            |
| `BankApiException`        | Thrown when BankOne or Interswitch calls fail.                  |

### 4.9 Utilities

| Class         | Remaining? | Notes                                                       |
|---------------|------------|-------------------------------------------------------------|
| `HmacUtils`   | DELETED    | Removed because we use OAuth2 (no HMAC).                    |
| `AesUtils`    | DELETED    | Removed because we use plain JSON over HTTPS.               |

---

## 5. USSD Menu Flow (from user perspective)

The menu is defined in `messages.properties` and rendered by the handlers. Below is the exact hierarchy:

```
*5151#
├── 1. Transfer
│   ├── 1. Self (Olive)
│   │   └── Enter amount → Enter PIN → Done
│   └── 2. Others
│       ├── 1. Olive Account
│       │   └── Amount → Destination account → PIN → Done
│       ├── 2. Other Bank
│       │   └── Amount → Account number → Select bank → Name enquiry → PIN → Done
│       └── 3. eWallet (OPAY, PalmPay, etc.)
│           └── Amount → Account/phone → Select provider → PIN → Done
├── 2. Airtime & Data
│   ├── 1. Airtime Self
│   │   └── Amount → Done (uses saved card/token)
│   ├── 2. Airtime Others
│   │   └── Phone number → Amount → PIN → Done
│   ├── 3. Buy Data
│   │   ├── 1. Self → Choose network → Choose plan → PIN → Done
│   │   └── 2. Others → Phone number → Network → Plan → PIN → Done
│   └── 4. Airtime Advance
│       └── Eligibility check → PIN → Airtime credited
├── 3. Bills & Utilities
│   ├── 1. Cable TV (DSTV, GOTV, Startimes)
│   │   └── Provider → Smartcard number → Bouquet → PIN → Done
│   ├── 2. Pay Bills (Electricity, Water, Internet, Education)
│   │   └── Category → Provider → Reference number → Amount → PIN → Done
│   └── 3. Check Balance
│       └── PIN → Display balance
├── 4. Account Services
│   ├── 1. Open Account
│   │   └── Full name → DOB → Phone → Account type (Savings/Current) → Confirm → Account number via SMS
│   ├── 2. Loan Balance
│   │   └── PIN → Display loan balance
│   └── 3. Generate Token
│       └── Amount (optional) → PIN → Token sent via SMS
└── 5. Security & Settings
    ├── 1. PIN Management
    │   ├── 1. Change PIN
    │   │   └── Old PIN → New PIN → Confirm → Done
    │   └── 2. Reset PIN
    │       └── Registered phone → OTP → New PIN → Confirm → Done
    └── 2. Opt In / Opt Out
        ├── 1. Opt In → PIN → Last 6 card digits → Confirmation
        └── 2. Opt Out → PIN → Last 6 card digits → Confirmation
```

---

## 6. Session Management

- **Stateless by design**: Interswitch sends a `sessionData` map in each request. The middleware returns an updated `sessionData` map and a `callbackUrl` pointing to the next step’s endpoint.
- **Key session variables** (see `SessionKeys`):
  - `amount`, `destAccount`, `bankCode`, `bankName`, `destName`, `dataPlan`, `cableProvider`, `smartcard`, `openAccountName`, etc.
- **Callback URLs** are predefined in `UssdController` (e.g., `/api/ussd/transfer/self/pin`). Each handler method stores the next callback in the response.

---

## 7. Security & Authentication

### 7.1 Outbound (to Interswitch)
- **OAuth2 Client Credentials** flow.
- `OAuth2TokenService` fetches a bearer token from `https://passport.interswitchng.com/passport/oauth/token` (sandbox or production).
- Token is cached until expiry (with a 60‑second buffer).
- All Interswitch API calls include `Authorization: Bearer <token>`.

### 7.2 Inbound (from Interswitch USSD gateway)
- No HMAC/AES – the gateway sends plain JSON over HTTPS.
- The filters `HmacVerificationFilter` and `AesDecryptionFilter` have been **removed**.
- Validation of the request is minimal (basic sanity checks in `UssdController`).

### 7.3 Test mode
- Set `interswitch.oauth2.test-mode=true` to bypass real token fetch and use a fake token (`"fake-test-token-for-sandbox"`).  
- All mock/stub calls in handlers return hardcoded success responses.

### 7.4 Production mode
- Set `test-mode=false`, provide real `clientId` and `clientSecret` from Interswitch.
- The same handler methods will execute real HTTP calls via `InterswitchPaymentClient`.

---

## 8. External API Integrations

### 8.1 BankOne (Core Banking)
| Endpoint (mock)          | Purpose                          | Real implementation later |
|--------------------------|----------------------------------|---------------------------|
| `GET /api/accounts/{msisdn}` | Fetch customer accounts           | BankOne equivalent        |
| `POST /api/transactions/airtime` | Debit account for airtime    | BankOne debit API         |
| `POST /api/transactions/transfer` | Internal transfer           | BankOne transfer API      |
| `POST /api/transactions/balance` | Check account balance        | BankOne balance API       |
| `POST /api/transactions/change-pin` | Change customer PIN       | BankOne PIN change API    |
| (account opening)        | Create new account               | BankOne onboarding API    |

### 8.2 Interswitch (Sandbox/Production)
| API Family               | Endpoints (examples)                                         |
|--------------------------|--------------------------------------------------------------|
| **Passport (OAuth2)**    | `POST /passport/oauth/token`                                 |
| **Card Payments**        | `POST /api/v3/purchases`, `POST /api/v3/purchases/otps/auths`, `POST /api/v3/purchases/otps/resend` |
| **Inter‑bank Transfer**  | `POST /transfer-service/api/v1/transfers/name-enquiry`, `POST /transfer-service/api/v1/transfers/single` |
| **Bills & Airtime**      | `POST /quicktellerservice/api/v5/Transactions` (bill advice) |
| **Wallet Payments**      | `POST /collections/api/v1/wallet-pay/initialize` etc.        |
| **Transaction Status**   | `GET /collections/api/v1/gettransaction.json`                |

---

## 9. Data Persistence

### 9.1 PostgreSQL (production)
- Table: `audit_logs`
  - `id` (UUID), `session_id`, `msisdn`, `step` (enum), `input`, `response_text`, `status` (enum), `error_code` (enum), `created_at`.
- Managed by Flyway migration (`V1__create_audit_logs.sql`).

### 9.2 Redis (Upstash)
- Key: `bank_list`
- Value: JSON array of banks (code, name)
- TTL: 24 hours
- Used by `BankCacheService` to avoid repeated calls to the staging API.

### 9.3 H2 (local development)
- In‑memory database used with `spring.profiles.active=h2`.  
- Mock accounts and transactions are stored in `MockBankService`.

---

## 10. USSD Simulator (HTML/JS)

- **File location**: `src/main/resources/static/ussd-simulator.html`
- **Behaviour**:
  - Starts with a welcome screen: “Welcome to OlivePay – Dial *5151# to start”
  - User types `*5151#` (virtual keypad or keyboard) and presses SEND.
  - Sends a POST request to `/api/ussd/menu` with session data.
  - Follows the callback URLs returned by the backend.
  - Displays the USSD text exactly as a feature phone would.
  - When `sessionEnd: true` is received, shows “Thank you for banking with us!” and resets to welcome screen after 2 seconds.
- **Purpose**: Full end‑to‑end testing without a real phone or Interswitch gateway.

---

## 11. Current Development Status

- ✅ All USSD menu flows implemented as stubs (no real external API calls yet).
- ✅ Mock BankOne client (in‑memory) for local testing.
- ✅ OAuth2 token service with test‑mode fake token.
- ✅ Redis caching for bank list.
- ✅ Asynchronous audit logging.
- ✅ HTML simulator fully functional.
- ✅ HMAC/AES filters removed – clean OAuth2 only.
- ⏳ **Pending**: Obtain production OAuth2 credentials from Interswitch.
- ⏳ Replace stub methods in handlers with real calls to `InterswitchPaymentClient` and `BankApiClient`.

---

## 12. How to Switch from Test to Production

1. **Obtain production credentials** from Interswitch (clientId, clientSecret, base URLs).
2. **Update `application.properties`**:
   ```properties
   interswitch.oauth2.test-mode=false
   interswitch.oauth2.client-id=PROD_CLIENT_ID
   interswitch.oauth2.client-secret=PROD_SECRET
   interswitch.api.base-url=https://api.interswitchng.com
   ```
3. **Remove mock data sources**: Replace `BankApiClient` endpoints with real BankOne URLs (provided by OlivePay).
4. **Deploy to production server** (Render, AWS, etc.).
5. **Inform Interswitch** of your production endpoint URL so they can route `*5151#` traffic.
6. **Test** with a real phone on a live SIM.

---

## 13. Future Enhancements (Optional)

- Webhook integration for transfer notifications.
- Support for additional billers (via Quickteller service enumeration).
- Admin dashboard for transaction monitoring.
- SMS notifications for transaction receipts.
- Multilingual USSD (English, Pidgin, Hausa, Yoruba, Igbo) using `MessageSource` with locale detection.

---

## 14. Troubleshooting Common Issues

| Issue                                      | Likely Cause                                      | Solution                                 |
|--------------------------------------------|---------------------------------------------------|------------------------------------------|
| Simulator shows “Network error”            | Backend not running on port 8080                  | Start Spring Boot app                    |
| Session data lost                          | Callback URL not set or wrong                     | Check handler returns `callbackUrl`      |
| OAuth2 token error (real mode)             | Invalid clientId/secret or wrong token URL       | Verify credentials, use correct env      |
| Stub returns success immediately           | `test-mode=true` still active                     | Set `test-mode=false` and implement real API calls |
| Bank list not cached                       | Redis not reachable                               | Check Redis configuration (Upstash)      |

---

## 15. Credits & Contact

- **Developer**: Ike Emmanuel (via this conversation)
- **Project Manager**: [Name]
- **Integrations**: Interswitch (payment) + BankOne (core banking)
- **Shortcode**: `*5151#` (OlivePay MFB)

---

This document is the complete knowledge base of the OlivePay USSD middleware. Any AI or developer reading this should be able to understand the system architecture, menu flow, session handling, external integrations, and the exact steps to take the project from its current test state to production.
