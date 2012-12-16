package de.fhb;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.Charsets;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * 
 * @author Team09 Servlet implementation class RssService
 */
@WebServlet("/*")
public class RssService extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public RssService() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // s3
        AwsS3Credentials credentials = AwsS3Credentials.getIns("AwsCredentials.properties");
        BucketUtil bucketUtil = new BucketUtil(credentials.initCredentials());

        List<String> strLst = bucketUtil.listBucketContent("team09bookz");
        // rss

        response.setContentType("application/rss+xml");

        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("rss_2.0");
        feed.setTitle("Amazon Bestseller Rss Feed");
        feed.setLink("...");
        feed.setDescription("Die aktuellen Buch Bestseller von Amazon von Camel integriert");

        List<SyndEntry> entries = new ArrayList<SyndEntry>();

        for (String string : strLst) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            try {
                DocumentBuilder builder = factory.newDocumentBuilder();

                Document dom = builder.parse(new InputSource(new StringReader(string)));

                SyndEntry entry = createEntry(dom);

                entries.add(entry);

                feed.setEntries(entries);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        PrintWriter writer = response.getWriter();
        SyndFeedOutput output = new SyndFeedOutput();

        try {
            output.output(feed, writer);
        } catch (FeedException e) {
            e.printStackTrace();
        }

    }

    private SyndEntry createEntry(Document dom) {
        NodeList isbnLst = dom.getElementsByTagName("isbn");
        NodeList titles = dom.getElementsByTagName("Title");
        NodeList authorsTexts = dom.getElementsByTagName("AuthorsText");
        NodeList publisherTexts = dom.getElementsByTagName("PublisherText");
        NodeList summarys = dom.getElementsByTagName("Summary");
        NodeList prices = dom.getElementsByTagName("Price");
        NodeList subject = dom.getElementsByTagName("Subject");
        NodeList publishDate = dom.getElementsByTagName("publishDate");
        NodeList thumbnail_url = dom.getElementsByTagName("thumbnail_url");
        NodeList preview_url = dom.getElementsByTagName("preview_url");

        StringBuilder content = new StringBuilder();

        SyndEntry entry = new SyndEntryImpl();
        if (titles.getLength() > 0) {
            String str = titles.item(0).getTextContent();
            entry.setTitle(str);
            content.append("Titel: " + titles.item(0).getTextContent());
        }
        // entry.setLink("...");
        entry.setPublishedDate(new Date());

        // content building
        content.append("<br>Isbns: " + isbnLst.item(0).getTextContent());
        for (int i = 1; i < isbnLst.getLength(); i++) {
            content.append(", " + isbnLst.item(i).getTextContent());
        }
        if (authorsTexts.getLength() > 0) {
            content.append("<br>Autor: " + authorsTexts.item(0).getTextContent());
        }
        if (publisherTexts.getLength() > 0) {
            content.append("<br>Verlag: " + publisherTexts.item(0).getTextContent());
        }
        if (summarys.getLength() > 0) {
            content.append("<br>Beschreibung: " + summarys.item(0).getTextContent());
        }
        if (prices.getLength() > 0) {
            content.append("<br>Preis: " + prices.item(0).getTextContent());
        }
        if (subject.getLength() > 0) {
            content.append("<br>Thema: " + subject.item(0).getTextContent());
        }
        if (publishDate.getLength() > 0) {
            content.append("<br>Erscheinungsdatum: " + publishDate.item(0).getTextContent());
        }
        if (thumbnail_url.getLength() > 0) {
            content.append("<br>Cover-URL: " + thumbnail_url.item(0).getTextContent());
        }
        if (preview_url.getLength() > 0) {
            content.append("<br>Vorschau-URL: " + preview_url.item(0).getTextContent());
        }

        SyndContent description = new SyndContentImpl();
        description.setType("text/html");
        description.setValue("<![CDATA[<div>" + content.toString() + "</div>]]");
        // entry.setDescription(description);

        List<SyndContent> l = new ArrayList<SyndContent>();
        l.add(description);
        entry.setContents(l);

        List<SyndCategory> categories = new ArrayList<SyndCategory>();
        SyndCategory category = new SyndCategoryImpl();
        category.setName("Buch");
        categories.add(category);
        entry.setCategories(categories);

        return entry;
    }
}
