package org.kor.portal.service.tm.command

import mu.KLogging
import org.kor.portal.config.TmBotProperties
import org.kor.portal.service.robofinist.RobofinistService
import org.kor.portal.service.robofinist.model.bid.Bid
import org.kor.portal.service.robofinist.model.program.Program
import org.kor.portal.service.tm.CommandRequest
import org.kor.portal.service.tm.create
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.io.ByteArrayInputStream
import java.io.Serializable
import java.nio.charset.StandardCharsets

class ExportHandler(
    private val robofinistService: RobofinistService,
    private val tmBotProperties: TmBotProperties,
) : Command {

    override val command: String
        get() = EVENTS_EXPORT

    override fun answer(request: CommandRequest): BotApiMethod<out Serializable> = throw NotImplementedError()

    fun handleEventExport(request: CommandRequest, eventId: String): ExportResult {
        return if (request.hasNext()) {
            processExportCommand(request, eventId)
        } else {
            val programs = robofinistService.getPrograms(eventId = eventId.toLong())
            val selectedIds = programs.map { it.id }.toSet()
            ExportResult.Message(showProgramsSelection(request, eventId, programs, selectedIds))
        }
    }

    private fun processExportCommand(request: CommandRequest, eventId: String): ExportResult {
        val action = request.next()

        return when (action) {
            "generate" -> {
                val selectedPrograms = parseSelectedProgramsFromButtons(request)
                generateCsvExport(request, eventId, selectedPrograms)
            }
            "toggle" -> {
                if (request.hasNext()) {
                    val programId = request.next().toLong()
                    val (programs, selectedIds) = parseProgramsFromButtons(request)
                    val newSelectedIds = toggleSelection(selectedIds, programId)
                    ExportResult.Message(showProgramsSelection(request, eventId, programs, newSelectedIds))
                } else {
                    val programs = robofinistService.getPrograms(eventId = eventId.toLong())
                    ExportResult.Message(showProgramsSelection(request, eventId, programs, programs.map { it.id }.toSet()))
                }
            }
            else -> {
                val programs = robofinistService.getPrograms(eventId = eventId.toLong())
                ExportResult.Message(showProgramsSelection(request, eventId, programs, programs.map { it.id }.toSet()))
            }
        }
    }

    private fun parseProgramsFromButtons(request: CommandRequest): Pair<List<Program>, Set<Long>> {
        val keyboard = request.replyMarkup ?: return Pair(emptyList(), emptySet())
        
        val programs = mutableListOf<Program>()
        val selectedIds = mutableSetOf<Long>()
        
        for (row in keyboard.keyboard) {
            for (button in row) {
                val callbackData = button.callbackData ?: continue
                if (!callbackData.contains("/toggle/")) continue
                
                val programId = callbackData.substringAfterLast("/toggle/").toLongOrNull() ?: continue
                val buttonText = button.text
                val isSelected = buttonText.startsWith(CHECKMARK_SELECTED)
                val programName = buttonText.removePrefix(CHECKMARK_SELECTED).removePrefix(CHECKMARK_UNSELECTED).trim()
                
                programs.add(Program(programId, programName))
                if (isSelected) {
                    selectedIds.add(programId)
                }
            }
        }
        
        return Pair(programs, selectedIds)
    }

    private fun parseSelectedProgramsFromButtons(request: CommandRequest): List<Program> {
        val (programs, selectedIds) = parseProgramsFromButtons(request)
        return programs.filter { selectedIds.contains(it.id) }
    }

    private fun toggleSelection(currentSelection: Set<Long>, programId: Long): Set<Long> =
        if (currentSelection.contains(programId)) {
            currentSelection - programId
        } else {
            currentSelection + programId
        }

    private fun showProgramsSelection(
        request: CommandRequest,
        eventId: String,
        programs: List<Program>,
        selectedIds: Set<Long>
    ): BotApiMethod<out Serializable> {
        val text = "<b>📤 Экспорт статистики по организациям</b>\n\n" +
            "Выберите программы для экспорта:\n\n" +
            "Выбрано: ${selectedIds.size} из ${programs.size}"

        val keyboard = createProgramSelectionKeyboard(eventId, programs, selectedIds)
        return createTmMessage(request, text, keyboard, html = true)
    }

    private fun createProgramSelectionKeyboard(
        eventId: String,
        programs: List<Program>,
        selectedIds: Set<Long>
    ): InlineKeyboardMarkup {
        val basePath = "/events/$eventId/export"

        val programButtons = programs.map { program ->
            val isSelected = selectedIds.contains(program.id)
            val checkmark = if (isSelected) CHECKMARK_SELECTED else CHECKMARK_UNSELECTED
            val buttonText = "$checkmark ${program.name}"
            val callbackData = "$basePath/toggle/${program.id}"
            listOf(InlineKeyboardButton().create(buttonText, callbackData))
        }

        val actionButtons = listOf(
            InlineKeyboardButton().create("📥 Экспортировать CSV", "$basePath/generate"),
            InlineKeyboardButton().create("Назад", "/events/$eventId")
        ).map { listOf(it) }

        return InlineKeyboardMarkup(programButtons + actionButtons)
    }

    private fun generateCsvExport(
        request: CommandRequest,
        eventId: String,
        selectedPrograms: List<Program>
    ): ExportResult {
        if (selectedPrograms.isEmpty()) {
            val (programs, selectedIds) = parseProgramsFromButtons(request)
            return ExportResult.Message(
                createTmMessage(
                    request,
                    "⚠️ Выберите хотя бы одну программу для экспорта",
                    createProgramSelectionKeyboard(eventId, programs, selectedIds),
                    html = true
                )
            )
        }

        val csvContent = buildOrganizationStatsCsv(selectedPrograms)
        val event = robofinistService.getEvent(eventId.toInt())
        val fileName = "org_stats_event_${eventId}_${System.currentTimeMillis()}.csv"

        // Add UTF-8 BOM for proper encoding recognition in Excel
        val utf8Bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        val csvBytes = utf8Bom + csvContent.toByteArray(StandardCharsets.UTF_8)

        val document = SendDocument().apply {
            chatId = request.chatId
            document = InputFile(
                ByteArrayInputStream(csvBytes),
                fileName
            )
            caption = "📊 Статистика организаций: ${event?.name ?: eventId}\n" +
                "Программ: ${selectedPrograms.size}"
        }

        return ExportResult.Document(document)
    }

    private fun buildOrganizationStatsCsv(programs: List<Program>): String {
        val orgStats = mutableMapOf<Int, OrganizationStats>()

        for (program in programs) {
            val bids = robofinistService.getBids(programId = program.id)
            
            for (bid in bids) {
                val participants = try {
                    robofinistService.getParticipants(bid.id)
                } catch (e: Exception) {
                    logger.warn("Failed to get participants for bid ${bid.id}", e)
                    emptyList()
                }
                
                val results = try {
                    robofinistService.getBidResults(bid.id)
                } catch (e: Exception) {
                    logger.warn("Failed to get results for bid ${bid.id}", e)
                    emptyList()
                }
                
                val place = results.firstOrNull { it.id == program.id.toInt() }?.place
                
                for (org in bid.organizations) {
                    val stats = orgStats.getOrPut(org.id) { 
                        OrganizationStats(org.id, org.name) 
                    }
                    stats.bidsCount++
                    stats.participantsCount += participants.size
                    
                    when (place) {
                        1 -> stats.firstPlaces++
                        2 -> stats.secondPlaces++
                        3 -> stats.thirdPlaces++
                    }
                }
            }
        }

        val sb = StringBuilder()
        sb.appendLine("Организация;ID организации;Количество заявок;Количество участников;1 место;2 место;3 место;Всего призовых мест")

        orgStats.values
            .sortedByDescending { it.totalPrizes }
            .forEach { stats ->
                sb.appendLine(
                    "${escapeCsv(stats.name)};${stats.id};${stats.bidsCount};${stats.participantsCount};" +
                    "${stats.firstPlaces};${stats.secondPlaces};${stats.thirdPlaces};${stats.totalPrizes}"
                )
            }

        return sb.toString()
    }

    private fun escapeCsv(value: String): String =
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }

    companion object : KLogging() {
        private const val EVENTS_EXPORT = "events/export"
        private const val CHECKMARK_SELECTED = "✅ "
        private const val CHECKMARK_UNSELECTED = "⬜ "
    }
}

private data class OrganizationStats(
    val id: Int,
    val name: String,
    var bidsCount: Int = 0,
    var participantsCount: Int = 0,
    var firstPlaces: Int = 0,
    var secondPlaces: Int = 0,
    var thirdPlaces: Int = 0,
) {
    val totalPrizes: Int get() = firstPlaces + secondPlaces + thirdPlaces
}

sealed class ExportResult {
    data class Message(val message: BotApiMethod<out Serializable>) : ExportResult()
    data class Document(val document: SendDocument) : ExportResult()
}
