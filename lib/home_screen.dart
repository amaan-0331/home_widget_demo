import 'dart:math';

import 'package:flutter/material.dart';
import 'package:home_widget/home_widget.dart';
import 'package:home_widget_demo/dummy_data.dart';
import 'package:home_widget_demo/news_data.dart';

const String appGroupId = 'group.homewidget';
const String iOSWidgetName = 'NewsWidgets';
const String androidWidgetName = 'NewsWidget';

void updateHeadline(NewsArticle newHeadline) {
  HomeWidget.saveWidgetData<String>(
    'headline_title',
    newHeadline.title,
  );
  HomeWidget.saveWidgetData<String>(
    'headline_description',
    newHeadline.description,
  );
  HomeWidget.updateWidget(
    iOSName: iOSWidgetName,
    androidName: androidWidgetName,
  );
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  @override
  void initState() {
    super.initState();

    HomeWidget.setAppGroupId(appGroupId);

    // Mock read in some data and update the headline
    final newHeadline = getArticles()[Random().nextInt(getArticles().length)];
    updateHeadline(newHeadline);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Top Stories'),
        centerTitle: false,
        titleTextStyle: const TextStyle(
          fontSize: 30,
          fontWeight: FontWeight.bold,
          color: Colors.black,
        ),
      ),
      body: ListView.separated(
        itemCount: getArticles().length,
        separatorBuilder: (_, __) => const Divider(),
        itemBuilder: (context, index) {
          final article = getArticles()[index];
          return ListTile(
            key: Key('$index ${article.hashCode}'),
            title: Text(article.title),
            subtitle: Text(article.description),
            onTap: () => Navigator.of(context).push(
              MaterialPageRoute(
                builder: (context) => ArticleScreen(article: article),
              ),
            ),
          );
        },
      ),
    );
  }
}

class ArticleScreen extends StatelessWidget {
  const ArticleScreen({super.key, required this.article});
  final NewsArticle article;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () {
          ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
            content: Text('Updating home screen widget...'),
          ));
          // New: call updateHeadline
          updateHeadline(article);
        },
        label: const Text('Update Homescreen'),
      ),
      appBar: AppBar(title: Text(article.title)),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Text(article.description),
      ),
    );
  }
}
