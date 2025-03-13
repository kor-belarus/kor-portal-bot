package org.kor.portal.service.tm

import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Service
@ConditionalOnProperty(value = ["bot.enabled"], matchIfMissing = true)
class TmBot(
    val tmMessageProcessor: TmMessageProcessor,
    @Value("\${bot.token}") token: String,
    @param:Value("\${bot.username}") private val username: String,
    @param:Value("\${bot.adminChatId}") val adminChatId: Long,
) : TelegramLongPollingBot(token) {

    override fun getBotUsername() = username

    override fun onUpdateReceived(update: Update) {
        try {
            logger.info("Received update: {}", update)
            tmMessageProcessor.processUpdate(update)?.let {
                sendMessageToTelegram(it)
            }
        } catch (e: Exception) {
            logger.error("Failed to process telegram update", e)
            sendErrorMessage(update, e)
        }
    }

    fun sendMessageToTelegram(botApiMethod: BotApiMethod<*>) {
        try {
            logger.info("Send message to telegram: {}", botApiMethod)
            val answer = super.execute(botApiMethod)
            logger.debug("Response from telegram: {}", answer)
        } catch (e: TelegramApiException) {
            logger.error("Telegram Api Exception: {}", e.message, e)
            throw RuntimeException("Failed to send message to telegram", e)
        }
    }


    private fun sendErrorMessage(update: Update, e: Exception) {
        try {
            val chatId = update.message?.chatId
                ?: update.callbackQuery?.message?.chatId
                ?: adminChatId
            val message = SendMessage().create(chatId.toString(), "Произошла ошибка: ${e.message}")
            sendMessageToTelegram(message)
        } catch (e: Exception) {
            logger.error("Failed to send telegram message with error", e)
        }
    }

    companion object : KLogging()
}
