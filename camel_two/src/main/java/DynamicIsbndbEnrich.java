import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.PollingConsumer;
import org.apache.camel.Processor;
import org.apache.camel.processor.PollEnricher;
import org.apache.log4j.Logger;

/**
 * This class enriches the ISBN number with additional book Information
 * @author Team09
 *
 */
public class DynamicIsbndbEnrich implements Processor {
    private Logger log = Logger.getLogger(DynamicIsbndbEnrich.class);

    /**
     * The method containing the process described in the class header.
     */
    public void process(Exchange exchange) throws Exception {
        log.info("Enrich IsbnDb Values");

        String isbn = exchange.getProperty("isbn", String.class);

        String path = "http://isbndb.com/api/books.xml?access_key=D8MLXKY4&results=details,texts,prices,subjects,authors&index1=isbn&value1=" + isbn;
        Endpoint endpoint = exchange.getContext().getEndpoint(path);

        PollingConsumer consumer = endpoint.createPollingConsumer();
        PollEnricher enricher = new PollEnricher(new IsbndbAggregation(), consumer, 10000);

        consumer.start();
        enricher.process(exchange);
        enricher.shutdown();
        consumer.stop();
    }

}
