package com.example.summitnavigator.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.summitnavigator.R
import com.example.summitnavigator.databinding.FragmentAuthBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AuthFragment : Fragment() {
    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If already logged in, skip auth
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val app = requireActivity().application as com.example.summitnavigator.SummitApp
            viewLifecycleOwner.lifecycleScope.launch {
                app.repository.syncUserBookmarks(currentUser.uid)
                findNavController().navigate(R.id.action_authFragment_to_scheduleFragment)
            }
            return
        }

        binding.btnSignIn.setOnClickListener {
            val email = binding.editEmail.text.toString()
            val pass = binding.editPassword.text.toString()
            viewModel.signIn(email, pass)
        }

        binding.btnSignUp.setOnClickListener {
            val email = binding.editEmail.text.toString()
            val pass = binding.editPassword.text.toString()
            viewModel.signUp(email, pass)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Loading -> {
                            binding.progressAuth.visibility = View.VISIBLE
                            binding.btnSignIn.isEnabled = false
                            binding.btnSignUp.isEnabled = false
                        }
                        is AuthState.Success -> {
                            binding.progressAuth.visibility = View.GONE
                            val user = FirebaseAuth.getInstance().currentUser
                            if (user != null) {
                                val app = requireActivity().application as com.example.summitnavigator.SummitApp
                                viewLifecycleOwner.lifecycleScope.launch {
                                    app.repository.syncUserBookmarks(user.uid)
                                    findNavController().navigate(R.id.action_authFragment_to_scheduleFragment)
                                }
                            } else {
                                findNavController().navigate(R.id.action_authFragment_to_scheduleFragment)
                            }
                        }
                        is AuthState.Error -> {
                            binding.progressAuth.visibility = View.GONE
                            binding.btnSignIn.isEnabled = true
                            binding.btnSignUp.isEnabled = true
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            binding.progressAuth.visibility = View.GONE
                            binding.btnSignIn.isEnabled = true
                            binding.btnSignUp.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
