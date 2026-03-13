package org.kor.portal.service.robofinist.model.bid

import com.fasterxml.jackson.annotation.JsonProperty
import org.kor.portal.service.robofinist.model.BaseRequest


data class BidSearchRequest(
    @get:JsonProperty("program_id") val programId: Long,
) : BaseRequest(url = "event/program/stage/bids")

data class BidSearchResponse(
    @param:JsonProperty("data") val data: List<Bid>,
)

data class ParticipantListRequest(
    @get:JsonProperty("bid_id") val bidId: Int,
) : BaseRequest(url = "event/bid/participant/list")

data class ParticipantListResponse(
    @param:JsonProperty("data") val data: List<Participant>,
)

data class BidResultRequest(
    @get:JsonProperty("bid_id") val bidId: Int,
) : BaseRequest(url = "event/bid/result/byBidList")

data class BidResultResponse(
    @param:JsonProperty("data") val data: List<BidResult>,
)

data class BidResult(
    @param:JsonProperty("bid_id") val bidId: Int?,
    val place: Int?,
    @param:JsonProperty("program_id") val programId: Int?,
)
