package org.kor.portal.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.util.ResourceUtils
import org.springframework.web.client.RestTemplate

class SpyRestTemplate(
    private val objectMapper: ObjectMapper,
) : RestTemplate() {

    private lateinit var stubbedResponse: String

    override fun <T> postForEntity(
        url: String, request: Any,
        responseType: Class<T>,
        vararg uriVariables: Any,
    ): ResponseEntity<T> = ok(objectMapper.readValue(stubbedResponse, responseType))

    fun stubResponseFileContent(response: String) {
        stubbedResponse = ResourceUtils
            .getFile("classpath:robofinist/model/$response")
            .readText()
    }

}
