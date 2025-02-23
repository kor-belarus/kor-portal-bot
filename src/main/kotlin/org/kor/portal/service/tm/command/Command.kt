package org.kor.portal.service.tm.command

import org.kor.portal.service.tm.CommandRequest
import org.kor.portal.service.tm.codeToButton
import org.kor.portal.service.tm.create
import org.springframework.core.annotation.Order
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.io.Serializable

@Order(1)
interface Command {

    val command: String

    fun answer(request: CommandRequest): BotApiMethod<*>

    fun createTmMessage(
        request: CommandRequest,
        text: String,
        inlineKeyboardMarkup: InlineKeyboardMarkup? = null,
        replyKeyboard: ReplyKeyboard? = null,
        html: Boolean = false,
    ): BotApiMethod<out Serializable> =
        if (request.messageId == null) {
            SendMessage().create(request.chatId, text, replyKeyboard ?: inlineKeyboardMarkup, html = html)
        } else {
            EditMessageText().create(request.chatId, request.messageId, text, inlineKeyboardMarkup, html = html)
        }

    fun createButtons(
        buttons: List<String> = listOf(),
        path: List<String> = listOf(),
        buttonsInRow: Int = BUTTONS_IN_ROW,
    ): InlineKeyboardMarkup =
        createButtons(
            buttons.associateBy({ it }, { codeToButton[it] ?: it }),
            path
        )

    fun createButtons(
        buttons: Map<String, String>,
        path: List<String> = listOf(),
        buttonsInRow: Int = BUTTONS_IN_ROW,
    ) =
        InlineKeyboardMarkup(
            addBack(buttons, path).entries
                .map { (key, value) -> createButton(key, value, path) }
                .chunked(buttonsInRow)
        )

    fun addBack(buttons: Map<String, String>, path: List<String>): Map<String, String> =
        buttons.toMutableMap().apply {
            if (command != "start" && command != "") {
                put("back", codeToButton["back"]!!)
            }
        }

    fun createButton(operation: String, text: String, path: List<String>): InlineKeyboardButton {
        val fullPath = mutableListOf<String>()
        if (command != "start" && command != "") fullPath.add(command)
        fullPath.addAll(path)
        if ("back" == operation) {
            fullPath.removeLastOrNull()
        } else {
            fullPath.add(operation)
        }

        val callbackData = "/" + fullPath.joinToString("/")

        return InlineKeyboardButton().create(text, callbackData)
    }

    companion object {
        const val BUTTONS_IN_ROW: Int = 1
    }
}