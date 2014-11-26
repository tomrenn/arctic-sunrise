package com.example.rennt.arcticsunrise.data.api.models;

import android.util.Log;
import android.util.Xml;

import com.example.rennt.arcticsunrise.data.api.requests.XMLRequest;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

/**
* Summary of an article
*/
public class Article {
    private String headline;
//    private String type;
    private boolean isDeco;
    private boolean isPaid;
    private String summmary;
//    private String thumbnail;
    private String articleSource;
    // Item key="share_link"
//    private String articleLink;



    public Article(String headline, String summary, boolean isDeco){
        this.headline = headline;
        this.summmary = summary;
        this.isDeco = isDeco;
    }

    public String getHeadline() { return headline; }


    public static class ArticleListParser implements XMLRequest.XMLParser<List<Article>> {
        private static final String ns = null;

        @DebugLog
        public List<Article> parse(String xml) throws XmlPullParserException, IOException {
            Reader reader = new StringReader(xml);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new StringReader(xml));
                parser.nextTag();
                return readFeed(parser);
            } catch (XmlPullParserException|IOException exception){
                Log.i("Parser", exception.toString());
            } finally {
                reader.close();
            }
            return null;
        }

        private List<Article> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
            List<Article> entries = new ArrayList<Article>();

            parser.require(XmlPullParser.START_TAG, ns, "pages");

            while (parser.next() != XmlPullParser.END_TAG){
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                parser.require(XmlPullParser.START_TAG, ns, "page");

                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    String name = parser.getName();
                    // Starts by looking for the entry tag
                    if ("link".equals(name)) {
                        entries.add(readEntry(parser));
                    } else {
                        skip(parser);
                    }
                }
            }


            return entries;
        }

        // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
        // to their respective "read" methods for processing. Otherwise, skips the tag.
        private Article readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(XmlPullParser.START_TAG, ns, "link");
            String decoStr = parser.getAttributeValue(ns, "class");
            boolean isDeco = "deco".equals(decoStr);
            String headline = null;
            String summary = null;
            String link = null;

            // iterate parser over metadata
            while (!"metadata".equals(parser.getName())) {
                parser.next();
            }
            parser.require(XmlPullParser.START_TAG, ns, "metadata");

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

//                parser.getE
                String key = parser.getAttributeValue(ns, "key");

                if ("headline".equals(key)) {
                    parser.require(XmlPullParser.START_TAG, ns, "item");
                    headline = readText(parser);
                    parser.require(XmlPullParser.END_TAG, ns, "item");
                } else if ("summary".equals(key)) {
                    summary = readSummary(parser);
                } else {
                    skip(parser);
                }
            }

            // finish over metadata, get to closing link tag
            while (!"link".equals(parser.getName()) ||
                    parser.getEventType() != XmlPullParser.END_TAG){
                parser.next();
            }

            return new Article(headline, summary, isDeco);
        }

        private String readSummary(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, ns, "item");
            String summary = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, "item");
            return summary;
        }


        // For the tags title and summary, extracts their text values.
        private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
            String result = "";
            if (parser.next() == XmlPullParser.TEXT) {
                result = parser.getText();
                parser.nextTag();
            }
            return result;
        }

        private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                Log.e("PARSER", "SKIP IS AT WRONG STATE");
                throw new IllegalStateException();
            }
            int depth = 1;
            while (depth != 0) {
                switch (parser.next()) {
                    case XmlPullParser.END_TAG:
                        depth--;
                        break;
                    case XmlPullParser.START_TAG:
                        depth++;
                        break;
                }
            }
        }
    }
}
