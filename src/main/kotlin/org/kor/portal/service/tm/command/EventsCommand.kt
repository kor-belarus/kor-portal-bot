package org.kor.portal.service.tm.command

import org.kor.portal.service.tm.CommandRequest
import org.springframework.stereotype.Service

@Service
class EventsCommand : Command {
    override val command: String
        get() = EVENTS

    override fun answer(request: CommandRequest) =
        createTmMessage(request, "Выберите соревнование:", createButtons(listOf(), listOf("back")))

    companion object {
        private const val EVENTS = "events"
    }
}