package com.example.physmath.ui.lessons

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.physmath.data.repository.LessonRepository
import com.example.physmath.databinding.FragmentLessonsBinding

class LessonsFragment : Fragment() {
    
    private var _binding: FragmentLessonsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: LessonsViewModel
    private lateinit var adapter: LessonAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val repository = LessonRepository(requireContext())
        viewModel = ViewModelProvider(this, LessonsViewModelFactory(repository))[LessonsViewModel::class.java]
        
        setupRecyclerView()
        observeData()
        
        // Load lessons from network on first launch
        viewModel.fetchLessonsFromNetwork(requireContext())
        viewModel.loadDownloadedLessons()
    }
    
    private fun setupRecyclerView() {
        adapter = LessonAdapter(
            onItemClick = { lesson ->
                val action = LessonsFragmentDirections.actionToLessonDetail(lesson.id)
                findNavController().navigate(action)
            },
            onDownloadClick = { lesson ->
                viewModel.downloadLesson(requireContext(), lesson)
                Toast.makeText(requireContext(), "Downloading lesson...", Toast.LENGTH_SHORT).show()
            }
        )
        
        binding.lessonsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@LessonsFragment.adapter
        }
    }
    
    private fun observeData() {
        viewModel.lessons.observe(viewLifecycleOwner) { lessons ->
            adapter.submitList(lessons)
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.error.value = null
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
