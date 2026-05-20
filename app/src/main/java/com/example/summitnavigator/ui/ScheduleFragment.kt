// AI-assisted
package com.example.summitnavigator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.summitnavigator.R
import com.example.summitnavigator.data.SummitRepository
import com.example.summitnavigator.data.model.Session
import com.example.summitnavigator.databinding.FragmentScheduleBinding
import com.example.summitnavigator.databinding.ItemSessionBinding
import kotlinx.coroutines.launch

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ScheduleViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize dependencies and ViewModel using SummitApp singleton repository
        val app = requireActivity().application as com.example.summitnavigator.SummitApp
        val repository = app.repository
        val factory = ScheduleViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ScheduleViewModel::class.java]

        // Setup RecyclerView
        val adapter = SessionAdapter { session ->
            val bundle = Bundle().apply {
                putString("sessionId", session.sessionId)
            }
            findNavController().navigate(
                R.id.action_scheduleFragment_to_sessionDetailFragment,
                bundle
            )
        }
        binding.recyclerSchedule.adapter = adapter
        binding.recyclerSchedule.layoutManager = LinearLayoutManager(requireContext())

        binding.editSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchQuery.value = s?.toString() ?: ""
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Refresh action triggers repository data refresh
        binding.btnRefresh.setOnClickListener {
            viewModel.refreshSchedule()
        }

        binding.btnProfile.setOnClickListener {
            findNavController().navigate(R.id.action_scheduleFragment_to_profileFragment)
        }
        
        binding.btnMyAgenda.setOnClickListener {
            findNavController().navigate(R.id.action_scheduleFragment_to_myAgendaFragment)
        }
        
        binding.fabAddSession.setOnClickListener {
            // For now, just generate a random session for demo purposes
            val dummySession = Session(
                sessionId = "sess_" + System.currentTimeMillis(),
                speakerOwnerId = "s1",
                title = "New Admin Session",
                roomLocation = "Admin Room",
                timestamp = System.currentTimeMillis(),
                isVipOnly = true
            )
            viewLifecycleOwner.lifecycleScope.launch {
                val app = requireActivity().application as com.example.summitnavigator.SummitApp
                app.repository.createSession(dummySession)
                Toast.makeText(requireContext(), "Session Created", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe schedule and status changes
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.scheduleUiState.collect { uiState ->
                        when (uiState) {
                            is ListUiState.Loading -> {
                                binding.layoutSkeleton.visibility = View.VISIBLE
                                binding.recyclerSchedule.visibility = View.GONE
                                binding.textEmpty.visibility = View.GONE
                            }
                            is ListUiState.Empty -> {
                                binding.layoutSkeleton.visibility = View.GONE
                                binding.recyclerSchedule.visibility = View.GONE
                                binding.textEmpty.visibility = View.VISIBLE
                            }
                            is ListUiState.Success -> {
                                binding.layoutSkeleton.visibility = View.GONE
                                binding.recyclerSchedule.visibility = View.VISIBLE
                                binding.textEmpty.visibility = View.GONE
                                adapter.submitList(uiState.sessions)
                            }
                        }
                    }
                }
                launch {
                    viewModel.refreshStatus.collect { status ->
                        when (status) {
                            is RefreshStatus.Loading -> {
                                binding.btnRefresh.isEnabled = false
                            }
                            is RefreshStatus.Success -> {
                                binding.btnRefresh.isEnabled = true
                            }
                            is RefreshStatus.Error -> {
                                binding.btnRefresh.isEnabled = true
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.text_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is RefreshStatus.Idle -> {
                                binding.btnRefresh.isEnabled = true
                            }
                        }
                    }
                }
                launch {
                    viewModel.userRole.collect { role ->
                        binding.fabAddSession.visibility = if (role == "admin") View.VISIBLE else View.GONE
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

class SessionAdapter(
    private val onItemClick: (Session) -> Unit
) : ListAdapter<Session, SessionAdapter.SessionViewHolder>(SessionDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemSessionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SessionViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SessionViewHolder(
        private val binding: ItemSessionBinding,
        private val onItemClick: (Session) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(session: Session) {
            binding.textSessionTitle.text = session.title
            
            val context = binding.root.context
            binding.textSessionRoom.text = context.getString(R.string.label_room, session.roomLocation)
            
            val timeString = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(session.timestamp))
            binding.textSessionTime.text = context.getString(R.string.label_time, timeString)
            
            binding.textVipMarker.visibility = if (session.isVipOnly) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                onItemClick(session)
            }
        }
    }

    object SessionDiffCallback : DiffUtil.ItemCallback<Session>() {
        override fun areItemsTheSame(oldItem: Session, newItem: Session): Boolean {
            return oldItem.sessionId == newItem.sessionId
        }

        override fun areContentsTheSame(oldItem: Session, newItem: Session): Boolean {
            return oldItem == newItem
        }
    }
}
