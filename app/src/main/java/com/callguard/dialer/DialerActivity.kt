package com.callguard.dialer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.callguard.R
import com.callguard.core.PermissionManager
import com.callguard.databinding.ActivityDialerBinding
import com.google.android.material.tabs.TabLayoutMediator

class DialerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDialerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDialerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        checkPermissions()
        setupViewPager()
        handleIntent(intent)
    }

    private fun checkPermissions() {
        if (!PermissionManager.hasAllPermissions(this)) {
            PermissionManager.requestPermissions(this)
        }
        if (!PermissionManager.isDefaultDialer(this)) {
            PermissionManager.requestDefaultDialer(this)
        }
    }

    private fun setupViewPager() {
        val fragments = listOf(DialpadFragment(), ContactsFragment(), CallHistoryFragment())
        val titles = listOf("Keypad", "Contacts", "History")
        
        binding.viewPager.adapter = object : androidx.viewpager2.adapter.FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
            tab.setIcon(when(position) { 0 -> R.drawable.ic_dialpad; 1 -> R.drawable.ic_contacts; else -> R.drawable.ic_history })
        }.attach()
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_DIAL || intent?.action == Intent.ACTION_CALL) {
            intent.data?.schemeSpecificPart?.let { number ->
                binding.viewPager.currentItem = 0
                (supportFragmentManager.fragments.firstOrNull { it is DialpadFragment } as? DialpadFragment)?.setNumber(number)
            }
        }
    }

    fun dialNumber(number: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            val uri = Uri.fromParts("tel", number, null)
            val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
            telecomManager.placeCall(uri, null)
        }
    }
}
