package com.truongtq_datn.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.truongtq_datn.activity.MainActivity
import com.truongtq_datn.databinding.AccountItemLayoutBinding
import com.truongtq_datn.dialog.DoorAddAccountAccessDialog
import com.truongtq_datn.model.AccountItem

class AccountAdapter(
    private var itemList: List<AccountItem>
) :
    RecyclerView.Adapter<AccountAdapter.ItemViewHolder>() {

    class ItemViewHolder(val binding: AccountItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            AccountItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.binding.accountName.text = item.name
        "ID: ${item.id}, RefID: ${item.refId}".also { holder.binding.accountRefId.text = it }
        holder.binding.accountCheckbox.isChecked = item.isChecked

        holder.binding.accountCheckbox.setOnCheckedChangeListener { _, isChecked ->
            item.isChecked = isChecked
        }
    }

    override fun getItemCount() = itemList.size

    fun updateList(filteredItems: List<AccountItem>) {
        itemList = filteredItems
        notifyDataSetChanged()
    }

    fun getTickAccount(): List<AccountItem> {
        return itemList.filter { it.isChecked }
    }
}