package org.kor.portal.service.robofinist.model.program

import com.fasterxml.jackson.annotation.JsonProperty

data class Program(
    @param:JsonProperty("id") val id: Int,
    @param:JsonProperty("name") val name: String,
)
