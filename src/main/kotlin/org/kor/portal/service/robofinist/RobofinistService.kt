package org.kor.portal.service.robofinist


import org.kor.portal.service.robofinist.model.event.EventsSearchResponse
import org.springframework.stereotype.Service


@Service
class RobofinistService (
    private val robofinistClient: RobofinistClient,
) {

    fun getEvents(): EventsSearchResponse = robofinistClient.getEvents()

}
