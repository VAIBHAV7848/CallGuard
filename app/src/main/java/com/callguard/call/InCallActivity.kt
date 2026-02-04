package com.callguard.call

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.callguard.ai.AICallHandler
import com.callguard.contacts.ContactLookup
import com.callguard.core.Constants
import com.callguard.databinding.ActivityIncallBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class InCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIncallBinding
    private var phoneNumber: String = ""
    private var contactName: String? = null
    private var isAICall: Boolean = false
    private var isMuted = false
    private var isSpeakerOn = false
    
    private val durationHandler = Handler(Looper.getMainLooper())
    private val durationRunnable = object : Runnable {
        override fun run() {
            updateDuration()
            durationHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        phoneNumber = intent.getStringExtra(Constants.EXTRA_PHONE_NUMBER) ?: ""
        contactName = intent.getStringExtra(Constants.EXTRA_CONTACT_NAME)
        isAICall = intent.getBooleanExtra(Constants.EXTRA_IS_AI_CALL, false)
        
        setupUI()
        setupControls()
        observeCallState()
        observeAI()
    }

    private fun setupUI() {
        binding.tvCallerName.text = contactName ?: phoneNumber
        binding.tvPhoneNumber.text = if (contactName != null) phoneNumber else ""
        
        ContactLookup.getPhotoUri(this, phoneNumber)?.let { uri ->
            binding.ivCallerPhoto.setImageURI(uri)
        }
        
        // AI overlay visibility
        binding.layoutAIOverlay.visibility = if (isAICall) View.VISIBLE else View.GONE
    }

    private fun setupControls() {
        // End call
        binding.btnEndCall.setOnClickListener {
            CallStateManager.endCall()
        }
        
        // Mute
        binding.btnMute.setOnClickListener {
            isMuted = !isMuted
            binding.btnMute.isSelected = isMuted
            CallStateManager.toggleMute()
        }
        
        // Speaker
        binding.btnSpeaker.setOnClickListener {
            isSpeakerOn = !isSpeakerOn
            binding.btnSpeaker.isSelected = isSpeakerOn
            CallStateManager.toggleSpeaker()
        }
        
        // Keypad
        binding.btnKeypad.setOnClickListener {
            binding.layoutKeypad.visibility = 
                if (binding.layoutKeypad.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        
        // Keypad buttons
        setupKeypad()
        
        // AI Controls
        binding.btnTakeOver.setOnClickListener {
            AICallHandler.userTakeover()
            isAICall = false
            binding.layoutAIOverlay.visibility = View.GONE
        }
        
        binding.btnMuteAI.setOnClickListener {
            AICallHandler.muteAI(!binding.btnMuteAI.isSelected)
            binding.btnMuteAI.isSelected = !binding.btnMuteAI.isSelected
        }
    }

    private fun setupKeypad() {
        val keypadButtons = mapOf(
            binding.btn0 to '0', binding.btn1 to '1', binding.btn2 to '2',
            binding.btn3 to '3', binding.btn4 to '4', binding.btn5 to '5',
            binding.btn6 to '6', binding.btn7 to '7', binding.btn8 to '8',
            binding.btn9 to '9', binding.btnStar to '*', binding.btnHash to '#'
        )
        
        keypadButtons.forEach { (button, digit) ->
            button.setOnClickListener {
                CallStateManager.sendDtmf(digit)
            }
        }
    }

    private fun observeCallState() {
        lifecycleScope.launch {
            CallStateManager.callState.collectLatest { state ->
                when (state) {
                    CallState.ACTIVE -> {
                        durationHandler.post(durationRunnable)
                        binding.tvCallState.text = "Connected"
                    }
                    CallState.DIALING -> binding.tvCallState.text = "Dialing..."
                    CallState.RINGING -> binding.tvCallState.text = "Ringing..."
                    CallState.HOLDING -> binding.tvCallState.text = "On Hold"
                    CallState.DISCONNECTED -> {
                        durationHandler.removeCallbacks(durationRunnable)
                        finish()
                    }
                    CallState.IDLE -> finish()
                }
            }
        }
    }

    private fun observeAI() {
        lifecycleScope.launch {
            AICallHandler.transcriptFlow.collectLatest { transcript ->
                binding.tvTranscript.text = transcript
                binding.scrollTranscript.fullScroll(View.FOCUS_DOWN)
            }
        }
        
        lifecycleScope.launch {
            AICallHandler.intentFlow.collectLatest { intent ->
                binding.tvIntent.text = "Intent: ${intent ?: "Detecting..."}"
            }
        }
    }

    private fun updateDuration() {
        val startTime = CallStateManager.callStartTime.value
        if (startTime > 0) {
            val duration = (System.currentTimeMillis() - startTime) / 1000
            val minutes = duration / 60
            val seconds = duration % 60
            binding.tvDuration.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        durationHandler.removeCallbacks(durationRunnable)
    }
}
