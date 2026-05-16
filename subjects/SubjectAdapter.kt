package com.smartstudent.planner.ui.subjects

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartstudent.planner.data.local.entities.SubjectEntity
import com.smartstudent.planner.databinding.ItemSubjectBinding

class SubjectAdapter(
    private val onSubjectClick: (SubjectEntity) -> Unit
) : ListAdapter<SubjectEntity, SubjectAdapter.SubjectViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val binding = ItemSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class SubjectViewHolder(private val binding: ItemSubjectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(subject: SubjectEntity) {
            binding.apply {
                tvSubjectName.text = subject.name
                tvSubjectCode.text = subject.code
                tvProfessor.text = subject.professor
                tvCredits.text = "${subject.credits} ECTS"

                // Color accent
                try {
                    val color = Color.parseColor(subject.color)
                    viewColorAccent.setBackgroundColor(color)
                    tvSubjectInitial.apply {
                        text = subject.name.firstOrNull()?.uppercaseChar()?.toString() ?: "S"
                        background?.mutate()?.let { bg ->
                            bg.setTint(color)
                            background = bg
                        }
                    }
                } catch (_: Exception) {}

                root.setOnClickListener { onSubjectClick(subject) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SubjectEntity>() {
        override fun areItemsTheSame(o: SubjectEntity, n: SubjectEntity) = o.id == n.id
        override fun areContentsTheSame(o: SubjectEntity, n: SubjectEntity) = o == n
    }
}
