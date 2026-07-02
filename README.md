# MyPad

A simple plain-text note-taking app for Android. No Room, no Material Components — minimal dependencies for maximum MIUI compatibility.

简洁的纯文本笔记应用。无 Room、无 Material Components，依赖最少，MIUI 兼容性最佳。

## Features / 功能

- **Plain text** / 纯文本 — no rich text, no formatting / 无富文本，无格式
- **Drag to reorder** / 拖拽排序 — long-press and drag to rearrange / 长按拖动调整顺序
- **Batch delete** / 批量删除 — tap "选择" button to enter selection mode / 点击工具栏选择按钮进入选择模式
- **Swipe to delete** / 滑动删除 — swipe left/right on a note / 左右滑动删除
- **Import/Export** / 导入导出 — ZIP file with one `.txt` per note, via SAF / 每条笔记一个 .txt 文件，通过 SAF 导出为 ZIP
- **Auto title** / 自动标题 — uses first non-blank line of content / 取内容第一非空行为标题
- **JSON file storage** / JSON 文件存储 — no Room, no kapt / 无 Room，无注解处理

## Build / 构建

```bash
./gradlew assembleDebug
```

Requires JDK 17, Android SDK 34. / 需要 JDK 17、Android SDK 34。

## Download / 下载

Download the latest APK from [Releases](https://github.com/soskitty/MyPad/releases).

从 [Releases](https://github.com/soskitty/MyPad/releases) 下载最新 APK。
