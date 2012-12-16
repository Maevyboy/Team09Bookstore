import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * @author MaccaPC
 *
 */
public class Dummy implements Processor {

    public void process(Exchange exchange) throws Exception {
        exchange.getOut().setBody(exchange.getIn().getBody());
    }

}
