import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Body;
import org.apache.log4j.Logger;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * This class is responsible for the RSS filtering
 * @author Team09
 * 
 */
public class RssSplitter {

    private Logger log = Logger.getLogger(RssSplitter.class);

    /**
     * This utility methode splits the information in search for the ISBN List.
     * @param feed the rss feed
     * @return the ISBN as List
     */
    public List<String> split(@Body SyndFeed feed) {
        List<SyndEntry> entryList = feed.getEntries();

        List<String> isbnList = new ArrayList<String>();
        for (SyndEntry syndEntry : entryList) {
            String temp = syndEntry.getUri();
            temp = temp.substring(24);

            log.info(temp);

            isbnList.add(temp);
        }

        return isbnList;
    }

}
