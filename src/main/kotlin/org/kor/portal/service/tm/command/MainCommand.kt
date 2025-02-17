package org.kor.portal.service.tm.command

import org.kor.portal.service.tm.CommandRequest
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

@Service
class MainCommand : Command {
    override val command: String
        get() = MAIN

    override fun answer(request: CommandRequest) =
        createTmMessage(request, "КОР Портал бот\n", replyKeyboard = createMainButtons())

    private fun createMainButtons() = ReplyKeyboardMarkup().apply {
        this.selective = true
        this.resizeKeyboard = true
        this.oneTimeKeyboard = false
        this.keyboard = createKeyboardRows()
    }

    private fun createKeyboardRows(): List<KeyboardRow> =
        listOf(KeyboardRow()
            .apply { addAll(buttons.map { KeyboardButton(it) }) })

    companion object {
        private const val MAIN = "main"
        protected val buttons = listOf("events", "start")
    }
}