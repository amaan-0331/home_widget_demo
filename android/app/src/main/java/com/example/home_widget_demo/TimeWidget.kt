package com.example.home_widget_demo

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class TimeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        setAlarm(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == UPDATE_ACTION) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisAppWidget = ComponentName(context.packageName, TimeWidgetProvider::class.java.name)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: String = fetchApiData()
                val views = RemoteViews(context.packageName, R.layout.time_widget)
                views.setTextViewText(R.id.textViewTime, response)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                // Handle errors, e.g., log or display an error message
                e.printStackTrace()
            }
        }
    }

    private fun setAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TimeWidgetProvider::class.java)
        intent.action = UPDATE_ACTION
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 60000, pendingIntent)
    }

    private suspend fun fetchApiData(): String {
        try {
            val client = HttpClient(CIO)
            val response: HttpResponse = client.get("http://worldtimeapi.org/api/timezone/Asia/Dubai")
            val body = response.bodyAsText()
            val apiResponse = parseApiResponse(body)
            val time = formatTime(apiResponse.datetime)
            client.close()
            return time
        } catch (e: Exception) {
            // Log exception or return a default error message
            e.printStackTrace()
            return "Error"
        }
    }

    private fun parseApiResponse(json: String): ApiResponse {
        val gson = Gson()
        return gson.fromJson(json, ApiResponse::class.java)
    }

    private fun formatTime(dateTimeString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateTimeString)
        return outputFormat.format(date!!)
    }

    companion object {
        private const val UPDATE_ACTION = "com.example.home_widget_demo.UPDATE_TIME"
    }
}

data class ApiResponse(
    val abbreviation: String,
    @SerializedName("client_ip") val clientIp: String,
    val datetime: String,
    @SerializedName("day_of_week") val dayOfWeek: Int,
    @SerializedName("day_of_year") val dayOfYear: Int,
    val dst: Boolean,
    @SerializedName("dst_from") val dstFrom: String?,
    @SerializedName("dst_offset") val dstOffset: Int,
    @SerializedName("dst_until") val dstUntil: String?,
    @SerializedName("raw_offset") val rawOffset: Int,
    val timezone: String,
    val unixtime: Long,
    @SerializedName("utc_datetime") val utcDatetime: String,
    @SerializedName("utc_offset") val utcOffset: String,
    @SerializedName("week_number") val weekNumber: Int
)
