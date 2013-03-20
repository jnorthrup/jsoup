package org.jsoup.test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.ParseError;
import org.jsoup.parser.Parser;
import org.jsoup.parser.XmlTreeBuilder;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.junit.Test;

public class JsoupTest {

    private static final Pattern PATTERN1 = Pattern.compile("\\n\\s*");

    @Test
	public void plaintextTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/plaintext.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");

		//Check if created head tags
		assertEquals("<head></head>",p.child(0).child(0).toString());
		//Check to see if body was created
		assertEquals("body", p.child(0).child(1).tagName());
		//Get inner html of body tag
		assertEquals("Let's see how Jsoup parses this! Plain text, invalid html document.",p.child(0).child(1).html());
	}
	
	@Test
	public void missingheadTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/missinghead.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");

		//Check if created head tags
		assertEquals("head",p.child(0).child(0).tagName());
		
		//Check to see if body was created
		assertEquals("body", p.body().tagName());
		//Check if tags in body were parsed 
		assertEquals("title",p.body().child(0).tagName());
		assertEquals("Hey, I am the headless html!",p.body().child(0).html());
		assertEquals("p",p.body().child(1).tagName());
		assertEquals("Some paragraphs",p.body().child(1).html());
	}
	
	@Test
	public void missinghtmltagTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/missinghtmltag.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");

		//Check if created html tags
		assertEquals("html",p.child(0).tagName());
		
		//Check if head elements were put into html tag
		assertEquals("head",p.child(0).child(0).tagName());
		//Check to see if body was created
		assertEquals("body", p.child(0).child(1).tagName());
		assertEquals("Straight into the head and the body, no html tag", p.child(0).child(1).child(0).html());

	}
	
	@Test
	public void missingbodyTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/missingbody.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//Check if head parsed
		assertEquals("head",p.child(0).child(0).tagName());
		assertEquals("title",p.head().child(0).tagName());
		//Check if body was created and elements in head were correctly put into body
		assertEquals("body",p.child(0).child(1).tagName());
		assertEquals("p",p.child(0).child(1).child(0).tagName());
		assertEquals("Hello, My body is in my head. What?!",p.child(0).child(1).child(0).html());
	}
	
	
	//Doesn't handle interlocking tags very well
	@Test
	public void interlockingtagsTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/interlockingtags.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//Check if correctly parses first child in body
		assertEquals("div",p.body().child(0).tagName());
		//Check if correctly parses first nested tag
		assertEquals("p",p.body().child(0).child(0).tagName());
		//Check if correctly recognizes interlocking and replaces </div> with </a>
		assertEquals("Interlocking Tags, first paragraph <a href=\"http://google.ca\"> Still in </a>",p.body().child(0).child(0).html());
		
	}
	
	@Test
	public void wrongclosingtagTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/wrongclosingtagname.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//check if changes wrong closing tag name to the correct one
		assertEquals("h1",p.body().child(0).tagName());
		assertEquals("<h1>Wrong closing tag name</h1>",p.body().child(0).outerHtml());
	}
	
	@Test
	public void invalidclosingtagsyntaxTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/invalidclosingtag.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//check if closes unclosed ending tag, add missing closing tag
		assertEquals("div",p.body().child(0).tagName());
		assertEquals("Invalid closing\n<div></div>",p.body().child(0).html());
		assertEquals("div",p.body().child(0).child(0).tagName());
	}
	
	@Test
	public void missingclosingtagTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/missingclosingtag.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//check if adds missing closing tag
		assertEquals("div",p.body().child(0).tagName());
		assertEquals("<div>\n Missing closing tag\n</div>",p.body().child(0).outerHtml());
	}
	
	@Test
	public void unclosedclosingtagTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/unclosedclosingtag.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//check if closes unclosed ending tag
		assertEquals("div",p.body().child(0).tagName());
		assertEquals("<div>\n Unclosed closing tag\n</div>",p.body().child(0).outerHtml());
	}
	
	@Test
	public void contentinemptytagTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/contentinsideemptytag.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//check if changes content inside start tag to attributes
		assertEquals("div",p.body().child(0).tagName());
		assertEquals("",p.body().child(0).attr("content"));
		assertEquals("",p.body().child(0).attr("inside"));
		assertEquals("",p.body().child(0).attr("again,"));
		assertEquals("",p.body().child(0).attr("different"));
		assertEquals("",p.body().child(0).attr("closing"));
		assertEquals("",p.body().child(0).attr("mechanism"));
	}
	
	@Test
	public void missingclosingquoteonattributeTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/missingendquote.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//Check if correctly discards tag with no closing quote on one attribute value
		assertEquals("",p.body().html());
	}
	
	@Test
	public void noquotationsforattrvaluesTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/noquotesforattr.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//Check if correctly parses attribute values even with no quotations
		assertEquals("a",p.body().child(0).tagName());
		assertEquals("http://www.google.ca",p.body().child(0).attr("href"));
		assertEquals("noquotesforvalue",p.body().child(0).attr("id"));
	}
	
	@Test
	public void nospacesbetweenattributesTest() throws IOException{
		File plaintext = new File("testres/invalidhtml/nospacebetweenattr.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//Check if correctly parses attribute values even with no spaces between attributes
		assertEquals("a",p.body().child(0).tagName());
		assertEquals("http://www.google.ca",p.body().child(0).attr("href"));
		assertEquals("nospacebetweenattributes",p.body().child(0).attr("id"));
	}
	
	//Valid HTML Tests
	@Test
	public void regularvalidhtmlTest() throws IOException{
		File plaintext = new File("testres/validhtml/regularvalidhtmldoc.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		assertEquals("html",p.child(0).tagName());
		assertEquals("head",p.child(0).child(0).tagName());
		assertEquals("title",p.head().child(0).tagName());
		assertEquals("Regular HTML file, well formed",p.head().child(0).html());
		
		assertEquals("body",p.child(0).child(1).tagName());
		assertEquals("p",p.body().child(0).tagName());
		assertEquals("Regular paragraph in the body",p.body().child(0).html());
		assertEquals("a",p.body().child(1).tagName());
		assertEquals("http://www.google.ca",p.body().child(1).attr("href"));
		assertEquals("Google",p.body().child(1).html());
		assertEquals("h1",p.body().child(2).tagName());
		assertEquals("Header",p.body().child(2).html());
		//Check if 3rd element is div, discarding the comment
		assertEquals("div",p.body().child(3).tagName());
		assertEquals("span",p.body().child(3).child(0).tagName());
		assertEquals("Section 1",p.body().child(3).child(0).html());
	}
	
	@Test
	public void nestedelementsTest() throws IOException{
		File plaintext = new File("testres/validhtml/nestedelements.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
		
		//Check if table correctly parsed
		assertEquals("table",p.body().child(0).tagName());
		//Check if table headers parsed
		assertEquals("tbody",p.body().child(0).child(0).tagName());
		assertEquals("tr",p.body().child(0).child(0).child(0).tagName());
		assertEquals("th",p.body().child(0).child(0).child(0).child(0).tagName());
		assertEquals("C1",p.body().child(0).child(0).child(0).child(0).html());
		
		assertEquals("th",p.body().child(0).child(0).child(0).child(0).tagName());
		assertEquals("C2",p.body().child(0).child(0).child(0).child(1).html());
		
		assertEquals("th",p.body().child(0).child(0).child(0).child(0).tagName());
		assertEquals("C3",p.body().child(0).child(0).child(0).child(2).html());
		
		//Second Row contains a nested table
		assertEquals("tr",p.body().child(0).child(0).child(1).tagName());
		assertEquals("td",p.body().child(0).child(0).child(1).child(0).tagName());
		//Check if table data, in fact, parses nested table correctly
		assertEquals("table",p.body().child(0).child(0).child(1).child(0).child(0).tagName());
		assertEquals("tbody",p.body().child(0).child(0).child(1).child(0).child(0).child(0).tagName());
		assertEquals("tr",p.body().child(0).child(0).child(1).child(0).child(0).child(0).child(0).tagName());
		assertEquals("<td>table</td>",p.body().child(0).child(0).child(1).child(0).child(0).child(0).child(0).child(0).outerHtml());
		assertEquals("<td>within</td>",p.body().child(0).child(0).child(1).child(0).child(0).child(0).child(0).child(1).outerHtml());
		assertEquals("<td>a</td>",p.body().child(0).child(0).child(1).child(0).child(0).child(0).child(0).child(2).outerHtml());
		assertEquals("<td><b>table</b></td>",p.body().child(0).child(0).child(1).child(0).child(0).child(0).child(0).child(3).outerHtml());
		
	}
	
	@Test
	public void multipleattributesTest() throws IOException{
		File plaintext = new File("testres/validhtml/multiattrelement.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
			
		assertEquals("a",p.body().child(0).tagName());
		assertEquals("http://www.google.ca",p.body().child(0).attr("href"));
		assertEquals("MultiAttribute element",p.body().child(0).attr("name"));
		assertEquals("_top",p.body().child(0).attr("target"));
	}
	
	@Test
	public void singlesimpleelementTest() throws IOException{
		File plaintext = new File("testres/validhtml/singleelement.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
			
		assertEquals("a",p.body().child(0).tagName());
		assertEquals("Simple element, check to see if content is correct",p.body().child(0).html());
	}
	
	@Test
	public void emptytagTest() throws IOException{
		File plaintext = new File("testres/validhtml/emptytag.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
			
		assertEquals("table",p.body().child(0).tagName());
		//Check if normalises element from empty tag to start tag and closing tag
		assertEquals("<table></table>",p.body().child(0).outerHtml());
		assertEquals("",p.body().child(0).html());
	}
	
	@Test
	public void formattingtagsTest() throws IOException{
		File plaintext = new File("testres/validhtml/formattingtags.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
			
		assertEquals("p",p.body().child(0).tagName());
		//Keeps formatting tags and doesn't discard them
		assertEquals("b",p.body().child(0).child(0).tagName());
		assertEquals("bold text and <i>emphasize</i> some words",p.body().child(0).child(0).html());
		assertEquals("i",p.body().child(0).child(0).child(0).tagName());
		assertEquals("emphasize",p.body().child(0).child(0).child(0).html());
	}
	
	@Test
	public void singlequotesforattrTest() throws IOException{
		File plaintext = new File("testres/validhtml/singlequotedattrelement.html");
		//Takes invalid text and normalizes into correct html file
		Document p = Jsoup.parse(plaintext, "UTF-8");
			
		assertEquals("a",p.body().child(0).tagName());
		//check if correctly parses attribute
		assertEquals("http://www.google.ca",p.body().child(0).attr("href"));
		//check if normalises to double quotations
		assertEquals("<a href=\"http://www.google.ca\">Single Quoted</a>",p.body().child(0).outerHtml());
	}
	
	
	//Yang's Stuff
	/*
	 * 2.    static Document 	parse(String html)
              Parse HTML into a Document.

	 */
	//Part I: Valid HTML String Tests
	@Test public void parsesComments() {
        String html="<html><head><title>Regular HTML file, well formed</title></head><body>"+
				"<p>Regular paragraph in the body</p>" +
				"<a href=\"http://www.google.ca\">Google</a>" +
				"<h1>Header</h1>" +
				"<!--Comment: Let's try some sections nested here-->"+
				"<div><span>Section 1</span><span>Section 2</span></div></body></html>";
		Document p = Jsoup.parse(html);

        Element body = p.body();
        Comment comment = (Comment) body.childNode(3); // comment should not be sub of img, as it's an empty tag
        assertEquals("Comment: Let's try some sections nested here", comment.getData());
        Element e = body.child(0);
        TextNode text = (TextNode) e.childNode(0);
        assertEquals("Regular paragraph in the body", text.getWholeText());
    }
    
	@Test
	public void regularvalidhtmlStringTest() throws IOException{
		String html="<html><head><title>Regular HTML file, well formed</title></head><body>"+
				"<p>Regular paragraph in the body</p>" +
				"<a href=\"http://www.google.ca\">Google</a>" +
				"<h1>Header</h1>" +
				"<!--Comment: Let's try some sections nested here-->"+
				"<div><span>Section 1</span><span>Section 2</span></div></html>";
		Document p = Jsoup.parse(html);

		assertEquals("html",p.child(0).tagName());
		assertEquals("head",p.child(0).child(0).tagName());
		assertEquals("title",p.head().child(0).tagName());
		assertEquals("Regular HTML file, well formed",p.head().child(0).html());

		assertEquals("body",p.child(0).child(1).tagName());
		assertEquals("p",p.body().child(0).tagName());
		assertEquals("Regular paragraph in the body",p.body().child(0).html());
		assertEquals("a",p.body().child(1).tagName());
		assertEquals("http://www.google.ca",p.body().child(1).attr("href"));
		assertEquals("Google",p.body().child(1).html());
		assertEquals("h1",p.body().child(2).tagName());
		assertEquals("Header",p.body().child(2).html());
		//	Check if 3rd element is div, discarding the comment
		assertEquals("div",p.body().child(3).tagName());
		assertEquals("span",p.body().child(3).child(0).tagName());
		assertEquals("Section 1",p.body().child(3).child(0).html());
	}
	
	@Test
	public void nestedelementsStringTest() throws IOException{
		String html="<table border=\"1\"><tr><th>C1</th><th>C2</th><th>C3<th></tr>"+
					"<tr><td>	<table border=\"2\">"+
					"<tr>	<td>table</td>	<td>within</td>	<td>a</td>	<td><b>table</b></td>	</tr>	<tr/>"+
					"</table></td>"+
					"<td>data1</td><td>data2</td></tr></table>";
		Document p = Jsoup.parse(html);

		//Check if table correctly parsed
		assertEquals("table",p.body().child(0).tagName());
		//Check if table headers parsed
		assertEquals("tbody",p.body().child(0).child(0).tagName());
		assertEquals("tr",p.body().child(0).child(0).child(0).tagName());
		assertEquals("th",p.body().child(0).child(0).child(0).child(0).tagName());
		assertEquals("C1",p.body().child(0).child(0).child(0).child(0).html());

		assertEquals("th",p.body().child(0).child(0).child(0).child(0).tagName());
		assertEquals("C2",p.body().child(0).child(0).child(0).child(1).html());

		assertEquals("th",p.body().child(0).child(0).child(0).child(0).tagName());
		assertEquals("C3",p.body().child(0).child(0).child(0).child(2).html());

		//	Second Row contains a nested table
		assertEquals("tr",p.body().child(0).child(0).child(1).tagName());
		assertEquals("td",p.body().child(0).child(0).child(1).child(0).tagName());
		//Check if table data, in fact, parses nested table correctly
		assertEquals("table",p.body().child(0).child(0).child(1).child(0).child(0).tagName());
		assertEquals("tbody",p.body().child(0).child(0).child(1).child(0).child(0).child(0).tagName());
		assertEquals("tr",p.body().child(0).child(0).child(1).child(0).child(0).child(0).child(0).tagName());
		assertEquals("<td>table</td>",p.body().child(0).child(0).child(1).child(0).child(0).child(0).child(0).child(0).outerHtml());
		assertEquals("<td>within</td>",p.body().child(0).child(0).child(1).child(0).child(0).child(0).child(0).child(1).outerHtml());
		assertEquals("<td>a</td>",p.body().child(0).child(0).child(1).child(0).child(0).child(0).child(0).child(2).outerHtml());
		assertEquals("<td><b>table</b></td>",p.body().child(0).child(0).child(1).child(0).child(0).child(0).child(0).child(3).outerHtml());

	}

	

	@Test
	public void multipleattributesStringTest() throws IOException{
		String html="<a href=\"http://www.google.ca\" name=\"MultiAttribute element\" target=\"_top\">Multiattribute link</a>";
		Document p = Jsoup.parse(html);

		assertEquals("a",p.body().child(0).tagName());
		assertEquals("http://www.google.ca",p.body().child(0).attr("href"));
		assertEquals("MultiAttribute element",p.body().child(0).attr("name"));
		assertEquals("_top",p.body().child(0).attr("target"));
	}

	@Test
	public void singlesimpleelementStringTest() throws IOException{
		String html="<a href=\"www.example.ca\">Simple element, check to see if content is correct</a>";
		Document p = Jsoup.parse(html);

		assertEquals("a",p.body().child(0).tagName());
		assertEquals("Simple element, check to see if content is correct",p.body().child(0).html());
	}

	@Test
	public void emptytagStringTest() throws IOException{
		String html="<table/>";
		Document p = Jsoup.parse(html);

		assertEquals("table",p.body().child(0).tagName());
		//Check if normalises element from empty tag to start tag and closing tag
		assertEquals("<table></table>",p.body().child(0).outerHtml());
		assertEquals("",p.body().child(0).html());
	}

	@Test
	public void formattingtagsStringTest() throws IOException{
		String html="<p>What happens when I <b>bold text and <i>emphasize</i> some words</b></p>";
		Document p = Jsoup.parse(html);

		assertEquals("p",p.body().child(0).tagName());
		//Keeps formatting tags and doesn't discard them
		assertEquals("b",p.body().child(0).child(0).tagName());
		assertEquals("bold text and <i>emphasize</i> some words",p.body().child(0).child(0).html());
		assertEquals("i",p.body().child(0).child(0).child(0).tagName());
		assertEquals("emphasize",p.body().child(0).child(0).child(0).html());
	}

	@Test
	public void singlequotesforattrStringTest() throws IOException{
		String html="<a href='http://www.google.ca'>Single Quoted</a>";
		
		Document p = Jsoup.parse(html);

		assertEquals("a",p.body().child(0).tagName());
		//check if correctly parses attribute
		assertEquals("http://www.google.ca",p.body().child(0).attr("href"));
		//check if normalises to double quotations
		assertEquals("<a href=\"http://www.google.ca\">Single Quoted</a>",p.body().child(0).outerHtml());
	}

	//Part II: invalid HTML String Paser Tests
	@Test public void parsesUnterminatedComments() {
        String html="<html><head><title>Regular HTML file, well formed</title></head><body>"+
				"<p>Regular paragraph in the body</p>" +
				"<a href=\"http://www.google.ca\">Google</a>" +
				"<h1>Header</h1>" +
				"<div><span>Section 1</span>"+
				"<!--Comment: Let's try some sections nested in a division";
		Document doc = Jsoup.parse(html);

        Element p = doc.getElementsByTag("div").get(0);
        assertEquals("Section 1", p.text());
        Comment comment = (Comment) p.childNode(1);
        assertEquals("Comment: Let's try some sections nested in a division", comment.getData());
    }

	
	@Test
	public void plaintextStringTest() throws IOException{
		String html="Let's see how Jsoup parses this! Plain text, invalid html document.";
		Document p = Jsoup.parse(html);
		
		//Check if created head tags
		assertEquals("<head></head>",p.child(0).child(0).toString());
		//Check to see if body was created
		assertEquals("body", p.child(0).child(1).tagName());
		//Get inner html of body tag
		assertEquals("Let's see how Jsoup parses this! Plain text, invalid html document.",p.child(0).child(1).html());
	}
	
	@Test
	public void missingheadStringTest() throws IOException{
		String html="<html><body><title>Hey, I am the headless html!</title>"+
					"<p>Some paragraphs</p>"+
					"<p>Some of the parts of my head are in my body! What, again?!</p>"+
					"</body></html>";
		Document p = Jsoup.parse(html);
		
		//Check if created head tags
		assertEquals("head",p.child(0).child(0).tagName());
		
		//Check to see if body was created
		assertEquals("body", p.body().tagName());
		//Check if tags in body were parsed
		assertEquals("title",p.body().child(0).tagName());
		assertEquals("Hey, I am the headless html!",p.body().child(0).html());
		assertEquals("p",p.body().child(1).tagName());
		assertEquals("Some paragraphs",p.body().child(1).html());
	}
	
	@Test
	public void missinghtmltagStringTest() throws IOException{
		String html="<head><title>Missing html tag</title>"+
					"</head><body><p>Straight into the head and the body, no html tag</p>"+
					"</body>";
		Document p = Jsoup.parse(html);
		
		//Check if created html tags
		assertEquals("html",p.child(0).tagName());
		
		//Check if head elements were put into html tag
		assertEquals("head",p.child(0).child(0).tagName());
		//Check to see if body was created
		assertEquals("body", p.child(0).child(1).tagName());
		assertEquals("Straight into the head and the body, no html tag", p.child(0).child(1).child(0).html());
	
	}
	
	@Test
	public void missingbodyStringTest() throws IOException{
		String html="<html><head><title>Missing Body</title>"+
					"Data right in the head. Not in the body. Let's add some tags that usually go into the missing body."+
					"<p>Hello, My body is in my head. What?!</p>"+
					"<p>That should be enough<p>"+
					"<head/></html>";
		Document p = Jsoup.parse(html);
		
		//Check if head parsed
		assertEquals("head",p.child(0).child(0).tagName());
		assertEquals("title",p.head().child(0).tagName());
		//Check if body was created and elements in head were correctly put into body
		assertEquals("body",p.child(0).child(1).tagName());
		assertEquals("p",p.child(0).child(1).child(0).tagName());
		assertEquals("Hello, My body is in my head. What?!",p.child(0).child(1).child(0).html());
	}
	
	@Test
	public void interlockingtagsStringTest() throws IOException{
		String html="<div>I'm a heading!"+
					"<p>Interlocking Tags, first paragraph <a href=\"http://google.ca\"> Still in </div>"+
					"first paragraph!</p> Link To Google!</a>";
		Document p = Jsoup.parse(html);
		
		//Check if correctly parses first child in body
		assertEquals("div",p.body().child(0).tagName());
		//Check if correctly parses first nested tag
		assertEquals("p",p.body().child(0).child(0).tagName());
		//Check if correctly recognizes interlocking and replaces </div> with </a>
		assertEquals("Interlocking Tags, first paragraph <a href=\"http://google.ca\"> Still in </a>",p.body().child(0).child(0).html());
	
	}
	
	@Test
	public void wrongclosingtagStringTest() throws IOException{
		String html="<h1>Wrong closing tag name</a>";
		Document p = Jsoup.parse(html);
		
		//check if changes wrong closing tag name to the correct one
		assertEquals("h1",p.body().child(0).tagName());
		assertEquals("<h1>Wrong closing tag name</h1>",p.body().child(0).outerHtml());
	}
	
	@Test
	public void invalidclosingtagsyntaxStringTest() throws IOException{
		String html="<div>Invalid closing<div>";
		Document p = Jsoup.parse(html);
		
		//check if closes unclosed ending tag, add missing closing tag
		assertEquals("div",p.body().child(0).tagName());
		assertEquals("Invalid closing\n<div></div>",p.body().child(0).html());
		assertEquals("div",p.body().child(0).child(0).tagName());
	}
	
	@Test
	public void missingclosingtagStringTest() throws IOException{
		String html="<div>Missing closing tag";
		Document p = Jsoup.parse(html);
		
		//check if adds missing closing tag
		assertEquals("div",p.body().child(0).tagName());
		assertEquals("<div>\n Missing closing tag\n</div>",p.body().child(0).outerHtml());
	}
	
	@Test
	public void unclosedclosingtagStringTest() throws IOException{
		String html="<div>Unclosed closing tag</div";
		Document p = Jsoup.parse(html);
		
		//check if closes unclosed ending tag
		assertEquals("div",p.body().child(0).tagName());
		assertEquals("<div>\n Unclosed closing tag\n</div>",p.body().child(0).outerHtml());
	}
	
	@Test
	public void contentinemptytagStringTest() throws IOException{
		String html="<div Content inside again, different closing mechanism />";
		Document p = Jsoup.parse(html);
		
		//check if changes content inside start tag to attributes
		assertEquals("div",p.body().child(0).tagName());
		assertEquals("",p.body().child(0).attr("content"));
		assertEquals("",p.body().child(0).attr("inside"));
		assertEquals("",p.body().child(0).attr("again,"));
		assertEquals("",p.body().child(0).attr("different"));
		assertEquals("",p.body().child(0).attr("closing"));
		assertEquals("",p.body().child(0).attr("mechanism"));
	}
	
	@Test
	public void missingclosingquoteonattributeStringTest() throws IOException{
		String html="<a href=\"http://www.google.ca\" id=\"missingendquote>No space between attributes</a>";
		Document p = Jsoup.parse(html);
		
		//Check if correctly discards tag with no closing quote on one attribute value
		assertEquals("",p.body().html());
	}
	
	@Test
	public void noquotationsforattrvaluesStringTest() throws IOException{
		String html="<a href=http://www.google.ca id=noquotesforvalue>No quotes for attribute value</a>";
		Document p = Jsoup.parse(html);
		
		//Check if correctly parses attribute values even with no quotations
		assertEquals("a",p.body().child(0).tagName());
		assertEquals("http://www.google.ca",p.body().child(0).attr("href"));
		assertEquals("noquotesforvalue",p.body().child(0).attr("id"));
	}
	
	@Test
	public void nospacesbetweenattributesStringTest() throws IOException{
		String html="<a href=\"http://www.google.ca\"id=\"nospacebetweenattributes\">No space between attributes</a>";
		Document p = Jsoup.parse(html);
		
		//Check if correctly parses attribute values even with no spaces between attributes
		assertEquals("a",p.body().child(0).tagName());
		assertEquals("http://www.google.ca",p.body().child(0).attr("href"));
		assertEquals("nospacebetweenattributes",p.body().child(0).attr("id"));
	}
	
	
	/*
	 * 3.    static Document 	parse(String html, String baseUri)
     			Parse HTML into a Document.
     */
    @Test public void BaseTagsStringBaseURITest() {
        // only listen to the first base href
        String h = "<a href=level1>#</a>"+
        			"<base href='/level2/'>"+
        			"<a href='level3'>#</a>"+
        			"<base href='http://bar'>"+
        			"<a href=/level4>#</a>";
        Document doc = Jsoup.parse(h, "http://SENG437.com/");
        assertEquals("http://SENG437.com/level2/", doc.baseUri()); // gets set once, so doc and descendants have first only

        Elements anchors = doc.getElementsByTag("a");
        assertEquals(3, anchors.size());

        assertEquals("http://SENG437.com/level2/", anchors.get(0).baseUri());
        assertEquals("http://SENG437.com/level2/", anchors.get(1).baseUri());
        assertEquals("http://SENG437.com/level2/", anchors.get(2).baseUri());

        assertEquals("http://SENG437.com/level2/level1", anchors.get(0).absUrl("href"));
        assertEquals("http://SENG437.com/level2/level3", anchors.get(1).absUrl("href"));
        assertEquals("http://SENG437.com/level4", anchors.get(2).absUrl("href"));
    }
    
    @Test public void BaseTagsWithoutHrefStringBaseURITest() {
        String h = "<body><a href=/org.jsoup.test>Test</a></body>";
        Document doc = Jsoup.parse(h, "http://SENG437.com/");
        Element a = doc.select("a").first();
        assertEquals("/org.jsoup.test", a.attr("href"));
        assertEquals("http://SENG437.com/org.jsoup.test", a.attr("abs:href"));
    }	
    
    /* Target 4:
     *  	 static Document 	parse(String html, String baseUri, Parser parser)
    		 	Parse HTML into a Document, using the provided Parser.
     */	
	@Test
	public void htmlParserStringTest() throws IOException{
		String html="<html><head><title>Regular HTML file, well formed</title></head><body>"+
				"<p>Regular paragraph in the body</p>" +
				"<h1>Header</h1>" +
				"<!--Comment: Let's try some sections nested here-->"+
				"<div><span>Section 1</span><span>Section 2</span></div></html>";
		Parser parser = Parser.htmlParser();
		Document p = Jsoup.parse(html,"http://SENG437.com",parser);

		assertEquals("html",p.child(0).tagName());
		assertEquals("head",p.child(0).child(0).tagName());
		assertEquals("title",p.head().child(0).tagName());
		assertEquals("Regular HTML file, well formed",p.head().child(0).html());

		assertEquals("body",p.child(0).child(1).tagName());
		assertEquals("p",p.body().child(0).tagName());
		assertEquals("Regular paragraph in the body",p.body().child(0).html());
		assertEquals("h1",p.body().child(1).tagName());
		assertEquals("Header",p.body().child(1).html());
		//	Check if 3rd element is div, discarding the comment
		assertEquals("div",p.body().child(2).tagName());
		assertEquals("span",p.body().child(2).child(0).tagName());
		assertEquals("Section 1",p.body().child(2).child(0).html());

        List<ParseError> errors = parser.getErrors();
        assertEquals(0, errors.size());

	}
	  
    
    @Test public void tracksErrors() {
        String html = "<p>One</p href='no'><!DOCTYPE html>&arrgh;<font /><br /><foo";
        Parser parser = Parser.htmlParser().setTrackErrors(500);
        Jsoup.parse(html, "http://SENG437.com", parser);
        
        List<ParseError> errors = parser.getErrors();
        assertEquals(5, errors.size());
        assertEquals("20: Attributes incorrectly present on end tag", errors.get(0).toString());
        assertEquals("35: Unexpected token [Doctype] when in state [InBody]", errors.get(1).toString());
        assertEquals("36: Invalid character reference: invalid named referenece 'arrgh'", errors.get(2).toString());
        assertEquals("50: Self closing flag not acknowledged", errors.get(3).toString());
        assertEquals("61: Unexpectedly reached end of file (EOF) in input state [TagName]", errors.get(4).toString());
    }
    
    
    @Test
    public void XmlParser() {
        String xml = "<doc><val>A<val>B</val></bar>C</doc>";
        Document doc = Jsoup.parse(xml, "http://SENG437.com/", Parser.xmlParser());
        assertEquals("<doc><val>A<val>B</val>C</val></doc>",
                PATTERN1.matcher(doc.html()).replaceAll(""));
    }
    
    @Test
    public void NoSelfClosingKnownTags() {
        Document html = Jsoup.parse("<br>A</br>");
        assertEquals("<br />A\n<br />", html.body().html());

        Document xml = Jsoup.parse("<br>A</br>", "", Parser.xmlParser());
        assertEquals("<br>A</br>", xml.html());
    }
    
    @Test public void XmlDeclaration() {
        String html = "<?xml encoding='UTF-8' ?><body>One</body><!-- comment -->";
        Document doc = Jsoup.parse(html, "", Parser.xmlParser());
        assertEquals("<?xml encoding='UTF-8' ?> <body> One </body> <!-- comment -->",
                StringUtil.normaliseWhitespace(doc.outerHtml()));
        assertEquals("#declaration", doc.childNode(0).nodeName());
        assertEquals("#comment", doc.childNode(2).nodeName());
    }
    
// --Commented out by Inspection START (3/20/13 10:02 AM):
//	/* Target:
//	 *	1.    static Document 	parse(InputStream in, String charsetName, String baseUri, Parser parser)
//    			Read an input stream, and parse it to a Document.
//	 */
//    //@Test
//    public static void ParserToStream() throws IOException, URISyntaxException {
//        File xmlFile = new File(XmlTreeBuilder.class.getResource("testres/validhtml/org.jsoup.test.xml").toURI());
//        Document doc;
//        try (InputStream inStream = new FileInputStream(xmlFile)) {
//            doc = Jsoup.parse(inStream, null, "http://SENG437.com", Parser.xmlParser());
//            assertEquals("<doc><val>A<val>B</val>C</val></doc>",
//                    PATTERN1.matcher(doc.html()).replaceAll(""));
//        }
//    }
// --Commented out by Inspection STOP (3/20/13 10:02 AM)

    
    
    
    
// --Commented out by Inspection START (3/20/13 10:02 AM):
//    /* Target 5:
//     * static Document 	parse(URL url, int timeoutMillis)
//          Fetch a URL, and parse it as HTML.
//
//     */
//    //@Test 
//    public static void ParseUrlwithinTimeout() throws IOException{
//        String url = "http://web.mit.edu/esg/www_pre2005/sample.html"; // no trailing / to force redir
//        Document doc = Jsoup.parse(new URL(url), 10*1000);
//        assertEquals("This is an example of HTML", doc.title());
//        assertEquals("This is a HUGE heading",doc.body().child(0).html());
//        assertEquals("This is a BIG heading",doc.body().child(1).html());
//        assertEquals("This is a LARGE heading",doc.body().child(2).html());
//        assertEquals("HTML stands for HyperText Markup Language. HTML documents are"+
//        			 " plain-text files that can be created using any text editor (such as"+
//        			 " emacs).",doc.body().child(3).html());
//    }	
// --Commented out by Inspection STOP (3/20/13 10:02 AM)
    	
    
    /* Target:
     * 6.    static Document 	parseBodyFragment(String bodyHtml)
    			Parse a fragment of HTML, with the assumption that it forms the body of the HTML.
     */
    @Test public void InlineTags() {
        String h = "<p><cust>Test</cust></p><p><cust><cust>Test</cust></cust></p>";
        Document doc = Jsoup.parseBodyFragment(h);
        String out = doc.body().html();
        assertEquals(h, PATTERN1.matcher(out).replaceAll(""));
    }
    @Test public void CommentsInTable() {
        String html = "<table><tr><td>text</td><!-- Comment --></tr></table>";
        Document node = Jsoup.parseBodyFragment(html);
        assertEquals("<html><head></head><body><table><tbody><tr><td>text</td>" +
                "<!-- Comment --></tr></tbody></table></body></html>", PATTERN1.matcher(node.outerHtml()).replaceAll(""));
    }
    @Test public void QuotesInCommentsInScripts() {
        String html = "<script>\n" +
                "  <!--\n" +
                "    document.write('</scr' + 'ipt>');\n" +
                "  // -->\n" +
                "</script>";
        Document node = Jsoup.parseBodyFragment(html);
        assertEquals("<script>\n" +
                "  <!--\n" +
                "    document.write('</scr' + 'ipt>');\n" +
                "  // -->\n" +
                "</script>", node.body().html());
    }

    
    
    /*
     * 7.    static Document 	parseBodyFragment(String bodyHtml, String baseUri)
    			Parse a fragment of HTML, with the assumption that it forms the body of the HTML.
    */
    @Test public void parsesBodyFragmentTest() {
        String h = "<!-- comment --><p><a href='abc'>One</a></p>";
        Document doc = Jsoup.parseBodyFragment(h, "http://SENG437.com");
        assertEquals("<body><!-- comment --><p><a href=\"abc\">One</a></p></body>", PATTERN1.matcher(doc.body().outerHtml()).replaceAll(""));
        assertEquals("http://SENG437.com/abc", doc.select("a").first().absUrl("href"));
    }

  
    
    @Test public void tracksLimitedErrors() {
        String html = "<p>One</p href='no'><!DOCTYPE html>&arrgh;<font /><br /><foo";
        Parser parser = Parser.htmlParser().setTrackErrors(3);
        parser.parseInput(html, "http://SENG437.com");

        List<ParseError> errors = parser.getErrors();
        assertEquals(3, errors.size());
        assertEquals("20: Attributes incorrectly present on end tag", errors.get(0).toString());
        assertEquals("35: Unexpected token [Doctype] when in state [InBody]", errors.get(1).toString());
        assertEquals("36: Invalid character reference: invalid named referenece 'arrgh'", errors.get(2).toString());
    }
    
  //Jsoup.clean() tests
	
    //Whitelist.none()
  	@Test
  	public void cleanNoneWhitelistValidTagsTest() throws IOException{

  		String html = "Some text, making sure the cleaner doesn't remove any stuff.";
  		String p = Jsoup.clean(html, Whitelist.none());
  		//check if clean() keeps all text, removing nothing
  		assertEquals("Some text, making sure the cleaner doesn't remove any stuff.",p);
  	}
  	
  	@Test
  	public void isvalidNoneWhitelistValidTagsTest() throws IOException{

  		String html = "Some text, making sure the cleaner doesn't remove any stuff.";
  		//checks if there is no html tags, and is valid.
  		assertTrue(Jsoup.isValid(html, Whitelist.none()));
  	}
  	
  	@Test
  	public void cleanNoneWhitelistInvalidTagsTest() throws IOException{

  		String html = "<head><title>A Text Title.</title></head><body><p> A paragraph of text.</p></body>";
  		String p = Jsoup.clean(html, Whitelist.none());
  		//check if clean() removes the html tags and keeps the text.
  		assertEquals("A Text Title. A paragraph of text.",p);
  	}
  	
  	@Test
  	public void isvalidNoneWhitelistInvalidTagsTest() throws IOException{

  		String html = "<head><title>A Text Title.</title></head><body><p> A paragraph of text.</p></body>";
  		//checks if there is no html tags, and is valid.
  		assertFalse(Jsoup.isValid(html, Whitelist.none()));
  	}
  	
  	//Whitelist.simpleText()
  	@Test
  	public void cleanSimpleTextWhitelistValidTagsTest() throws IOException{

  		String html = "Some \n<b>text</b>, \n<em>making</em> \n<i>sure</i> the \n<strong>cleaner</strong> \n<u>doesn't</u> remove any stuff.";
  		String p = Jsoup.clean(html, Whitelist.simpleText());
  		//check if clean() keeps all text, removing nothing
  		assertEquals("Some \n<b>text</b>, \n<em>making</em> \n<i>sure</i> the \n<strong>cleaner</strong> \n<u>doesn't</u> remove any stuff.",p);
  	}
  	
  	@Test
  	public void isvalidSimpleTextWhitelistValidTagsTest() throws IOException{

  		String html = "Some \n<B>text</B>, \n<em>making</em> \n<i>sure</i> the \n<strong>cleaner</strong> \n<u>doesn't</u> remove any stuff.";
  		//checks if there is no html tags, and is valid.
  		assertTrue(Jsoup.isValid(html, Whitelist.simpleText()));
  	}
  	
  	@Test
  	public void cleanSimpleTextWhitelistInvalidTagsTest() throws IOException{

  		String html = "<head>A Head</head> <body>A Body</body> \n<b>text</b>, \n<em>making</em> \n<i>sure</i> the \n<strong>cleaner</strong> \n<u>doesn't</u> remove any stuff.";
  		String p = Jsoup.clean(html, Whitelist.simpleText());
  		//check if clean() keeps all valid html text, removing invalid html
  		assertEquals("A Head A Body \n<b>text</b>, \n<em>making</em> \n<i>sure</i> the \n<strong>cleaner</strong> \n<u>doesn't</u> remove any stuff.",p);
  	}
  	
// --Commented out by Inspection START (3/20/13 10:02 AM):
//  	//@Test
//  	public static void isvalidSimpleTextWhitelistInvalidTagsTest() {
//
//  		String html = "<head>A Head</head> <body>A Body</body> \n<b>text</b>, \n<em>making</em> \n<i>sure</i> the \n<strong>cleaner</strong> \n<u>doesn't</u> remove any stuff.";
//  		//checks if there is invalid html tags, thus will be invalid
//  		assertFalse(Jsoup.isValid(html, Whitelist.simpleText()));
//  	}
// --Commented out by Inspection STOP (3/20/13 10:02 AM)
  	
  	//Whitelist.basic()
  	@Test
  	public void cleanBasicWhitelistValidTagsTest() throws IOException{

  		String html = "Some" +
  				"<b>text</b>," +
  				"<em>making</em>" +
  				"<i>sure</i> the" +
  				"<strong>cleaner</strong>" +
  				"<u>doesn't</u> remove any stuff." +
  				"<a href=\"https://www.google.ca\">google</a>" +
  				"<blockquote cite=\"http://www.shakespeare.com\">2B|~2B</blockquote>" +
  				"A line break" +
  				"<br />" +
  				"<cite>Shakespeare</cite>" +
  				"<code>Hello World</code>" +
  				"<dd>A Definition</dd>" +
  				"<dl>Definition list</dl>" +
  				"<li>List</li>" +
  				"<ol>Ordered List</ol>" +
  				"<p>Paragraph</p>" +
  				"<pre>preformatted</pre>" +
  				"<q cite='google.ca'>quote</q>" +
  				"<small>smalltext</small>" +
  				"<strike>you're out!</strike>" +
  				"<sub>zero</sub>" +
  				"<sup>erman</sup>" +
  				"<ul>unordered</ul>";
  		
  		String p = Jsoup.clean(html, "google.ca", Whitelist.basic(),new Document.OutputSettings().prettyPrint(false));
  		
  		//check if clean() keeps all text, removing nothing and adds enforced attributes
  		assertEquals("Some" +
  				"<b>text</b>," +
  				"<em>making</em>" +
  				"<i>sure</i> the" +
  				"<strong>cleaner</strong>" +
  				"<u>doesn't</u> remove any stuff." +
  				"<a href=\"https://www.google.ca\" rel=\"nofollow\">google</a>" +
  				"<blockquote cite=\"http://www.shakespeare.com\">2B|~2B</blockquote>" +
  				"A line break" +
  				"<br />" +
  				"<cite>Shakespeare</cite>" +
  				"<code>Hello World</code>" +
  				"<dd>A Definition</dd>" +
  				"<dl>Definition list</dl>" +
  				"<li>List</li>" +
  				"<ol>Ordered List</ol>" +
  				"<p>Paragraph</p>" +
  				"<pre>preformatted</pre>" +
  				"<q cite=\"google.ca\">quote</q>" +
  				"<small>smalltext</small>" +
  				"<strike>you're out!</strike>" +
  				"<sub>zero</sub>" +
  				"<sup>erman</sup>" +
  				"<ul>unordered</ul>",p);
  	}
  	
  	@Test
  	public void isvalidBasicWhitelistValidTagsTest() throws IOException{

  		String html = "Some" +
  				"<b>text</b>," +
  				"<em>making</em>" +
  				"<i>sure</i> the" +
  				"<strong>cleaner</strong>" +
  				"<u>doesn't</u> remove any stuff." +
  				"<a href=\"https://www.google.ca\">google</a>" +
  				"<blockquote cite=\"http://www.shakespeare.com\">2B|~2B</blockquote>" +
  				"A line break" +
  				"<br />" +
  				"<cite>Shakespeare</cite>" +
  				"<code>Hello World</code>" +
  				"<dd>A Definition</dd>" +
  				"<dl>Definition list</dl>" +
  				"<li>List</li>" +
  				"<ol>Ordered List</ol>" +
  				"<p>Paragraph</p>" +
  				"<pre>preformatted</pre>" +
  				"<q cite='google.ca'>quote</q>" +
  				"<small>smalltext</small>" +
  				"<strike>you're out!</strike>" +
  				"<sub>zero</sub>" +
  				"<sup>erman</sup>" +
  				"<ul>unordered</ul>";
  		//checks if there is no html tags, and is valid.
  		assertTrue(Jsoup.isValid(html, Whitelist.basic()));
  	}
  	
  	@Test
  	public void cleanBasicWhitelistInvalidTagsTest() throws IOException{

  		String html = "Some" +
  				"<b>text</b>," +
  				"<em>making</em>" +
  				"<i>sure</i> the" +
  				"<strong>cleaner</strong>" +
  				"<u>doesn't</u> remove any stuff." +
  				"<a href=\"https://www.google.ca\">google</a>" +
  				"<blockquote cite=\"http://www.shakespeare.com\">2B|~2B</blockquote>" +
  				"A line break" +
  				"<br />" +
  				"<cite>Shakespeare</cite>" +
  				"<code>Hello World</code>" +
  				"<dd>A Definition</dd>" +
  				"<dl>Definition list</dl>" +
  				"<li>List</li>" +
  				"<ol>Ordered List</ol>" +
  				"<p>Paragraph</p>" +
  				"<pre>preformatted</pre>" +
  				"<q cite='google.ca'>quote</q>" +
  				"<small>smalltext</small>" +
  				"<strike>you're out!</strike>" +
  				"<sub>zero</sub>" +
  				"<sup>erman</sup>" +
  				"<ul>unordered</ul>" +
  				//Tags that are not allowed for the current whitelist, should be removed or modified
  				"<malicious>I should be removed</malicious>" +
  				"<img>A tag from a basicWithImages Whitelist</img>" +
  				"<a name=\"thisattributeisnotallowed\">badlink</a>" +
  				"<a href=\"badprotocal://google.ca\">badprotocol</a>";
  		
  		String p = Jsoup.clean(html, "google.ca", Whitelist.basic(),new Document.OutputSettings().prettyPrint(false));
  		//check if clean() keeps all valid html text, removing invalid html
  		assertEquals("Some" +
  				"<b>text</b>," +
  				"<em>making</em>" +
  				"<i>sure</i> the" +
  				"<strong>cleaner</strong>" +
  				"<u>doesn't</u> remove any stuff." +
  				"<a href=\"https://www.google.ca\" rel=\"nofollow\">google</a>" +
  				"<blockquote cite=\"http://www.shakespeare.com\">2B|~2B</blockquote>" +
  				"A line break" +
  				"<br />" +
  				"<cite>Shakespeare</cite>" +
  				"<code>Hello World</code>" +
  				"<dd>A Definition</dd>" +
  				"<dl>Definition list</dl>" +
  				"<li>List</li>" +
  				"<ol>Ordered List</ol>" +
  				"<p>Paragraph</p>" +
  				"<pre>preformatted</pre>" +
  				"<q cite=\"google.ca\">quote</q>" +
  				"<small>smalltext</small>" +
  				"<strike>you're out!</strike>" +
  				"<sub>zero</sub>" +
  				"<sup>erman</sup>" +
  				"<ul>unordered</ul>" +
  				//Tags that are not allowed for the current whitelist and are removed or modified
  				"I should be removed" +
  				"A tag from a basicWithImages Whitelist" +
  				"<a rel=\"nofollow\">badlink</a>" +
  				"<a rel=\"nofollow\">badprotocol</a>",p);
  	}
  	
  	@Test
  	public void isvalidBasicWhitelistInvalidTagsTest() throws IOException{

  		String html = "Some" +
  				"<b>text</b>," +
  				"<em>making</em>" +
  				"<i>sure</i> the" +
  				"<strong>cleaner</strong>" +
  				"<u>doesn't</u> remove any stuff." +
  				"<a href=\"https://www.google.ca\">google</a>" +
  				"<blockquote cite=\"http://www.shakespeare.com\">2B|~2B</blockquote>" +
  				"A line break" +
  				"<br />" +
  				"<cite>Shakespeare</cite>" +
  				"<code>Hello World</code>" +
  				"<dd>A Definition</dd>" +
  				"<dl>Definition list</dl>" +
  				"<li>List</li>" +
  				"<ol>Ordered List</ol>" +
  				"<p>Paragraph</p>" +
  				"<pre>preformatted</pre>" +
  				"<q cite='google.ca'>quote</q>" +
  				"<small>smalltext</small>" +
  				"<strike>you're out!</strike>" +
  				"<sub>zero</sub>" +
  				"<sup>erman</sup>" +
  				"<ul>unordered</ul>" +
  				//Tags that are not allowed for the current whitelist, should be removed or modified
  				"<malicious>I should be removed</malicious>" +
  				"<img>A tag from a basicWithImages Whitelist</img>" +
  				"<a name=\"thisattributeisnotallowed\">badlink</a>" +
  				"<a href=\"badprotocal://google.ca\">badprotocol</a>";
  		//checks if there is invalid html tags, thus will be invalid
  		assertFalse(Jsoup.isValid(html, Whitelist.basic()));
  	}
  	
  //Whitelist.basicWithImages()
  	@Test
  	public void cleanBasicWithImagesWhitelistValidTagsTest() throws IOException{

  		String html = "Some" +
  				"<b>text</b>," +
  				"<em>making</em>" +
  				"<i>sure</i> the" +
  				"<strong>cleaner</strong>" +
  				"<u>doesn't</u> remove any stuff." +
  				"<a href=\"https://www.google.ca\">google</a>" +
  				"<blockquote cite=\"http://www.shakespeare.com\">2B|~2B</blockquote>" +
  				"A line break" +
  				"<br />" +
  				"<cite>Shakespeare</cite>" +
  				"<code>Hello World</code>" +
  				"<dd>A Definition</dd>" +
  				"<dl>Definition list</dl>" +
  				"<li>List</li>" +
  				"<ol>Ordered List</ol>" +
  				"<p>Paragraph</p>" +
  				"<pre>preformatted</pre>" +
  				"<q cite='google.ca'>quote</q>" +
  				"<small>smalltext</small>" +
  				"<strike>you're out!</strike>" +
  				"<sub>zero</sub>" +
  				"<sup>erman</sup>" +
  				"<ul>unordered</ul>" +
  				//Image Tag with allowed attributes and protocols
  				"<img align=\"left\" alt=\"top\" height=\"100\" src=\"http://google.ca\" title=\"img01\" width=\"100\" />";
  		
  		String p = Jsoup.clean(html, "google.ca", Whitelist.basicWithImages(),new Document.OutputSettings().prettyPrint(false));
  		
  		//check if clean() keeps all text, removing nothing and adds enforced attributes
  		assertEquals("Some" +
  				"<b>text</b>," +
  				"<em>making</em>" +
  				"<i>sure</i> the" +
  				"<strong>cleaner</strong>" +
  				"<u>doesn't</u> remove any stuff." +
  				"<a href=\"https://www.google.ca\" rel=\"nofollow\">google</a>" +
  				"<blockquote cite=\"http://www.shakespeare.com\">2B|~2B</blockquote>" +
  				"A line break" +
  				"<br />" +
  				"<cite>Shakespeare</cite>" +
  				"<code>Hello World</code>" +
  				"<dd>A Definition</dd>" +
  				"<dl>Definition list</dl>" +
  				"<li>List</li>" +
  				"<ol>Ordered List</ol>" +
  				"<p>Paragraph</p>" +
  				"<pre>preformatted</pre>" +
  				"<q cite=\"google.ca\">quote</q>" +
  				"<small>smalltext</small>" +
  				"<strike>you're out!</strike>" +
  				"<sub>zero</sub>" +
  				"<sup>erman</sup>" +
  				"<ul>unordered</ul>" +
  				//Image Tag with allowed attributes and protocols
  				"<img align=\"left\" alt=\"top\" height=\"100\" src=\"http://google.ca\" title=\"img01\" width=\"100\" />",p);
  	}
  	
  	@Test
  	public void isvalidBasicWithImagesWhitelistValidTagsTest() throws IOException{

  		String html = "Some" +
  				"<b>text</b>," +
  				"<em>making</em>" +
  				"<i>sure</i> the" +
  				"<strong>cleaner</strong>" +
  				"<u>doesn't</u> remove any stuff." +
  				"<a href=\"https://www.google.ca\">google</a>" +
  				"<blockquote cite=\"http://www.shakespeare.com\">2B|~2B</blockquote>" +
  				"A line break" +
  				"<br />" +
  				"<cite>Shakespeare</cite>" +
  				"<code>Hello World</code>" +
  				"<dd>A Definition</dd>" +
  				"<dl>Definition list</dl>" +
  				"<li>List</li>" +
  				"<ol>Ordered List</ol>" +
  				"<p>Paragraph</p>" +
  				"<pre>preformatted</pre>" +
  				"<q cite='google.ca'>quote</q>" +
  				"<small>smalltext</small>" +
  				"<strike>you're out!</strike>" +
  				"<sub>zero</sub>" +
  				"<sup>erman</sup>" +
  				"<ul>unordered</ul>" +
  				//Image Tag with allowed attributes and protocols
  				"<img align=\"left\" alt=\"top\" height=\"100\" src=\"http://google.ca\" title=\"img01\" width=\"100\" />";
  		//checks if there is no html tags, and is valid.
  		assertTrue(Jsoup.isValid(html, Whitelist.basicWithImages()));
  	}
  	
  	@Test
  	public void cleanBasicWithImagesWhitelistInvalidTagsTest() throws IOException{

  		String html = "Some" +
  				"<b>text</b>," +
  				"<em>making</em>" +
  				"<i>sure</i> the" +
  				"<strong>cleaner</strong>" +
  				"<u>doesn't</u> remove any stuff." +
  				"<a href=\"https://www.google.ca\">google</a>" +
  				"<blockquote cite=\"http://www.shakespeare.com\">2B|~2B</blockquote>" +
  				"A line break" +
  				"<br />" +
  				"<cite>Shakespeare</cite>" +
  				"<code>Hello World</code>" +
  				"<dd>A Definition</dd>" +
  				"<dl>Definition list</dl>" +
  				"<li>List</li>" +
  				"<ol>Ordered List</ol>" +
  				"<p>Paragraph</p>" +
  				"<pre>preformatted</pre>" +
  				"<q cite='google.ca'>quote</q>" +
  				"<small>smalltext</small>" +
  				"<strike>you're out!</strike>" +
  				"<sub>zero</sub>" +
  				"<sup>erman</sup>" +
  				"<ul>unordered</ul>" +
  				//Image Tag with allowed attributes and protocols
  				"<img align=\"left\" alt=\"top\" height=\"100\" src=\"http://google.ca\" title=\"img01\" width=\"100\" />" +
  				//Disallowed tags and img tags with disallowed attributes
  				"<malicious>tag</malicious>" +
  				"<img name=\"notallowed\" id=\"image1\" align=\"left\" />";
  		
  		String p = Jsoup.clean(html, "google.ca", Whitelist.basicWithImages(),new Document.OutputSettings().prettyPrint(false));
  		//check if clean() keeps all valid html text, removing invalid html
  		assertEquals("Some" +
  				"<b>text</b>," +
  				"<em>making</em>" +
  				"<i>sure</i> the" +
  				"<strong>cleaner</strong>" +
  				"<u>doesn't</u> remove any stuff." +
  				"<a href=\"https://www.google.ca\" rel=\"nofollow\">google</a>" +
  				"<blockquote cite=\"http://www.shakespeare.com\">2B|~2B</blockquote>" +
  				"A line break" +
  				"<br />" +
  				"<cite>Shakespeare</cite>" +
  				"<code>Hello World</code>" +
  				"<dd>A Definition</dd>" +
  				"<dl>Definition list</dl>" +
  				"<li>List</li>" +
  				"<ol>Ordered List</ol>" +
  				"<p>Paragraph</p>" +
  				"<pre>preformatted</pre>" +
  				"<q cite=\"google.ca\">quote</q>" +
  				"<small>smalltext</small>" +
  				"<strike>you're out!</strike>" +
  				"<sub>zero</sub>" +
  				"<sup>erman</sup>" +
  				"<ul>unordered</ul>" +
  				//Image Tag with allowed attributes and protocols
  				"<img align=\"left\" alt=\"top\" height=\"100\" src=\"http://google.ca\" title=\"img01\" width=\"100\" />" +
				//Disallowed tags and img tags with disallowed attributes
  				"tag" +
				"<img align=\"left\" />",p);
  	}
  	
  	@Test
  	public void isvalidBasicWithImagesWhitelistInvalidTagsTest() throws IOException{

  		String html = "Some" +
  				"<b>text</b>," +
  				"<em>making</em>" +
  				"<i>sure</i> the" +
  				"<strong>cleaner</strong>" +
  				"<u>doesn't</u> remove any stuff." +
  				"<a href=\"https://www.google.ca\">google</a>" +
  				"<blockquote cite=\"http://www.shakespeare.com\">2B|~2B</blockquote>" +
  				"A line break" +
  				"<br />" +
  				"<cite>Shakespeare</cite>" +
  				"<code>Hello World</code>" +
  				"<dd>A Definition</dd>" +
  				"<dl>Definition list</dl>" +
  				"<li>List</li>" +
  				"<ol>Ordered List</ol>" +
  				"<p>Paragraph</p>" +
  				"<pre>preformatted</pre>" +
  				"<q cite='google.ca'>quote</q>" +
  				"<small>smalltext</small>" +
  				"<strike>you're out!</strike>" +
  				"<sub>zero</sub>" +
  				"<sup>erman</sup>" +
  				"<ul>unordered</ul>" +
  				//Image Tag with allowed attributes and protocols
  				"<img align=\"left\" alt=\"top\" height=\"100\" src=\"http://google.ca\" title=\"img01\" width=\"100\" />" +
  				//Disallowed tags and img tags with disallowed attributes
  				"<malicious>tag</malicious>" +
  				"<img name=\"notallowed\" id=\"image1\" align=\"left\" />";
  		//checks if there is invalid html tags, thus will be invalid
  		assertFalse(Jsoup.isValid(html, Whitelist.basicWithImages()));
  	}
  	
  //Whitelist.relaxed()
  	@Test
  	public void cleanRelaxedWhitelistValidTagsTest() throws IOException{

  		String html = "Some" +
  				"<b>text</b>," +
  				"<em>making</em>" +
  				"<i>sure</i> the" +
  				"<strong>cleaner</strong>" +
  				"<u>doesn't</u> remove any stuff." +
  				"<a href=\"https://www.google.ca\">google</a>" +
  				"<blockquote cite=\"http://www.shakespeare.com\">2B|~2B</blockquote>" +
  				"A line break" +
  				"<br />" +
  				"<cite>Shakespeare</cite>" +
  				"<code>Hello World</code>" +
  				"<dd>A Definition</dd>" +
  				"<dl>Definition list</dl>" +
  				"<li>List</li>" +
  				//Added attributes to ol
  				"<ol start=\"beginning\" type=\"linked\">Ordered List</ol>" +
  				"<p>Paragraph</p>" +
  				"<pre>preformatted</pre>" +
  				"<q cite=\"http://www.google.ca\">quote</q>" +
  				"<small>smalltext</small>" +
  				"<strike>you're out!</strike>" +
  				"<sub>zero</sub>" +
  				"<sup>erman</sup>" +
  				//Added attributes to ul
  				"<ul type=\"chaos\">unordered</ul>" +
  				"<img align=\"left\" alt=\"top\" height=\"100\" src=\"http://google.ca\" title=\"img01\" width=\"100\" />" +
  				//More tags and attributes allowed in relaxed mode
  				
  				"<div>division</div>" +
  				"<h1>header1</h1>" +
  				"<h2>header1</h2>" +
  				"<h3>header1</h3>" +
  				"<h4>header1</h4>" +
  				"<h5>header1</h5>" +
  				"<h6>header1</h6>" +
	  			//Table
				"<table summary=\"data\" width=\"5\">" +
				"<caption>sentence</caption>" +
				"<colgroup span=\"5\" width=\"5\">" +
					"<col span=\"5\" width=\"5\" />" +
				"</colgroup>" +
				"<thead>" +
					"<tr>" +
						"<th abbr=\"A\" axis=\"X\" colspan=\"5\" rowspan=\"1\" width=\"5\" scope=\"1\">theader</th>" +
					"</tr>" +
				"</thead>" +
					"<tbody>" +
						"<tr>" +
							"<td abbr=\"A\" axis=\"X\" colspan=\"5\" rowspan=\"1\" width=\"5\">tabledata</td>" +
						"</tr>" +
					"</tbody>" +
				"<tfoot>foottable</tfoot>" +
				"</table>";
  		
  		String p = Jsoup.clean(html, "google.ca", Whitelist.relaxed(),new Document.OutputSettings().prettyPrint(false));
  		
  		//check if clean() keeps all text, removing nothing and adds enforced attributes
  		assertEquals("Some" +
  				"<b>text</b>," +
  				"<em>making</em>" +
  				"<i>sure</i> the" +
  				"<strong>cleaner</strong>" +
  				"<u>doesn't</u> remove any stuff." +
  				"<a href=\"https://www.google.ca\">google</a>" +
  				"<blockquote cite=\"http://www.shakespeare.com\">2B|~2B</blockquote>" +
  				"A line break" +
  				"<br />" +
  				"<cite>Shakespeare</cite>" +
  				"<code>Hello World</code>" +
  				"<dd>A Definition</dd>" +
  				"<dl>Definition list</dl>" +
  				"<li>List</li>" +
  				//Added attributes to ol
				"<ol start=\"beginning\" type=\"linked\">Ordered List</ol>" +
  				"<p>Paragraph</p>" +
  				"<pre>preformatted</pre>" +
  				"<q cite=\"http://www.google.ca\">quote</q>" +
  				"<small>smalltext</small>" +
  				"<strike>you're out!</strike>" +
  				"<sub>zero</sub>" +
  				"<sup>erman</sup>" +
  				//Added attributes to ul
				"<ul type=\"chaos\">unordered</ul>" +
  				"<img align=\"left\" alt=\"top\" height=\"100\" src=\"http://google.ca\" title=\"img01\" width=\"100\" />" +
  				//More tags and attributes allowed in relaxed mode
				"<div>division</div>" +
				"<h1>header1</h1>" +
				"<h2>header1</h2>" +
				"<h3>header1</h3>" +
				"<h4>header1</h4>" +
				"<h5>header1</h5>" +
				"<h6>header1</h6>" +
				//Table
				"<table summary=\"data\" width=\"5\">" +
				"<caption>sentence</caption>" +
				"<colgroup span=\"5\" width=\"5\">" +
					"<col span=\"5\" width=\"5\" />" +
				"</colgroup>" +
				"<thead>" +
					"<tr>" +
					"<th abbr=\"A\" axis=\"X\" colspan=\"5\" rowspan=\"1\" width=\"5\" scope=\"1\">theader</th>" +
					"</tr>" +
				"</thead>" +
					"<tbody>" +
						"<tr>" +
							"<td abbr=\"A\" axis=\"X\" colspan=\"5\" rowspan=\"1\" width=\"5\">tabledata</td>" +
						"</tr>" +
					"</tbody>" +
				"<tfoot>foottable</tfoot>" +
				"</table>",p);
  	}
  	
  	@Test
  	public void isvalidRelaxedWhitelistValidTagsTest() throws IOException{

  		String html = "Some" +
  				"<b>text</b>," +
  				"<em>making</em>" +
  				"<i>sure</i> the" +
  				"<strong>cleaner</strong>" +
  				"<u>doesn't</u> remove any stuff." +
  				"<a href=\"https://www.google.ca\">google</a>" +
  				"<blockquote cite=\"http://www.shakespeare.com\">2B|~2B</blockquote>" +
  				"A line break" +
  				"<br />" +
  				"<cite>Shakespeare</cite>" +
  				"<code>Hello World</code>" +
  				"<dd>A Definition</dd>" +
  				"<dl>Definition list</dl>" +
  				"<li>List</li>" +
  				//Added attributes to ol
  				"<ol start=\"beginning\" type=\"linked\">Ordered List</ol>" +
  				"<p>Paragraph</p>" +
  				"<pre>preformatted</pre>" +
  				"<q cite=\"http://www.google.ca\">quote</q>" +
  				"<small>smalltext</small>" +
  				"<strike>you're out!</strike>" +
  				"<sub>zero</sub>" +
  				"<sup>erman</sup>" +
  				//Added attributes to ul
  				"<ul type=\"chaos\">unordered</ul>" +
  				"<img align=\"left\" alt=\"top\" height=\"100\" src=\"http://google.ca\" title=\"img01\" width=\"100\" />" +
  				//More tags and attributes allowed in relaxed mode
  				"<div>division</div>" +
  				"<h1>header1</h1>" +
  				"<h2>header1</h2>" +
  				"<h3>header1</h3>" +
  				"<h4>header1</h4>" +
  				"<h5>header1</h5>" +
  				"<h6>header1</h6>" +
	  			//Table
				"<table summary=\"data\" width=\"5\">" +
				"<caption>sentence</caption>" +
				"<colgroup span=\"5\" width=\"5\">" +
					"<col span=\"5\" width=\"5\" />" +
				"</colgroup>" +
				"<thead>" +
					"<tr>" +
						"<th abbr=\"A\" axis=\"X\" colspan=\"5\" rowspan=\"1\" width=\"5\" scope=\"1\">theader</th>" +
					"</tr>" +
				"</thead>" +
					"<tbody>" +
						"<tr>" +
							"<td abbr=\"A\" axis=\"X\" colspan=\"5\" rowspan=\"1\" width=\"5\">tabledata</td>" +
						"</tr>" +
					"</tbody>" +
				"<tfoot>foottable</tfoot>" +
				"</table>";
  		//checks if there is no html tags, and is valid.
  		assertTrue(Jsoup.isValid(html, Whitelist.relaxed()));
  	}
  	
  	@Test
  	public void cleanRelaxedWhitelistInvalidTagsTest() throws IOException{

  		String html = "Some" +
  				"<b>text</b>," +
  				"<em>making</em>" +
  				"<i>sure</i> the" +
  				"<strong>cleaner</strong>" +
  				"<u>doesn't</u> remove any stuff." +
  				"<a href=\"https://www.google.ca\">google</a>" +
  				"<blockquote cite=\"http://www.shakespeare.com\">2B|~2B</blockquote>" +
  				"A line break" +
  				"<br />" +
  				"<cite>Shakespeare</cite>" +
  				"<code>Hello World</code>" +
  				"<dd>A Definition</dd>" +
  				"<dl>Definition list</dl>" +
  				"<li>List</li>" +
  				//Added attributes to ol
  				"<ol start=\"beginning\" type=\"linked\">Ordered List</ol>" +
  				"<p>Paragraph</p>" +
  				"<pre>preformatted</pre>" +
  				//disallowed protocol for q cite
  				"<q cite=\"invalid://www.google.ca\">quote</q>" +
  				"<small>smalltext</small>" +
  				"<strike>you're out!</strike>" +
  				"<sub>zero</sub>" +
  				"<sup>erman</sup>" +
  				//Added attributes to ul
  				"<ul type=\"chaos\">unordered</ul>" +
  				"<img align=\"left\" alt=\"top\" height=\"100\" src=\"http://google.ca\" title=\"img01\" width=\"100\" />" +
  				//More tags and attributes allowed in relaxed mode
  				
  				"<div>division</div>" +
  				"<h1>header1</h1>" +
  				"<h2>header1</h2>" +
  				"<h3>header1</h3>" +
  				"<h4>header1</h4>" +
  				"<h5>header1</h5>" +
  				"<h6>header1</h6>" +
				//Invalid tags, attributes or protocols while keeping valid ones
				"<malicious>tag</malicious>" +
				//Invalid Table attributes
				"<table summary=\"data\" width=\"5\" name\"table1\">" +
				"<caption>sentence</caption>" +
				"<colgroup span=\"5\" width=\"5\" id=\"columngroup\">" +
					"<col span=\"5\" width=\"5\" id=\"column1\" />" +
				"</colgroup>" +
				"<thead>" +
					"<tr>" +
						"<th name=\"tablerowheader\" abbr=\"A\" axis=\"X\" colspan=\"5\" rowspan=\"1\" width=\"5\" scope=\"1\">theader</th>" +
					"</tr>" +
				"</thead>" +
					"<tbody>" +
						"<tr>" +
							"<td name=\"tabledataname\" abbr=\"A\" axis=\"X\" colspan=\"5\" rowspan=\"1\" width=\"5\">tabledata</td>" +
						"</tr>" +
					"</tbody>" +
				"<tfoot>foottable</tfoot>" +
				"</table>" +
				//Invalid ordered and unordered list
				"<ol name=\"hello\" start=\"beginning\" type=\"linked\">Ordered List</ol>" +
				"<ul type=\"chaos\" id=\"ul1\">unordered</ul>";
  		
  		String p = Jsoup.clean(html, "google.ca", Whitelist.relaxed(),new Document.OutputSettings().prettyPrint(false));
  		//check if clean() keeps all valid html text, removing invalid html
  		assertEquals("Some" +
  				"<b>text</b>," +
  				"<em>making</em>" +
  				"<i>sure</i> the" +
  				"<strong>cleaner</strong>" +
  				"<u>doesn't</u> remove any stuff." +
  				"<a href=\"https://www.google.ca\">google</a>" +
  				"<blockquote cite=\"http://www.shakespeare.com\">2B|~2B</blockquote>" +
  				"A line break" +
  				"<br />" +
  				"<cite>Shakespeare</cite>" +
  				"<code>Hello World</code>" +
  				"<dd>A Definition</dd>" +
  				"<dl>Definition list</dl>" +
  				"<li>List</li>" +
  				"<ol start=\"beginning\" type=\"linked\">Ordered List</ol>" +
  				"<p>Paragraph</p>" +
  				"<pre>preformatted</pre>" +
  				//Disallowed protocol for q
  				"<q>quote</q>" +
  				"<small>smalltext</small>" +
  				"<strike>you're out!</strike>" +
  				"<sub>zero</sub>" +
  				"<sup>erman</sup>" +
  				"<ul type=\"chaos\">unordered</ul>" +
  				"<img align=\"left\" alt=\"top\" height=\"100\" src=\"http://google.ca\" title=\"img01\" width=\"100\" />" +
  			//More tags and attributes allowed in relaxed mode
				
				"<div>division</div>" +
				"<h1>header1</h1>" +
				"<h2>header1</h2>" +
				"<h3>header1</h3>" +
				"<h4>header1</h4>" +
				"<h5>header1</h5>" +
				"<h6>header1</h6>" +
			//Invalid tags while keeping valid ones
			"tag" +
			//Invalid table attributes
			"<table summary=\"data\" width=\"5\">" +
			"<caption>sentence</caption>" +
			"<colgroup span=\"5\" width=\"5\">" +
				"<col span=\"5\" width=\"5\" />" +
			"</colgroup>" +
			"<thead>" +
				"<tr>" +
					"<th abbr=\"A\" axis=\"X\" colspan=\"5\" rowspan=\"1\" width=\"5\" scope=\"1\">theader</th>" +
				"</tr>" +
			"</thead>" +
				"<tbody>" +
					"<tr>" +
						"<td abbr=\"A\" axis=\"X\" colspan=\"5\" rowspan=\"1\" width=\"5\">tabledata</td>" +
					"</tr>" +
				"</tbody>" +
			"<tfoot>foottable</tfoot>" +
			"</table>" +
			"<ol start=\"beginning\" type=\"linked\">Ordered List</ol>" +
			"<ul type=\"chaos\">unordered</ul>",p);
  	}
  	
  	@Test
  	public void isvalidRelaxedWhitelistInvalidTagsTest() throws IOException{

  		String html = "Some" +
  				"<b>text</b>," +
  				"<em>making</em>" +
  				"<i>sure</i> the" +
  				"<strong>cleaner</strong>" +
  				"<u>doesn't</u> remove any stuff." +
  				"<a href=\"https://www.google.ca\">google</a>" +
  				"<blockquote cite=\"http://www.shakespeare.com\">2B|~2B</blockquote>" +
  				"A line break" +
  				"<br />" +
  				"<cite>Shakespeare</cite>" +
  				"<code>Hello World</code>" +
  				"<dd>A Definition</dd>" +
  				"<dl>Definition list</dl>" +
  				"<li>List</li>" +
  				//Added attributes to ol
  				"<ol start=\"beginning\" type=\"linked\">Ordered List</ol>" +
  				"<p>Paragraph</p>" +
  				"<pre>preformatted</pre>" +
  				//disallowed protocol for q cite
  				"<q cite=\"invalid://www.google.ca\">quote</q>" +
  				"<small>smalltext</small>" +
  				"<strike>you're out!</strike>" +
  				"<sub>zero</sub>" +
  				"<sup>erman</sup>" +
  				//Added attributes to ul
  				"<ul type=\"chaos\">unordered</ul>" +
  				"<img align=\"left\" alt=\"top\" height=\"100\" src=\"http://google.ca\" title=\"img01\" width=\"100\" />" +
  				//More tags and attributes allowed in relaxed mode
  				
  				"<div>division</div>" +
  				"<h1>header1</h1>" +
  				"<h2>header1</h2>" +
  				"<h3>header1</h3>" +
  				"<h4>header1</h4>" +
  				"<h5>header1</h5>" +
  				"<h6>header1</h6>" +
	  			//Table
				"<table summary=\"data\" width=\"5\">" +
				"<caption>sentence</caption>" +
				"<colgroup span=\"5\" width=\"5\">" +
					"<col span=\"5\" width=\"5\" />" +
				"</colgroup>" +
				"<thead>" +
					"<tr>" +
						"<th abbr=\"A\" axis=\"X\" colspan=\"5\" rowspan=\"1\" width=\"5\" scope=\"1\">theader</th>" +
					"</tr>" +
				"</thead>" +
					"<tbody>" +
						"<tr>" +
							"<td abbr=\"A\" axis=\"X\" colspan=\"5\" rowspan=\"1\" width=\"5\">tabledata</td>" +
						"</tr>" +
					"</tbody>" +
				"<tfoot>foottable</tfoot>" +
				"</table>" +
				//Invalid tags, attributes or protocols while keeping valid ones
				"<malicious>tag</malicious>" +
				//Invalid Table attributes
				"<table summary=\"data\" width=\"5\" name\"table1\">" +
				"<caption>sentence</caption>" +
				"<colgroup span=\"5\" width=\"5\" id=\"columngroup\">" +
					"<col span=\"5\" width=\"5\" id=\"column1\" />" +
				"</colgroup>" +
				"<thead>" +
					"<tr>" +
						"<th name=\"tablerowheader\" abbr=\"A\" axis=\"X\" colspan=\"5\" rowspan=\"1\" width=\"5\" scope=\"1\">theader</th>" +
					"</tr>" +
				"</thead>" +
					"<tbody>" +
						"<tr>" +
							"<td name=\"tabledataname\" abbr=\"A\" axis=\"X\" colspan=\"5\" rowspan=\"1\" width=\"5\">tabledata</td>" +
						"</tr>" +
					"</tbody>" +
				"<tfoot>foottable</tfoot>" +
				"</table>" +
				//Invalid ordered and unordered list
				"<ol name=\"hello\" start=\"beginning\" type=\"linked\">Ordered List</ol>" +
				"<ul type=\"chaos\" id=\"ul1\">unordered</ul>";
  		//checks if there is invalid html tags, thus will be invalid
  		assertFalse(Jsoup.isValid(html, Whitelist.relaxed()));
  	}
	
}
