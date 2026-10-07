# Review Checklists

These checklists are tailored to this repo, but they also fit many small application codebases with clear layers.

## Business Logic / Services

What to look for:

- Are task rules implemented in `TaskManager` instead of leaking into the UI layer?
- Do operations preserve id stability and task state consistency?
- Is the logic easy to test without the terminal or file system?

Common failure modes:

- accidentally reusing ids
- allowing invalid state transitions
- mixing formatting logic and domain logic in a way that makes changes harder

Good review questions:

- If this behavior changes, what invariant might break?
- Is this rule really part of the service layer, or did UI logic sneak in here?
- Does the code still behave correctly after loading existing tasks from disk?

Subtle bugs a junior dev might miss:

- returning internal mutable state directly
- updating `completed` without updating `completedAt`
- adding new task fields but forgetting to initialize them in all creation paths

## Routes / Controllers / Handlers

In this repo, `App` plays the role that a controller or handler would play in a web app.

What to look for:

- Does `App` only coordinate input, output, and calls into other classes?
- Is invalid input handled gracefully?
- Are startup and shutdown behaviors still reliable?

Common failure modes:

- putting business rules directly into the menu methods
- fragile parsing of user input
- forgetting to save or load at the right lifecycle point

Good review questions:

- If the input is bad, what exactly happens?
- Does this handler stay readable as features grow?
- Is the user-facing message consistent with the actual result?

Subtle bugs a junior dev might miss:

- early returns that skip important cleanup
- inconsistent handling of invalid ids across different menu actions
- new features increasing `App` complexity until it becomes the de facto service layer

## Persistence And Data Flow

What to look for:

- Does the save format still match the load format?
- Are missing files treated as normal when appropriate?
- Are parsing failures surfaced clearly enough?

Common failure modes:

- changing one field name in save logic but not load logic
- introducing ambiguous serialization
- assuming user-entered text is always safe for a line-based format

Good review questions:

- What happens to existing saved data after this change?
- Can this field contain characters that break the format?
- Is corrupted data handled in a safe and understandable way?

Subtle bugs a junior dev might miss:

- newline characters inside titles or descriptions breaking parsing
- invalid boolean text silently reading as `false`
- introducing a new field without backward-compatibility thinking

## Frontend State And UI Behavior

This repo has no browser frontend, but the terminal UX still has state and flow.

What to look for:

- Is the menu loop easy to follow?
- Are prompts clear and consistent?
- Does the output make task state obvious?

Common failure modes:

- confusing prompts
- unclear success or failure messages
- output format changes that hide important information

Good review questions:

- Would a first-time user know what to type here?
- Does the list output make completed vs incomplete tasks obvious?
- Is the app resilient to repeated mistakes by the user?

Subtle bugs a junior dev might miss:

- whitespace handling causing unexpected empty values
- output changes that make debugging saved state harder

## Async Code And Side Effects

Observed from code:

- This project is synchronous and single-threaded.

What to look for anyway:

- Where side effects happen
- Whether file reads and writes happen at predictable times
- Whether time-based values are assigned in one place

Common failure modes in similar codebases:

- scattering side effects across many layers
- mixing state updates and I/O in a way that is hard to reason about

Good review questions:

- Could this side effect happen twice?
- If the app fails midway, what state is left behind?

Subtle bugs a junior dev might miss:

- assuming side effects are harmless because the project is small
- adding background behavior later without rethinking state ownership

## Auth / Permissions / Security-Sensitive Paths

Observed from code:

- There is no authentication or authorization system in this repo.

Still worth practicing the habit:

- note where user input enters the system
- note where file paths or persisted text are used

Good review questions:

- If this project later accepted external file paths, where would validation belong?
- If task text were displayed in another environment, would encoding or escaping matter?

## Error Handling And Validation

What to look for:

- Are invalid inputs rejected with clear messages?
- Are I/O errors handled without crashing the app?
- Are required fields validated close to where the data enters the system?

Common failure modes:

- catching errors too broadly and hiding useful detail
- failing to validate empty or malformed values
- allowing one bad saved record to create confusing behavior

Good review questions:

- Does the user get a helpful message here?
- Is this validation happening in the best layer?
- If loading fails, is the fallback behavior safe?

Subtle bugs a junior dev might miss:

- relying on one layer's validation and forgetting another creation path
- error messages that are technically true but not actionable

## Tests And Missing Coverage

Observed from code:

- There are currently no test files in the repo.

What to look for:

- Which rules are important enough to deserve tests first
- Which changes are most likely to create regressions

High-value test targets in this repo:

- id generation after loading saved tasks
- completing a task twice
- deleting missing ids
- loading malformed or partial task data
- save/load round trips

Good review questions:

- What would be the smallest useful test for this change?
- Does the change affect persistence compatibility?
- Would a regression here be obvious in manual testing?

Subtle bugs a junior dev might miss:

- tests that only cover happy paths
- manual testing that skips restart behavior
- not testing the interaction between business logic and persistence

## Known Instances In This Snapshot (Verified)

The two behaviors below were reproduced by compiling this repo's four source files with JDK 17 (the `javac`/`java` commands in the README) and piping menu input into `todo.App` from a scratch directory.

1. **A corrupt `tasks.txt` is silently erased.** If `loadTasks` throws, `App.loadTasksFromDisk` prints a warning and continues with an empty list (`App.java:53-63`). The `finally` block in `run()` (`App.java:46-50`) then calls `saveTasksToDisk`, and `Files.newBufferedWriter` truncates the file before writing nothing (`TaskRepository.java:52-71`). Repro: append the line `garbage` to a valid `tasks.txt`, start the app, choose `5`. The app reports "Could not load tasks", and on exit the file is 0 bytes. Every previously saved task is gone.
   - Goal: never overwrite a file you failed to read.
   - **Check:** after your fix, the same repro leaves `tasks.txt` byte-for-byte unchanged, and the app tells the user why it will not save.
2. **Ids are reused across restarts.** `calculateNextId` returns `max(id) + 1` (`TaskManager.java:96-106`). Add A (id 1) and B (id 2), delete 2, exit, restart, and add C: C gets **id 2**. Anything that remembered "task 2" now points at a different task.
   - Goal: decide whether ids must be unique for the life of the file, or only among live tasks, and write that rule down.
   - **Check:** the repro above prints `ID 3 - C` if you chose "life of the file". That needs the counter persisted, because it cannot be derived from the remaining tasks.

Also worth a review comment, though not reproduced here:
- Changes are written only when the loop ends (`App.java:46-50`). Killing the process (for example, closing the terminal) skips the `finally` block and loses the whole session.
- The save truncates the live file and then rewrites it. A crash during `saveTasks` leaves a partial file, which the next start treats as corrupt, and that leads straight into item 1.
- Typing `-1` at an id prompt is indistinguishable from invalid input, because `readTaskId` uses `-1` as its error sentinel (`App.java:150-160`). The app returns to the menu without any message.
