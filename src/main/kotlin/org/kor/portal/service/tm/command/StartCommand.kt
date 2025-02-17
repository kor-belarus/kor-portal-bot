package org.kor.portal.service.tm.command

import org.kor.portal.service.tm.CommandRequest
import org.springframework.stereotype.Service

@Service
class StartCommand : Command {
    override val command: String
        get() = START

    override fun answer(request: CommandRequest) =
        createTmMessage(request,
            "КОР Портал бот\n\n" +
                "Бот для управления мероприятием КОР на поротале robofinist\n\n" +
                "/events - список соревнований\n",
            inlineKeyboardMarkup = createButtons(buttons))

    companion object {
        private const val START = "start"

        @JvmStatic
        protected val buttons = listOf("events")
    }
}
