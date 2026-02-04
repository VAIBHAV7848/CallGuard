package com.callguard.contacts

import android.content.Context
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsRepository(private val context: Context) {

    suspend fun getAllContacts(): List<Contact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<Contact>()
        
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )
        
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null, null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
            
            val seenIds = mutableSetOf<String>()
            
            while (cursor.moveToNext()) {
                val id = cursor.getString(idIndex)
                if (id !in seenIds) {
                    seenIds.add(id)
                    contacts.add(
                        Contact(
                            id = id,
                            name = cursor.getString(nameIndex) ?: "",
                            phoneNumber = cursor.getString(numberIndex) ?: "",
                            photoUri = cursor.getString(photoIndex)
                        )
                    )
                }
            }
        }
        
        contacts
    }

    suspend fun searchContacts(query: String): List<Contact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<Contact>()
        
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?"
        val selectionArgs = arrayOf("%$query%", "%$query%")
        
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            ),
            selection,
            selectionArgs,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                contacts.add(
                    Contact(
                        id = cursor.getString(0),
                        name = cursor.getString(1) ?: "",
                        phoneNumber = cursor.getString(2) ?: "",
                        photoUri = cursor.getString(3)
                    )
                )
            }
        }
        
        contacts
    }
}

data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val photoUri: String?
)
