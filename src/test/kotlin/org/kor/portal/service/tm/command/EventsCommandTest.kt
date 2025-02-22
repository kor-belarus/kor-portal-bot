package org.kor.portal.service.tm.command

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.kor.portal.config.RestTemplateTestConfig
import org.kor.portal.service.tm.CommandRequest
import org.kor.portal.util.SpyRestTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

@SpringBootTest
@ActiveProfiles("test")
@Import(RestTemplateTestConfig::class)
class EventsCommandTest(
    @Autowired val eventsCommand: EventsCommand,
    @Autowired val spyRestTemplate: SpyRestTemplate,
) {

    @Test
    fun answerList() {
        spyRestTemplate.stubResponseFileContent("event/event-search-response.json")

        val result = eventsCommand.answer(CommandRequest(listOf(), "123")) as SendMessage

        assertThat(result.chatId).isEqualTo("123")
        assertThat(result.text).contains("Выберите соревнование:")
        val replyMarkup = result.replyMarkup as InlineKeyboardMarkup
        assertThat(replyMarkup.keyboard.size).isEqualTo(11)
        val inlineKeyboardButton = replyMarkup.keyboard[0][0]
        assertThat(inlineKeyboardButton.text).contains("#1303 Конкурс \"Дорога в будущее\".")
        assertThat(inlineKeyboardButton.callbackData).isEqualTo("/events/1303")

    }

    @Test
    fun answerListEdit() {
        spyRestTemplate.stubResponseFileContent("event/event-search-response.json")

        val result = eventsCommand.answer(CommandRequest(listOf(), "123", 567)) as EditMessageText

        assertThat(result.chatId).isEqualTo("123")
        assertThat(result.messageId).isEqualTo(567)
        assertThat(result.text).contains("Выберите соревнование:")
    }


    @Test
    fun answerListCheckShortNames() {
        spyRestTemplate.stubResponseFileContent("event/event-search-response.json")

        val result = eventsCommand.answer(CommandRequest(listOf(), "123")) as SendMessage

        val replyMarkup = result.replyMarkup as InlineKeyboardMarkup
        val buttons = replyMarkup.keyboard.flatten()
        assertThat(buttons.count { it.text.contains("КОР") }).isEqualTo(8)
        assertThat(buttons.count { it.text.contains("МОЛР") }).isEqualTo(3)

    }

    @Test
    fun answerEventDetailsEdit() {
        val result = eventsCommand.answer(CommandRequest(listOf("1303"), "123", 567)) as EditMessageText

        assertThat(result.chatId).isEqualTo("123")
        assertThat(result.messageId).isEqualTo(567)
        assertThat(result.text).contains("Выбрано 1303")
    }
}
