package org.kor.portal

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@SpringBootApplication
@ConfigurationPropertiesScan
class KorPortalApplication

fun main(args: Array<String>) {
    runApplication<KorPortalApplication>(*args)
}
