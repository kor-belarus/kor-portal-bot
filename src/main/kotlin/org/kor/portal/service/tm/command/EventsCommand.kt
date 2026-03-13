package org.kor.portal.service.tm.command

import mu.KLogging
import org.kor.portal.config.TmBotProperties
import org.kor.portal.service.robofinist.RobofinistService
import org.kor.portal.service.tm.CommandRequest
import org.kor.portal.service.tm.CommandResponse
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import java.io.Serializable

@Service
class EventsCommand(
    private val robofinistService: RobofinistService,
    private val tmBotProperties: TmBotProperties,
) : Command {

    private val exportHandler = ExportHandler(robofinistService, tmBotProperties)

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

    fun answerWithDocument(request: CommandRequest): CommandResponse {
        if (!request.hasNext()) {
            return CommandResponse.Message(answer(request))
        }
        val eventId = request.next()
        val event = robofinistService.getEvent(eventId.toInt())
            ?: return CommandResponse.Message(createTmMessage(request, "Мероприятие #$eventId не найдено", createButtons()))

        return if (request.hasNext()) {
            processEventCommandWithDocument(request, eventId)
        } else {
            val text = "<b>ID</b>: $eventId\n\n" +
                "<b>Название</b>: ${event.name}\n\n" +
                "<b>Место</b>: ${event.location ?: ""}\n\n" +
                "<b>Дата</b>: ${event.beginAt ?: ""}\n\n" +
                "<b>Регистрация до</b>: ${event.registrationEndAt ?: ""}"
            val buttons = mutableListOf("programs")
            if (isAdmin(request.chatId.toLong())) buttons.add("export")
            val keyboardMarkup = createButtons(buttons, listOf(eventId))
            CommandResponse.Message(createTmMessage(request, text, keyboardMarkup, html = true))
        }
    }

    private fun processEvent(request: CommandRequest): BotApiMethod<out Serializable> {
        val eventId = request.next()
        val event = robofinistService.getEvent(eventId.toInt())
            ?: return createTmMessage(request, "Мероприятие #$eventId не найдено", createButtons())

        return if (request.hasNext()) {
            val response = processEventCommandWithDocument(request, eventId)
            when (response) {
                is CommandResponse.Message -> response.message
                is CommandResponse.Document -> throw UnsupportedOperationException("Use answerWithDocument for document responses")
            }
        } else {
            val text = "<b>ID</b>: $eventId\n\n" +
                "<b>Название</b>: ${event.name}\n\n" +
                "<b>Место</b>: ${event.location ?: ""}\n\n" +
                "<b>Дата</b>: ${event.beginAt ?: ""}\n\n" +
                "<b>Регистрация до</b>: ${event.registrationEndAt ?: ""}"
            val buttons = mutableListOf("programs")
            if (isAdmin(request.chatId.toLong())) buttons.add("export")
            val keyboardMarkup = createButtons(buttons, listOf(eventId))
            createTmMessage(request, text, keyboardMarkup, html = true)
        }
    }

    private fun processEventCommandWithDocument(request: CommandRequest, eventId: String): CommandResponse =
        when (val command = request.next()) {
            "programs" -> {
                CommandResponse.Message(handlePrograms(eventId, request))
            }
            "export" -> {
                if (isAdmin(request.chatId.toLong())) {
                    when (val result = exportHandler.handleEventExport(request, eventId)) {
                        is ExportResult.Message -> CommandResponse.Message(result.message)
                        is ExportResult.Document -> CommandResponse.Document(result.document)
                    }
                } else {
                    CommandResponse.Message(createTmMessage(request, "Нет доступа к экспорту", createButtons()))
                }
            }
            else -> CommandResponse.Message(createTmMessage(request, "Команда `$command` не найдена", createButtons()))
        }

    private fun handlePrograms(eventId: String, request: CommandRequest): BotApiMethod<out Serializable> {
        val programs = robofinistService.getPrograms(eventId = eventId.toLong())

        return if (request.hasNext()) {
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

    private fun shortEventName(name: String): String = name
        .replace(korRegex, "КОР")
        .replace(molrRegex, "МОЛР")


    fun isAdmin(chatId: Long): Boolean = tmBotProperties.adminUserIds.contains(chatId)

    companion object : KLogging() {
        private const val EVENTS = "events"
        private val korRegex = "Куб.+ по образовательной робототехнике".toRegex()
        private val molrRegex = "Минская открытая лига робототехники".toRegex()
    }
}
