package org.kor.portal.service.robofinist.model.bid

import com.fasterxml.jackson.annotation.JsonProperty

data class Bid(
    val id: Int,
    val name: String,
    val status: Int,
    val organizations: List<Organization>,
)

data class Organization(
    val id: Int,
    val name: String,
)

data class Participant(
    val id: Int,
    @param:JsonProperty("first_name") val firstName: String?,
    @param:JsonProperty("last_name") val lastName: String?,
    @param:JsonProperty("middle_name") val middleName: String?,
    val mentor: Boolean?,
    val organization: Organization?,
)
