package org.kor.portal.service.tm

import mu.KLogging
import org.kor.portal.service.tm.command.Command
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update

@Service
class TmMessageProcessor(
    commands: List<Command>,
    @param:Value("\${bot.username}") private val username: String,
) {
    private val commandsMap = commands.associateBy { it.command }

    fun processUpdate(update: Update): BotApiMethod<*>? {
        logger.info("Process telegram update: {}", update)
        if (update.hasMessage() && update.message.hasText()) {
            val message = update.message
            return processMessage(message.chatId, message.text, null)
        }
        if (update.hasCallbackQuery()) {
            val callback = update.callbackQuery
            if (callback.data?.isNotEmpty() == true) {
                return processMessage(callback.message.chatId, callback.data, callback.message.messageId)
            }
        }
        return null
    }

    private fun processMessage(chatId: Long, sourceText: String, messageId: Int?): BotApiMethod<*>? {
        logger.info("Process message: text [{}], messageId {}", sourceText, messageId)
        val text = sourceText.replace("@$username", "")

        val path = (splitPath(text).takeIf { it.isNotEmpty() } ?: listOf(""))
            .toMutableList()
        logger.info("Process message path: {}", path)
        val commandName = path.removeAt(0)

        commandsMap[commandName]?.apply {
            logger.info("Found command: [{}]", commandName)
            return this.answer(CommandRequest(path, chatId.toString(), messageId))
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