import 'dart:convert';
import 'dart:math';

import 'package:flutter/material.dart';
import 'package:home_widget_demo/news_data.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:workmanager/workmanager.dart';
import 'package:home_widget/home_widget.dart';
import 'package:http/http.dart' as http;

const startBackgroundUpdating = "com.homewidget.demo.startBackgroundUpdating";

// Used for Background Updates using Workmanager Plugin
@pragma("vm:entry-point")
void callbackDispatcher() {
  Workmanager().executeTask((task, inputData) async {
    switch (task) {
      case startBackgroundUpdating:
        await updateLatestNews();
    }

    return Future.value(true);
  });
}

/// Callback invoked by HomeWidget Plugin when performing interactive actions
/// The @pragma('vm:entry-point') Notification is required so that the Plugin can find it
@pragma('vm:entry-point')
Future<void> interactiveCallback(Uri? uri) async {
  final receivedUrlString = uri?.host;
  if (receivedUrlString != null) {
    final receivedUrl = Uri.tryParse(receivedUrlString);
    if (receivedUrl != null) launchUrl(receivedUrl);
  }
}

void updateHomeWidgetData(NewsArticle newHeadline) {
  HomeWidget.saveWidgetData<String>(
    'headline_title',
    newHeadline.title,
  ).then((value) => debugPrint('headline_title update status : $value'));
  HomeWidget.saveWidgetData<String>(
    'headline_description',
    newHeadline.description,
  ).then((value) => debugPrint('headline_description update status : $value'));
  HomeWidget.saveWidgetData<String>(
    'link',
    newHeadline.url,
  ).then((value) => debugPrint('link update status : $value'));
  HomeWidget.saveWidgetData<String>(
    'last_time',
    DateTime.now().toString(),
  ).then((value) => debugPrint('last_time update status : $value'));

  HomeWidget.updateWidget(
    androidName: 'LatestNewsWidgetReceiver',
  ).then((value) => debugPrint(' HomeWidget.updateWidget status : $value'));
}

Future<void> updateLatestNews() async {
  updateHomeWidgetData(
    NewsArticle(
      title: 'Loading',
      description: 'Pls wait...',
    ),
  );
  var url = Uri.parse("https://ok.surf/api/v1/cors/news-feed");

  try {
    var response = await http.get(url);
    final responseBody = json.decode(response.body) as Map<String, dynamic>;

    final newsList = (responseBody['Business'] as List<dynamic>);
    final selectedNews =
        newsList[Random().nextInt(newsList.length)] as Map<String, dynamic>;

    updateHomeWidgetData(
      NewsArticle(
        title: selectedNews['source'],
        description: selectedNews['title'],
        url: selectedNews['link'],
      ),
    );
  } on Exception catch (e) {
    debugPrint('updateLatestNews went wrong!!!');
    debugPrint('error is $e');
  }
}
