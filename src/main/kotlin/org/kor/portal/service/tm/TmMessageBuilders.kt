package org.kor.portal.service.tm

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

fun SendMessage.create(
    chatId: String,
    text: String,
    replyMarkup: ReplyKeyboard? = null,
) = apply {
    this.chatId = chatId
    this.text = text
    replyMarkup?.let { this.replyMarkup = it }
}

fun EditMessageText.create(
    chatId: String,
    messageId: Int,
    text: String,
    replyMarkup: InlineKeyboardMarkup? = null,
) = apply {
    this.chatId = chatId
    this.messageId = messageId
    this.text = text
    replyMarkup.let { this.replyMarkup = it }
}

fun InlineKeyboardButton.create(text: String, callbackData: String) = apply {
    this.text = text
    this.callbackData = callbackData
}
