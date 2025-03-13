package org.kor.portal.service.robofinist.model.event

import com.fasterxml.jackson.annotation.JsonProperty
import org.kor.portal.service.robofinist.model.BaseRequest

data class EventsSearchRequest(
    @get:JsonProperty("id") val id: Int? = null,
    @get:JsonProperty("onlyUser") val onlyUser: Boolean? = null,
    @get:JsonProperty("partnerId") val partnerId: Int? = null,
) : BaseRequest(url = "event/search")

data class EventsSearchResponse(
    val data: List<Event>,
)
