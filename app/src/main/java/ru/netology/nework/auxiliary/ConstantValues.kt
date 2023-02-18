package ru.netology.nework.auxiliary

import ru.netology.nework.dto.EventResponse
import ru.netology.nework.dto.EventType
import ru.netology.nework.dto.Post
import ru.netology.nework.model.MediaModel

object ConstantValues {
    const val POST_CONTENT = "content"
    const val POST_LINK = "link"
    const val POST_MENTIONS_COUNT = "count mentions in post"
    const val EVENT_ID = "event id"
    const val EVENT_REQUEST_TYPE = "party or speakers"
    val emptyPost = Post(
        id = 0,
        authorId = 0,
        content = "",
        author = "Нетология",
        likeOwnerIds = emptyList(),
        countShared = 0,
        mentionIds = emptyList(),
        published = ""
    )
    val emptyEvent = EventResponse(
        id = 0,
        authorId = 0,
        content = "",
        author = "",
        likeOwnerIds = emptyList(),
        datetime = "",
        type = EventType.OFFLINE,
        published = "",
    )
    val noPhoto = MediaModel()
}