package org.kor.portal.service.robofinist.model.event

import com.fasterxml.jackson.annotation.JsonProperty
import org.kor.portal.service.robofinist.model.BaseRequest

data class EventsSearchRequest(
    @get:JsonProperty val onlyUser: Boolean? = null,
    @get:JsonProperty val partnerId: Int? = null,
) : BaseRequest(url = "event/search")

data class EventsSearchResponse(
    val data: List<Event>,
)
