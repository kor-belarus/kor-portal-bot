package org.kor.portal.service.tm.command

import mu.KLogging
import org.kor.portal.service.robofinist.RobofinistService
import org.kor.portal.service.tm.CommandRequest
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import java.io.Serializable

@Service
class EventsCommand(
    private val robofinistService: RobofinistService,
) : Command {
    override val command: String
        get() = EVENTS

    override fun answer(request: CommandRequest): BotApiMethod<out Serializable> =
        if (request.hasNext()) {
            processEvent(request)
        } else {
            val events = robofinistService.getEvents()
            val map = events.data.associateBy({ it.id.toString() }, { "#${it.id} ${shortEventName(it.name)}" })
            createTmMessage(request, "Выберите мероприятие:", createButtons(map))
        }

    private fun processEvent(request: CommandRequest): BotApiMethod<out Serializable> {
        val eventId = request.next()
        val event = robofinistService.getEvents(eventId.toInt())
            ?: return createTmMessage(request, "Мероприятие #$eventId не найдено", createButtons())

        val text = "<b>ID</b>: $eventId\n\n" +
            "<b>Name</b>: ${event.name}\n\n" +
            "<b>Location</b>: ${event.location ?: ""}"
        return createTmMessage(request, text, createButtons(listOf(), listOf(eventId)), html = true)
    }

    private fun shortEventName(name: String): String = name
        .replace(korRegex, "КОР")
        .replace(molrRegex, "МОЛР")

    companion object : KLogging() {
        private const val EVENTS = "events"
        private val korRegex = "Куб.+ по образовательной робототехнике".toRegex()
        private val molrRegex = "Минская открытая лига робототехники".toRegex()
    }
}