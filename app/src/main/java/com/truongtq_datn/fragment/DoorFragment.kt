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
import com.truongtq_datn.adapter.DoorAdapter
import com.truongtq_datn.databinding.FragmentDoorBinding
import com.truongtq_datn.dialog.DoorConfigDialog
import com.truongtq_datn.dialog.DoorCreateDialog
import com.truongtq_datn.extensions.Extensions
import com.truongtq_datn.model.DoorItem
import com.truongtq_datn.model.DoorResponse
import com.truongtq_datn.okhttpcrud.GetRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DoorFragment(private val mainActivity: MainActivity) : Fragment() {
    private var _binding: FragmentDoorBinding? = null
    private val binding get() = _binding!!
    private val gson = Gson()
    private var job: Job? = null

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

        binding.doorBtnConfig.setOnClickListener {
            showDoorConfigFragment()
        }

        binding.doorBtnRegister.setOnClickListener {
            showDoorRegisterForm()
        }

        loadDoorToAdapter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.doorMain.layoutManager = null
        job?.cancel()
        _binding = null
    }

    private var doorResponse: List<DoorResponse> = emptyList()

    fun loadDoorToAdapter() {
        val getDoorsApi = ApiEndpoint.Endpoint_Door_GetAll

        job = lifecycleScope.launch(Dispatchers.IO) {
            val getRequest = GetRequest(getDoorsApi)
            val response = getRequest.execute(true)

            withContext(Dispatchers.Main) {
                if (response == null) {
                    Extensions.toastCall(mainActivity, "Failed to load doors!")
                    return@withContext
                }

                val responseBody = response.body!!.string()


                if (response.isSuccessful) {
                    if (responseBody.isNotEmpty()) {
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
                        Extensions.toastCall(mainActivity, "Can't found any door!")
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

    private fun showDoorRegisterForm() {
        val doorCreateDialog = DoorCreateDialog(mainActivity, this)
        doorCreateDialog.show(childFragmentManager, "DoorCreateDialog")
    }

    private fun showDoorConfigFragment(){
        val doorConfigDialog = DoorConfigDialog(mainActivity)
        doorConfigDialog.show(childFragmentManager, "DoorConfigDialog")

    }
}