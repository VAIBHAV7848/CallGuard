package com.callguard.call

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import com.callguard.contacts.ContactLookup
import com.callguard.core.Constants

class CallGuardInCallService : InCallService() {

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallStateManager.setCall(call)
        
        val phoneNumber = call.details.handle?.schemeSpecificPart ?: ""
        val isIncoming = call.state == Call.STATE_RINGING
        
        if (isIncoming) {
            val contactName = ContactLookup.getName(this, phoneNumber)
            
            val intent = Intent(this, IncomingCallActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(Constants.EXTRA_PHONE_NUMBER, phoneNumber)
                putExtra(Constants.EXTRA_CONTACT_NAME, contactName)
            }
            startActivity(intent)
        } else {
            // Outgoing call
            val intent = Intent(this, InCallActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(Constants.EXTRA_PHONE_NUMBER, phoneNumber)
                putExtra(Constants.EXTRA_IS_AI_CALL, false)
            }
            startActivity(intent)
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        if (CallStateManager.currentCall.value == call) {
            CallStateManager.setCall(null)
        }
    }

    override fun onBringToForeground(showDialpad: Boolean) {
        super.onBringToForeground(showDialpad)
        val intent = Intent(this, InCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        startActivity(intent)
    }
}
