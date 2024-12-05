package com.truongtq_datn.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.truongtq_datn.okhttpcrud.ApiEndpoint
import com.truongtq_datn.activity.MainActivity
import com.truongtq_datn.adapter.TicketAdapter
import com.truongtq_datn.databinding.FragmentTicketBinding
import com.truongtq_datn.dialog.TicketCreateDialog
import com.truongtq_datn.extensions.Extensions
import com.truongtq_datn.model.TicketItem
import com.truongtq_datn.model.TicketResponse
import com.truongtq_datn.okhttpcrud.GetRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TicketFragment(private val mainActivity: MainActivity) : Fragment() {
    private var _binding: FragmentTicketBinding? = null
    private val binding get() = _binding!!
    private val gson = Gson()
    private var job: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTicketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        binding.ticketMain.setLayoutManager(layoutManager)

        binding.ticketBtnRegister.setOnClickListener { showTicketRegisterForm() }

        loadTicketToAdapter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
        _binding = null
    }

    private var ticketResponse: List<TicketResponse> = emptyList()

    fun loadTicketToAdapter() {
        val getTicketsApi = ApiEndpoint.Endpoint_Ticket_GetAll

        job = lifecycleScope.launch(Dispatchers.IO) {
            val getRequest = GetRequest(getTicketsApi)
            val response = getRequest.execute(true)

            withContext(Dispatchers.Main) {
                if (response == null) {
                    Extensions.toastCall(mainActivity, "Failed to load tickets")
                    return@withContext
                }

                val responseBody = response.body!!.string()

                if (response.isSuccessful) {
                    if (responseBody.isNotEmpty()) {
                        val listType = object : TypeToken<List<TicketResponse>>() {}.type
                        ticketResponse = gson.fromJson(responseBody, listType)
                        val ticketPositionList: List<TicketItem> =
                            ticketResponse.map {
                                TicketItem(
                                    it.fullName,
                                    it.positionDoor,
                                    it.idTicket,
                                    it.idDoor,
                                    it.idAccount,
                                    it.startTime,
                                    it.endTime,
                                    it.reason,
                                    it.createdAt,
                                    it.isAccept,
                                )
                            }

                        val adapter =
                            TicketAdapter(
                                ticketPositionList,
                                mainActivity,
                                childFragmentManager,
                                this@TicketFragment
                            )
                        binding.ticketMain.adapter = adapter
                    } else {
                        Extensions.toastCall(mainActivity, "You don't have any tickets")
                    }
                } else {
                    Extensions.toastCall(
                        mainActivity,
                        Extensions.extractJson(responseBody).get("message").toString()
                    )
                }
            }
        }
    }

    private fun showTicketRegisterForm() {
        val ticketCreateDialog = TicketCreateDialog(mainActivity, this)
        ticketCreateDialog.show(childFragmentManager, "TicketCreateDialog")
    }
}