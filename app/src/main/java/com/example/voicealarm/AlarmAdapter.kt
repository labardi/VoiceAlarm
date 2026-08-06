package com.example.voicealarm
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class AlarmAdapter :
    ListAdapter<AlarmEntity, AlarmAdapter.AlarmViewHolder>(AlarmDiffCallback()) {

    class AlarmViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val timeText: TextView = itemView.findViewById(R.id.tvTime)
        val dateText: TextView = itemView.findViewById(R.id.tvDate)
        val messageText: TextView = itemView.findViewById(R.id.tvMessage)
    }

    override fun onCreateViewHolder( parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)

        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: AlarmViewHolder,
        position: Int
    ) {
        val alarm = getItem(position)

        holder.timeText.text =
            "%02d:%02d".format(alarm.hour, alarm.minute)

        holder.dateText.text =
            "%02d.%02d.%04d".format(
                alarm.day,
                alarm.month,
                alarm.year
            )

        holder.messageText.text = alarm.message
    }

}

private class AlarmDiffCallback :
    DiffUtil.ItemCallback<AlarmEntity>() {

    override fun areItemsTheSame(
        oldItem: AlarmEntity,
        newItem: AlarmEntity
    ): Boolean {
        return oldItem.requestCode == newItem.requestCode
    }

    override fun areContentsTheSame(
        oldItem: AlarmEntity,
        newItem: AlarmEntity
    ): Boolean {
        return oldItem == newItem
    }
}