package com.homewidget.demo

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
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        setAlarm(context)
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
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

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ) {
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
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime(),
            60000,
            pendingIntent,
        )
    }

    private suspend fun fetchApiData(): String {
        try {
            val client = HttpClient(CIO)
            val url = "https://argaamv2mobileapis.argaam.com/v2.2/json/article-listing/Market-News-Saudi"
            val reqHeaders = mapOf(
                "offSet" to "300",
                "content-type" to "application/json",
                "deviceType" to "android",
                "devicetoken" to "cDOsE8FyRTynHOrCHchCHT:APA91bFHjRX1E731fv7OkqkRQHLjwbqfcWGAVKjcCuhO9ouWsqCq6yzFTb6GusWaHRq595C9hkQLfJSNTBA1gEWaXEOkFsZulXCXxIGhXgJ9924OmT8uO9Gj-n6Fslx6t55UJ7fPAtHK",
            )
            val reqBody = RequestBody(
                param = Param(
                    companyId = 0,
                    langId = 2,
                    marketId = 3,
//                    pageNo = 1,
                    pageSize = 5,
                ),
            )

            val response: HttpResponse = client.request(url) {
                method = HttpMethod.Post
                headers {
                    reqHeaders.forEach { (key, value) ->
                        append(key, value)
                    }
                }
                contentType(ContentType.Application.Json)
                setBody(Gson().toJson(reqBody))
            }

            println(response.request.url)
            val body = response.bodyAsText()
            println(body)
            val apiResponse = parseApiResponse(body)

            client.close()

            return apiResponse.data?.first()?.title?:"something went wrong"
        } catch (e: Exception) {
            println(e)
            return e.localizedMessage ?: "No Error Msg Available"
        }
    }

    private fun parseApiResponse(jsonString: String): ApiResponse {
        val gson = Gson()
        return gson.fromJson(jsonString, ApiResponse::class.java)
    }

    companion object {
        private const val UPDATE_ACTION = "com.homewidget.demo.UPDATE_TIME"
    }
}

data class RequestBody(
    val param: Param,
)

data class Param(
    val companyId: Int,
    val langId: Int,
    val marketId: Int,
//    val pageNo: Int,
    val pageSize: Int,
)

data class ApiResponse(
    @SerializedName("Data") val data: List<Article>?,
)

data class Article(
    @SerializedName("ArticleID") val articleID: Int?,
    @SerializedName("Title") val title: String?,
    @SerializedName("ShortUrl") val shortUrl: String?,
    @SerializedName("ArticleImageUrl") val articleImageUrl: String?,
)
