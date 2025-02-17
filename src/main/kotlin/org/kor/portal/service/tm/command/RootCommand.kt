package org.kor.portal.service.tm.command

import org.springframework.stereotype.Service

@Service
class RootCommand : StartCommand() {
    override val command: String
        get() = ""
}