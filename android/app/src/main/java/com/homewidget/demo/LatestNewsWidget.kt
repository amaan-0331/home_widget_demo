package com.homewidget.demo

import HomeWidgetGlanceState
import HomeWidgetGlanceStateDefinition
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import java.text.SimpleDateFormat
import java.util.Locale


class LatestNewsWidget : GlanceAppWidget() {

    // Needed for Updating
    override val stateDefinition = HomeWidgetGlanceStateDefinition()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceContent(context, currentState())
        }
    }

    @Composable
    private fun GlanceContent(context: Context, currentState: HomeWidgetGlanceState) {
        val data = currentState.preferences

        val time = data.getString("last_time", null)
        val formattedTime = formatIsoDateTime(time!!)
        val title = data.getString("headline_title", null)
        val description = data.getString("headline_description", null)
        val link = data.getString("link", null)

        Column(
            modifier = GlanceModifier.fillMaxSize().background(Color.White),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        ) {
            Text(
                title!!,
                style = TextStyle(fontSize = 14.sp, textAlign = TextAlign.Center),
            )

            Text(
                description!!,
                style = TextStyle(
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                ),
                modifier = GlanceModifier.clickable(onClick = actionRunCallback<OpenArticleAction>(
                    actionParametersOf(NewsUriValue to link!!)
                ))
            )


            Text(
                formattedTime!!,
                style = TextStyle(fontSize = 12.sp, textAlign = TextAlign.Center),
            )

        }
    }

    private fun formatIsoDateTime(isoDateTimeString: String): String? {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.getDefault())
        val outputFormat = SimpleDateFormat("h:mm a, MMMM dd", Locale.getDefault())
        val date = inputFormat.parse(isoDateTimeString)
        return date?.let { outputFormat.format(it) }
    }
}
val NewsUriValue = ActionParameters.Key<String>("NewsUriValue")

class OpenArticleAction : ActionCallback {
    override suspend fun onAction(
        context: Context, glanceId: GlanceId, parameters: ActionParameters
    ) {
        val newsUri = parameters[NewsUriValue] ?: ""

        println(newsUri)

//        val backgroundIntent = HomeWidgetBackgroundIntent.getBroadcast(
//            context, Uri.parse("latestNewsWidget://$newsUri")
//        )
//        backgroundIntent.send()

        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(newsUri))
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(context,browserIntent, Bundle())
    }
}
