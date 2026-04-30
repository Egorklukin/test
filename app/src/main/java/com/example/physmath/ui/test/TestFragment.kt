package com.example.physmath.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.physmath.R
import com.example.physmath.data.repository.LessonRepository
import com.example.physmath.databinding.FragmentTestBinding

class TestFragment : Fragment() {
    
    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: TestViewModel
    private val args: TestFragmentArgs by navArgs()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val repository = LessonRepository(requireContext())
        viewModel = ViewModelProvider(
            this,
            TestViewModelFactory(repository, args.lessonId)
        )[TestViewModel::class.java]
        
        observeData()
        viewModel.loadQuestions()
        
        binding.submitButton.setOnClickListener {
            viewModel.submitAnswer()
        }
    }
    
    private fun observeData() {
        viewModel.questions.observe(viewLifecycleOwner) { questions ->
            if (questions.isNotEmpty()) {
                updateUI()
            }
        }
        
        viewModel.currentQuestionIndex.observe(viewLifecycleOwner) { index ->
            updateQuestionUI()
        }
        
        viewModel.selectedAnswer.observe(viewLifecycleOwner) { answer ->
            // Update radio button selection
        }
        
        viewModel.showExplanation.observe(viewLifecycleOwner) { show ->
            if (show) {
                displayExplanation()
            }
        }
        
        viewModel.testResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                showResults(it)
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
    
    private fun updateUI() {
        val questions = viewModel.questions.value ?: return
        val currentIndex = viewModel.currentQuestionIndex.value ?: 0
        
        if (currentIndex < questions.size) {
            val question = questions[currentIndex]
            
            binding.questionNumber.text = "Question ${currentIndex + 1} of ${questions.size}"
            binding.questionText.text = question.questionText
            
            binding.optionsRadioGroup.removeAllViews()
            question.options.forEachIndexed { index, option ->
                val radioButton = RadioButton(requireContext()).apply {
                    text = option
                    setTextColor(resources.getColor(R.color.text_primary, null))
                    setOnClickListener {
                        viewModel.selectAnswer(index)
                    }
                }
                binding.optionsRadioGroup.addView(radioButton)
            }
        }
    }
    
    private fun updateQuestionUI() {
        updateUI()
        binding.explanationText.visibility = View.GONE
        binding.submitButton.text = getString(R.string.submit_answer)
    }
    
    private fun displayExplanation() {
        val questions = viewModel.questions.value ?: return
        val currentIndex = viewModel.currentQuestionIndex.value ?: return
        val question = questions[currentIndex]
        
        binding.explanationText.apply {
            visibility = View.VISIBLE
            text = "${getString(R.string.wrong_answer)}\n\n${getString(R.string.explanation)}: ${question.explanation}"
            setTextColor(resources.getColor(R.color.error, null))
        }
        
        binding.submitButton.text = getString(R.string.next_question)
    }
    
    private fun showResults(result: com.example.physmath.data.model.TestResult) {
        val percentage = (result.correctAnswers.toFloat() / result.totalQuestions) * 100
        
        Toast.makeText(
            requireContext(),
            "Test completed! Score: ${result.correctAnswers}/${result.totalQuestions} ($percentage%)",
            Toast.LENGTH_LONG
        ).show()
        
        findNavController().popBackStack()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
