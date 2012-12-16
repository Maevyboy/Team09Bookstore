import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.camel.Exchange;
import org.apache.camel.converter.jaxp.XmlConverter;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is responsible for the ISBNDB book info aggregation. 
 * @author Team09
 */
public class IsbndbAggregation implements AggregationStrategy {
    private Logger log = Logger.getLogger(IsbndbAggregation.class);

    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        log.info("Aggregate IsbnDB Values");

        String responseStr = newExchange.getIn().getBody(String.class);
        String inStr = oldExchange.getIn().getBody(String.class);

        XmlConverter x = new XmlConverter();

        try {
            Document newDom = x.toDOMDocument(responseStr);
            Document oldDom = x.toDOMDocument(inStr);

            NodeList titles = newDom.getElementsByTagName("Title");
            NodeList authorsTexts = newDom.getElementsByTagName("AuthorsText");
            NodeList publisherTexts = newDom.getElementsByTagName("PublisherText");
            NodeList summarys = newDom.getElementsByTagName("Summary");
            NodeList prices = newDom.getElementsByTagName("Price");
            NodeList subjects = newDom.getElementsByTagName("Subject");

            Node root = oldDom.getDocumentElement();
            Node bookInfo = root.appendChild(oldDom.createElement("bookInfo"));

            if (titles.getLength() > 0) {
                String s = titles.item(0).getTextContent();
                bookInfo.appendChild(oldDom.createElement("title")).setTextContent(s);
            }
            if (authorsTexts.getLength() > 0) {
                String s = authorsTexts.item(0).getTextContent();
                bookInfo.appendChild(oldDom.createElement("authorsText")).setTextContent(s);
            }
            if (publisherTexts.getLength() > 0) {
                String s = publisherTexts.item(0).getTextContent();
                bookInfo.appendChild(oldDom.createElement("publisherText")).setTextContent(s);
            }
            if (summarys.getLength() > 0) {
                String s = summarys.item(0).getTextContent();
                bookInfo.appendChild(oldDom.createElement("summary")).setTextContent(s);
            }
            if (prices.getLength() > 0) {
                String s = prices.item(0).getAttributes().getNamedItem("price").getTextContent();
                bookInfo.appendChild(oldDom.createElement("price")).setTextContent(s);
            }
            if (subjects.getLength() > 0) {
                String s = subjects.item(0).getTextContent();
                bookInfo.appendChild(oldDom.createElement("subject")).setTextContent(s);
            }

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            StringWriter buffer = new StringWriter();
            transformer.transform(new DOMSource(oldDom), new StreamResult(buffer));
            String result = buffer.toString();
            oldExchange.getOut().setBody(result);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return oldExchange;
    }
}
