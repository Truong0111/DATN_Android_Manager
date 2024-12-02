package com.truongtq_datn_manager.activity

import android.app.Application
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.truongtq_datn_manager.extensions.ActivityTracker
import com.truongtq_datn_manager.extensions.Constants
import com.truongtq_datn_manager.okhttpcrud.ApiEndpoint

class MyApplication : Application() {

    val activityTracker = ActivityTracker()
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference

    companion object {
        var instance: MyApplication? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(activityTracker)
        instance = this

        initIpAPI()
    }

    private fun initIpAPI() {
        database = FirebaseDatabase.getInstance()
        myRef = database.getReference(Constants.IP)

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(String::class.java)

                if (value != null) {
                    ApiEndpoint.Url_Server = "https://$value:3001"
                } else {
                    Log.d("Firebase IP", "No data available")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Firebase IP", "Failed to read value.", databaseError.toException())
            }
        })
    }
}