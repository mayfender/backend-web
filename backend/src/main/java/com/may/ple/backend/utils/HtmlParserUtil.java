package com.may.ple.backend.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlParserUtil {

	public static void test(String[] args) {
		try {
			Document doc = Jsoup.connect("http://en.wikipedia.org/").get();
			Elements newsHeadlines = doc.select("#searchInput");
			
			for (Element element : newsHeadlines) {
				System.out.println(element.attr("placeholder"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
