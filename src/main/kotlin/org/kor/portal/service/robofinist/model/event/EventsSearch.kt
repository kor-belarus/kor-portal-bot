package org.kor.portal.service.robofinist.model.event

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.kor.portal.service.robofinist.model.BaseRequest

@JsonInclude(JsonInclude.Include.NON_NULL)
data class EventsSearchRequest(
    @get:JsonProperty val id: Int? = null,
    @get:JsonProperty val onlyUser: Boolean? = null,
    @get:JsonProperty val partnerId: Int? = null,
) : BaseRequest(url = "event/search")

data class EventsSearchResponse(
    val data: List<Event>,
)
