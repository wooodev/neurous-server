package com.example.server.domain.news.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class GenericArticleParser implements ArticleParser {

    @Override
    public boolean supports(String url) {
        return true;
    }

    @Override
    public Parsed parse(String html, String url) {
        Document doc = Jsoup.parse(html, url);

        doc.select("script, style, noscript, iframe").remove();

        Element body = firstNonNull(
                doc.selectFirst("article"),
                doc.selectFirst("#articleBodyContents"),
                doc.selectFirst("#content"),
                doc.selectFirst("#contents"),
                doc.selectFirst(".article_body"),
                doc.selectFirst(".article-body"),
                doc.selectFirst(".news_body"),
                doc.selectFirst(".view_cont"),
                doc.selectFirst(".content")
        );

        String raw;
        if (body != null) {
            raw = body.text();
        } else {
            raw = firstMeta(doc, "meta[property=og:description]", "content");
            if (raw.isBlank()) raw = firstMeta(doc, "meta[name=description]", "content");
            if (raw.isBlank()) raw = doc.body() != null ? doc.body().text() : "";
        }

        String clean = normalize(raw);
        return new Parsed(raw, clean);
    }

    private static String firstMeta(Document doc, String cssQuery, String attr) {
        Element e = doc.selectFirst(cssQuery);
        return e == null ? "" : e.attr(attr).trim();
    }

    private static Element firstNonNull(Element... elements) {
        for (Element e : elements) if (e != null) return e;
        return null;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.replace("\u00A0", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}