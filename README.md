# CallGuard

AI-powered phone assistant for Android.

## Structure

```
CallGuard/
├── app/                    # Android application
│   ├── src/main/
│   │   ├── java/com/callguard/
│   │   │   ├── core/       # App, Constants, Permissions
│   │   │   ├── dialer/     # Dialer UI
│   │   │   ├── call/       # Call services & activities
│   │   │   ├── contacts/   # Contact lookup
│   │   │   ├── history/    # Call history database
│   │   │   └── ai/         # AI call handler
│   │   └── res/            # Layouts, drawables, values
│   └── build.gradle.kts
├── backend/                # FastAPI server
│   ├── ai/                 # STT, LLM, TTS modules
│   ├── main.py
│   └── requirements.txt
└── README.md
```

## Build Android

1. Open in Android Studio
2. Sync Gradle
3. Build & Run on device

## Deploy Backend

1. Set environment variables:
   - `DEEPGRAM_API_KEY`
   - `GROQ_API_KEY`

2. Deploy to Railway/Fly.io:

```bash
cd backend
fly launch
fly secrets set DEEPGRAM_API_KEY=xxx GROQ_API_KEY=xxx
fly deploy
```

3. Update `Constants.kt` with backend URL

## Permissions Required

- READ_PHONE_STATE
- CALL_PHONE
- ANSWER_PHONE_CALLS
- READ_CONTACTS
- RECORD_AUDIO
- FOREGROUND_SERVICE

## AI Activation Rule

```
IF incoming_call AND number NOT IN contacts
→ AI intercepts
ELSE
→ Normal phone behavior
```
