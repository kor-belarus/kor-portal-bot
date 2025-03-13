package org.kor.portal.service.robofinist.model.program

import com.fasterxml.jackson.annotation.JsonProperty
import org.kor.portal.service.robofinist.model.BaseRequest

data class ProgramSearchRequest(
    @get:JsonProperty("event_id") val eventId: Long? = null,
) : BaseRequest(url = "event/program/search")

data class ProgramSearchResponse(
    @param:JsonProperty("data") val data: List<Program>,
)
