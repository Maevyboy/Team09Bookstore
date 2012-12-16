import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.PollingConsumer;
import org.apache.camel.Processor;
import org.apache.camel.processor.PollEnricher;
import org.apache.log4j.Logger;

/**
 * This class is responsible for library thing information get
 * @author Team09
 *
 */
public class DynamicIsbnEnrich implements Processor {

    private Logger log = Logger.getLogger(DynamicIsbnEnrich.class);

    public void process(Exchange exchange) throws Exception {
        log.info("Enrich Alternative Isbn Values");

        String isbn = exchange.getIn().getBody(String.class);

        Endpoint endpoint = exchange.getContext().getEndpoint("http://www.librarything.com/api/thingISBN/" + isbn);

        PollingConsumer consumer = endpoint.createPollingConsumer();
        PollEnricher enricher = new PollEnricher(new IsbnAggregation(), consumer, 10000);

        consumer.start();
        enricher.process(exchange);
        enricher.shutdown();
        consumer.stop();
    }
}