package ir.demoodite.dakhlokharj.eventsystem.file

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.io.File

object FileEventChannel {
    @Volatile
    private var INSTANCE: Channel<FileEvent>? = null

    fun getSender(): Channel<FileEvent> {
        return INSTANCE ?: synchronized(this) {
            if (INSTANCE == null) {
                INSTANCE = Channel()
            }
            INSTANCE!!
        }
    }

    fun getReceiver(): Flow<FileEvent> {
        return INSTANCE?.receiveAsFlow() ?: synchronized(this) {
            if (INSTANCE == null) {
                INSTANCE = Channel()
            }
            INSTANCE!!.receiveAsFlow()
        }
    }

    data class FileEvent(
        val type: FileEventType,
        val file: File,
    )

    enum class FileEventType {
        SAVE_FILE,
    }
}