package org.kor.portal.config

import mu.KLogging
import org.kor.portal.service.tm.TmBot
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@Configuration
@ConditionalOnProperty(value = ["bot.enabled"], matchIfMissing = true)
class TmBotConfig {

    @Bean
    fun tmBotApi(bot: TmBot): TelegramBotsApi = try {
        TelegramBotsApi(DefaultBotSession::class.java).apply {
            registerBot(bot)
        }
    } catch (e: TelegramApiException) {
        logger.error("Bot creation failed", e)
        throw RuntimeException("Failed to create bot", e)
    }

    companion object : KLogging()
}
