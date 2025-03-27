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

        return if (request.hasNext()) {
            processEventCommand(request, eventId)
        } else {
            val text = "<b>ID</b>: $eventId\n\n" +
                "<b>Название</b>: ${event.name}\n\n" +
                "<b>Место</b>: ${event.location ?: ""}\n\n" +
                "<b>Дата</b>: ${event.beginAt ?: ""}\n\n" +
                "<b>Регистрация до</b>: ${event.registrationEndAt ?: ""}"
            createTmMessage(request, text, createButtons(listOf("programs"), listOf(eventId)), html = true)
        }
    }

    private fun processEventCommand(request: CommandRequest, eventId: String) =
        when (val command = request.next()) {
            "programs" -> {

                val programs = robofinistService.getPrograms(eventId = eventId.toLong())

                if (request.hasNext()) {
                    val programId = request.next().toLong()
                    val program = programs.first { it.id == programId }
                    val bids = robofinistService.getBids(programId = programId)
                    val text = "<b>Мероприятие</b>: $eventId\n\n" +
                        "<b>Программа</b>: ${program.name}\n\n" +
                        "<b>Список участников (${bids.size}):</b>\n" +
                        bids.joinToString("\n") { it.name }
                    createTmMessage(request, text,
                        createButtons(listOf("back"), listOf(eventId, "programs", programId.toString())), html = true)
                } else {
                    val text = "<b>ID</b>: $eventId\n\n" +
                        "<b>Программы</b>"
                    val buttons = programs.associate { it.id.toString() to it.name }
                    createTmMessage(request, text, createButtons(buttons, listOf(eventId, "programs")), html = true)
                }
            }

            else -> createTmMessage(request, "Команда `$command` не найдена", createButtons())
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