package com.truongtq_datn_manager.fragment

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
import com.truongtq_datn_manager.okhttpcrud.ApiEndpoint
import com.truongtq_datn_manager.activity.MainActivity
import com.truongtq_datn_manager.adapter.DoorAdapter
import com.truongtq_datn_manager.databinding.FragmentDoorBinding
import com.truongtq_datn_manager.dialog.DoorCreateDialog
import com.truongtq_datn_manager.extensions.Extensions
import com.truongtq_datn_manager.model.DoorItem
import com.truongtq_datn_manager.model.DoorResponse
import com.truongtq_datn_manager.okhttpcrud.GetRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DoorFragment(private val mainActivity: MainActivity) : Fragment() {
    private var _binding: FragmentDoorBinding? = null
    private val binding get() = _binding!!
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        binding.doorMain.setLayoutManager(layoutManager)

        binding.doorBtnRegister.setOnClickListener { showDoorRegisterForm() }

        loadDoorToAdapter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private var doorResponse: List<DoorResponse> = emptyList()

    fun loadDoorToAdapter() {
        val getDoorsApi = ApiEndpoint.Endpoint_Door_GetAll

        lifecycleScope.launch(Dispatchers.IO) {
            val getRequest = GetRequest(getDoorsApi)
            val response = getRequest.execute(true)

            withContext(Dispatchers.Main) {
                if (response != null && response.isSuccessful) {
                    val responseBody = response.body?.string()

                    if (!responseBody.isNullOrEmpty()) {
                        val listType = object : TypeToken<List<DoorResponse>>() {}.type
                        doorResponse = gson.fromJson(responseBody, listType)
                        val doorPositionList: List<DoorItem> =
                            doorResponse.map {
                                DoorItem(
                                    it.idDoor,
                                    it.idAccountCreate,
                                    it.position,
                                    it.createdAt,
                                    it.lastUpdate,
                                )
                            }

                        val adapter =
                            DoorAdapter(
                                doorPositionList,
                                mainActivity,
                                childFragmentManager,
                                this@DoorFragment
                            )
                        binding.doorMain.adapter = adapter
                    } else {
                        Extensions.toastCall(mainActivity, "Failed to load doors")
                    }
                } else {
                    Extensions.toastCall(mainActivity, "Failed to load doors")
                }
            }
        }
    }

    private fun showDoorRegisterForm() {
        val doorCreateDialog = DoorCreateDialog(mainActivity, this)
        doorCreateDialog.show(childFragmentManager, "DoorCreateDialog")
    }
}