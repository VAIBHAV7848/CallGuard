package com.callguard.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.callguard.core.CallGuardApplication
import com.callguard.core.Constants
import com.callguard.databinding.ActivityAiSummaryBinding
import com.callguard.dialer.DialerActivity
import kotlinx.coroutines.launch

class AISummaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAiSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val callId = intent.getLongExtra(Constants.EXTRA_CALL_LOG_ID, -1)
        if (callId == -1L) { finish(); return }
        
        loadCallDetails(callId)
    }

    private fun loadCallDetails(callId: Long) {
        lifecycleScope.launch {
            val entry = CallGuardApplication.instance.database.callHistoryDao().getCallById(callId)
            entry?.let { displaySummary(it) }
        }
    }

    private fun displaySummary(entry: CallLogEntry) {
        binding.tvCallerNumber.text = entry.contactName ?: entry.phoneNumber
        binding.tvIntent.text = "Intent: ${entry.aiIntent?.name ?: "Unknown"}"
        binding.tvSummary.text = entry.aiSummary ?: "No summary available"
        binding.tvKeyPoints.text = entry.aiKeyPoints?.joinToString("\n• ", "• ") ?: ""
        binding.tvTranscript.text = entry.aiTranscript ?: ""
        binding.btnCallBack.setOnClickListener { (this as? DialerActivity)?.dialNumber(entry.phoneNumber); finish() }
        binding.btnBack.setOnClickListener { finish() }
    }
}
