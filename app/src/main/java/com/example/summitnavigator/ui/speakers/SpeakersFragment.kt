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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.summitnavigator.R
import com.example.summitnavigator.data.model.Speaker
import com.example.summitnavigator.databinding.FragmentSpeakersBinding
import com.example.summitnavigator.databinding.ItemSpeakerBinding
import kotlinx.coroutines.launch

class SpeakersFragment : Fragment() {

    private var _binding: FragmentSpeakersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpeakersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val app = requireActivity().application as com.example.summitnavigator.SummitApp
        val repository = app.repository

        val adapter = SpeakerAdapter { speaker ->
            val bundle = Bundle().apply {
                putString("speakerId", speaker.speakerId)
            }
            findNavController().navigate(R.id.action_speakersFragment_to_speakerDetailFragment, bundle)
        }
        binding.recyclerSpeakers.adapter = adapter
        binding.recyclerSpeakers.layoutManager = LinearLayoutManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.getAllSpeakers().collect { speakers ->
                    adapter.submitList(speakers)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class SpeakerAdapter(
    private val onItemClick: (Speaker) -> Unit
) : ListAdapter<Speaker, SpeakerAdapter.SpeakerViewHolder>(SpeakerDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpeakerViewHolder {
        val binding = ItemSpeakerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SpeakerViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: SpeakerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SpeakerViewHolder(
        private val binding: ItemSpeakerBinding,
        private val onItemClick: (Speaker) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(speaker: Speaker) {
            binding.textSpeakerName.text = speaker.name
            binding.textSpeakerCompany.text = speaker.company
            binding.root.setOnClickListener { onItemClick(speaker) }
        }
    }

    object SpeakerDiffCallback : DiffUtil.ItemCallback<Speaker>() {
        override fun areItemsTheSame(oldItem: Speaker, newItem: Speaker) = oldItem.speakerId == newItem.speakerId
        override fun areContentsTheSame(oldItem: Speaker, newItem: Speaker) = oldItem == newItem
    }
}
