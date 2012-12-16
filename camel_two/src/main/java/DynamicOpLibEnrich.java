import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.PollingConsumer;
import org.apache.camel.Processor;
import org.apache.camel.converter.jaxp.XmlConverter;
import org.apache.camel.processor.PollEnricher;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * This is the class which gets bookinformation from open Library and stores it into an XML Doc.
 * @author Team09
 *
 */
public class DynamicOpLibEnrich implements Processor {
    private Logger log = Logger.getLogger(DynamicOpLibEnrich.class);

    /**
     * Is the initiaton process the camel exchange object.
     */
    public void process(Exchange exchange) throws Exception {
        log.info("Enrich OpenLibrary Values");

        String isbn = exchange.getProperty("isbn", String.class);
        String inStr = exchange.getIn().getBody(String.class);

        poll(exchange, isbn);

        if (exchange.getProperty("success", Boolean.class)) {
            return;
        }

        XmlConverter x = new XmlConverter();
        Document oldDom = x.toDOMDocument(inStr);

        NodeList n = oldDom.getElementsByTagName("isbn");
        for (int i = 0; i < n.getLength(); i++) {
            poll(exchange, n.item(i).getTextContent());
            if (exchange.getProperty("success", Boolean.class)) {
                return;
            }
        }
    }

    /**
     * Is the information Poll from OpenLibrary.
     * @param exchange the camel exchange object
     * @param isbn the isbn number
     * @throws Exception
     */
    private void poll(Exchange exchange, String isbn) throws Exception {
        String path = "http://openlibrary.org/api/volumes/brief/json/isbn:" + isbn;
        Endpoint endpoint = exchange.getContext().getEndpoint(path);

        PollingConsumer consumer = endpoint.createPollingConsumer();
        PollEnricher enricher = new PollEnricher(new OpLibAggregation(), consumer, 10000);

        consumer.start();
        enricher.process(exchange);
        enricher.shutdown();
        consumer.stop();
    }
}
