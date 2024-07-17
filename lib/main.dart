import 'package:flutter/material.dart';
import 'package:home_widget/home_widget.dart';
import 'package:home_widget_demo/home_screen.dart';
import 'package:home_widget_demo/widget_related.dart' as wr;
import 'package:workmanager/workmanager.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  Workmanager().initialize(wr.callbackDispatcher, isInDebugMode: true);

  await HomeWidget.registerInteractivityCallback(wr.interactiveCallback);

  runApp(const MaterialApp(home: HomeScreen()));
}
