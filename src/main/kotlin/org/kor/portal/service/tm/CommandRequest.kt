package org.kor.portal.service.tm

import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import java.io.Serializable

data class CommandRequest(
    val path: List<String>,
    val chatId: String,
    val messageId: Int? = null,
) : Iterator<String> {

    private var read = 0

    override fun next(): String {
        if (hasNext()) {
            return path[read++]
        }
        throw NoSuchElementException()
    }

    override fun hasNext() = path.size > read

    fun createPathRemoving(vararg removing: String) = path.filter { !removing.contains(it) }

}

sealed class CommandResponse {
    data class Message(val message: BotApiMethod<out Serializable>) : CommandResponse()
    data class Document(val document: SendDocument) : CommandResponse()
}
