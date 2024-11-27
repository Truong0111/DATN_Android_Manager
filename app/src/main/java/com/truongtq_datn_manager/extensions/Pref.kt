package com.truongtq_datn_manager.extensions

import android.app.Activity
import android.content.Context

class Pref {
    companion object {
        fun setString(activity: Activity, key: String, value: String) {
            val sharedPreferences = activity.getSharedPreferences("Pref", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(key, value)
            editor.apply()
        }

        fun setBoolean(activity: Activity, key: String, value: Boolean) {
            val sharedPreferences = activity.getSharedPreferences("Pref", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean(key, value)
            editor.apply()
        }

        fun setInt(activity: Activity, key: String, value: Int) {
            val sharedPreferences = activity.getSharedPreferences("Pref", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt(key, value)
            editor.apply()
        }

        fun setFloat(activity: Activity, key: String, value: Float) {
            val sharedPreferences = activity.getSharedPreferences("Pref", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putFloat(key, value)
            editor.apply()
        }

        fun setLong(activity: Activity, key: String, value: Long) {
            val sharedPreferences = activity.getSharedPreferences("Pref", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putLong(key, value)
            editor.apply()
        }

        fun getString(activity: Activity, key: String): String {
            val sharedPreferences = activity.getSharedPreferences("Pref", Context.MODE_PRIVATE)
            return sharedPreferences.getString(key, "") ?: ""
        }

        fun getBoolean(activity: Activity, key: String): Boolean {
            val sharedPreferences = activity.getSharedPreferences("Pref", Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean(key, false)
        }

        fun getInt(activity: Activity, key: String): Int {
            val sharedPreferences = activity.getSharedPreferences("Pref", Context.MODE_PRIVATE)
            return sharedPreferences.getInt(key, Int.MIN_VALUE)
        }

        fun getFloat(activity: Activity, key: String): Float {
            val sharedPreferences = activity.getSharedPreferences("Pref", Context.MODE_PRIVATE)
            return sharedPreferences.getFloat(key, Float.MIN_VALUE)
        }

        fun getLong(activity: Activity, key: String): Long {
            val sharedPreferences = activity.getSharedPreferences("Pref", Context.MODE_PRIVATE)
            return sharedPreferences.getLong(key, Long.MIN_VALUE)
        }

        fun clearData(activity: Activity) {
            val sharedPreferences = activity.getSharedPreferences("Pref", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
        }

        fun remove(activity: Activity, key: String) {
            val sharedPreferences = activity.getSharedPreferences("Pref", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove(key)
            editor.apply()
        }
    }
}