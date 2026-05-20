package com.example.summitnavigator.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.summitnavigator.R
import com.example.summitnavigator.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            binding.textUserEmail.text = user.email
        } else {
            // Not logged in, go back
            findNavController().popBackStack()
        }

        binding.btnSignOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val app = requireActivity().application as com.example.summitnavigator.SummitApp
            viewLifecycleOwner.lifecycleScope.launch {
                app.repository.clearLocalBookmarks()
                findNavController().navigate(R.id.action_profileFragment_to_authFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
