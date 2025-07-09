package com.theweek.plan.ui.settings

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.theweek.plan.databinding.ItemStreakLevelBinding
import com.theweek.plan.ui.statistics.StreakLevel
import java.util.*

class StreakLevelAdapter(private val streakLevels: List<StreakLevel>) : 
    RecyclerView.Adapter<StreakLevelAdapter.StreakLevelViewHolder>() {

    class StreakLevelViewHolder(val binding: ItemStreakLevelBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StreakLevelViewHolder {
        val binding = ItemStreakLevelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StreakLevelViewHolder(binding)
    }

    override fun getItemCount(): Int = streakLevels.size

    override fun onBindViewHolder(holder: StreakLevelViewHolder, position: Int) {
        val level = streakLevels[position]
        val binding = holder.binding

        // Set level name and icon
        binding.textLevelIcon.text = level.icon
        binding.textLevelName.text = level.name
        
        // Set level days range
        binding.textLevelDays.text = if (level.level == 12) {
            "${level.minDays}+ days"
        } else {
            "${level.minDays}-${level.maxDays} days"
        }
        
        // Set phase and description
        binding.textLevelPhase.text = "Phase: ${level.phase.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}"
        binding.textLevelDescription.text = level.description
        
        // Set the background color based on the streak level color
        val gradientColors = getGradientColors(level.color)
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            gradientColors
        )
        gradient.cornerRadius = 16f
        binding.levelBackground.background = gradient
    }
    
    private fun getGradientColors(colorName: String): IntArray {
        return when (colorName) {
            "red" -> intArrayOf(Color.parseColor("#F44336"), Color.parseColor("#D32F2F"))
            "orange" -> intArrayOf(Color.parseColor("#FF9800"), Color.parseColor("#F57C00"))
            "yellow" -> intArrayOf(Color.parseColor("#FFEB3B"), Color.parseColor("#FBC02D"))
            "green" -> intArrayOf(Color.parseColor("#4CAF50"), Color.parseColor("#388E3C"))
            "cyan" -> intArrayOf(Color.parseColor("#00BCD4"), Color.parseColor("#0097A7"))
            "blue" -> intArrayOf(Color.parseColor("#2196F3"), Color.parseColor("#1976D2"))
            "indigo" -> intArrayOf(Color.parseColor("#3F51B5"), Color.parseColor("#303F9F"))
            "purple" -> intArrayOf(Color.parseColor("#9C27B0"), Color.parseColor("#7B1FA2"))
            "pink" -> intArrayOf(Color.parseColor("#E91E63"), Color.parseColor("#C2185B"))
            "white" -> intArrayOf(Color.parseColor("#EEEEEE"), Color.parseColor("#BDBDBD"))
            "purple-pink-red" -> intArrayOf(Color.parseColor("#9C27B0"), Color.parseColor("#E91E63"))
            else -> intArrayOf(Color.parseColor("#2196F3"), Color.parseColor("#1976D2"))
        }
    }
}
