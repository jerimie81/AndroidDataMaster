# Implementation Directions for Secondary AI Coder

## Objective
Use these directions to implement **part** of the Android native app described in `docs/app-blueprint.txt`.

You are responsible for generating the Android-side files only:
- `app/src/main/res/layout/activity_main.xml`
- `app/src/main/res/layout/item_chat_message.xml`
- `app/src/main/res/drawable/bg_chat_bubble.xml`
- `app/src/main/java/com/jerimie/acrdai/ChatMessage.java`
- `app/src/main/java/com/jerimie/acrdai/ChatAdapter.java`
- `app/src/main/java/com/jerimie/acrdai/MainActivity.java`

Do **not** implement Rust files or Gradle Rust integration in this pass.

## Hard Requirements
1. Native Android only (Java + XML). No web stack or JavaScript.
2. Package must be exactly: `com.jerimie.acrdai`.
3. `MainActivity` must load native library:
   - `System.loadLibrary("rust_core");`
4. `MainActivity` must declare native method:
   - `private native String processGeminiPrompt(String prompt);`
5. JNI call must run on a background `ExecutorService`.
6. UI updates must occur on main thread using `Handler(Looper.getMainLooper())` or equivalent.
7. Match layout behavior:
   - Chat history in `RecyclerView`
   - Bottom-pinned message input row with `EditText` + send button
8. Bubble alignment/color behavior in adapter:
   - user message aligned END
   - AI message aligned START
9. Keep code production-safe:
   - prevent empty prompt sends
   - append response/error message safely
   - scroll RecyclerView to latest message

## File-by-File Guidance

### 1) `activity_main.xml`
- Root: `ConstraintLayout`
- Child A: `RecyclerView` occupying available space
- Child B: bottom `LinearLayout` with:
  - `EditText` (weight=1)
  - send `Button`
- Use IDs:
  - `@+id/recyclerViewChat`
  - `@+id/editTextPrompt`
  - `@+id/buttonSend`

### 2) `item_chat_message.xml`
- Container can be `FrameLayout` or `LinearLayout` full width.
- Include one message `TextView` with ID:
  - `@+id/textViewMessage`
- Apply bubble background drawable (`@drawable/bg_chat_bubble`) and reasonable paddings/margins.

### 3) `bg_chat_bubble.xml`
- `<shape android:shape="rectangle">`
- rounded corners 16dp
- a neutral default solid color (adapter can override with tint/background color)

### 4) `ChatMessage.java`
- Immutable model:
  - `private final String text;`
  - `private final boolean isUser;`
- Constructor + getters.

### 5) `ChatAdapter.java`
- RecyclerView adapter over `List<ChatMessage>`.
- ViewHolder binds `textViewMessage`.
- In `onBindViewHolder`:
  - set text
  - set gravity/alignment START/END based on sender
  - apply different bubble/text colors for user vs AI
- Keep implementation simple and deterministic.

### 6) `MainActivity.java`
- Initialize `RecyclerView`, `ChatAdapter`, backing `ArrayList<ChatMessage>`.
- Setup `ExecutorService` (single-thread executor is fine).
- On send button click:
  1. read/trim prompt
  2. ignore if empty
  3. append user message + clear input
  4. run JNI method in executor
  5. post AI result back to main thread and append AI message
- Add helper method to append message and auto-scroll.
- Shutdown executor in `onDestroy()`.

## Acceptance Checklist
- App compiles with Java/XML resources present.
- Sending a prompt does not block UI thread.
- RecyclerView updates correctly for both user and AI messages.
- Message bubbles are visibly distinct by sender.

## Output Format Expected from Secondary AI
Return sections in this order:
1. `app/src/main/res/layout/activity_main.xml`
2. `app/src/main/res/layout/item_chat_message.xml`
3. `app/src/main/res/drawable/bg_chat_bubble.xml`
4. `app/src/main/java/com/jerimie/acrdai/ChatMessage.java`
5. `app/src/main/java/com/jerimie/acrdai/ChatAdapter.java`
6. `app/src/main/java/com/jerimie/acrdai/MainActivity.java`

Each section must include complete file contents in fenced code blocks.
