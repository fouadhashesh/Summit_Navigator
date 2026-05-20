package com.example.summitnavigator.ui.speakers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.summitnavigator.R
import com.example.summitnavigator.databinding.FragmentSpeakerDetailBinding
import com.example.summitnavigator.ui.SessionAdapter
import kotlinx.coroutines.launch

class SpeakerDetailFragment : Fragment() {

    private var _binding: FragmentSpeakerDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpeakerDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val speakerId = arguments?.getString("speakerId") ?: return
        
        val app = requireActivity().application as com.example.summitnavigator.SummitApp
        val repository = app.repository

        val adapter = SessionAdapter { session ->
            val bundle = Bundle().apply {
                putString("sessionId", session.sessionId)
            }
            findNavController().navigate(R.id.action_speakerDetailFragment_to_sessionDetailFragment, bundle)
        }
        
        binding.recyclerSpeakerSessions.adapter = adapter
        binding.recyclerSpeakerSessions.layoutManager = LinearLayoutManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    repository.getSpeakerById(speakerId).collect { speaker ->
                        speaker?.let {
                            binding.textSpeakerName.text = it.name
                            binding.textSpeakerCompany.text = it.company
                            binding.textSpeakerBio.text = it.biography
                        }
                    }
                }
                launch {
                    repository.getSessionsForSpeaker(speakerId).collect { sessions ->
                        adapter.submitList(sessions)
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
