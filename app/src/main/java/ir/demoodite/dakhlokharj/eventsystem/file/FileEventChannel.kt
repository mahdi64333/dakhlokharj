package ir.demoodite.dakhlokharj.eventsystem.file

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

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
}