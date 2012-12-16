import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.s3.S3Constants;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * This class is the main initiation for instance two, responsible for getting all book information content.
 * @author Team09
 * 
 */
public class CamelInstanceTwo {

    /**
     * This method is the main initiation for the book finding process.
     * @param args the console arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        /*
         * Here the sqs camel route gets the ISBN out of the queues and stores it in S3.
         */
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("aws-sqs://team09bookz_sqs" + "?accessKey=AKIAJJDOQK5QYCVP6CNQ" + "&secretKey=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").process(new DynamicIsbnEnrich()).process(
                        new DynamicIsbndbEnrich()).process(new DynamicOpLibEnrich()).setHeader(S3Constants.KEY, simple("${property.isbn}")).to(
                        "aws-s3://team09bookz" + "?accessKey=AKIAJJDOQK5QYCVP6CNQ" + "&secretKey=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" + "&region=eu-west-1");

            }
        });
        context.start();
        Thread.sleep(10000);
        context.stop();
    }
}
