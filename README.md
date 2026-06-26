# MyPad

A simple plain-text note-taking app for Android. No Room, no Material Components — minimal dependencies for maximum MIUI compatibility.

## Features

- **Plain text notes** — no rich text, no formatting
- **Drag to reorder** — long-press and drag to rearrange notes
- **Batch delete** — tap toolbar "选择" button to enter selection mode, then delete
- **Swipe to delete** — swipe left/right on a note
- **Import/Export** — ZIP file with one `.md` per note, via SAF
- **Title** — automatically uses the first non-blank line of content
- **JSON file storage** — no Room / no kapt

## Build

```bash
./gradlew assembleDebug
```

Requires JDK 17, Android SDK 34.

## Download

Download the latest APK from [Releases](https://github.com/soskitty/MyPad/releases).
