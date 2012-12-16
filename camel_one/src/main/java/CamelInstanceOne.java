import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * This method ist responsible for the main logic in instance one.
 * It will get and filter ISBN indicators from the <i>Amazon BookStore RSS</i>.
 * @author Team09
 * 
 */
public class CamelInstanceOne {

	/**
	 * This is the main method containing the camel Route to filter the ISBN number and send it to an authorized AWS SQS Service.
	 * @param args the console arguments
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			public void configure() {

				from(
						"rss:http://www.amazon.de/gp/rss/bestsellers/books/ref=zg_bs_books_rsslink?splitEntries=false")
						.split()
						.method("RssSplitter", "split")
						.process(new Dummy())
						.setProperty("isbn", simple("${body}"))
						.to("aws-sqs://team09bookz_sqs?accessKey=AKIAJJDOQK5QYCVP6CNQ&secretKey=lvw8YAZ23Iz3suXfsAjt4wH5ONZZMePcIY13lpGR");

			}
		});
		context.start();
		Thread.sleep(10000);
		context.stop();

	}

}
