package com.truongtq_datn_manager.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.truongtq_datn_manager.okhttpcrud.ApiEndpoint
import com.truongtq_datn_manager.extensions.Constants
import com.truongtq_datn_manager.activity.MainActivity
import com.truongtq_datn_manager.databinding.DialogDoorDetailsBinding
import com.truongtq_datn_manager.extensions.Extensions
import com.truongtq_datn_manager.extensions.Pref
import com.truongtq_datn_manager.fragment.DoorFragment
import com.truongtq_datn_manager.model.DoorItem
import com.truongtq_datn_manager.model.EditDoorForm
import com.truongtq_datn_manager.model.ResponseMessage
import com.truongtq_datn_manager.model.Ticket
import com.truongtq_datn_manager.okhttpcrud.DeleteRequest
import com.truongtq_datn_manager.okhttpcrud.GetRequest
import com.truongtq_datn_manager.okhttpcrud.PatchRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DoorDetailDialog(
    private val item: DoorItem,
    private val mainActivity: MainActivity,
    private val doorFragment: DoorFragment
) : DialogFragment() {

    private var _binding: DialogDoorDetailsBinding? = null
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
        _binding = DialogDoorDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.doorDetailIdDoor.setText(item.idDoor)
        binding.doorDetailIdAccountCreate.setText(item.idAccountCreate)
        binding.doorDetailPosition.setText(item.position)
        binding.doorDetailCreatedAt.setText(item.createdAt)
        binding.doorDetailLastUpdate.setText(item.lastUpdate)

        binding.doorDetailBtnCancel.setOnClickListener { dismiss() }
        binding.doorDetailBtnEdit.setOnClickListener { editDoor() }
        binding.doorDetailBtnDelete.setOnClickListener { deleteDoor() }
        binding.doorDetailBtnEditAccept.setOnClickListener { acceptEditDoor() }
        binding.doorDetailBtnEditCancel.setOnClickListener { cancelEditDoor() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun editDoor() {
        binding.doorDetailBtnEdit.visibility = View.GONE
        binding.doorDetailBtnDelete.visibility = View.GONE
        binding.doorDetailBtnCancel.visibility = View.GONE

        binding.doorDetailBtnEditAccept.visibility = View.VISIBLE
        binding.doorDetailBtnEditCancel.visibility = View.VISIBLE

        binding.doorDetailPosition.isEnabled = true
    }

    private fun acceptEditDoor() {
        lifecycleScope.launch(Dispatchers.IO) {
            val editDoorApi = "${ApiEndpoint.Endpoint_Door}/${item.idDoor}"

            val idAccount = Pref.getString(mainActivity, Constants.ID_ACCOUNT)
            val newPosition = binding.doorDetailPosition.text.toString()
            val editDoorForm = EditDoorForm(idAccount, newPosition)

            val requestBody = gson.toJson(editDoorForm)
            val patchRequest = PatchRequest(editDoorApi, requestBody)
            val response = patchRequest.execute(true)

            withContext(Dispatchers.Main) {
                if (response != null && response.isSuccessful) {
                    val responseBody = response.body?.string()

                    val responseMessage = responseBody?.let {
                        gson.fromJson(
                            responseBody,
                            ResponseMessage::class.java
                        )
                    }

                    Extensions.toastCall(mainActivity, responseMessage?.message!!)
                    doorFragment.loadDoorToAdapter()
                    dismiss()
                } else {
                    Extensions.toastCall(mainActivity, "Failed to edit door")
                }
            }
        }
    }

    private fun cancelEditDoor() {
        binding.doorDetailBtnEdit.visibility = View.VISIBLE
        binding.doorDetailBtnDelete.visibility = View.VISIBLE
        binding.doorDetailBtnCancel.visibility = View.VISIBLE

        binding.doorDetailBtnEditAccept.visibility = View.GONE
        binding.doorDetailBtnEditCancel.visibility = View.GONE

        binding.doorDetailPosition.isEnabled = false

        binding.doorDetailPosition.setText(item.position)
    }

    private fun deleteDoor() {
        fetchTicketsRefDoor(item.idDoor) { totalTicketRefDoor ->
            val dialog = MaterialAlertDialogBuilder(mainActivity)
                .setTitle("Confirm")
                .setMessage("Are you sure to delete this door?\nHas $totalTicketRefDoor ticket(s) request to this door")
                .setPositiveButton("Yes") { _, _ ->
                    acceptDeleteDoor()
                }
                .setNegativeButton("No") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .setOnDismissListener { dialogInterface ->
                    dialogInterface.dismiss()
                }

            dialog.show()
        }
    }

    private fun fetchTicketsRefDoor(idDoor: String, callback: (Int) -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            val getTicketApi = "${ApiEndpoint.Endpoint_Ticket}/idDoor/${idDoor}"

            val getRequest = GetRequest(getTicketApi)
            val response = getRequest.execute(true)

            withContext(Dispatchers.Main) {
                if (response != null && response.isSuccessful) {
                    val listType = object : TypeToken<List<Ticket>>() {}.type
                    val responseBody = response.body?.string()
                    val tickets: List<Ticket> = gson.fromJson(responseBody, listType)
                    callback(tickets.count())
                } else {
                    callback(0)
                }
            }
        }
    }

    private fun acceptDeleteDoor() {
        lifecycleScope.launch(Dispatchers.IO) {
            val editDoorApi = "${ApiEndpoint.Endpoint_Door}/${item.idDoor}"


            val idAccount = Pref.getString(mainActivity, Constants.ID_ACCOUNT)
            val body = gson.toJson(
                mapOf("idAccountDelete" to idAccount)
            )

            println("Test door: $body")

            val deleteRequest = DeleteRequest(editDoorApi, body)
            val response = deleteRequest.execute(true)

            withContext(Dispatchers.Main) {
                if (response != null && response.isSuccessful) {
                    val responseBody = response.body?.string()

                    val responseMessage = responseBody?.let {
                        gson.fromJson(
                            responseBody,
                            ResponseMessage::class.java
                        )
                    }

                    Extensions.toastCall(mainActivity, responseMessage?.message!!)
                    doorFragment.loadDoorToAdapter()
                    dismiss()
                } else {
                    Extensions.toastCall(mainActivity, "Failed to delete door")
                }
            }
        }
    }
}