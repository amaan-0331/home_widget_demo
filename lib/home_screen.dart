import 'package:flutter/material.dart';
import 'package:home_widget/home_widget.dart';
import 'package:home_widget_demo/dummy_data.dart';
import 'package:home_widget_demo/news_data.dart';
import 'package:home_widget_demo/widget_related.dart';
import 'package:workmanager/workmanager.dart';

const String appGroupId = 'group.homewidget';
const String iOSWidgetName = 'NewsWidgets';
const String androidWidgetName = 'NewsWidget';

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

    Workmanager().registerPeriodicTask(
      startBackgroundUpdating,
      startBackgroundUpdating,
      constraints: Constraints(networkType: NetworkType.connected),
    );
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
        actions: [
          IconButton(
            onPressed: () => HomeWidget.isRequestPinWidgetSupported().then(
              (value) => (value ?? false)
                  ? HomeWidget.requestPinWidget(
                      name: 'LatestNewsWidgetReceiver',
                    )
                  : null,
            ),
            icon: const Icon(Icons.widgets_rounded),
          )
        ],
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
      appBar: AppBar(title: Text(article.title)),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Text(article.description),
      ),
    );
  }
}
