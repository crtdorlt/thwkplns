package com.theweek.plan.ui.timer

import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
// Task spinner functionality removed
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.theweek.plan.R
import com.theweek.plan.databinding.FragmentTimerBinding
// Task model import removed
import com.theweek.plan.ui.tasks.TaskViewModel

class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var taskViewModel: TaskViewModel
    private var timer: CountDownTimer? = null
    private var isRunning = false
    private var remainingTimeInSeconds = 25 * 60 // 25 minutes in seconds
    // Task selection removed as requested

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]
        
        // Initialize timer display
        updateTimerDisplay()
        
        // Task spinner removed as requested
        
        // Setup timer controls
        setupTimerControls()
        
        // Setup timer presets
        setupTimerPresets()
    }
    // Task spinner setup method removed as requested
    
    private fun setupTimerControls() {
        binding.buttonStartPause.setOnClickListener {
            if (isRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }
        
        binding.buttonReset.setOnClickListener {
            resetTimer()
        }
    }
    
    private fun setupTimerPresets() {
        binding.buttonPomodoro.setOnClickListener {
            resetTimer(25 * 60) // 25 minutes
        }
        
        binding.buttonShortBreak.setOnClickListener {
            resetTimer(5 * 60) // 5 minutes
        }
        
        binding.buttonLongBreak.setOnClickListener {
            resetTimer(15 * 60) // 15 minutes
        }
    }
    
    private fun startTimer() {
        isRunning = true
        binding.buttonStartPause.text = getString(R.string.pause)
        
        timer = object : CountDownTimer(remainingTimeInSeconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeInSeconds = (millisUntilFinished / 1000).toInt()
                updateTimerDisplay()
            }
            
            override fun onFinish() {
                isRunning = false
                binding.buttonStartPause.text = getString(R.string.start)
                remainingTimeInSeconds = 0
                updateTimerDisplay()
                showCompletionDialog()
            }
        }.start()
    }
    
    private fun pauseTimer() {
        isRunning = false
        binding.buttonStartPause.text = getString(R.string.start)
        timer?.cancel()
    }
    
    private fun resetTimer(timeInSeconds: Int = 25 * 60) {
        pauseTimer()
        remainingTimeInSeconds = timeInSeconds
        updateTimerDisplay()
    }
    
    private fun updateTimerDisplay() {
        val minutes = remainingTimeInSeconds / 60
        val seconds = remainingTimeInSeconds % 60
        binding.textTimer.text = String.format("%02d:%02d", minutes, seconds)
        
        // Update progress circle
        val progress = if (remainingTimeInSeconds > 0) {
            (remainingTimeInSeconds.toFloat() / (25 * 60)) * 100
        } else {
            0f
        }
        binding.progressCircle.progress = progress.toInt()
    }
    
    private fun showCompletionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.timer_completed)
            .setMessage(getString(R.string.timer_session_finished))
            .setPositiveButton(R.string.ok, null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
    }
}
