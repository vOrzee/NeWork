package ru.netology.nework.auxiliary

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.ZoneOffset


object FloatingValue {
    var textNewPost = ""
    var currentFragment = ""

    fun convertDatePublished(dateString: String): String {
        val date = dateString.substring(0..9)
        val time = dateString.substring(11..18)
        return "$date $time"
    }

    fun getExtensionFromUri(uri: Uri, contentResolver: ContentResolver): String? {
        val mimeType = contentResolver.getType(uri) ?: return null
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    }

    fun convertDateTimeString(inputString: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val dateTime = LocalDateTime.parse(inputString, inputFormatter)
            .atOffset(ZoneOffset.UTC)

        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
        return dateTime.format(outputFormatter)
    }
}