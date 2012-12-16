import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
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
 * This class is responsible to aggregate a book with information into xml Nodes.
 * @author Team09
 *
 */
public class IsbnAggregation implements AggregationStrategy {

    private Logger log = Logger.getLogger(IsbnAggregation.class);

    /**
     * This mehtode is responsible to feed the BookNodes with information
     */
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        log.info("Aggregate Alternative Isbn Values");

        String originalBody = oldExchange.getIn().getBody(String.class);
        String resourceBody = newExchange.getIn().getBody(String.class);
        oldExchange.setProperty("isbn", originalBody);
        XmlConverter x = new XmlConverter();

        try {
            Document idList = x.toDOMDocument(resourceBody);
            Document bookInfo = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            Node root = bookInfo.appendChild(bookInfo.createElement("book"));
            Node isbn = root.appendChild(bookInfo.createElement("isbn"));
            isbn.setTextContent(originalBody);

            Node altIsbn = root.appendChild(bookInfo.createElement("altIsbn"));

            NodeList n = idList.getElementsByTagName("isbn");
            for (int i = 0; i < n.getLength(); i++) {
                Node node = altIsbn.appendChild(bookInfo.createElement("isbn"));
                node.setTextContent(n.item(i).getTextContent());
            }

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            StringWriter buffer = new StringWriter();
            transformer.transform(new DOMSource(bookInfo), new StreamResult(buffer));
            String result = buffer.toString();
            oldExchange.getIn().setBody(null);
            oldExchange.getOut().setBody(result);
            log.info("done");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return oldExchange;
    }
}
