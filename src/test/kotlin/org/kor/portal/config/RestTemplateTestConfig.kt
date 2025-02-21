package org.kor.portal.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.kor.portal.util.SpyRestTemplate
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class RestTemplateTestConfig {

    @Bean
    @Primary
    fun spyRestTemplate(objectMapper: ObjectMapper) = SpyRestTemplate(objectMapper).apply {
        interceptors.add(LoggingRequestInterceptor())
    }

}
