package ir.demoodite.dakhlokharj.eventsystem.file

import android.net.Uri

data class FileEvent(
    val type: FileEventType,
    val uri: Uri,
)