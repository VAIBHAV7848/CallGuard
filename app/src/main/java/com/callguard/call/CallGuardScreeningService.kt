package com.callguard.call

import android.telecom.Call
import android.telecom.CallScreeningService
import com.callguard.ai.AICallHandler
import com.callguard.contacts.ContactLookup

class CallGuardScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: ""
        
        // HARD RULE: Check if number is in contacts
        val isKnownContact = ContactLookup.existsInContacts(this, phoneNumber)
        
        if (isKnownContact) {
            // ✅ KNOWN CONTACT → Normal phone behavior
            respondToCall(callDetails, CallResponse.Builder()
                .setDisallowCall(false)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build())
        } else {
            // 🤖 UNKNOWN NUMBER → AI intercepts
            respondToCall(callDetails, CallResponse.Builder()
                .setDisallowCall(false)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build())
            
            // Trigger AI interception
            AICallHandler.scheduleInterception(this, phoneNumber)
        }
    }
}
