package com.truongtq_datn.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.truongtq_datn.activity.MainActivity
import com.truongtq_datn.databinding.TicketItemLayoutBinding
import com.truongtq_datn.dialog.TicketDetailDialog
import com.truongtq_datn.fragment.TicketFragment
import com.truongtq_datn.model.TicketItem

class TicketAdapter(
    private val itemList: List<TicketItem>,
    private val mainActivity: MainActivity,
    private val fragmentManager: androidx.fragment.app.FragmentManager,
    private val ticketFragment: TicketFragment
) : RecyclerView.Adapter<TicketAdapter.ItemViewHolder>() {

    class ItemViewHolder(val binding: TicketItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            TicketItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.binding.ticketTitle.text = item.fullName
        "Create at: ${item.createdAt}".also { holder.binding.ticketDescription.text = it }
        holder.binding.ticketBtnViewTicket.setOnClickListener { showTicketInfo(item) }
    }

    override fun getItemCount() = itemList.size

    private fun showTicketInfo(item: TicketItem) {
        val dialog = TicketDetailDialog(item, mainActivity, ticketFragment)
        dialog.show(fragmentManager, "TicketDetailDialog")
    }
}