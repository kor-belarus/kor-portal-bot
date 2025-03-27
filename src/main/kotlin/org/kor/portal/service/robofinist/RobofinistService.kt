package org.kor.portal.service.robofinist


import org.kor.portal.service.robofinist.model.bid.Bid
import org.kor.portal.service.robofinist.model.event.Event
import org.kor.portal.service.robofinist.model.event.EventsSearchResponse
import org.kor.portal.service.robofinist.model.program.Program
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service


@Service
class RobofinistService(
    private val robofinistClient: RobofinistClient,
    @param:Value("\${robofinist.partnerId}") val partnerId: Int,
) {

    fun getEvents(): EventsSearchResponse = robofinistClient.getEvents(partnerId = partnerId)

    fun getEvents(id: Int): Event? = robofinistClient.getEvents(id = id).data.firstOrNull()

    fun getPrograms(eventId: Long): List<Program> = robofinistClient.getPrograms(eventId = eventId).data

    fun getBids(programId: Long): List<Bid> = robofinistClient.getBids(programId = programId).data

}
