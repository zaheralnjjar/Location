package com.directnumber.app.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract

sealed class ContactLaunchResult {
    data object Success : ContactLaunchResult()
    data object NoContactsApp : ContactLaunchResult()
}

/**
 * Delegates contact creation to the device's own Contacts app via ACTION_INSERT, pre-filling
 * name/number/type. The user always sees the system "new contact" screen and picks the account
 * (Google, Samsung, device-only, ...) themselves — this app never touches the contacts
 * provider directly and never requests READ_CONTACTS/WRITE_CONTACTS.
 */
object ContactLauncher {

    fun open(
        context: Context,
        name: String,
        e164Number: String,
        note: String? = null,
    ): ContactLaunchResult {
        val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
            if (name.isNotBlank()) putExtra(ContactsContract.Intents.Insert.NAME, name)
            putExtra(ContactsContract.Intents.Insert.PHONE, e164Number)
            putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
            if (!note.isNullOrBlank()) putExtra(ContactsContract.Intents.Insert.NOTES, note)
        }
        return try {
            context.startActivity(intent)
            ContactLaunchResult.Success
        } catch (e: ActivityNotFoundException) {
            ContactLaunchResult.NoContactsApp
        }
    }
}
