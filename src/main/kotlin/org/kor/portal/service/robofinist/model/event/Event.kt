package org.kor.portal.service.robofinist.model.event

data class Event(
    val id: Int,
    val name: String,
    val location: String? = null,
    val brief: String? = null,
)
