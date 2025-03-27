package org.kor.portal.service.robofinist.model.bid

import com.fasterxml.jackson.annotation.JsonProperty
import org.kor.portal.service.robofinist.model.BaseRequest


data class BidSearchRequest(
    @get:JsonProperty("program_id") val programId: Long,
) : BaseRequest(url = "event/program/stage/bids")

data class BidSearchResponse(
    @param:JsonProperty("data") val data: List<Bid>,
)
