package com.truongtq_datn.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.truongtq_datn.okhttpcrud.ApiEndpoint
import com.truongtq_datn.extensions.Constants
import com.truongtq_datn.activity.MainActivity
import com.truongtq_datn.databinding.DialogDoorDetailsBinding
import com.truongtq_datn.extensions.Extensions
import com.truongtq_datn.extensions.Pref
import com.truongtq_datn.fragment.DoorFragment
import com.truongtq_datn.model.DoorItem
import com.truongtq_datn.model.EditDoorForm
import com.truongtq_datn.model.ResponseMessage
import com.truongtq_datn.model.Ticket
import com.truongtq_datn.okhttpcrud.DeleteRequest
import com.truongtq_datn.okhttpcrud.GetRequest
import com.truongtq_datn.okhttpcrud.PatchRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DoorDetailDialog(
    private val doorItem: DoorItem,
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

        binding.doorDetailIdDoor.setText(doorItem.idDoor)
        binding.doorDetailIdAccountCreate.setText(doorItem.idAccountCreate)
        binding.doorDetailPosition.setText(doorItem.position)
        binding.doorDetailCreatedAt.setText(doorItem.createdAt)
        binding.doorDetailLastUpdate.setText(doorItem.lastUpdate)

        binding.doorDetailBtnCancel.setOnClickListener { dismiss() }
        binding.doorDetailBtnEdit.setOnClickListener { editDoor() }
        binding.doorDetailBtnDelete.setOnClickListener { deleteDoor() }
        binding.doorDetailBtnEditAccept.setOnClickListener { acceptEditDoor() }
        binding.doorDetailBtnEditCancel.setOnClickListener { cancelEditDoor() }
        binding.doorDetailBtnAddAccountAccess.setOnClickListener { addAccountAccessDoor() }
        binding.doorDetailBtnRemoveAccountAccess.setOnClickListener { removeAccountAccessDoor() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addAccountAccessDoor() {
        val doorAddAccountAccessDialog = DoorAddAccountAccessDialog(mainActivity, doorItem.idDoor)
        doorAddAccountAccessDialog.show(childFragmentManager, "DoorCreateDialog")
    }

    private fun removeAccountAccessDoor() {
        val doorRemoveAccountAccessDialog = DoorRemoveAccountAccessDialog(mainActivity, doorItem.idDoor)
        doorRemoveAccountAccessDialog.show(childFragmentManager, "DoorCreateDialog")
    }

    private fun editDoor() {
        binding.doorDetailBtnEdit.visibility = View.GONE
        binding.doorDetailBtnDelete.visibility = View.GONE
        binding.doorDetailBtnCancel.visibility = View.GONE
        binding.doorDetailBtnAddAccountAccess.visibility = View.GONE

        binding.doorDetailBtnEditAccept.visibility = View.VISIBLE
        binding.doorDetailBtnEditCancel.visibility = View.VISIBLE

        binding.doorDetailPosition.isEnabled = true
    }

    private fun acceptEditDoor() {
        lifecycleScope.launch(Dispatchers.IO) {
            val editDoorApi = "${ApiEndpoint.Endpoint_Door}/${doorItem.idDoor}"

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
        binding.doorDetailBtnAddAccountAccess.visibility = View.VISIBLE

        binding.doorDetailBtnEditAccept.visibility = View.GONE
        binding.doorDetailBtnEditCancel.visibility = View.GONE

        binding.doorDetailPosition.isEnabled = false

        binding.doorDetailPosition.setText(doorItem.position)
    }

    private fun deleteDoor() {
        fetchTicketsRefDoor(doorItem.idDoor) { totalTicketRefDoor ->
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
            val editDoorApi = "${ApiEndpoint.Endpoint_Door}/${doorItem.idDoor}"


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