package com.truongtq_datn_manager.extensions

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.*
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.auth0.android.jwt.JWT
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.truongtq_datn_manager.okhttpcrud.ApiEndpoint


class Extensions {
    companion object {
        private lateinit var database: FirebaseDatabase
        private lateinit var myRef: DatabaseReference

        fun sha256(param: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(param.toByteArray())
            return Base64.encodeToString(bytes, Base64.NO_WRAP)
        }

        fun toastCall(view: Context?, string: String) {
            view?.let {
                val toast = Toast.makeText(it, string, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 100)
                toast.show()
                return
            }
        }

        fun <T : Activity> changeIntent(view: Context?, activityClass: Class<T>) {
            view?.let {
                val intent = Intent(it, activityClass)
                startActivity(it, intent, null)
            }
        }

        fun convertStringToIsoString(dateString: String): String? {
            return try {
                val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                val localDateTime = LocalDateTime.parse(dateString, inputFormatter)
                val isoFormatter = DateTimeFormatter.ISO_INSTANT
                localDateTime.atZone(ZoneId.of("UTC")).format(isoFormatter)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun log(tag: String, message: String) {
            Log.d(tag, message)
        }

        fun logError(tag: String, message: String) {
            Log.e(tag, message)
        }

        fun showDateTimePickerDialog(context: Context, input: TextInputEditText) {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val datePickerDialog = DatePickerDialog(
                context, { _, selectedYear, selectedMonth, selectedDay ->
                    calendar.set(selectedYear, selectedMonth, selectedDay)

                    val timePickerDialog = TimePickerDialog(
                        context, { _, selectedHour, selectedMinute ->
                            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                            calendar.set(Calendar.MINUTE, selectedMinute)

                            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            val formattedTime = timeFormat.format(calendar.time)

                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val formattedDate = dateFormat.format(calendar.time)

                            val formattedDateTime = "$formattedDate $formattedTime"

                            input.setText(formattedDateTime)
                        }, hour, minute, true
                    )

                    timePickerDialog.show()
                }, year, month, day
            )

            datePickerDialog.show()
        }

        fun saveAuthToken(context: Context, token: String) {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encryptedSharedPreferences = EncryptedSharedPreferences.create(
                context,
                "auth_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            encryptedSharedPreferences.edit()
                .putString("auth_token", token)
                .apply()
        }

        fun getAuthToken(context: Context): String? {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encryptedSharedPreferences = EncryptedSharedPreferences.create(
                context,
                "auth_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            return encryptedSharedPreferences.getString("auth_token", null)
        }

        fun decodeJWT(token: String): Pair<String?, String?> {
            val jwt = JWT(token)
            val claim = jwt.getClaim("idAccount").asString()
            val exp = jwt.getClaim("exp").asString()
            return Pair(claim, exp)
        }

        fun extractJson(response: String): JsonObject {
            val gson = Gson()
            val jsonObject: JsonObject = gson.fromJson(response, JsonObject::class.java)
            return jsonObject
        }

        fun generateRandomKey(): String {
            val randomBytes = ByteArray(32)
            Random.Default.nextBytes(randomBytes)
            val bytes = MessageDigest.getInstance("SHA-256").digest(randomBytes)
            return Base64.encodeToString(bytes, Base64.NO_WRAP)
        }

        fun removePreAndSuffix(string: String): String {
            return string.removePrefix("\"").removeSuffix("\"")
        }
    }
}