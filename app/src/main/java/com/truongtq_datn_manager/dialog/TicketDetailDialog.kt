package com.truongtq_datn_manager.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.truongtq_datn_manager.okhttpcrud.ApiEndpoint
import com.truongtq_datn_manager.activity.MainActivity
import com.truongtq_datn_manager.databinding.DialogTicketDetailBinding
import com.truongtq_datn_manager.extensions.Extensions
import com.truongtq_datn_manager.fragment.TicketFragment
import com.truongtq_datn_manager.model.TicketItem
import com.truongtq_datn_manager.okhttpcrud.PatchRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TicketDetailDialog(
    private val item: TicketItem,
    private val mainActivity: MainActivity,
    private val ticketFragment: TicketFragment
) : DialogFragment() {
    private var _binding: DialogTicketDetailBinding? = null
    private val binding get() = _binding!!
    private var gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTicketDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ticketDetailIdTicket.setText(item.idTicket)
        binding.ticketDetailPositionDoor.setText(item.positionDoor)
        binding.ticketDetailFullName.setText(item.fullName)
        binding.ticketDetailStartTime.setText(item.startTime)
        binding.ticketDetailEndTime.setText(item.endTime)
        binding.ticketDetailReason.setText(item.reason)
        binding.ticketDetailCreatedAt.setText(item.createdAt)
        binding.ticketDetailStatus.setText(if (item.isAccept) "Accepted" else "Pending")

        if (item.isAccept) {
            binding.ticketDetailBtnAccept.visibility = View.GONE
        }

        binding.ticketDetailBtnAccept.setOnClickListener { acceptTicket() }

        binding.ticketDetailBtnCancel.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun acceptTicket() {
        val dialog = MaterialAlertDialogBuilder(mainActivity)
            .setTitle("Confirm")
            .setMessage("Are you sure to accept this ticket?")
            .setPositiveButton("Yes") { _, _ ->
                sendRequest()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setOnDismissListener { dialogInterface ->
                dialogInterface.dismiss()
            }

        dialog.show()
    }

    private fun sendRequest() {
        lifecycleScope.launch(Dispatchers.IO) {

            val idTicket = item.idTicket
            val api = "${ApiEndpoint.Endpoint_Ticket}/$idTicket"

            val body = gson.toJson(mapOf("isAccept" to true))

            val patchRequest = PatchRequest(api, body)
            val response = patchRequest.execute(true)

            withContext(Dispatchers.Main) {
                if (response != null && response.isSuccessful) {
                    ticketFragment.loadTicketToAdapter()
                    Extensions.toastCall(mainActivity, "Accept ticket successfully")
                    dismiss()
                } else {
                    Extensions.toastCall(mainActivity, "Failed to accept ticket")
                }
            }
        }
    }
}