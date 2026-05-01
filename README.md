# Insightly Spend

A local-first personal finance app for Android. Data is stored in **Room**; lists and summaries use **observed queries** so changes propagate across tabs when you add, remove, or duplicate transactions.

The bottom navigation has **six** sections: **Home**, **Ledger**, **Insights**, **Budget**, **Vault** (receipts), and **Settings**.

---

## Home (Dashboard)

**Purpose:** High-level snapshot of your finances and a shortcut to add a transaction.

| UI element | What it does |
|------------|----------------|
| **Overview** card | **Total balance** — sum of all wallet (account) balances. **Income / Spend** — this month’s total income vs total expenses. |
| **Budget progress** (if you have category budgets) | **Linear bar** — spend vs total monthly budget cap. **“Spent X of Y”** — numeric comparison. |
| **Daily spending (7 days)** | Line chart of **expense** totals per day for the last seven calendar days. |
| **Recent transactions** | Up to **five** latest rows (category, note, amount). Tapping the FAB is the main way to add more; the list updates when the database changes. |
| **Floating action button (+)** | Opens **Quick add** (see below). |

---

## Quick add (bottom sheet)

Opened from the **Home** FAB. Creates a new transaction in Room and updates account balance.

| Field / control | What it does |
|-----------------|--------------|
| **Amount** | Transaction amount (decimal). |
| **Category** | Picker of all categories; label follows your app language (English/Arabic names when set). |
| **Wallet** | Account to book the transaction against (affects **total balance**). |
| **Income** | If on, the line is treated as income; if off, as spending. |
| **Cash payment** | If on, payment method is **cash**; if off, **card**. |
| **Recurring template** | Marks the row for recurring/repeat logic where supported. |
| **Debt / lending** | **Standard** / **I owe** / **Owed to me** — how the amount relates to debt tracking. |
| **Note** | Optional text (searchable from the ledger). |
| **Choose from gallery** / **Take photo** | **Gallery** opens the **system photo picker** (images only). **Take photo** launches the device camera and saves into app cache via `FileProvider`; the URI is stored on the transaction so it appears under **Vault**. **Remove image** clears the selection before save. |
| **Save** | Inserts the transaction (with optional receipt URI) and closes the sheet. |

---

## Ledger

**Purpose:** Full searchable list of transactions with filters and swipe actions.

| UI element | What it does |
|------------|----------------|
| **Search notes** | Filters rows whose **note** contains the text (case-insensitive). |
| **Cash only** | When selected, only **cash** payment-method transactions. |
| **This month** | Restricts to **start of current month → now** (and can stack with cash filter). |
| **Clear filters** | Removes **date range** and **payment** filters (search is separate). |
| **Day group headers** | “Today”, “Yesterday”, or a calendar date; rows are grouped by day. |
| **Each card** | Category icon, **category name** (localized), **note**, **time**, and **signed amount** (green + income, red − spend). |
| **Swipe end → start** | **Delete** that transaction (updates balance). |
| **Swipe start → end** | **Duplicate** — creates a **new** row with the same details and **current** time; original row stays. |

The ledger is driven by a **Room `Flow`**, so it stays in sync with the database.

---

## Insights (Analytics)

**Purpose:** On-device analytics: forecast, month comparison, category mix, and simple anomaly hints.

| Section | What it does |
|---------|----------------|
| **Title** | Screen heading. |
| **Predictive forecast** | Estimated **month-end spend**, optional **confidence range**; method name may reflect linear regression and/or a bundled ML model. |
| **Month comparison** | **This month** vs **last month** spend, and **percent change** when available. |
| **Category breakdown** | **Pie** and **bar** charts of share of spending by category (for the reporting window the engine uses). |
| **Anomaly hints** | Short bullet lines with a **z-score**-style stat for unusual days/categories, when the report includes any. |

This screen is built from a **computed report**; pull-to-refresh or reopening the tab refreshes it (it is not the same live `Flow` pipeline as Home, unless you extend it).

