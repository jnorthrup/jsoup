package org.jsoup.examples;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * HTML to plain-text. This example program demonstrates the use of jsoup to convert HTML input to lightly-formatted
 * plain-text. That is divergent from the general goal of jsoup's .text() methods, which is to get clean data from a
 * scrape.
 * <p/>
 * Note that this is a fairly simplistic formatter -- for real world use you'll want to embrace and extend.
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */
public class HtmlToPlainText {
    private static final Pattern COMPILE = Pattern.compile("\\s+");

    public static void main(String... args) throws IOException {
        Validate.isTrue(args.length == 1, "usage: supply url to fetch");
        String url = args[0];

        // fetch the specified URL and parse to a HTML DOM
        Document doc = Jsoup.connect(url).get();

        HtmlToPlainText formatter = new HtmlToPlainText();
        String plainText = getPlainText(doc);
        System.out.println(plainText);
    }

    /**
     * Format an Element to plain-text
     * @param element the root element to format
     * @return formatted text
     */
    private static String getPlainText(Element element) {
        HtmlToPlainText.FormattingVisitor formatter = new HtmlToPlainText.FormattingVisitor();
        NodeTraversor traversor = new NodeTraversor(formatter);
        traversor.traverse(element); // walk the DOM, and call .head() and .tail() for each node

        return formatter.toString();
    }

    // the formatting rules, implemented in a breadth-first DOM traverse
    private static class FormattingVisitor implements NodeVisitor {
        private static final int maxWidth = 80;
        private int width;
        private StringBuilder accum = new StringBuilder(); // holds the accumulated text

        FormattingVisitor() {
        }

        // hit when the node is first seen
        public void head(Node node, int depth) {
            String name = node.nodeName();
            if (node instanceof TextNode)
                append(((TextNode) node).text()); // TextNodes carry all user-readable text in the DOM.
            else if ("li".equals(name))
                append("\n * ");
        }

        // hit when all of the node's children (if any) have been visited
        public void tail(Node node, int depth) {
            String name = node.nodeName();
            switch (name) {
                case "br":
                    append("\n");
                    break;
                case"p":case "h1":case "h2":case "h3":case "h4":case "h5":
                    append("\n\n");
                    break;
                default:

                    if ("a".equals(name))
                        append(String.format(" <%s>", node.absUrl("href")));
                    break;
            }
        }

        // appends text to the string builder with a simple word wrap method
        private void append(String text) {
            if (!text.isEmpty() && text.charAt(0) == '\n')
                width = 0; // reset counter if starts with a newline. only from formats above, not in natural text
            if (!" ".equals(text) ||
                    accum.length() != 0 && !Arrays.asList(" ", "\n").contains(accum.substring(accum.length() - 1))) {

                if (text.length() + width <= maxWidth) { // fits as is, without need to wrap text
                    accum.append(text);
                    width += text.length();
                } else { // won't fit, needs to wrap
                    String[] words = COMPILE.split(text);
                    StringBuilder word = new StringBuilder(words[0].length());
                    for (int i = 0; i < words.length; i++) {
                        word.setLength(0);
                        word.append(words[i]);
                        boolean last = i == words.length - 1;
                        if (!last) // insert a space if not the last word
                            word.append(' ');
                        if (word.length() + width > maxWidth) { // wrap and reset counter
                            accum.append('\n').append(word);
                            width = word.length();
                        } else {
                            accum.append(word);
                            width += word.length();
                        }
                    }
                }
            }
        }

        public String toString() {
            return accum.toString();
        }
    }
}
