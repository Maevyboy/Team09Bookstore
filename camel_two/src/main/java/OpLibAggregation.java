import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.camel.Exchange;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.converter.jaxp.XmlConverter;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class is responsible for the openbook library aggregation
 * @author Team09
 */
public class OpLibAggregation implements AggregationStrategy {
    private Logger log = Logger.getLogger(OpLibAggregation.class);

    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        String responseStr = newExchange.getIn().getBody(String.class);
        String inStr = oldExchange.getIn().getBody(String.class);

        if ("{}".equals(responseStr)) {
            oldExchange.setProperty("success", false);
            return oldExchange;
        } else {
            oldExchange.setProperty("success", true);
        }
        log.info("Aggregate OpenLibrary Values");

        JacksonDataFormat jackson = new JacksonDataFormat();
        ObjectMapper mapper = jackson.getObjectMapper();

        XmlConverter x = new XmlConverter();

        try {
            Document oldDom = x.toDOMDocument(inStr);
            JsonNode rootNode = mapper.readValue(responseStr, JsonNode.class);

            JsonNode publishDates = search(rootNode, "publishDates");
            JsonNode thumbnail_url = search(rootNode, "thumbnail_url");
            JsonNode preview_url = search(rootNode, "preview_url");

            Node bookInfo = oldDom.getElementsByTagName("bookInfo").item(0);

            if (publishDates != null) {
                String s = publishDates.toString();
                bookInfo.appendChild(oldDom.createElement("publishDate")).setTextContent(s);;
            }

            if (thumbnail_url != null) {
                String s = thumbnail_url.toString();
                bookInfo.appendChild(oldDom.createElement("thumbnail_url")).setTextContent(s);;
            }

            if (preview_url != null) {
                String s = preview_url.toString();
                bookInfo.appendChild(oldDom.createElement("preview_url")).setTextContent(s);;
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

    /**
     * This method searches JsonNodes after a key value.
     * @param rootNode the rootNode
     * @param key they Key
     * @return they ment JsonNode
     */
    private JsonNode search(JsonNode rootNode, String key) {
        Iterator<Entry<String, JsonNode>> i = rootNode.getFields();

        while (i.hasNext()) {
            Entry<String, JsonNode> e = i.next();
            if (key.equals(e.getKey())) {
                return e.getValue();
            }

            JsonNode s = search(e.getValue(), key);
            if (s != null) {
                return s;
            }
        }
        return null;
    }
}
