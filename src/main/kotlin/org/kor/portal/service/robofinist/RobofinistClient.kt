package org.kor.portal.service.robofinist

import mu.KLogging
import org.kor.portal.service.robofinist.model.BaseRequest
import org.kor.portal.service.robofinist.model.event.EventsSearchRequest
import org.kor.portal.service.robofinist.model.event.EventsSearchResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class RobofinistClient(
    private val restTemplate: RestTemplate,
    @param:Value("\${robofinist.token}") private var token: String,
    @param:Value("\${robofinist.url.v2}") val url: String,
) {

    fun getEvents(partnerId: Int): EventsSearchResponse =
        execute<EventsSearchResponse>(EventsSearchRequest(partnerId = partnerId))

    private inline fun <reified T> execute(request: BaseRequest): T {
        try {
            request.token = token
            val response = restTemplate.postForEntity(url, request, T::class.java)
            logger.debug("Response: {}", response)
            return response.body!!
        } catch (e: Exception) {
            throw RuntimeException("Failed to invoke robofinist api", e)
        }
    }

    companion object : KLogging()
}
