package com.truongtq_datn_manager.dialog

import com.truongtq_datn_manager.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.truongtq_datn_manager.okhttpcrud.ApiEndpoint
import com.truongtq_datn_manager.extensions.Constants
import com.truongtq_datn_manager.activity.MainActivity
import com.truongtq_datn_manager.databinding.DialogTicketCreateBinding
import com.truongtq_datn_manager.extensions.Extensions
import com.truongtq_datn_manager.extensions.Pref
import com.truongtq_datn_manager.fragment.TicketFragment
import com.truongtq_datn_manager.model.DoorResponse
import com.truongtq_datn_manager.model.Ticket
import com.truongtq_datn_manager.okhttpcrud.GetRequest
import com.truongtq_datn_manager.okhttpcrud.PostRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TicketCreateDialog(
    private val mainActivity: MainActivity,
    private val ticketFragment: TicketFragment
) : DialogFragment() {

    private var _binding: DialogTicketCreateBinding? = null
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
        _binding = DialogTicketCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ticketBtnStartDate.setOnClickListener {
            Extensions.showDateTimePickerDialog(mainActivity, binding.ticketStartDateInput)
        }

        binding.ticketBtnEndDate.setOnClickListener {
            Extensions.showDateTimePickerDialog(mainActivity, binding.ticketEndDateInput)
        }

        binding.ticketBtnSendTicket.setOnClickListener { submitTicketRequest() }

        binding.ticketBtnCancel.setOnClickListener { dismiss() }

        loadDoorsIntoSpinner()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private var doorMap: Map<String, String> = emptyMap()

    private fun loadDoorsIntoSpinner() {
        val getDoorsApi = ApiEndpoint.Endpoint_Door_GetAll

        lifecycleScope.launch(Dispatchers.IO) {
            val getRequest = GetRequest(getDoorsApi)
            val response = getRequest.execute(true)

            withContext(Dispatchers.Main) {
                if (response != null && response.isSuccessful) {
                    val responseBody = response.body?.string()

                    println("Test ticket: $responseBody")

                    if (!responseBody.isNullOrEmpty()) {
                        val listType = object : TypeToken<List<DoorResponse>>() {}.type
                        val doorList: List<DoorResponse> = gson.fromJson(responseBody, listType)
                        val doorPositionList: List<String> = doorList.map { it.position }
                        doorMap = doorList.associate { it.idDoor to it.position }


                        val adapter =
                            ArrayAdapter(
                                mainActivity,
                                R.layout.spinner_item,
                                doorPositionList
                            )

                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                        binding.ticketDoorSpinner.adapter = adapter
                    } else {
                        Extensions.toastCall(mainActivity, "Failed to load doors")
                    }
                } else {
                    Extensions.toastCall(mainActivity, "Failed to load doors")
                }
            }
        }
    }

    private fun submitTicketRequest() {
        val startTime = binding.ticketStartDateInput.text.toString()
        val endTime = binding.ticketEndDateInput.text.toString()
        val reason = binding.ticketReasonInput.text.toString()
        val selectedDoor = binding.ticketDoorSpinner.selectedItem.toString()

        val startDate = Extensions.convertStringToIsoString(startTime)
        val endDate = Extensions.convertStringToIsoString(endTime)

        if (startTime.isEmpty() || endTime.isEmpty() || reason.isEmpty() || selectedDoor.isEmpty()) {
            Extensions.toastCall(mainActivity, "All fields are required")
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val idAccount = Pref.getString(mainActivity, Constants.ID_ACCOUNT)
            val idDoor = getIdDoor(selectedDoor)!!.toString()

            val ticketForm = Ticket(idAccount, idDoor, startDate!!, endDate!!, reason)
            val requestBody = gson.toJson(ticketForm)
            val createTicketApi = ApiEndpoint.Endpoint_Ticket_Create

            val postRequest = PostRequest(createTicketApi, requestBody)
            val response = postRequest.execute(true)

            withContext(Dispatchers.Main) {
                if (response != null && response.isSuccessful) {
                    Extensions.toastCall(mainActivity, "Send ticket success")
                    ticketFragment.loadTicketToAdapter()
                    clearTicketForm()
                    dismiss()
                } else {
                    Extensions.toastCall(mainActivity, "Failed to send ticket")
                }
            }
        }
    }

    private fun clearTicketForm() {
        binding.ticketStartDateInput.text?.clear()
        binding.ticketEndDateInput.text?.clear()
        binding.ticketReasonInput.text?.clear()
        binding.ticketDoorSpinner.setSelection(0)
    }

    private fun getIdDoor(positionDoor: String): String? {
        return doorMap.entries.find { it.value == positionDoor }?.key
    }
}