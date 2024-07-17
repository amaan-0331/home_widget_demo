class NewsArticle {
  final String title;
  final String description;
  final String? articleText;
  final String? url;

  NewsArticle({
    required this.title,
    required this.description,
    this.articleText = 'loremIpsum',
    this.url,
  });
}
