package com.smartstudent.planner.ui.exams

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartstudent.planner.R
import com.smartstudent.planner.data.local.entities.ExamEntity
import com.smartstudent.planner.databinding.ItemExamBinding
import java.text.SimpleDateFormat
import java.util.*

class ExamAdapter(
    private val onExamClick: (ExamEntity) -> Unit
) : ListAdapter<ExamEntity, ExamAdapter.ExamViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
        val binding = ItemExamBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExamViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExamViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ExamViewHolder(private val binding: ItemExamBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(exam: ExamEntity) {
            binding.apply {
                tvExamTitle.text = exam.title
                tvSubjectName.text = exam.subjectName
                tvExamType.text = exam.examType
                tvLocation.text = exam.location.ifEmpty { "—" }

                val sdf = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
                tvExamDate.text = sdf.format(Date(exam.examDate))
                tvExamTime.text = exam.examTime.ifEmpty { "—" }

                // Countdown badge
                val days = exam.daysUntil()
                val ctx = root.context
                tvCountdown.text = when {
                    !exam.isUpcoming() -> ctx.getString(R.string.exam_passed)
                    days == 0L -> ctx.getString(R.string.exam_today)
                    days == 1L -> ctx.getString(R.string.exam_tomorrow)
                    else -> ctx.getString(R.string.exam_in_days, days.toInt())
                }

                val countdownColor = when {
                    !exam.isUpcoming() -> R.color.on_surface_variant
                    days <= 1L -> R.color.error
                    days <= 3L -> R.color.warning
                    else -> R.color.success
                }
                tvCountdown.setTextColor(ContextCompat.getColor(ctx, countdownColor))

                // Subject color stripe
                try {
                    viewColorStripe.setBackgroundColor(Color.parseColor(exam.subjectColor))
                } catch (_: Exception) {}

                root.setOnClickListener { onExamClick(exam) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ExamEntity>() {
        override fun areItemsTheSame(o: ExamEntity, n: ExamEntity) = o.id == n.id
        override fun areContentsTheSame(o: ExamEntity, n: ExamEntity) = o == n
    }
}
