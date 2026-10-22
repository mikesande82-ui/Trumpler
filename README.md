# Rambler

Turns any message into a long, winding ramble in the style of a certain
president's unscripted speeches: tangents, superlatives, a big strong guy with
tears in his eyes calling you "sir", and then back to the point as if that was
the plan all along. Your actual message always survives, so the person you
send it to can still work out what you meant.

> **In:** Running late, be there in 10.
>
> **Out** (offline phrase book, Medium): Look, I'll say it. Running late, be there in 10. Everybody knows it.
> And it's 10, okay? Not 11. A lot of people are saying 11. It's 10. And the
> windmills. Don't get me started on the windmills. Anyway, where was I? Oh,
> right. Running late, be there in 10. Believe me.

This is the first prototype: an Android app that adds **Ramble** to the
text-selection menu of every app, plus a small server that writes the rambles
with Claude. The app also has a built-in offline phrase book, so it works
before the server exists.

## What's here

| Path | What it is |
| --- | --- |
| `android/` | The Android app (Kotlin, no libraries beyond Android itself). |
| `android/app/src/main/java/app/rambler/OfflineRambler.kt` | The offline phrase-book rambler. |
| `api/ramble.js` | The server: one Vercel function that asks Claude for a ramble. The voice lives in its `SYSTEM` prompt. |
| `scripts/try.js` | Try the prompt from the command line. |
| `test/` | Server tests (`npm test`). |
| `.github/workflows/ci.yml` | Runs both test suites and builds the APK on every push. |

## 1. Try it on your phone

1. On GitHub, open **Actions**, click the latest green **CI** run, and
   download **rambler-debug-apk** at the bottom. It's a zip with
   `app-debug.apk` inside.
2. Open the APK on your Android phone. It will ask you to allow installs from
   that app (your browser or Files); allow it once.
3. Open **Rambler** and try a message. Without a server it uses the offline
   phrase book.
4. The real use: in any app, select some text, tap **⋮** or **More** in the
   menu that pops up, then **Ramble**. In a box you're typing in, **Replace**
   swaps your text for the ramble. Elsewhere, use **Copy**.

Each CI build is signed with a fresh debug key, so Android refuses to install
a new build over an old one. Uninstall Rambler first, then install the new APK.

## 2. Turn on AI rambles

The server holds your Anthropic API key, so the key never goes in the app.

1. **API key.** In the [Claude Console](https://console.anthropic.com), create
   an API key. While you're there, set a **monthly spend limit** on the
   workspace. The app key below keeps casual callers out, but anyone who
   unpacks the APK can read it, so the spend limit is what caps the bill.
2. **Deploy on Vercel.** Add New → Project → import this repo. Before
   deploying, add these environment variables:
   - `ANTHROPIC_API_KEY`: the key from step 1.
   - `RAMBLE_APP_KEY`: make up a long random string, for example the output of
     `openssl rand -hex 16`.
   - `RAMBLE_MODEL` (optional): see "Model and cost" below.

   Deploy. Your server address is `https://<project>.vercel.app/api/ramble`.
3. **Check it.**
   ```sh
   curl -X POST https://<project>.vercel.app/api/ramble \
     -H 'Content-Type: application/json' -H 'X-App-Key: <your RAMBLE_APP_KEY>' \
     -d '{"text":"Running late, be there in 10","level":"full"}'
   ```
4. **Point the app at it.** Either type the address and app key into the app's
   **AI rambles** section and tap Save, or add them as GitHub repo secrets
   named `RAMBLE_SERVER_URL` and `RAMBLE_APP_KEY` (Settings → Secrets and
   variables → Actions). Every APK built after that starts out connected.

If the server is down, out of budget, or declines a message, the app quietly
falls back to the phrase book and says so under the ramble.

## Model and cost

| `RAMBLE_MODEL` | About per ramble | Notes |
| --- | --- | --- |
| `claude-opus-5-5` (default) | 1¢ | The stronger writer. A few seconds per ramble. |
| `claude-haiku-5-5` | $0.0002 (about 5,000 per dollar) | Much faster and cheaper. Worth comparing. |

Requests run at low effort, since speed matters here. `RAMBLE_EFFORT=medium`
gives the model more room to think. On Opus, server-side fallback is on: if
Claude's safety checks decline a message, the API retries it on a fallback
model before giving up.

## Tuning the voice

The style and the rules (keep the facts, stay apolitical, never name the man)
are in the `SYSTEM` prompt at the top of `api/ramble.js`. To try changes
without deploying:

```sh
npm install
ANTHROPIC_API_KEY=sk-ant-... npm run try -- "Can you grab milk on the way home?" full
```

The offline phrase book is the lists at the bottom of `OfflineRambler.kt`. Add
tangents freely.

## Development

```sh
npm test                                  # server tests
cd android && ./gradlew testDebugUnitTest assembleDebug   # needs the Android SDK
```

Or open `android/` in Android Studio.

## Privacy

Only the text you choose to ramble leaves the phone, and only when AI rambles
are on. The server stores nothing and logs only the message's length, the
model, and token counts. Messages pass through Anthropic's API under its
standard data terms.

## Next steps

- **A real keyboard.** Android: an `InputMethodService` with a Ramble key that
  reads the text before the cursor and replaces it. iPhone: a keyboard
  extension. iOS has no selection-menu equivalent, so the keyboard is the only
  route there. It needs "Allow Full Access" for AI rambles and must still
  type, and ramble offline, without it.
- **Store listing.** Keep the president's name, photo and likeness out of the
  app name, icon and screenshots. That's the most likely cause of a rejection,
  and "TRUMP" is a registered trademark. Describe the style in the description
  instead.
- **Release signing** for the Play Store, and a stable signing key so updates
  install over old builds.
