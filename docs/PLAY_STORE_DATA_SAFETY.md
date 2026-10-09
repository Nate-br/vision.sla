# Google Play Store Data Safety & Financial Services Declaration

## 1. Google Play Financial Services Declaration (S-505)
**Organization:** Vision for a Better Life Savings and Credit Cooperative Society (SLA)  
**Jurisdiction:** Federal Democratic Republic of Ethiopia  
**Regulatory Authority:** Federal Cooperative Agency / National Bank of Ethiopia (NBE) Directives  
**Category:** Licensed Financial Institution / Credit Cooperative (Non-Bank SACCO)  

### Disclosures & Terms (Plan Section 13):
- **Loan Tenure:** 1 to 24 Months.
- **Maximum Annual Percentage Rate (APR):** 12.0% p.a.
- **Representative Example:**
  - Loan Principal: ETB 10,000.00
  - Tenure: 12 months
  - Interest (12% flat): ETB 1,200.00
  - Administrative processing fee (1%): ETB 100.00
  - Total Repayment: ETB 11,200.00 in 12 equal monthly installments of ETB 933.33.
- **Cooling-Off / Cancellation Period:** 3 business days from application submission before disbursement.
- **No Deceptive Lending:** No loans with repayment terms under 60 days unless explicitly classified as emergency salary advances under collective bargaining cooperative agreements.

---

## 2. Play Store Data Safety Form (S-504)

### Data Collection & Usage:
| Data Type | Collected | Shared | Purpose | Ephemeral? |
|---|---|---|---|---|
| **Phone number** | Yes | No | Account management, 2FA OTP, SMS notifications | No |
| **Full name & DOB** | Yes | No | Regulatory KYC identification (Fayda / National ID) | No |
| **National ID / Selfie** | Yes | No | Fraud prevention, Identity verification | No (Encrypted) |
| **Financial info (Savings, Loans)** | Yes | No | Cooperative financial service provision | No (Encrypted) |
| **Payment Receipts** | Yes | Forwarded to verify.et API switch | Automated transaction verification | Forwarded securely |
| **Device IDs / Integrity signals** | Yes | No | Play Integrity fraud prevention & account security | Ephemeral |

### Security Practices:
- **Data Encrypted in Transit:** Yes (TLS 1.3 + SPKI SHA-256 certificate pinning).
- **Data Encrypted at Rest:** Yes (PostgreSQL AES-256 + Android SQLCipher Keystore key).
- **Account Deletion Flow:** Yes — available in-app and via API (`DELETE /v1/members/me`).
