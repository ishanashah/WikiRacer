import org.attoparser.config.ParseConfiguration;
import org.attoparser.simple.ISimpleMarkupParser;
import org.attoparser.simple.SimpleMarkupParser;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

/*
WikiPage represents a Wikipedia page.

Every WikiPage must contain the URL it represents and the name of the page.
A WikiPage may optionally include a set of pages that this page links to.
 */
public class WikiPage {
    //Markup parser used by all WikiPages
    private static final ISimpleMarkupParser PARSER =
            new SimpleMarkupParser(ParseConfiguration.htmlConfiguration());

    //Markup handler used by all WikiPages
    private static final WikiMarkupHandler HANDLER = new WikiMarkupHandler();

    //The String prefix of all Wikipedia pages
    static final String PAGE_PREFIX = "https://en.wikipedia.org/wiki/";

    //Construct a new WikiPage using the given page name
    //Return null if the URL is invalid or the page
    //cannot be reached
    static WikiPage factory(String pageName) {
        try {
            URL page = new URL(PAGE_PREFIX + pageName);
            HttpURLConnection huc =  ( HttpURLConnection )  page.openConnection ();
            huc.setRequestMethod ("GET");
            huc.connect ();
            if (huc.getResponseCode() == 200) {
                return new WikiPage(pageName, page);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    //The URL that this WIkiPage represents
    private URL page;

    //The name of this WikiPage
    private String pageName;

    //The set of all WikiPages linked to by this page
    private Set<WikiPage> linkedPages = null;

    private WikiPage(String pageName, URL page) {
        this.pageName = pageName;
        this.page = page;
    }

    WikiPage(URL page) {
        this(page.toString().substring(PAGE_PREFIX.length()), page);
    }

    @Override
    public String toString() {
        return pageName;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof WikiPage && pageName.equals(((WikiPage) other).pageName);
    }

    @Override
    public int hashCode() {
        return pageName.hashCode();
    }

    //Parse this page and initialize linkedPages
    void parse(){
        if(linkedPages != null) {
            return;
        }
        try {
            try {
                PARSER.parse(new InputStreamReader(page.openStream()), HANDLER);
                linkedPages = HANDLER.getLinkedPages();
            } catch (FileNotFoundException ignore) {

            }
        } catch (Exception e) {
            System.err.println("Error:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    Set<WikiPage> linkedPages() {
        return linkedPages;
    }

}
