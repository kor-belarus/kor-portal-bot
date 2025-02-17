package org.kor.portal.service.tm


data class CommandRequest(
    val path: List<String>,
    val chatId: String,
    val messageId: Int? = null,
) : Iterator<String> {

    private var read = 0

    override fun next(): String {
        if (hasNext()) {
            return path[read++]
        }
        throw NoSuchElementException()
    }

    override fun hasNext() = path.size > read

    fun createPathRemoving(vararg removing: String) = path.filter { !removing.contains(it) }

}
