package org.kor.portal.service.tm.command

import mu.KLogging
import org.kor.portal.config.TmBotProperties
import org.kor.portal.service.robofinist.RobofinistService
import org.kor.portal.service.robofinist.model.program.Program
import org.kor.portal.service.tm.CommandRequest
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.io.Serializable

class ExportHandler(
    private val robofinistService: RobofinistService,
    private val tmBotProperties: TmBotProperties,
) : Command {

    override val command: String
        get() = EVENTS_EXPORT

    override fun answer(request: CommandRequest): BotApiMethod<out Serializable> = throw NotImplementedError()

    fun handleEventExport(request: CommandRequest, eventId: String): BotApiMethod<out Serializable> {
        if (request.hasNext()) {
            TODO()
        }
        return showProgramsSelection(request, eventId)
    }

    fun showProgramsSelection(request: CommandRequest, eventId: String): BotApiMethod<out Serializable> {
        val programs = robofinistService.getPrograms(eventId = eventId.toLong())
        val selectedPrograms = programs.map { it.id }.toSet()
        val text = "<b>Экспорт статистики</b>\n\nВыберите программы для экспорта:\n\nВыбрано: ${selectedPrograms.size}"
        return createTmMessage(request, text, html = true)
    }

    companion object : KLogging() {
        private const val EVENTS_EXPORT = "events/export"
    }
}
