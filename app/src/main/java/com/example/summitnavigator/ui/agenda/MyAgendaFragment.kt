package com.example.summitnavigator.ui.agenda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.summitnavigator.R
import com.example.summitnavigator.SummitApp
import com.example.summitnavigator.databinding.FragmentMyAgendaBinding
import com.example.summitnavigator.ui.ListUiState
import com.example.summitnavigator.ui.ScheduleViewModel
import com.example.summitnavigator.ui.ScheduleViewModelFactory
import com.example.summitnavigator.ui.SessionAdapter
import kotlinx.coroutines.launch

class MyAgendaFragment : Fragment() {

    private var _binding: FragmentMyAgendaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScheduleViewModel by viewModels {
        ScheduleViewModelFactory((requireActivity().application as SummitApp).repository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyAgendaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SessionAdapter { session ->
            val bundle = Bundle().apply {
                putString("sessionId", session.sessionId)
            }
            findNavController().navigate(
                R.id.action_myAgendaFragment_to_sessionDetailFragment,
                bundle
            )
        }
        binding.recyclerAgenda.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAgenda.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.agendaUiState.collect { uiState ->
                    when (uiState) {
                        is ListUiState.Loading -> {
                            binding.recyclerAgenda.visibility = View.GONE
                            binding.layoutEmptyState.visibility = View.GONE
                        }
                        is ListUiState.Empty -> {
                            binding.recyclerAgenda.visibility = View.GONE
                            binding.layoutEmptyState.visibility = View.VISIBLE
                        }
                        is ListUiState.Success -> {
                            binding.recyclerAgenda.visibility = View.VISIBLE
                            binding.layoutEmptyState.visibility = View.GONE
                            adapter.submitList(uiState.sessions)
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
