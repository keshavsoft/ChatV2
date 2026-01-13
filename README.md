# AndroidMUI5 – Jetchat Extensions (SMS & Voice to Text)

AndroidMUI5 is an extended version of the official **Jetchat** sample app from Google, enhanced with:

- Multiple **SMS Inbox** visualizations (V1–V5)
- A rich **Voice to Text lab** (V1–V4) built with Jetpack Compose and Android’s `SpeechRecognizer`
- A collapsible **navigation drawer** with neatly organized sections for Chats, SMS versions, and Voice-to-Text versions

The goal of this project is to **experiment with UI/UX patterns** for text-based communication and speech input, while staying clean, modern, and easy for new developers to extend.

---

## ✨ Features

### 📨 Chats & Navigation

- Based on the original **Jetchat** (Jetpack Compose + Material 3)
- Left-side **Navigation Drawer** with:
  - **Chats**
    - Composers (original Jetchat screen)
    - TestByKeshav
    - Droidcon-nyc
    - Gps
  - **SMS** (collapsible group)
    - SMS Inbox (base)
    - SMS Inbox V1
    - SMS Inbox V2
    - SMS Inbox V3
    - SMS Inbox V4
    - SMS Inbox V5
  - **Voice to Text** (collapsible group)
    - Voice to Text V1
    - Voice to Text V2
    - Voice to Text V3
    - Voice to Text V4
- Recent Profiles + Settings + “Add Widget to Home Page” (from original Jetchat)

---

### 📱 SMS Inbox Variants (V1–V5)

Under **SMS** you’ll find multiple versions of the SMS Inbox UI.  
Each version experiments with different layouts / grouping styles:

- **SmsListScreen** + **SmsDetailScreen** (base)
- **SmsListScreenV3 / V4 / V5** etc. with alternative list and detail presentations

These are useful to:
- Compare list designs
- Try different item visuals (chips, cards, dividers)
- Test how a consistent navigation pattern handles multiple versions

---

### 🎙 Voice to Text Lab (V1–V4)

All Voice screens use Android’s **`SpeechRecognizer` API** and runtime **RECORD_AUDIO permission**.

#### 🔹 V1 – Simple Voice to Text

`VoiceToTextScreenV1`

- Clean, minimal UI
- A single button to **start/stop listening**
- One big text box showing the **accumulated transcript**
- **Clear** and **Copy** actions at the bottom

Use case: simplest “dictation pad” experience.

---

#### 🔹 V2 – Session Card + Transcript

`VoiceToTextScreenV2`

- Center-aligned Top App Bar: `Voice to Text – V2`
- Top **“Session” card** with:
  - Status text (Listening / Speak now / Tap to start again)
  - Tonal **Start/Stop Listening** button
- Secondary **Transcript** card:
  - Header: “Transcript”
  - Scrollable area with full text
- **Clear** & **Copy** buttons

Use case: more “professional” / clinic-style dictation UI.

---

#### 🔹 V3 – Live Text + History + Floating Mic

`VoiceToTextScreenV3`

- Emphasis on **live, streaming speech recognition**
- Uses `EXTRA_PARTIAL_RESULTS = true`
- UI Layout:
  - Card for **Live text** (current partial result)
  - Card for **Transcript history** (finalized sentences)
  - Centered **FloatingActionButton mic** at bottom
- Bottom row: **Clear** and **Copy**

Use case: when user wants to watch text appear while speaking, and see past sentences separately.

---

#### 🔹 V4 – Chat Bubble Style with Per-Bubble Editing

`VoiceToTextScreenV4`

- Each recognized sentence is converted into a **chat-style bubble**
- Bubbles are right-aligned (as if user messages)
- **Partial (live) text** shown as a faded bubble while speaking
- Top status line: “Tap the mic and start speaking”
- **Floating mic FAB** for start/stop
- **Per-bubble options**:
  - Three-dot (⋮) menu on each bubble
  - **Edit** – opens dialog to modify the text
  - **Delete** – removes that bubble
- App bar action: **Copy all**, which joins all bubbles + live text

Use case: when you want spoken sentences to behave like editable chat messages.

---

## 🏗 Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Design**: Material 3 (M3)
- **Navigation**:
  - Custom drawer with sealed class `DrawerDestination`
  - NavHostFragment for legacy fragments (ContentMainBinding) + Compose for new screens
- **Speech Recognition**:
  - Android `SpeechRecognizer`
  - `RecognizerIntent.ACTION_RECOGNIZE_SPEECH`
  - Partial and full results via `RecognitionListener`
- **Permissions**:
  - Runtime permission for `Manifest.permission.RECORD_AUDIO`
  - Permission handling inside each VoiceToTextX screen

---

## 📂 Project Structure (simplified)

```text
app/
 └── src/main/java/com/example/compose/jetchat/
     ├── NavActivity.kt               # Main host activity with drawer + content
     ├── core/navigation/
     │    └── DrawerDestination.kt    # Sealed class for drawer items
     ├── components/
     │    └── JetchatDrawer.kt        # Drawer UI + collapsible groups
     ├── feature/
     │    ├── sms/
     │    │    ├── SmsListScreen*.kt
     │    │    └── SmsDetailScreen*.kt
     │    └── voicetotext/
     │         ├── VoiceToTextScreenV1.kt
     │         ├── VoiceToTextScreenV2.kt
     │         ├── VoiceToTextScreenV3.kt
     │         ├── VoiceToTextScreenV4.kt
     │         └── VoiceModels.kt     # Shared data classes like VoiceBubble
     ├── data/
     ├── theme/
     └── widget/
res/
 ├── layout/
 ├── values/strings.xml               # Menu labels, VoiceToText versions, etc.
 └── ...


V6 started

V6 websocket perfect

V7 websocket incom perfect

new main menu added for chat

new main menu added for chat and V1 is perfect, not from ws but from fake data

ChatWs V1 menu click fixed

menu no crash