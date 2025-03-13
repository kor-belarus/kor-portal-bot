package org.kor.portal.service.robofinist.model.event

data class Event(
    val id: Int,
    val name: String,
    val location: String? = null,
    val beginAt: String? = null,
    val registrationEndAt: String? = null,
)
