package com.truongtq_datn.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.truongtq_datn.activity.MainActivity
import com.truongtq_datn.adapter.AccountAdapter
import com.truongtq_datn.databinding.DialogDoorAddAccountBinding
import com.truongtq_datn.extensions.Extensions
import com.truongtq_datn.model.AccountItem
import com.truongtq_datn.model.AccountResponse
import com.truongtq_datn.okhttpcrud.ApiEndpoint
import com.truongtq_datn.okhttpcrud.GetRequest
import com.truongtq_datn.okhttpcrud.PostRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DoorAddAccountAccessDialog(
    private val mainActivity: MainActivity,
    private val idDoor: String
) : DialogFragment() {

    private var _binding: DialogDoorAddAccountBinding? = null
    private val binding get() = _binding!!
    private var gson = Gson()
    private var job: Job? = null

    private lateinit var adapter: AccountAdapter
    private lateinit var accountList: List<AccountItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDoorAddAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        binding.accountView.setLayoutManager(layoutManager)

        binding.doorBtnAdd.setOnClickListener { addAccountCanAccess() }
        binding.doorBtnCancel.setOnClickListener { dismiss() }

        loadAccountToAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchBar.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(searchText: String?): Boolean {
                val filteredList = accountList.filter {
                    it.name.contains(searchText!!, true) ||
                            it.id.contains(searchText, true) ||
                            it.refId.contains(searchText, true)
                }
                adapter.updateList(filteredList)
                return true
            }
        })

        binding.doorBtnCancel.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
        _binding = null
    }

    private var accountResponse: List<AccountResponse> = emptyList()

    private fun loadAccountToAdapter() {
        val getAccountApi = ApiEndpoint.Endpoint_Account_GetAll

        job = lifecycleScope.launch(Dispatchers.IO) {
            val getRequest = GetRequest(getAccountApi)
            val response = getRequest.execute(true)

            withContext(Dispatchers.Main) {
                if (response == null) {
                    Extensions.toastCall(mainActivity, "Failed to load accounts!")
                    return@withContext
                }

                val responseBody = response.body!!.string()


                if (response.isSuccessful) {
                    if (responseBody.isNotEmpty()) {
                        val listType = object : TypeToken<List<AccountResponse>>() {}.type
                        accountResponse = gson.fromJson(responseBody, listType)
                        accountList =
                            accountResponse.map {
                                AccountItem(
                                    "${it.firstName} ${it.lastName}",
                                    it.idAccount,
                                    it.refId,
                                    it.email,
                                    it.phoneNumber,
                                    false
                                )
                            }

                        adapter = AccountAdapter(accountList)
                        binding.accountView.adapter = adapter
                    } else {
                        Extensions.toastCall(mainActivity, "Can't found any account!")
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

    private fun addAccountCanAccess() {
        if (accountList.isEmpty()) return

        lifecycleScope.launch(Dispatchers.IO) {
            val accountIds = adapter.getTickAccount().map { it.id }

            val postAddAccountAccessDoorApi = ApiEndpoint.Endpoint_Door_AddAccountAccessDoor
            val requestBody = gson.toJson(mapOf("idDoor" to idDoor, "accounts" to accountIds))
            val postRequest = PostRequest(postAddAccountAccessDoorApi, requestBody)
            val response = postRequest.execute(true)

            withContext(Dispatchers.Main) {
                if (response != null && response.isSuccessful) {
                    Extensions.toastCall(requireActivity(), "Add account access door successful.")
                } else {
                    Extensions.toastCall(requireActivity(), "Add account access door failed.")
                }
            }
        }
    }
}