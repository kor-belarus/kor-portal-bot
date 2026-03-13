package org.kor.portal.service.tm

import mu.KLogging
import org.kor.portal.service.tm.command.Command
import org.kor.portal.service.tm.command.EventsCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

@Service
class TmMessageProcessor(
    commands: List<Command>,
    @param:Value("\${bot.username}") private val username: String,
) {
    private val commandsMap = commands.associateBy { it.command }

    fun processUpdate(update: Update): BotApiMethod<*>? {
        val response = processUpdateWithDocument(update)
        return when (response) {
            is CommandResponse.Message -> response.message
            is CommandResponse.Document -> null
            null -> null
        }
    }

    fun processUpdateWithDocument(update: Update): CommandResponse? {
        logger.info("Process telegram update: {}", update)
        if (update.hasMessage() && update.message.hasText()) {
            val message = update.message
            return processMessageWithDocument(message.chatId, message.text, null, null)
        }
        if (update.hasCallbackQuery()) {
            val callback = update.callbackQuery
            if (callback.data?.isNotEmpty() == true) {
                val message = callback.message as? Message
                val replyMarkup = message?.replyMarkup as? InlineKeyboardMarkup
                return processMessageWithDocument(
                    callback.message.chatId,
                    callback.data,
                    callback.message.messageId,
                    replyMarkup
                )
            }
        }
        return null
    }

    private fun processMessageWithDocument(
        chatId: Long,
        sourceText: String,
        messageId: Int?,
        replyMarkup: InlineKeyboardMarkup?
    ): CommandResponse? {
        logger.info("Process message: text [{}], messageId {}", sourceText, messageId)
        val text = sourceText.replace("@$username", "")

        val path = (splitPath(text).takeIf { it.isNotEmpty() } ?: listOf(""))
            .toMutableList()
        logger.info("Process message path: {}", path)
        val commandName = path.removeAt(0)

        commandsMap[commandName]?.apply {
            logger.info("Found command: [{}]", commandName)
            val request = CommandRequest(path, chatId.toString(), messageId, replyMarkup)
            return if (this is EventsCommand) {
                this.answerWithDocument(request)
            } else {
                CommandResponse.Message(this.answer(request))
            }
        }
        logger.info("Command not found: [{}]", commandName)
        return null
    }

    private fun splitPath(text: String): List<String> =
        text.split(commandPathSeparator)
            .dropLastWhile { it.isEmpty() }
            .filter { it.isNotBlank() }

    companion object : KLogging() {
        private val commandPathSeparator = "/".toRegex()
    }
}