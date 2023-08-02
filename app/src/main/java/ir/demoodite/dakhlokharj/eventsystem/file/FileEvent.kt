package ir.demoodite.dakhlokharj.eventsystem.file

import java.io.File

data class FileEvent(
    val type: FileEventType,
    val file: File,
)