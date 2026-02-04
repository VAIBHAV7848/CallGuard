package com.callguard.contacts

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

object ContactLookup {

    fun existsInContacts(context: Context, phoneNumber: String): Boolean {
        val normalizedNumber = normalizeNumber(phoneNumber)
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(normalizedNumber)
        )
        
        return context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup._ID),
            null, null, null
        )?.use { cursor ->
            cursor.count > 0
        } ?: false
    }

    fun getContactInfo(context: Context, phoneNumber: String): ContactInfo? {
        val normalizedNumber = normalizeNumber(phoneNumber)
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(normalizedNumber)
        )
        
        return context.contentResolver.query(
            uri,
            arrayOf(
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.PHOTO_URI
            ),
            null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                ContactInfo(
                    name = cursor.getString(0),
                    photoUri = cursor.getString(1)
                )
            } else null
        }
    }

    fun getName(context: Context, phoneNumber: String): String? {
        return getContactInfo(context, phoneNumber)?.name
    }

    fun getPhotoUri(context: Context, phoneNumber: String): Uri? {
        return getContactInfo(context, phoneNumber)?.photoUri?.let { Uri.parse(it) }
    }

    private fun normalizeNumber(number: String): String {
        return number.replace(Regex("[^0-9+]"), "")
    }

    data class ContactInfo(
        val name: String?,
        val photoUri: String?
    )
}
