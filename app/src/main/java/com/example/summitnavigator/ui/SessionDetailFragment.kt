// AI-assisted
package com.example.summitnavigator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.summitnavigator.R
import com.example.summitnavigator.data.SummitRepository
import com.example.summitnavigator.databinding.FragmentSessionDetailBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class SessionDetailFragment : Fragment() {

    private var _binding: FragmentSessionDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionId = arguments?.getString("sessionId") ?: ""
        if (sessionId.isEmpty()) return

        // Initialize dependencies using SummitApp singleton repository
        val app = requireActivity().application as com.example.summitnavigator.SummitApp
        val repository = app.repository
        
        val user = FirebaseAuth.getInstance().currentUser

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe session flow reactively from Room
                launch {
                    repository.getSessionById(sessionId).collect { session ->
                        session?.let { currentSession ->
                            binding.textDetailTitle.text = currentSession.title
                            binding.textDetailRoom.text = getString(R.string.label_room, currentSession.roomLocation)
                            
                            val timeString = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                .format(java.util.Date(currentSession.timestamp))
                            binding.textDetailTime.text = getString(R.string.label_time, timeString)
                            
                            val isBookmarked = currentSession.isBookmarked
                            binding.btnBookmark.text = getString(if (isBookmarked) R.string.action_unbookmark else R.string.action_bookmark)
                            binding.btnBookmark.setOnClickListener {
                                if (user != null) {
                                    viewLifecycleOwner.lifecycleScope.launch {
                                        repository.toggleBookmark(sessionId, user.uid, !isBookmarked)
                                    }
                                }
                            }
                            
                            viewLifecycleOwner.lifecycleScope.launch {
                                val role = if (user != null) repository.getUserRole(user.uid) else "attendee"
                                binding.btnDeleteSession.visibility = if (role == "admin") View.VISIBLE else View.GONE
                                binding.btnEditSession.visibility = if (role == "admin") View.VISIBLE else View.GONE
                                binding.btnEditSpeaker.visibility = if (role == "admin") View.VISIBLE else View.GONE
                            }
                            
                            binding.btnEditSession.setOnClickListener {
                                val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_session, null)
                                val editTitle = dialogView.findViewById<android.widget.EditText>(R.id.edit_title)
                                val editRoom = dialogView.findViewById<android.widget.EditText>(R.id.edit_room)
                                val editTime = dialogView.findViewById<android.widget.EditText>(R.id.edit_time)
                                val checkVip = dialogView.findViewById<android.widget.CheckBox>(R.id.check_vip)

                                editTitle.setText(currentSession.title)
                                editRoom.setText(currentSession.roomLocation)
                                checkVip.isChecked = currentSession.isVipOnly
                                
                                var updatedTimestamp = currentSession.timestamp
                                val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                editTime.setText(timeFormat.format(java.util.Date(updatedTimestamp)))

                                editTime.setOnClickListener {
                                    val calendar = java.util.Calendar.getInstance()
                                    calendar.timeInMillis = updatedTimestamp
                                    android.app.TimePickerDialog(
                                        requireContext(),
                                        { _, hourOfDay, minute ->
                                            calendar.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                                            calendar.set(java.util.Calendar.MINUTE, minute)
                                            updatedTimestamp = calendar.timeInMillis
                                            editTime.setText(timeFormat.format(java.util.Date(updatedTimestamp)))
                                        },
                                        calendar.get(java.util.Calendar.HOUR_OF_DAY),
                                        calendar.get(java.util.Calendar.MINUTE),
                                        true
                                    ).show()
                                }

                                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                    .setTitle("Edit Session")
                                    .setView(dialogView)
                                    .setPositiveButton("Save") { _, _ ->
                                        val updatedSession = currentSession.copy(
                                            title = editTitle.text.toString(),
                                            roomLocation = editRoom.text.toString(),
                                            timestamp = updatedTimestamp,
                                            isVipOnly = checkVip.isChecked
                                        )
                                        viewLifecycleOwner.lifecycleScope.launch {
                                            repository.updateSession(updatedSession)
                                        }
                                    }
                                    .setNegativeButton("Cancel", null)
                                    .show()
                            }
                            
                            binding.btnDeleteSession.setOnClickListener {
                                viewLifecycleOwner.lifecycleScope.launch {
                                    repository.deleteSession(sessionId)
                                    findNavController().navigateUp()
                                }
                            }
                        }
                    }
                }

                // Observe speaker flow reactively based on the active session's owner
                launch {
                    repository.getSessionById(sessionId)
                        .flatMapLatest { session ->
                            if (session != null) {
                                repository.getSpeakerById(session.speakerOwnerId)
                            } else {
                                flowOf(null)
                            }
                        }
                        .collect { speaker ->
                            speaker?.let { currentSpeaker ->
                                binding.textSpeakerName.text = currentSpeaker.name
                                binding.textSpeakerCompany.text = currentSpeaker.company
                                binding.textSpeakerBio.text = currentSpeaker.biography
                                
                                binding.btnEditSpeaker.setOnClickListener { _ ->
                                    val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_speaker, null)
                                    val editName = dialogView.findViewById<android.widget.EditText>(R.id.edit_speaker_name)
                                    val editCompany = dialogView.findViewById<android.widget.EditText>(R.id.edit_speaker_company)
                                    val editBio = dialogView.findViewById<android.widget.EditText>(R.id.edit_speaker_bio)
                                    
                                    editName.setText(currentSpeaker.name)
                                    editCompany.setText(currentSpeaker.company)
                                    editBio.setText(currentSpeaker.biography)
                                    
                                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                        .setTitle("Edit Speaker")
                                        .setView(dialogView)
                                        .setPositiveButton("Save") { _, _ ->
                                            val updatedSpeaker = currentSpeaker.copy(
                                                name = editName.text.toString(),
                                                company = editCompany.text.toString(),
                                                biography = editBio.text.toString()
                                            )
                                            viewLifecycleOwner.lifecycleScope.launch {
                                                repository.updateSpeaker(updatedSpeaker)
                                            }
                                        }
                                        .setNegativeButton("Cancel", null)
                                        .show()
                                }
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
