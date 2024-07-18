package com.homewidget.demo

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.content.ContextCompat.startActivity
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Implementation of App Widget functionality.
 */
class LatestNews : AppWidgetProvider() {
    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == "com.homewidget.demo.ITEM_CLICK") {
            val newsLink = intent.getStringExtra("news_link")

            if (newsLink != null) {
                val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(newsLink))
                urlIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(context,urlIntent, Bundle())

            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }


    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val latestNews = fetchApiData();
                val top5News = latestNews.take(4)

                val views = RemoteViews(context.packageName, R.layout.latest_news)

                // Clear the container before adding new items
                views.removeAllViews(R.id.widget_container)

                // Add new items
                top5News.forEachIndexed { index, news ->
                    val itemView = RemoteViews(context.packageName, R.layout.latest_news_item)
                    itemView.setTextViewText(R.id.title, news.title)
                    itemView.setTextViewText(R.id.description, news.source)

                    // Set up click event for each item
                    val intent = Intent(context, LatestNews::class.java).apply {
                        action = "com.homewidget.demo.ITEM_CLICK"
                        putExtra("news_link", news.link)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        index,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                    itemView.setOnClickPendingIntent(R.id.latest_news_item, pendingIntent)



                    views.addView(R.id.widget_container, itemView)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                // Handle errors, e.g., log or display an error message
                e.printStackTrace()
            }
        }

    }


    private suspend fun fetchApiData(): List<NewsApiResponse> {
        try {
            val client = HttpClient(CIO)
            val response: HttpResponse = client.get("https://ok.surf/api/v1/news-feed")
            val body = response.bodyAsText()
            val newsItems = parseApiResponse(body)
            client.close()

            if (newsItems == null) throw Exception("Didn't receive any news")

            return newsItems
        } catch (e: Exception) {
            println(e)
            throw e
        }
    }

    private fun parseApiResponse(json: String): List<NewsApiResponse>? {
        // Create Gson instance
        val gson = Gson()

        // Define the type of the list you want to deserialize
        val listType = object : TypeToken<Map<String, List<NewsApiResponse>>>() {}.type

        // Deserialize JSON to your data class
        val responseMap: Map<String, List<NewsApiResponse>> = gson.fromJson(json, listType)

        // Access the list of NewsApiResponse objects
        val newsList: List<NewsApiResponse>? = responseMap["Business"]

        return newsList
    }
}

data class NewsApiResponse(
    val link: String,
    val source: String,
    val title: String,
    @SerializedName("source_icon") val sourceIcon: String,
)
