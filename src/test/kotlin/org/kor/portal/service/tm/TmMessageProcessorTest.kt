package org.kor.portal.service.tm

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update

@SpringBootTest
@ActiveProfiles("test")
class TmMessageProcessorTest(
    @Autowired val tmMessageProcessor: TmMessageProcessor,
) {

    @Test
    fun processStart() {
        val update = Update().apply {
            this.message = Message().apply {
                this.chat = Chat().apply {
                    this.id = 123
                }
                this.text = "/start"
            }
        }

        val result = tmMessageProcessor.processUpdate(update) as SendMessage

        assertThat(result.chatId).isEqualTo("123")
        assertThat(result.text).contains("КОР Портал")
        assertThat(result.replyMarkup).isNotNull()
    }

    @Test
    fun processMain() {
        val update = Update().apply {
            this.message = Message().apply {
                this.chat = Chat().apply {
                    this.id = 123
                }
                this.text = "/main"
            }
        }

        val result = tmMessageProcessor.processUpdate(update) as SendMessage

        assertThat(result.chatId).isEqualTo("123")
        assertThat(result.text).contains("КОР Портал")
        assertThat(result.replyMarkup).isNotNull()
    }

}