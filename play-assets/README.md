# Play Store release assets

## 1. Signing key (required for Play App Signing upload)

1. Create `release/` and generate a **private** keystore (keep backups off-repo):

   ```bash
   mkdir -p release
   keytool -genkeypair -v -storetype PKCS12 \
     -keystore release/insightly-release.jks \
     -alias insightly \
     -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Copy `keystore.properties.example` → **`keystore.properties`** in the project root and fill in paths/passwords.  
   `keystore.properties` is gitignored.

3. Build an App Bundle:

   ```bash
   ./gradlew :app:bundleRelease
   ```

   Output: `app/build/outputs/bundle/release/app-release.aab` — upload this in Play Console.

---

## 2. Graphics (Python)

Install deps once:

```bash
python3 -m venv .venv
source .venv/bin/activate   # Windows: .venv\Scripts\activate
pip install -r scripts/requirements.txt
```

**Default (recommended):** renders the **same wallet / coin / tracker artwork** and **blue diagonal gradient** as the adaptive launcher icon (`ic_launcher_*`). No image file needed:

```bash
python3 scripts/generate_play_assets.py
```

Outputs land in **`play-assets/generated/`**:

| File | Use on Play Console |
|------|---------------------|
| `play_store_icon_512.png` | **App icon** (512 × 512) |
| `feature_graphic_1024x500.png` | **Feature graphic** (1024 × 500) |

Optional: use your own **square PNG** instead:

```bash
python3 scripts/generate_play_assets.py --input path/to/your_logo.png
```

Gradient overrides (defaults match **`#1E88E5` → `#0D47A1`** like `ic_launcher_background.xml`):

```bash
python3 scripts/generate_play_assets.py --primary "#1E88E5" --secondary "#0D47A1"
```

Optional: generate **legacy launcher PNGs** for `mipmap-*` folders (some older devices):

```bash
python3 scripts/generate_play_assets.py --write-launcher-res
```

Adaptive icons (`mipmap-anydpi-v26`, vectors) stay as in the project; this only adds raster fallbacks.

---

## 3. Screenshots (adb — six tabs)

Play Console needs **phone** screenshots (min **2**, up to **8** per form factor). This repo includes an **adb** script that opens each bottom-nav destination via an in-app deep link and saves a PNG.

**Before you run:**

1. Install a build on the device/emulator: `./gradlew :app:installDebug`
2. **Turn off biometric lock** in Settings (otherwise the gate blocks automation).
3. **adb** — the script looks for `adb` on your `PATH`, then `~/Library/Android/sdk/platform-tools/adb` (Android Studio on macOS). If it still fails, install **SDK Platform-Tools** in Android Studio (SDK Manager) or run:  
   `export PATH="$HOME/Library/Android/sdk/platform-tools:$PATH"`
4. `adb devices` shows your hardware or emulator as `device`.

**Capture all six tabs** (Home → Ledger → Insights → Budget → Vault → Settings):

```bash
chmod +x scripts/capture_play_store_screenshots.sh
./scripts/capture_play_store_screenshots.sh
```

Outputs **`play-assets/screenshots/`**:

| File | Tab |
|------|-----|
| `01_home.png` | Home |
| `02_ledger.png` | Ledger |
| `03_insights.png` | Insights |
| `04_budget.png` | Budget |
| `05_vault.png` | Vault |
| `06_settings.png` | Settings |

Slow device or heavy UI: `SCREENSHOT_DELAY=3.5 ./scripts/capture_play_store_screenshots.sh`

Deep links used (also usable manually): `insightlyspend://nav/dashboard`, `…/ledger`, `…/analytics`, `…/budget`, `…/receipts`, `…/settings`.

**One-off adb** (any screen):

```bash
adb exec-out screencap -p > play-assets/screenshots/custom.png
```

---

## 4. Store listing text (copy into Play Console)

### App name (max 30 characters)

**Insightly Spend**

---

### Short description (max 80 characters)

```
Offline finance: home, ledger, insights, budgets, receipt vault. EN & Arabic.
```
*(79 characters including spaces and punctuation.)*

---

### Full description (paste below; under 4000 characters)

```
Insightly Spend helps you manage everyday money on Android — budgets, transactions, and receipts stay on your phone first (Room database). No sign-up required to start.

HOME — Dashboard
• Total balance across your wallets (accounts)
• This month’s income vs spending
• Budget progress when you set category limits
• Seven-day spending trend
• Recent activity + quick add (+)

LEDGER — Transactions
• Search notes; filter by date range, cash-only, and more
• Swipe to duplicate or delete
• Grouped by Today, Yesterday, or calendar date

INSIGHTS — Analytics
• On-device forecasts, month comparison, category breakdown, and anomaly-style hints

BUDGET — Limits
• Per-category monthly budgets with spend vs limit

VAULT — Receipts
• Attach photos from gallery or camera to transactions

SETTINGS
• Currency, light/dark/system theme, optional biometric lock
• Interface language: English or Arabic

Your financial entries are processed and stored locally on the device. Android backup may apply depending on your device settings.

Support: michaelsam00@yahoo.com
```

---

## 5. Before each Play upload

- Bump **`versionCode`** (integer, must increase every upload) and **`versionName`** in `app/build.gradle.kts` → `defaultConfig`.
- Run `./gradlew :app:bundleRelease` with `keystore.properties` present.
- In Play Console: complete **Data safety**, **Content rating**, **Target audience**, and **Store listing** for each locale you ship.
