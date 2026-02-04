package com.callguard.dialer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.callguard.core.CallGuardApplication
import com.callguard.core.Constants
import com.callguard.databinding.FragmentCallHistoryBinding
import com.callguard.databinding.ItemCallLogBinding
import com.callguard.history.AISummaryActivity
import com.callguard.history.CallLogEntry
import com.callguard.history.CallType
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CallHistoryFragment : Fragment() {
    private var _binding: FragmentCallHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CallHistoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCallHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CallHistoryAdapter(
            onCall = { entry -> (activity as? DialerActivity)?.dialNumber(entry.phoneNumber) },
            onDetails = { entry -> if (entry.wasAIHandled) startActivity(Intent(context, AISummaryActivity::class.java).putExtra(Constants.EXTRA_CALL_LOG_ID, entry.id)) }
        )
        binding.rvHistory.layoutManager = LinearLayoutManager(context)
        binding.rvHistory.adapter = adapter
        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            CallGuardApplication.instance.database.callHistoryDao().getAllCalls().collectLatest { adapter.submitList(it) }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

class CallHistoryAdapter(private val onCall: (CallLogEntry) -> Unit, private val onDetails: (CallLogEntry) -> Unit) : androidx.recyclerview.widget.ListAdapter<CallLogEntry, CallHistoryAdapter.VH>(object : androidx.recyclerview.widget.DiffUtil.ItemCallback<CallLogEntry>() {
    override fun areItemsTheSame(old: CallLogEntry, new: CallLogEntry) = old.id == new.id
    override fun areContentsTheSame(old: CallLogEntry, new: CallLogEntry) = old == new
}) {
    inner class VH(private val binding: ItemCallLogBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: CallLogEntry) {
            binding.tvName.text = entry.contactName ?: entry.phoneNumber
            binding.tvTime.text = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(entry.timestamp))
            binding.ivCallType.setImageResource(when(entry.callType) { CallType.INCOMING -> com.callguard.R.drawable.ic_incoming; CallType.OUTGOING -> com.callguard.R.drawable.ic_outgoing; CallType.MISSED -> com.callguard.R.drawable.ic_missed })
            binding.ivAIBadge.visibility = if (entry.wasAIHandled) View.VISIBLE else View.GONE
            binding.tvIntent.text = entry.aiIntent?.name ?: ""
            binding.tvIntent.visibility = if (entry.wasAIHandled) View.VISIBLE else View.GONE
            binding.btnCall.setOnClickListener { onCall(entry) }
            binding.root.setOnClickListener { onDetails(entry) }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(ItemCallLogBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}
