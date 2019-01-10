import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.attoparser.simple.*;

/**
 * A markup handler which is called by the attoparser markup parser as it parses the input;
 */
public class WikiMarkupHandler extends AbstractSimpleMarkupHandler {

    //Set of pages linked to by the current page
    private Set<WikiPage> linkedPages;

    //Switch that determines whether the current page
    //should continue to be parsed
    private boolean parse;

    Set<WikiPage> getLinkedPages() {
        return linkedPages;
    }

    //public WikiMarkupHandler() {}

    /**
    * Called when the parser first starts reading a document.
    * @param startTimeNanos  the current time (in nanoseconds) when parsing starts
    * @param line            the line of the document where parsing starts
    * @param col             the column of the document where parsing starts
    */
    public void handleDocumentStart(long startTimeNanos, int line, int col) {
        //Reset linkedPages and parse
        linkedPages = new HashSet<>();
        parse = true;
    }

    /**
    * Called at the start of any tag.
    * @param elementName the element name (such as "div")
    * @param attributes  the element attributes map, or null if it has no attributes
    * @param line        the line in the document where this elements appears
    * @param col         the column in the document where this element appears
    */
    public void handleOpenElement(String elementName, Map<String, String> attributes, int line, int col) {
        //Don't do anything
        if(!parse || attributes == null) { return; }

        if(elementName.toLowerCase().equals("span")) {
            for(String s: attributes.keySet()) {
                if(s.toLowerCase().equals("id") &&
                        (attributes.get(s).equals("Notes") || attributes.get(s).equals("References"))) {
                    //Stop parsing this page
                    parse = false;
                    return;
                }
            }
        }

        if(elementName.toLowerCase().equals("a")) {
            for(String s: attributes.keySet()) {
                if(s.toLowerCase().equals("href")) {
                    String fin_link = attributes.get(s);

                    if(fin_link.length() < 7 || (!fin_link.substring(0, 6).equals("/wiki/"))) {
                        continue;
                    }

                    try {
                        URL next = new URL(WikiPage.PAGE_PREFIX + fin_link.substring(6));
                        try {
                            next = new URL(URLDecoder.decode(next.toString(), StandardCharsets.UTF_8));
                        } catch (Exception ignore) { }

                        WikiPage nextPage =  new WikiPage(next);
                        if((!nextPage.toString().contains("#"))
                                && (!nextPage.toString().contains(":"))
                                && (!nextPage.toString().equals("Main_Page"))) {
                            //Add this page to linkedPages
                            linkedPages.add(nextPage);
                        }
                    } catch(MalformedURLException ignore) { }
                }
            }
        }
    }
}
