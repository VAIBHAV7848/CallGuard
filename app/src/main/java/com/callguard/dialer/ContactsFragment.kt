package com.callguard.dialer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.callguard.contacts.Contact
import com.callguard.contacts.ContactsRepository
import com.callguard.databinding.FragmentContactsBinding
import kotlinx.coroutines.launch

class ContactsFragment : Fragment() {
    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ContactsAdapter
    private lateinit var repository: ContactsRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ContactsRepository(requireContext())
        adapter = ContactsAdapter { contact -> (activity as? DialerActivity)?.dialNumber(contact.phoneNumber) }
        binding.rvContacts.layoutManager = LinearLayoutManager(context)
        binding.rvContacts.adapter = adapter
        loadContacts()
    }

    private fun loadContacts() {
        lifecycleScope.launch {
            val contacts = repository.getAllContacts()
            adapter.submitList(contacts)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

class ContactsAdapter(private val onClick: (Contact) -> Unit) : androidx.recyclerview.widget.ListAdapter<Contact, ContactsAdapter.VH>(object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Contact>() {
    override fun areItemsTheSame(old: Contact, new: Contact) = old.id == new.id
    override fun areContentsTheSame(old: Contact, new: Contact) = old == new
}) {
    inner class VH(private val binding: com.callguard.databinding.ItemContactBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact) {
            binding.tvName.text = contact.name
            binding.tvNumber.text = contact.phoneNumber
            contact.photoUri?.let { binding.ivPhoto.setImageURI(android.net.Uri.parse(it)) }
            binding.root.setOnClickListener { onClick(contact) }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(com.callguard.databinding.ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}