---

## Budget

**Purpose:** See **per-category monthly budgets** that you have set, with spend vs limit; add or manage those categories from here.

| UI element | What it does |
|------------|----------------|
| **Title + hint** | Explains that alerts can fire around high usage. |
| **Loading / error** | States while data loads or if something fails. |
| **Each category card** | **Name** (localized), **Remaining**, **Spent X of Y**, **progress bar**. Can highlight when past a threshold. |
| **Edit (pencil)** | Opens a sheet: **English/Arabic names** (per language rules), **monthly budget** for that category; **Save** updates Room. |
| **Delete (bin)** | Only if more than one category exists in the app. Confirms that **transactions move** to another category and **recurring rules** for that category are removed. |
| **FAB (+)** | **New budget category** — bilingual **names**, **monthly budget amount**, creates the category with a limit so it appears here. |

Only categories that have a **monthly budget limit** appear as rows on this tab.

---

## Vault (Receipts)

**Purpose:** Grid of transactions that have an **attachment image** (receipt photo). Attach images when creating a transaction via **Home → Quick add → Choose from gallery** or **Take photo**; those rows show up here automatically.

| UI element | What it does |
|------------|----------------|
| **Title + hint** | Describes storing image URIs on transactions in the database. |
| **Each tile** | **Thumbnail** of the image (or **Attachment** placeholder if the URI cannot be loaded), **category**, optional **note**. |

---

## Settings

**Purpose:** Categories and recurring rules, app preferences, security, and exports.

### Categories

| UI element | What it does |
|------------|----------------|
| **Section title** | “Categories” — manage all categories. |
| **Add category** | Opens sheet with **English** and **Arabic** names (required/optional rules follow **interface language**). |
| **Each row** | Category label; **edit** opens the same bilingual editor; **delete** (if not the last category) merges transactions and clears recurring rules for that category (with confirmation). |

### Recurring transactions

| UI element | What it does |
|------------|----------------|
| **Add rule** | Opens editor for a **monthly** recurring rule. |
| **Each rule card** | Shows **category**, **amount**, **day of month**, **wallet**, optional note; **edit** / **delete**. |
| **Rule editor fields** | Amount, **category**, **wallet**, **day (1–28)**, **income**, **cash vs card**, **debt mode**, **note**, **Save**. Saving enqueues a **one-time** background job so a charge can post **soon** if **today’s calendar day** matches the rule’s day (once per month). Daily periodic work also runs as a backup. Posted rows appear on **Home** and **Ledger** like any other transaction (Room flows). |

### Rest of Settings

| UI element | What it does |
|------------|----------------|
| **Currency** | Chips (USD, EUR, GBP, AED, SAR, EGP, JPY, INR, …) — **display/formatting** for amounts. |
| **Language** | **System**, **English**, or **Arabic** — UI strings and category/name picking behavior. |
| **Theme** | **System**, **Light**, or **Dark**. |
| **Biometric lock** | When enabled, unlocking uses device biometrics before showing the app (see startup gate). |
| **Export CSV / Export PDF** | Generates files and opens the **Android share sheet** so you can save or send backups/summaries. |

---

## Startup & theme

- **MainActivity** applies **language** (via `AppCompatDelegate`) and **Compose theme** from saved preferences.
- If **biometric lock** is on, **BiometricGate** asks you to authenticate before **MainShell** (tabs) is shown.

---

## Data flow (single source of truth)

- **Transactions, accounts, categories, recurring rules** live in **Room**.
- **Home** summary reacts to **database changes** via observed flows (transactions, accounts, categories); collectors use **eager** sharing so updates still arrive when other tabs were foreground (e.g. recurring worker inserts a row).
- **Ledger** observes filtered transactions as a **Flow** (same eager behavior).
- **Budget** and **Categories** sections observe categories / budget calculations as appropriate after writes.

For questions about building or extending the project, refer to `build.gradle` files and the package layout under `app/src/main/java/com/michael/insightlyspend/`.
