package org.kor.portal.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "bot")
class TmBotProperties(
    val token: String,
    val username: String,
    val adminChatId: Long,
    val adminUserIds: List<Long> = emptyList(),
)
