package com.callguard.call

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.callguard.ai.AICallHandler
import com.callguard.contacts.ContactLookup
import com.callguard.core.Constants
import com.callguard.databinding.ActivityIncomingCallBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class IncomingCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIncomingCallBinding
    private var phoneNumber: String = ""
    private var contactName: String? = null
    private var isUnknownCaller = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        
        binding = ActivityIncomingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        phoneNumber = intent.getStringExtra(Constants.EXTRA_PHONE_NUMBER) ?: ""
        contactName = intent.getStringExtra(Constants.EXTRA_CONTACT_NAME)
        isUnknownCaller = !ContactLookup.existsInContacts(this, phoneNumber)
        
        setupUI()
        observeCallState()
    }

    private fun setupUI() {
        // Display caller info
        binding.tvCallerName.text = contactName ?: phoneNumber
        binding.tvPhoneNumber.text = if (contactName != null) phoneNumber else ""
        
        // Load contact photo
        ContactLookup.getPhotoUri(this, phoneNumber)?.let { uri ->
            binding.ivCallerPhoto.setImageURI(uri)
        }
        
        // AI indicator for unknown callers
        binding.tvAIIndicator.visibility = if (isUnknownCaller) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }
        
        // Answer button
        binding.btnAnswer.setOnClickListener {
            CallStateManager.answerCall()
            navigateToInCall(false)
        }
        
        // Reject button
        binding.btnReject.setOnClickListener {
            CallStateManager.rejectCall()
            finish()
        }
        
        // Let AI handle (only for unknown callers)
        binding.btnLetAIHandle.visibility = if (isUnknownCaller) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }
        
        binding.btnLetAIHandle.setOnClickListener {
            AICallHandler.interceptCurrentCall(this)
            navigateToInCall(true)
        }
    }

    private fun observeCallState() {
        lifecycleScope.launch {
            CallStateManager.callState.collectLatest { state ->
                when (state) {
                    CallState.ACTIVE -> navigateToInCall(AICallHandler.isActive)
                    CallState.DISCONNECTED, CallState.IDLE -> finish()
                    else -> { /* Continue showing incoming UI */ }
                }
            }
        }
    }

    private fun navigateToInCall(isAICall: Boolean) {
        val intent = Intent(this, InCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Constants.EXTRA_PHONE_NUMBER, phoneNumber)
            putExtra(Constants.EXTRA_CONTACT_NAME, contactName)
            putExtra(Constants.EXTRA_IS_AI_CALL, isAICall)
        }
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Prevent back press during incoming call
    }
}
