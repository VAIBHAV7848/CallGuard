package com.callguard.dialer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.callguard.databinding.FragmentDialpadBinding

class DialpadFragment : Fragment() {
    private var _binding: FragmentDialpadBinding? = null
    private val binding get() = _binding!!
    private val numberBuilder = StringBuilder()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDialpadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupKeypad()
        binding.btnCall.setOnClickListener { if (numberBuilder.isNotEmpty()) (activity as? DialerActivity)?.dialNumber(numberBuilder.toString()) }
        binding.btnBackspace.setOnClickListener { if (numberBuilder.isNotEmpty()) { numberBuilder.deleteCharAt(numberBuilder.length - 1); updateDisplay() } }
        binding.btnBackspace.setOnLongClickListener { numberBuilder.clear(); updateDisplay(); true }
    }

    private fun setupKeypad() {
        val buttons = mapOf(binding.btn0 to "0", binding.btn1 to "1", binding.btn2 to "2", binding.btn3 to "3", binding.btn4 to "4", binding.btn5 to "5", binding.btn6 to "6", binding.btn7 to "7", binding.btn8 to "8", binding.btn9 to "9", binding.btnStar to "*", binding.btnHash to "#")
        buttons.forEach { (btn, digit) -> btn.setOnClickListener { numberBuilder.append(digit); updateDisplay() } }
    }

    private fun updateDisplay() { binding.tvNumber.text = numberBuilder.toString() }
    fun setNumber(number: String) { numberBuilder.clear(); numberBuilder.append(number); updateDisplay() }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
