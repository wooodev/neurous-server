package com.example.server.domain.news.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class NaverArticleParser implements ArticleParser {

    @Override
    public boolean supports(String url) {
        return url != null && (url.contains("n.news.naver.com") || url.contains("news.naver.com"));
    }

    @Override
    public Parsed parse(String html, String url) {
        Document doc = Jsoup.parse(html, url);

        Element body = firstNonNull(
                doc.selectFirst("#dic_area"),
                doc.selectFirst("#newsct_article #dic_area"),
                doc.selectFirst("#articleBodyContents"),
                doc.selectFirst("#articleBody"),
                doc.selectFirst("#articeBody")
        );

        String raw = body != null ? body.text() : "";
        String clean = normalize(raw);

        return new Parsed(raw, clean);
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