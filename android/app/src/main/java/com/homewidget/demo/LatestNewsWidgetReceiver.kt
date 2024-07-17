package com.homewidget.demo

import HomeWidgetGlanceWidgetReceiver

class LatestNewsWidgetReceiver : HomeWidgetGlanceWidgetReceiver<LatestNewsWidget>() {
    override val glanceAppWidget = LatestNewsWidget()
}