package com.example.physmath.ui.lessons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.physmath.data.repository.LessonRepository
import com.example.physmath.databinding.FragmentLessonDetailBinding
import io.noties.markwon.Markwon

class LessonDetailFragment : Fragment() {
    
    private var _binding: FragmentLessonDetailBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: LessonDetailViewModel
    private val args: LessonDetailFragmentArgs by navArgs()
    private lateinit var markwon: Markwon
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        markwon = Markwon.create(requireContext())
        
        val repository = LessonRepository(requireContext())
        viewModel = ViewModelProvider(
            this, 
            LessonDetailViewModelFactory(repository, args.lessonId)
        )[LessonDetailViewModel::class.java]
        
        observeData()
        
        viewModel.loadLesson()
        viewModel.loadQuestions(requireContext())
        
        binding.startTestButton.setOnClickListener {
            val action = LessonDetailFragmentDirections.actionToTest(args.lessonId)
            findNavController().navigate(action)
        }
    }
    
    private fun observeData() {
        viewModel.lesson.observe(viewLifecycleOwner) { lesson ->
            lesson?.let {
                binding.lessonTitle.text = it.title
                markwon.setMarkdown(binding.lessonContent, it.content)
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
