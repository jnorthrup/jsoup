package org.jsoup.test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.junit.Test;

public class HtmlTreeBuilderStateTest{
    private static final Pattern COMPILE = Pattern.compile("\\n\\s*");

    private static String stripNewlines(String text) {
        String text1 = COMPILE.matcher(text).replaceAll("");
        return text1;
    }
	  	
	   
	    @Test public void testParsesTextarea() {
	    	Document doc = Jsoup.parse("<body><p><textarea>A<p>B");
	        Element t = doc.select("textarea").first();
	        assertEquals("A", t.text());
	        assertEquals("B", doc.select("p").get(1).text());
	    }

	    @Test public void testInTableBody() {
	        Document doc = Jsoup.parse("<table><tr><td>One</td><td><table><tr><td>Two</td></tr></tr></table></td><td>Three</td></tr></table>"); // two </tr></tr>, must ignore or will close table
	        assertEquals("<table><tbody><tr><td>One</td><td><table><tbody><tr><td>Two</td></tr></tbody></table></td><td>Three</td></tr></tbody></table>",
	                stripNewlines(doc.body().html()));
	    }

	
	    @Test public void testInitial() {
	        String h = " <html><head></head><body></body></html>";
	        Document doc = Jsoup.parse(h);
	        assertEquals("<html><head></head><body></body></html>",
	                stripNewlines(doc.html()));
	        String h1 = "<!doctype html><html><head></head><body></body></html>";
	        Document doc1 = Jsoup.parse(h1);
	        assertEquals("<!DOCTYPE html><html><head></head><body></body></html>",
	                stripNewlines(doc1.html()));
	        String h2 = "<!--This is a comment.--><html><head></head><body></body></html>";
	        Document doc2 = Jsoup.parse(h2);
	        assertEquals("<!--This is a comment.--><html><head></head><body></body></html>",
	                stripNewlines(doc2.html()));
	        String h3 = "<!doctype html public><html><head></head><body></body></html>";
	        Document doc3 = Jsoup.parse(h3);
	        assertEquals("<!DOCTYPE html><html><head></head><body></body></html>",
	                stripNewlines(doc3.html()));
	        
	        String h4 = "specialtype<html><head></head><body></body></html>";
	        Document doc4 = Jsoup.parse(h4);
	        assertEquals("<html><head></head><body>specialtype</body></html>",
	                stripNewlines(doc4.html()));
	    }
	    @Test public void testBeforeHtml() {
	    	Document doc = Jsoup.parse("<h1>Hello <div>There</div> now</h1> <h2>More <h3>Content</h3></h2>");
	        assertEquals("<h1>Hello <div>There</div> now</h1> <h2>More </h2><h3>Content</h3>", 
	        		stripNewlines(doc.body().html()));
	        
	        Document doc1 = Jsoup.parse("<html><head><noscript><img src='foo'></noscript></head><body><p>Hello</p></body></html>");
	        assertEquals("<html><head><noscript></noscript></head><body><img src=\"foo\" /><p>Hello</p></body></html>", 
	        		stripNewlines(doc1.html()));
	        
	        Document doc2 = Jsoup.parse("<!DOCTYPE html><!DOCTYPE html><html><head><title>Title of the document</title></head>" +
	        		"<body>The content of the document......</body></html>");
	        assertEquals("The content of the document......", 
	        		stripNewlines(doc2.body().html()));
	        
	        String h3 = "<!DOCTYPE html><!--This is a comment.--><html><head>A</head><body>B</body></html>";
	        Document doc3 = Jsoup.parse(h3);
	        assertEquals("<!DOCTYPE html><!--This is a comment.--><html><head></head><body>AB</body></html>",
	                stripNewlines(doc3.html()));
	        
	        String h4 = "<!DOCTYPE html>   <html><head>A</head><body>B</body></html>";
	        Document doc4 = Jsoup.parse(h4);
	        assertEquals("<!DOCTYPE html><html><head></head><body>AB</body></html>",
	                stripNewlines(doc4.html()));
	        
	        String h5 = "<!DOCTYPE html></body><html><head>A</head><body>B</body></html>";
	        Document doc5 = Jsoup.parse(h5);
	        assertEquals("<!DOCTYPE html><html><head></head><body>AB</body></html>",
	                stripNewlines(doc5.html()));
	        
	        String h6 = "<!DOCTYPE html></p><html><head>A</head><body>B</body></html>";
	        Document doc6 = Jsoup.parse(h6);
	        assertEquals("<!DOCTYPE html><html><head></head><body>AB</body></html>",
	                stripNewlines(doc6.html()));
	    }
	    
	    
	    @Test public void testBeforeHead(){
	    	String h1 = "<!DOCTYPE html><html>   <head>A</head><body>B</body></html>";
	        Document doc1 = Jsoup.parse(h1);
	        assertEquals("<!DOCTYPE html><html><head></head><body>AB</body></html>",
	                stripNewlines(doc1.html()));
	    	
	        String h2 = "<!DOCTYPE html><html><!--This is a comment.--><head>A</head><body>B</body></html>";
	        Document doc2 = Jsoup.parse(h2);
	        assertEquals("<!DOCTYPE html><html><!--This is a comment.--><head></head><body>AB</body></html>",
	                stripNewlines(doc2.html()));
	    	
	        String h3 = "<!DOCTYPE html><html><!DOCTYPE html><!DOCTYPE html><head>A</head><body>B</body><html>";
	        Document doc3 = Jsoup.parse(h3);
	        assertEquals("<!DOCTYPE html><html><head></head><body>AB</body></html>",
	                stripNewlines(doc3.html()));
	    	
	        String h4 = "<!DOCTYPE html><html><html><head>A</head><body>B</body></html>";
	        Document doc4 = Jsoup.parse(h4);
	        assertEquals("<!DOCTYPE html><html><head></head><body>AB</body></html>",
	                stripNewlines(doc4.html()));
	        
	        String h5 = "<!DOCTYPE html><html></body><head>A</head><body>B</body></html>";
	        Document doc5 = Jsoup.parse(h5);
	        assertEquals("<!DOCTYPE html><html><head></head><body>AB</body></html>",
	                stripNewlines(doc5.html()));
	        
	        String h6 = "<!DOCTYPE html><html></p><head>A</head><body>B</body></html>";
	        Document doc6 = Jsoup.parse(h6);
	        assertEquals("<!DOCTYPE html><html><head></head><body>AB</body></html>",
	                stripNewlines(doc6.html()));
	        
	    }
	    
	    @Test public void testInHead(){
	    	String h1 = "<!DOCTYPE html><html><head>   </head><body>B</body></html>";
	        Document doc1 = Jsoup.parse(h1);
	        assertEquals("<!DOCTYPE html><html><head> </head><body>B</body></html>",
	                stripNewlines(doc1.html()));
	        
	        String h2 = "<!DOCTYPE html><html><head><!--This is a comment.--></head><body>B</body></html>";
	        Document doc2 = Jsoup.parse(h2);
	        assertEquals("<!DOCTYPE html><html><head><!--This is a comment.--></head><body>B</body></html>",
	                stripNewlines(doc2.html()));
	    	
	        String h3 = "<!DOCTYPE html><html><!DOCTYPE html><head><!DOCTYPE html></head><body>B</body><html>";
	        Document doc3 = Jsoup.parse(h3);
	        assertEquals("<!DOCTYPE html><html><head></head><body>B</body></html>",
	                stripNewlines(doc3.html()));
	    	
	        String h4 = "<!DOCTYPE html><html><head><html></head><body>B</body></html>";
	        Document doc4 = Jsoup.parse(h4);
	        assertEquals("<!DOCTYPE html><html><head></head><body>B</body></html>",
	                stripNewlines(doc4.html()));
	    	
	        
	        String h5 = "<!DOCTYPE html><html><head><base href=\"http://SENG437.com\"></head><body>B</body></html>";
	        Document doc5 = Jsoup.parse(h5);
	        assertEquals("<!DOCTYPE html><html><head><base href=\"http://SENG437.com\" /></head><body>B</body></html>",
	                stripNewlines(doc5.html()));
	        
	        String h6 = "<!DOCTYPE html><html><head><base></head><body>B</body></html>";
	        Document doc6 = Jsoup.parse(h6);
	        assertEquals("<!DOCTYPE html><html><head><base /></head><body>B</body></html>",
	                stripNewlines(doc6.html()));
	        
	        String h7 = "<!DOCTYPE html><html><head><head></head><body>B</body></html>";
	        Document doc7 = Jsoup.parse(h7);
	        assertEquals("<!DOCTYPE html><html><head></head><body>B</body></html>",
	                stripNewlines(doc7.html()));
	        
	        String h8 = "<!DOCTYPE html><html><head></p></head><body>B</body></html>";
	        Document doc8 = Jsoup.parse(h8);
	        assertEquals("<!DOCTYPE html><html><head></head><body>B</body></html>",
	                stripNewlines(doc8.html())); 
	        
	        String t = "<style>font-family: bold</style>";
	        List<Element> tels = Jsoup.parse(t).getElementsByTag("style");
	        assertEquals("font-family: bold", tels.get(0).data());
	        assertEquals("", tels.get(0).text());

	        String s = "<p>Hello</p><script>obj.insert('<a rel=\"none\" />');\ni++;</script><p>There</p>";
	        Document doc = Jsoup.parse(s);
	        assertEquals("Hello There", doc.text());
	        assertEquals("obj.insert('<a rel=\"none\" />');\ni++;", doc.data());
	        
	        String html = "<meta name=keywords /><link rel=stylesheet /><title>jsoup</title><p>Hello world</p>";
	        Document doctemp = Jsoup.parse(html);
	        Element head = doctemp.head();
	        Element body = doctemp.body();

	        assertEquals(1, doctemp.children().size()); // root node: contains html node
	        assertEquals(2, doctemp.child(0).children().size()); // html node: head and body
	        assertEquals(3, head.children().size());
	        assertEquals(1, body.children().size());

	        assertEquals("keywords", head.getElementsByTag("meta").get(0).attr("name"));
	        assertEquals(0, body.getElementsByTag("meta").size());
	        assertEquals("", doc.title());
	        assertEquals("Hello world", body.text());
	        assertEquals("Hello world", body.children().get(0).text());
	        
	        
	    	
	    }
	    
	    @Test public void testInHeadNoscript(){
	    	String h1 = "<!DOCTYPE html><html><head><noscript><html></noscript></head><body>B</body></html>";
	        Document doc1 = Jsoup.parse(h1);
	        assertEquals("<!DOCTYPE html><html><head><noscript></noscript></head><body>B</body></html>",
	                stripNewlines(doc1.html()));
	    	
	    	String h2 = "<!DOCTYPE html><html><head><noscript><p></noscript></head><body>B</body></html>";
	        Document doc2 = Jsoup.parse(h2);
	        assertEquals("<!DOCTYPE html><html><head><noscript></noscript></head><body><p>B</p></body></html>",
	                stripNewlines(doc2.html()));
	        
	        String h3 = "<!DOCTYPE html><html><head><noscript></p></noscript></head><body>B</body></html>";
	        Document doc3 = Jsoup.parse(h3);
	        assertEquals("<!DOCTYPE html><html><head><noscript></noscript></head><body>B</body></html>",
	                stripNewlines(doc3.html()));
	    	
	        String h4 = "<!DOCTYPE html><html><head><noscript>  </noscript></head><body>B</body></html>";
	        Document doc4 = Jsoup.parse(h4);
	        assertEquals("<!DOCTYPE html><html><head><noscript> </noscript></head><body>B</body></html>",
	                stripNewlines(doc4.html()));
	        
	        String h5 = "<!DOCTYPE html><html><head><noscript><!--This is a comment.--></noscript></head><body>B</body></html>";
	        Document doc5 = Jsoup.parse(h5);
	        assertEquals("<!DOCTYPE html><html><head><noscript><!--This is a comment.--></noscript></head><body>B</body></html>",
	                stripNewlines(doc5.html()));
	        
	        String h6 = "<!DOCTYPE html><html><head><noscript><!DOCTYPE html></noscript></head><body>B</body></html>";
	        Document doc6 = Jsoup.parse(h6);
	        assertEquals("<!DOCTYPE html><html><head><noscript></noscript></head><body>B</body></html>",
	                stripNewlines(doc6.html()));
		
	        String h7 = "<!DOCTYPE html><html><head><noscript><meta></noscript></head><body>B</body></html>";
	        Document doc7 = Jsoup.parse(h7);
	        assertEquals("<!DOCTYPE html><html><head><noscript><meta /></noscript></head><body>B</body></html>",
	                stripNewlines(doc7.html()));
	        
	        String h8 = "<!DOCTYPE html><html><head><noscript></br></noscript></head><body>B</body></html>";
	        Document doc8 = Jsoup.parse(h8);
	        assertEquals("<!DOCTYPE html><html><head><noscript></noscript></head><body>B</body></html>",
	                stripNewlines(doc8.html()));
	        
	        String h9 = "<!DOCTYPE html><html><head><noscript><head></noscript></head><body>B</body></html>";
	        Document doc9 = Jsoup.parse(h9);
	        assertEquals("<!DOCTYPE html><html><head><noscript></noscript></head><body>B</body></html>",
	                stripNewlines(doc9.html()));
		
	    }
	    
	    @Test public void testAfterHead(){
	    	String h = "<html><head><script></script><noscript></noscript></head><frameset><frame src=foo></frame><frame src=foo></frameset></html>";
	        Document doc = Jsoup.parse(h);
	        assertEquals("<html><head><script></script><noscript></noscript></head><frameset><frame src=\"foo\" /><frame src=\"foo\" /></frameset></html>",
	                stripNewlines(doc.html()));
	    	
	        String h1 = "<html><head></head>  </html>";
	        Document doc1 = Jsoup.parse(h1);
	        assertEquals("<html><head></head> <body></body></html>",
	                stripNewlines(doc1.html()));
	    	
	        String h2 = "<html><head></head><!--This is a comment.--></html>";
	        Document doc2 = Jsoup.parse(h2);
	        assertEquals("<html><head></head><!--This is a comment.--><body></body></html>",
	                stripNewlines(doc2.html()));
	        
	        String h3 = "<html><head></head><!DOCTYPE html></html>";
	        Document doc3 = Jsoup.parse(h3);
	        assertEquals("<html><head></head><body></body></html>",
	                stripNewlines(doc3.html()));
	        
	        String h4 = "<html><head></head><html></html>";
	        Document doc4 = Jsoup.parse(h4);
	        assertEquals("<html><head></head><body></body></html>",
	                stripNewlines(doc4.html()));
	        
	        String h5 = "<html><head></head><base></html>";
	        Document doc5 = Jsoup.parse(h5);
	        assertEquals("<html><head><base /></head><body></body></html>",
	                stripNewlines(doc5.html()));
	        
	        String h6 = "<html><head></head><head></html>";
	        Document doc6 = Jsoup.parse(h6);
	        assertEquals("<html><head></head><body></body></html>",
	                stripNewlines(doc6.html()));
	    }
	    
	    @Test public void testInBody_UntilStartTag(){
	    	String h = "<html><head></head><body><!DOCTYPE html></body></html>";
	        Document doc = Jsoup.parse(h);
	        assertEquals("<html><head></head><body></body></html>",
	                stripNewlines(doc.html()));
	    	
	    	String h1 = "<html><head></head><body><!--This is a comment.--></body></html>";
	        Document doc1 = Jsoup.parse(h1);
	        assertEquals("<html><head></head><body><!--This is a comment.--></body></html>",
	                stripNewlines(doc1.html()));
	    	
	        String h2 = "<html><head></head><body><html href=\"http://SENG437.com\"></body></html>";
	        Document doc2 = Jsoup.parse(h2);
	        assertEquals("<html href=\"http://SENG437.com\"><head></head><body></body></html>",
	                stripNewlines(doc2.html()));
	        
	        String h3 = "<html><head></head><body><html href=\"http://SENG437.com\" onkeypress=\"\"></body></html>";
	        Document doc3 = Jsoup.parse(h3);
	        assertEquals("<html href=\"http://SENG437.com\" onkeypress=\"\"><head></head><body></body></html>",
	                stripNewlines(doc3.html()));
	        
	        String h4 = "<html><head></head><body><body href=\"http://SENG437.com\" onkeypress=\"\"></body></html>";
	        Document doc4 = Jsoup.parse(h4);
	        assertEquals("<html><head></head><body href=\"http://SENG437.com\" onkeypress=\"\"></body></html>",
	                stripNewlines(doc4.html()));
	        
	        
	        String h5 = "<html><head></head><body><frameset>"+
	        		"</frameset></body></html>";
	        Document doc5 = Jsoup.parse(h5);
	        assertEquals("<html><head></head><body></body></html>",
	                stripNewlines(doc5.html()));
	        
	        String h6 = "<p><b>One</p> <table><tr><td><p><i>Three<p>Four</i></td></tr></table> <p>Five</p>";
	        Document doc6 = Jsoup.parse(h6);
	        String want = "<p><b>One</b></p>\n" +
	                "<b> \n" +
	                " <table>\n" +
	                "  <tbody>\n" +
	                "   <tr>\n" +
	                "    <td><p><i>Three</i></p><p><i>Four</i></p></td>\n" +
	                "   </tr>\n" +
	                "  </tbody>\n" +
	                " </table> <p>Five</p></b>";
	        assertEquals(want, doc6.body().html());
	        
	        String h7 = "<p><h1><p><b>One</b></h1></b>";
	        Document doc7 = Jsoup.parse(h7);
	        assertEquals("<p></p>\n<h1><p><b>One</b></p></h1>", doc7.body().html());
	       
	        String h8 = "<b><h1><p><b>One</b></h1></b>";
	        Document doc8 = Jsoup.parse(h8);
	        assertEquals("<html><head></head><body><b><h1><p><b>One</b></p></h1></b></body></html>",
	                stripNewlines(doc8.html()));
	        
	        String h9 = "<h2><h1><p><b>One</b></h1></b>";
	        Document doc9 = Jsoup.parse(h9);
	        assertEquals("<html><head></head><body><h2></h2><h1><p><b>One</b></p></h1></body></html>",
	                stripNewlines(doc9.html()));
	        
	        String h10 = "<p><listing><p><b>One</b></h1></b>";
	        Document doc10 = Jsoup.parse(h10);
	        assertEquals("<p></p>\n" +
	        		"<listing>\n" +
	        		" <p><b>One</b></p>\n" +
	        		"</listing>", doc10.body().html());
	       
	        String h11 = "<b><listing><h1><p><b>One</b></h1></b>";
	        Document doc11 = Jsoup.parse(h11);
	        assertEquals("<html><head></head><body><b></b><listing><b><h1><p><b>One</b></p></h1></b></listing></body></html>",
	                stripNewlines(doc11.html()));
	        
	        String h12 = "<p><form><p><b>One</b></h1></b>";
	        Document doc12 = Jsoup.parse(h12);
	        assertEquals("<p></p>\n" +
	        		"<form>\n" +
	        		" <p><b>One</b></p>\n" +
	        		"</form>", doc12.body().html());
	       
	        String h13 = "<b><form><h1><p><b>One</b></h1></b>";
	        Document doc13 = Jsoup.parse(h13);
	        assertEquals("<html><head></head><body><b></b><form><b><h1><p><b>One</b></p></h1></b></form></body></html>",
	                stripNewlines(doc13.html()));
	        
	        //li
	        String h14 = "<li>Point one<li>Point two";
	        Document doc14 = Jsoup.parse(h14);
	        Elements ol = doc14.select("ul"); 
	        assertEquals(0, ol.size());
	       
	        String h15 = "<ol><li><p>Point the first<li><p>Point the second";
	        Document doc15 = Jsoup.parse(h15);

	        assertEquals(0, doc15.select("ul").size());
	        assertEquals(1, doc15.select("ol").size());
	        assertEquals(2, doc15.select("ol li").size());
	        assertEquals(2, doc15.select("ol li p").size());
	        assertEquals(1, doc15.select("ol li").get(0).children().size());
	     
	        String h16 = "<dt>Foo<dd>Bar<dt>Qux<dd>Zug";
	        Document doc16 = Jsoup.parse(h16);
	        assertEquals(0, doc16.select("dl").size()); 
	        assertEquals(4, doc16.select("dt, dd").size());
	        Elements dts = doc16.select("dt");
	        assertEquals(2, dts.size());
	        assertEquals("Zug", dts.get(1).nextElementSibling().text());
	       
	        
	        String h17 = "<p><plaintext><p><b>One</b></h1></b>";
	        Document doc17 = Jsoup.parse(h17);
	        assertEquals("<p></p>\n" +
	        		"<plaintext>\n" +
	        		" &lt;p&gt;&lt;b&gt;One&lt;/b&gt;&lt;/h1&gt;&lt;/b&gt;\n" +
	        		"</plaintext>", doc17.body().html());
	       
	        String h18 = "<b><plaintext><h1><p><b>One</b></h1></b>";
	        Document doc18 = Jsoup.parse(h18);
	        assertEquals("<html><head></head><body><b><plaintext>&lt;h1&gt;&lt;p&gt;&lt;b&gt;One&lt;/b&gt;&lt;/h1&gt;&lt;/b&gt;</plaintext></b></body></html>",
	                stripNewlines(doc18.html()));
	             
	        String h19 = "<button><button><p><b>One</b></h1></b>";
	        Document doc19 = Jsoup.parse(h19);
	        assertEquals("<button></button>\n" +
	        		"<button><p><b>One</b></p></button>", doc19.body().html());
	       
	        String h20 = "<b><button><h1><p><b>One</b></h1></b>";
	        Document doc20 = Jsoup.parse(h20);
	        assertEquals("<html><head></head><body><b></b><button><b><h1><p><b>One</b></p></h1></b></button></body></html>",
	                stripNewlines(doc20.html()));
	        
	        String h21 = "<html><head></head><body><a href=\"http://SENG437.com\"></body></html>";
	        Document doc21 = Jsoup.parse(h21);
	        assertEquals("<html><head></head><body><a href=\"http://SENG437.com\"></a></body></html>",
	                stripNewlines(doc21.html()));
	        
	        String h22 = "<html><head></head><body><nobr></body></html>";
	        Document doc22 = Jsoup.parse(h22);
	        assertEquals("<html><head></head><body><nobr></nobr></body></html>",
	                stripNewlines(doc22.html()));
	        
	        String h23 = "<html><head></head><body><nobr><nobr></body></html>";
	        Document doc23 = Jsoup.parse(h23);
	        assertEquals("<html><head></head><body><nobr></nobr><nobr></nobr></body></html>",
	                stripNewlines(doc23.html()));
	        
	        String h24 = "<html><head></head><body><applet></body></html>";
	        Document doc24 = Jsoup.parse(h24);
	        assertEquals("<html><head></head><body><applet></applet></body></html>",
	                stripNewlines(doc24.html()));
	     
	        String h_table = "<!DOCTYPE html><html><head></head><body><p><table></body></html>";
	        Document doc_table = Jsoup.parse(h_table);
	        assertEquals("<!DOCTYPE html><html><head></head><body><p></p><table></table></body></html>",
	                stripNewlines(doc_table.html()));
	        
	        String h_table2 = "<!DOCTYPE html PUBLIC><html><head></head><body><p><table></body></html>";
	        Document doc_table2 = Jsoup.parse(h_table2);
	        assertEquals("<!DOCTYPE html><html><head></head><body><p><table></table></p></body></html>",
	                stripNewlines(doc_table2.html()));
	        
	        String h25 = "<html><head></head><body><area></body></html>";
	        Document doc25 = Jsoup.parse(h25);
	        assertEquals("<html><head></head><body><area></area></body></html>",
	                stripNewlines(doc25.html()));
	        
	        String h26 = "<html><head></head><body><input></body></html>";
	        Document doc26 = Jsoup.parse(h26);
	        assertEquals("<html><head></head><body><input /></body></html>",
	                stripNewlines(doc26.html()));
	        
	        String h_input = "<html><head></head><body><input type=\"date\"></body></html>";
	        Document doc_input = Jsoup.parse(h_input);
	        assertEquals("<html><head></head><body><input type=\"date\" /></body></html>",
	                stripNewlines(doc_input.html()));
	        
	        String h27 = "<html><head></head><body><param></body></html>";
	        Document doc27 = Jsoup.parse(h27);
	        assertEquals("<html><head></head><body><param></param></body></html>",
	                stripNewlines(doc27.html()));
	        
	        String h28 = "<html><head></head><body><hr></body></html>";
	        Document doc28 = Jsoup.parse(h28);
	        assertEquals("<html><head></head><body><hr /></body></html>",
	                stripNewlines(doc28.html()));
	        
	        String h_hr = "<html><head></head><body><p><hr></body></html>";
	        Document doc_hr = Jsoup.parse(h_hr);
	        assertEquals("<html><head></head><body><p></p><hr /></body></html>",
	                stripNewlines(doc_hr.html()));
	        
	        String h29 = "<html><head></head><body><image></body></html>";
	        Document doc29 = Jsoup.parse(h29);
	        assertEquals("<html><head></head><body><img /></body></html>",
	                stripNewlines(doc29.html()));
	        
	        String h30 = "<html><head></head><body><isindex></body></html>";
	        Document doc30 = Jsoup.parse(h30);
	        assertEquals("<html><head></head><body><form><hr /><label>This is a searchable index. Enter search keywords: <input name=\"isindex\" /></label><hr /></form></body></html>",
	                stripNewlines(doc30.html()));
	        
	        String h31 = "<html><head></head><body><textarea></body></html>";
	        Document doc31 = Jsoup.parse(h31);
	        assertEquals("<html><head></head><body><textarea>&lt;/body&gt;&lt;/html&gt;</textarea></body></html>",
	                stripNewlines(doc31.html()));
	        
	        String h32 = "<html><head></head><body><xmp></body></html>";
	        Document doc32 = Jsoup.parse(h32);
	        assertEquals("<html><head></head><body><xmp>&lt;/body&gt;&lt;/html&gt;</xmp></body></html>",
	                stripNewlines(doc32.html()));
	        
	        String h_xmp = "<html><head></head><body><p><xmp></body></html>";
	        Document doc_xmp = Jsoup.parse(h_xmp);
	        assertEquals("<html><head></head><body><p></p><xmp>&lt;/body&gt;&lt;/html&gt;</xmp></body></html>",
	                stripNewlines(doc_xmp.html()));
	        
	        
	        String h33 = "<html><head></head><body><iframe></body></html>";
	        Document doc33 = Jsoup.parse(h33);
	        assertEquals("<html><head></head><body><iframe>&lt;/body&gt;&lt;/html&gt;</iframe></body></html>",
	                stripNewlines(doc33.html()));
	        
	        String h34 = "<html><head></head><body><noembed></body></html>";
	        Document doc34 = Jsoup.parse(h34);
	        assertEquals("<html><head></head><body><noembed>&lt;/body&gt;&lt;/html&gt;</noembed></body></html>",
	                stripNewlines(doc34.html()));
	        
	        String h35 = "<html><head></head><body><select></body></html>";
	        Document doc35 = Jsoup.parse(h35);
	        assertEquals("<html><head></head><body><select></select></body></html>",
	                stripNewlines(doc35.html()));
	        
	        String h36 = "<ruby><rt></rt>";
	        Document doc36 = Jsoup.parse(h36);
	        assertEquals("<html><head></head><body><ruby><rt></rt></ruby></body></html>",
	                stripNewlines(doc36.html()));
	        
	        String h37 = "<html><head></head><body><math></body></html>";
	        Document doc37 = Jsoup.parse(h37);
	        assertEquals("<html><head></head><body><math></math></body></html>",
	                stripNewlines(doc37.html()));
	        
	        String h38 = "<html><head></head><body><svg></body></html>";
	        Document doc38 = Jsoup.parse(h38);
	        assertEquals("<html><head></head><body><svg></svg></body></html>",
	                stripNewlines(doc38.html()));
	        
	       /*Error:
	        *       } else if (StringUtil.in("optgroup", "option")) {
                        if (tb.currentElement().nodeName().equals("option"))
                            tb.process(new Token.EndTag("option"));
                        tb.reconstructFormattingElements();
                        tb.insert(startTag);
                    } else if (StringUtil.in("rp", "rt")) {
                        if (tb.inScope("ruby")) {
                            tb.generateImpliedEndTags();
                            if (!tb.currentElement().nodeName().equals("ruby")) {
                                tb.error(this);
                                tb.popStackToBefore("ruby"); // i.e. close up to but not include name
                            }
                            tb.insert(startTag);
	        */
	        
	    }
	    
	    @Test public void testInBody_EndTagToEnd(){

	    	String h1="<html><head></head><body></body></sarcasm>";
	    	Document doc1 = Jsoup.parse(h1);
	    	assertEquals("<html><head></head><body></body></html>",
	                stripNewlines(doc1.html()));
	    	
	    	String h2="<html><head></head><body></body></applet>";
	    	Document doc2 = Jsoup.parse(h2);
	    	assertEquals("<html><head></head><body></body></html>",
	                stripNewlines(doc2.html()));
	    	
	    	String h3="<html><head></head><body></body></br>";
	    	Document doc3 = Jsoup.parse(h3);
	    	assertEquals("<html><head></head><body><br /></body></html>",
	                stripNewlines(doc3.html()));
	    	
	    	String h4="<html><head></head><body></body></div>";
	    	Document doc4 = Jsoup.parse(h4);
	    	assertEquals("<html><head></head><body></body></html>",
	                stripNewlines(doc4.html()));
	    	
	    	String h5="<html><head></head><body></body></form>";
	    	Document doc5 = Jsoup.parse(h5);
	    	assertEquals("<html><head></head><body></body></html>",
	                stripNewlines(doc5.html()));
	    	
	    	String h6="<html><head></head><body></body></p>";
	    	Document doc6 = Jsoup.parse(h6);
	    	assertEquals("<html><head></head><body><p></p></body></html>",
	                stripNewlines(doc6.html()));
	    	
	    	String h7="<html><head></head><body></body></li>";
	    	Document doc7 = Jsoup.parse(h7);
	    	assertEquals("<html><head></head><body></body></html>",
	                stripNewlines(doc7.html()));
	    	
	    	String h8="<html><head></head><body></body></dd>";
	    	Document doc8 = Jsoup.parse(h8);
	    	assertEquals("<html><head></head><body></body></html>",
	                stripNewlines(doc8.html()));
	    	
	    	String h = "<!DOCTYPE html>\n" +
	                "<p><b class=x><b class=x><b><b class=x><b class=x><b>X\n" +
	                "<p>X\n" +
	                "<p><b><b class=x><b>X\n" +
	                "<p></b></b></b></b></b></b>X";
	        Document doc = Jsoup.parse(h);
	        doc.outputSettings().indentAmount(0);
	        String want = "<!DOCTYPE html>\n" +
	                "<html>\n" +
	                "<head></head>\n" +
	                "<body>\n" +
	                "<p><b class=\"x\"><b class=\"x\"><b><b class=\"x\"><b class=\"x\"><b>X </b></b></b></b></b></b></p>\n" +
	                "<p><b class=\"x\"><b><b class=\"x\"><b class=\"x\"><b>X </b></b></b></b></b></p>\n" +
	                "<p><b class=\"x\"><b><b class=\"x\"><b class=\"x\"><b><b><b class=\"x\"><b>X </b></b></b></b></b></b></b></b></p>\n" +
	                "<p>X</p>\n" +
	                "</body>\n" +
	                "</html>";
	        assertEquals(want, doc.html());
	        
	        String h9="<html><head></head><body></body></abc>";
	    	Document doc9 = Jsoup.parse(h9);
	    	assertEquals("<html><head></head><body></body></html>",
	                stripNewlines(doc9.html()));
	        
	    	/*Error:
	    	 * else if (StringUtil.in(name, "applet", "marquee", "object")) {
                        if (!tb.inScope("name")) {
                            if (!tb.inScope(name)) {
                                tb.error(this);
                                return false;
                            }
                            tb.generateImpliedEndTags();
                            if (!tb.currentElement().nodeName().equals(name))
                                tb.error(this);
                            tb.popStackToClose(name);
                            tb.clearFormattingElementsToLastMarker();
                        }
	    	 * 
	    	 */
	    }
	    
	    
	    @Test public void testInTable(){
	    	
	    	
	    	Document doc = Jsoup.parse("<table><caption>A caption<td>One<td>Two");
	        assertEquals("<table><caption>A caption</caption><tbody><tr><td>One</td><td>Two</td></tr></tbody></table>", stripNewlines(doc.body().html()));
	    	
	        Document doc1 = Jsoup.parse("<table> <colgroup> " +
	        		"<col> <colgroup> <col> <col> <col> " +
	        		"<thead> <tr> <th> " +
	        		"<th>2008 <th>2007 <th>2006 " +
	        		"<tbody> <tr> <th scope=rowgroup> Research and development " +
	        		"<td> $ 1,109 <td> $ 782 <td> $ 712 <tr> <th scope=row> " +
	        		"Percentage of net sales <td> 3.4% <td> 3.3% <td> 3.7% <tbody> <tr> " +
	        		"<th scope=rowgroup> Selling, general, and administrative <td> $ 3,761 <td> $ 2,963 <td> $ 2,433 <tr> " +
	        		"<th scope=row> Percentage of net sales <td> 11.6% <td> 12.3% <td> 12.6% </table>");
	        assertEquals("<table> <colgroup> <col /> </colgroup><colgroup> <col /> <col /> <col /> </colgroup>" +
	        		"<thead> <tr> <th> </th><th>2008 </th><th>2007 </th><th>2006 </th></tr></thead>" +
	        		"<tbody> <tr> <th scope=\"rowgroup\"> Research and development </th><td> $ 1,109 </td><td> $ 782 </td><td> $ 712 </td></tr>" +
	        		"<tr> <th scope=\"row\"> Percentage of net sales </th><td> 3.4% </td><td> 3.3% </td><td> 3.7% </td></tr></tbody>" +
	        		"<tbody> <tr> <th scope=\"rowgroup\"> Selling, general, and administrative </th><td> $ 3,761 </td><td> $ 2,963 </td><td> $ 2,433 </td></tr>" +
	        		"<tr> <th scope=\"row\"> Percentage of net sales </th><td> 11.6% </td><td> 12.3% </td><td> 12.6% </td></tr></tbody></table>", stripNewlines(doc1.body().html()));
	   
	        Document doc2 = Jsoup.parse("<table><!--This is a comment-->");
	        assertEquals("<table><!--This is a comment--></table>", stripNewlines(doc2.body().html()));
	        
	        Document doc3 = Jsoup.parse("<table>  </table>");
	        assertEquals("<table> </table>", stripNewlines(doc3.body().html()));
	        
	        Document doc4 = Jsoup.parse("<table><!DOCTYPE html></table>");
	        assertEquals("<table></table>", stripNewlines(doc4.body().html()));
	        
	        Document doc5 = Jsoup.parse("<table><col>A col<td>One<td>Two");
	        assertEquals("<table><colgroup><col /></colgroup>A col<tbody><tr><td>One</td><td>Two</td></tr></tbody></table>", stripNewlines(doc5.body().html()));
	    
 	        Document doc6 = Jsoup.parse("<table><tbody><span class='1'><tr><td>One</td></tr><tr><td>Two</td></tr></span></tbody></table>");
	        assertEquals(0, doc6.select("span").first().children().size());
	        assertEquals(1, doc6.select("table").size());
	    
	        Document doc7 = Jsoup.parse("<table><!DOCTYPE html></table>");
	        assertEquals("<table></table>", stripNewlines(doc7.body().html()));
	        
	        Document doc8 = Jsoup.parse("<table><table></table></table>");
	        assertEquals("<table></table><table></table>", stripNewlines(doc8.body().html()));
	        
	        Document doc9 = Jsoup.parse("<table><style></table>");
	        assertEquals("<table><style></table></style></table>", stripNewlines(doc9.body().html()));
	        
	        Document doc10 = Jsoup.parse("<table><input></table>");
	        assertEquals("<input /><table></table>", stripNewlines(doc10.body().html()));
	    
	        Document doc11 = Jsoup.parse("<table><form></table>");
	        assertEquals("<table><form></form></table>", stripNewlines(doc11.body().html()));
	        
	        Document doc12 = Jsoup.parse("<table></table></table>");
	        assertEquals("<table></table>", stripNewlines(doc12.body().html()));
	        
	        Document doc13 = Jsoup.parse("<table></caption></table>");
	        assertEquals("<table></table>", stripNewlines(doc13.body().html()));
	        
	        Document doc14 = Jsoup.parse("<table><html>");
	        assertEquals("<table></table>", stripNewlines(doc14.body().html()));
	        
	    }
	   
	    @Test public void testInCaption(){
	    	Document doc = Jsoup.parse("<table><caption>A caption</noncaption><td>One<td>Two");
	        assertEquals("<table><caption>A caption</caption><tbody><tr><td>One</td><td>Two</td></tr></tbody></table>", stripNewlines(doc.body().html()));
	        
	        Document doc1 = Jsoup.parse("<caption>A caption</noncaption>");
	        assertEquals("A caption", stripNewlines(doc1.body().html()));
	        
	        Document doc2 = Jsoup.parse("<caption><p>A");
	        assertEquals("<p>A</p>", stripNewlines(doc2.body().html()));
	        
	        Document doc3 = Jsoup.parse("<caption>A</html>");
	        assertEquals("A", stripNewlines(doc3.body().html()));


	    }
	   
	    
	    @Test public void testInColumnGroup(){
	    	Document doc1 = Jsoup.parse("<table> <colgroup> " +
	        		"<col> <colgroup> <col> <col> <col> " +
	        		"<thead> <tr> <th> " +
	        		"<th>2008 <th>2007 <th>2006 " +
	        		"<tbody> <tr> <th scope=rowgroup> Research and development " +
	        		"<td> $ 1,109 <td> $ 782 <td> $ 712 <tr> <th scope=row> " +
	        		"Percentage of net sales <td> 3.4% <td> 3.3% <td> 3.7% <tbody> <tr> " +
	        		"<th scope=rowgroup> Selling, general, and administrative <td> $ 3,761 <td> $ 2,963 <td> $ 2,433 <tr> " +
	        		"<th scope=row> Percentage of net sales <td> 11.6% <td> 12.3% <td> 12.6% </table>");
	        assertEquals("<table> <colgroup> <col /> </colgroup><colgroup> <col /> <col /> <col /> </colgroup>" +
	        		"<thead> <tr> <th> </th><th>2008 </th><th>2007 </th><th>2006 </th></tr></thead>" +
	        		"<tbody> <tr> <th scope=\"rowgroup\"> Research and development </th><td> $ 1,109 </td><td> $ 782 </td><td> $ 712 </td></tr>" +
	        		"<tr> <th scope=\"row\"> Percentage of net sales </th><td> 3.4% </td><td> 3.3% </td><td> 3.7% </td></tr></tbody>" +
	        		"<tbody> <tr> <th scope=\"rowgroup\"> Selling, general, and administrative </th><td> $ 3,761 </td><td> $ 2,963 </td><td> $ 2,433 </td></tr>" +
	        		"<tr> <th scope=\"row\"> Percentage of net sales </th><td> 11.6% </td><td> 12.3% </td><td> 12.6% </td></tr></tbody></table>", stripNewlines(doc1.body().html()));
	   
	        Document doc2 = Jsoup.parse("<table> <colgroup><!--This is a comment--></colgroup>");
	        assertEquals("<table> <colgroup><!--This is a comment--></colgroup></table>",stripNewlines(doc2.body().html()));
	    	
	        Document doc3 = Jsoup.parse("<table> <colgroup><!DOCTYPE html></colgroup>");
	        assertEquals("<table> <colgroup></colgroup></table>",stripNewlines(doc3.body().html()));
	    	
	        Document doc4 = Jsoup.parse("<table> <colgroup><html></colgroup>");
	        assertEquals("<table> <colgroup></colgroup></table>",stripNewlines(doc4.body().html()));
	        
	        Document doc5 = Jsoup.parse("<html><colgroup></colgroup></html>");
	        assertEquals("",stripNewlines(doc5.body().html()));
	    	
	        Document doc6 = Jsoup.parse("<html><colgroup></p></colgroup></html>");
	        assertEquals("<p></p>",stripNewlines(doc6.body().html()));
	    }
	   
	    
	    @Test public void testInRow(){
	    	Document doc1 = Jsoup.parse("<table><tr></tbody></table>");
	    	assertEquals("<table><tbody><tr></tr></tbody></table>",stripNewlines(doc1.body().html()));
	    	
	    	Document doc2 = Jsoup.parse("<table> <tr><specialtype>");
	        assertEquals("<specialtype></specialtype><table> <tbody><tr></tr></tbody></table>",stripNewlines(doc2.body().html()));
	        
	        Document doc3 = Jsoup.parse("<table><tr></body></table>");
	    	assertEquals("<table><tbody><tr></tr></tbody></table>",stripNewlines(doc3.body().html()));
	    	
	    	Document doc4 = Jsoup.parse("<table><tr></specialtype></table>");
	    	assertEquals("<table><tbody><tr></tr></tbody></table>",stripNewlines(doc4.body().html()));
	    }
	    
	    @Test public void testInCell(){
	    	String h1 = "<table><tr><td></body>";
	        Document doc1 = Jsoup.parse(h1);
	        String want = "<table>\n" +
	        		" <tbody>\n" +
	        		"  <tr>\n" +
	        		"   <td></td>\n" +
	        		"  </tr>\n" +
	        		" </tbody>\n" +
	        		"</table>";
	        assertEquals(want, doc1.body().html());
	    }
	    
	    @Test public void testInSelect() {
	    	Document doc = Jsoup.parse("<body><p><select><option>A<option>B</p><p>C</p>");
	        Elements options = doc.select("option");
	        assertEquals(2, options.size());
	        assertEquals("A", options.first().text());
	        assertEquals("BC", options.last().text());
	    	
	    	Document doc1 = Jsoup.parse("<body><p><select><!--This is a comment-->");
	    	assertEquals("<p><select><!--This is a comment--></select></p>",stripNewlines(doc1.body().html()));
	    	
	    	Document doc2 = Jsoup.parse("<body><p><select><!DOCTYPE html>");
	    	assertEquals("<p><select></select></p>",stripNewlines(doc2.body().html()));
	    	
	    	Document doc3 = Jsoup.parse("<body><p><select><html>");
	    	assertEquals("<p><select></select></p>",stripNewlines(doc3.body().html()));
	    	
	    	Document doc4 = Jsoup.parse("<body><p><select><optgroup>");
	    	assertEquals("<p><select><optgroup></optgroup></select></p>",stripNewlines(doc4.body().html()));

	    	Document doc5 = Jsoup.parse("<body><p><select><select>");
	    	assertEquals("<p><select></select></p>",stripNewlines(doc5.body().html()));

	    	Document doc6 = Jsoup.parse("<body><p><select><input>");
	    	assertEquals("<p><select></select><input /></p>",stripNewlines(doc6.body().html()));

	    	Document doc7 = Jsoup.parse("<body><p><select><script>");
	    	assertEquals("<p><select><script></script></select></p>",stripNewlines(doc7.body().html()));

	    	Document doc8 = Jsoup.parse("<body><p><select></optgroup>");
	    	assertEquals("<p><select></select></p>",stripNewlines(doc8.body().html()));

	    	Document doc9 = Jsoup.parse("<body><p><select></specialtype>");
	    	assertEquals("<p><select></select></p>",stripNewlines(doc9.body().html()));

	   
	    }
	    
	
	    
	    @Test public void testAfterBody() {
	        Document doc = Jsoup.parse("<font face=Arial><body class=name><div>One</div></body></font>");
	        assertEquals("<html><head></head><body class=\"name\"><font face=\"Arial\"><div>One</div></font></body></html>",
	                stripNewlines(doc.html()));
	        
	        Document doc1 = Jsoup.parse("<html><body></body>  </html>");
	        assertEquals("<html><head></head><body> </body></html>",
	                stripNewlines(doc1.html()));
	        
	        Document doc2 = Jsoup.parse("<html><body></body><!--This is a comment--></html>");
	        assertEquals("<html><head></head><body><!--This is a comment--></body></html>",
	                stripNewlines(doc2.html()));
	        
	        Document doc3 = Jsoup.parse("<html><body></body><!DOCTYPE html></html>");
	        assertEquals("<html><head></head><body></body></html>",
	                stripNewlines(doc3.html()));
	        
	        Document doc4 = Jsoup.parse("<html><body></body></html>");
	        assertEquals("<html><head></head><body></body></html>",
	                stripNewlines(doc4.html()));
	        
	        Document doc5 = Jsoup.parse("<html><body></body>");
	        assertEquals("<html><head></head><body></body></html>",
	                stripNewlines(doc5.html()));
	        
	        Document doc6 = Jsoup.parse("<html><body></body><html>");
	        assertEquals("<html><head></head><body></body></html>",
	                stripNewlines(doc6.html()));
	        
	        Document doc7 = Jsoup.parse("<html><body></body><specialtype>");
	        assertEquals("<html><head></head><body><specialtype></specialtype></body></html>",
	                stripNewlines(doc7.html()));
	    }

	
	    @Test public void testInFrameset(){
	    	   String h = "<html><head><title>One</title></head><frameset>  <frame /><frame /></frameset><table></table></html>";
		        Document doc = Jsoup.parse(h);
		        assertEquals("<html><head><title>One</title></head><frameset> <frame /><frame /></frameset></html>", stripNewlines(doc.html()));
	    	
		        String h1 = "<html><head><title>One</title></head><frameset><!--This is a comment--><frame /><frame /></frameset><table></table></html>";
		        Document doc1 = Jsoup.parse(h1);
		        assertEquals("<html><head><title>One</title></head><frameset><!--This is a comment--><frame /><frame /></frameset></html>", stripNewlines(doc1.html()));
	    	
		        String h2 = "<html><head><title>One</title></head><frameset><!DOCTYPE html><frame /><frame /></frameset><table></table></html>";
		        Document doc2 = Jsoup.parse(h2);
		        assertEquals("<html><head><title>One</title></head><frameset><frame /><frame /></frameset></html>", stripNewlines(doc2.html()));
	   
		        String h3 = "<html><head><title>One</title></head><frameset><html><frame /><frame /></frameset><table></table></html>";
		        Document doc3 = Jsoup.parse(h3);
		        assertEquals("<html><head><title>One</title></head><frameset><frame /><frame /></frameset></html>", stripNewlines(doc3.html()));
		        
		        String h4 = "<html><head><title>One</title></head><frameset><frameset><frame /><frame /></frameset><table></table></html>";
		        Document doc4 = Jsoup.parse(h4);
		        assertEquals("<html><head><title>One</title></head><frameset><frameset><frame /><frame /></frameset></frameset></html>", stripNewlines(doc4.html()));
		        
		        String h5 = "<html><head><title>One</title></head><frameset><frame><frame /><frame /></frameset><table></table></html>";
		        Document doc5 = Jsoup.parse(h5);
		        assertEquals("<html><head><title>One</title></head><frameset><frame /><frame /><frame /></frameset></html>", stripNewlines(doc5.html()));
		        
		        String h6 = "<html><head><title>One</title></head><frameset><noframes><frame /><frame /></frameset><table></table></html>";
		        Document doc6 = Jsoup.parse(h6);
		        assertEquals("<html><head><title>One</title></head><frameset><noframes>&lt;frame /&gt;&lt;frame /&gt;&lt;/frameset&gt;&lt;table&gt;&lt;/table&gt;&lt;/html&gt;</noframes></frameset></html>", stripNewlines(doc6.html()));
		        
		        String h7 = "<html><head><title>One</title></head><frameset><othertypes><frame /><frame /></frameset><table></table></html>";
		        Document doc7 = Jsoup.parse(h7);
		        assertEquals("<html><head><title>One</title></head><frameset><frame /><frame /></frameset></html>", stripNewlines(doc7.html()));
		        
		        
	    
	    }
	    
	    @Test public void testAfterFrameset(){
	    	String h = "<html><head><title>One</title></head><frameset></frameset>  </html>";
	        Document doc = Jsoup.parse(h);
	        assertEquals("<html><head><title>One</title></head><frameset></frameset> </html>", stripNewlines(doc.html()));
	        
	        String h1 = "<html><head><title>One</title></head><frameset></frameset><!--This is a comment--></html>";
	        Document doc1 = Jsoup.parse(h1);
	        assertEquals("<html><head><title>One</title></head><frameset></frameset><!--This is a comment--></html>", stripNewlines(doc1.html()));
	        
	        String h2 = "<html><head><title>One</title></head><frameset></frameset><!DOCTYPE html></html>";
	        Document doc2 = Jsoup.parse(h2);
	        assertEquals("<html><head><title>One</title></head><frameset></frameset></html>", stripNewlines(doc2.html()));
	        
	        String h3 = "<html><head><title>One</title></head><frameset></frameset><noframes></html>";
	        Document doc3 = Jsoup.parse(h3);
	        assertEquals("<html><head><title>One</title></head><frameset></frameset><noframes>&lt;/html&gt;</noframes></html>", stripNewlines(doc3.html()));
	    	
	        String h4 = "<html><head><title>One</title></head><frameset></frameset>";
	        Document doc4 = Jsoup.parse(h4);
	        assertEquals("<html><head><title>One</title></head><frameset></frameset></html>", stripNewlines(doc4.html()));
	        
	        String h5 = "<html><head><title>One</title></head><frameset></frameset><html>";
	        Document doc5 = Jsoup.parse(h5);
	        assertEquals("<html><head><title>One</title></head><frameset></frameset></html>", stripNewlines(doc5.html()));
	        
	        String h6 = "<html><head><title>One</title></head><frameset></frameset></html>";
	        Document doc6 = Jsoup.parse(h6);
	        assertEquals("<html><head><title>One</title></head><frameset></frameset></html>", stripNewlines(doc6.html()));
	        
	        String h7 = "<html><head><title>One</title></head><frameset></frameset><specialtype>";
	        Document doc7 = Jsoup.parse(h7);
	        assertEquals("<html><head><title>One</title></head><frameset></frameset></html>", stripNewlines(doc7.html()));
	        
	        String h8 = "<html><head><title>One</title></head><frameset></frameset></specialtype>";
	        Document doc8 = Jsoup.parse(h8);
	        assertEquals("<html><head><title>One</title></head><frameset></frameset></html>", stripNewlines(doc8.html()));
	    }
	    

	
	    @Test public void testMisnestedTagsBP() {
	    	String h = "<b>1<p>2</b>3</p>";
	        Document doc = Jsoup.parse(h);
	        assertEquals("<b>1</b>\n<p><b>2</b>3</p>", doc.body().html());
	    }

	    @Test public void testUnclosedElements() {
	    	String h = "<!DOCTYPE html>\n" +
	                "<p><b class=x><b class=x><b><b class=x><b class=x><b>X\n" +
	                "<p>X\n" +
	                "<p><b><b class=x><b>X\n" +
	                "<p></b></b></b></b></b></b>X";
	        Document doc = Jsoup.parse(h);
	        doc.outputSettings().indentAmount(0);
	        String want = "<!DOCTYPE html>\n" +
	                "<html>\n" +
	                "<head></head>\n" +
	                "<body>\n" +
	                "<p><b class=\"x\"><b class=\"x\"><b><b class=\"x\"><b class=\"x\"><b>X </b></b></b></b></b></b></p>\n" +
	                "<p><b class=\"x\"><b><b class=\"x\"><b class=\"x\"><b>X </b></b></b></b></b></p>\n" +
	                "<p><b class=\"x\"><b><b class=\"x\"><b class=\"x\"><b><b><b class=\"x\"><b>X </b></b></b></b></b></b></b></b></p>\n" +
	                "<p>X</p>\n" +
	                "</body>\n" +
	                "</html>";
	        assertEquals(want, doc.html());
	    }
	
	    @Test public void testAfterAfterBody(){
	    	Document doc = Jsoup.parse("<html><head></head><body></body></html><!--This is a comment-->");
	        assertEquals("<html><head></head><body><!--This is a comment--></body></html>", stripNewlines(doc.html()));
	    
	        Document doc1 = Jsoup.parse("<html><head></head><body></body></html><!DOCTYPE html>");
	        assertEquals("<html><head></head><body></body></html>", stripNewlines(doc1.html()));
	    	
	        Document doc2 = Jsoup.parse("<html><head></head><body></body></html>  ");
	        assertEquals("<html><head></head><body> </body></html>", stripNewlines(doc2.html()));
	        
	        Document doc3 = Jsoup.parse("<html><head></head><body></body></html><html>");
	        assertEquals("<html><head></head><body></body></html>", stripNewlines(doc3.html()));
	        
	        Document doc4 = Jsoup.parse("<html><head></head><body></body></html><othertypes>");
	        assertEquals("<html><head></head><body><othertypes></othertypes></body></html>", stripNewlines(doc4.html()));
	    }

	    @Test public void testAfterAfterFrameset(){
	    	
	        Document doc = Jsoup.parse("<html><head><title>One</title></head><frameset></frameset></html><!--This is a comment>");
	        assertEquals("<html><head><title>One</title></head><frameset></frameset><!--This is a comment>--></html>", stripNewlines(doc.html()));
	        
	        Document doc1 = Jsoup.parse("<html><head><title>One</title></head><frameset></frameset></html> ");
	        assertEquals("<html><head><title>One</title></head><frameset></frameset> </html>", stripNewlines(doc1.html()));
	        
	        Document doc2 = Jsoup.parse("<html><head><title>One</title></head><frameset></frameset></html><!DOCTYPE html>");
	        assertEquals("<html><head><title>One</title></head><frameset></frameset></html>", stripNewlines(doc2.html()));
	        
	        Document doc3 = Jsoup.parse("<html><head><title>One</title></head><frameset></frameset></html><html>");
	        assertEquals("<html><head><title>One</title></head><frameset></frameset></html>", stripNewlines(doc3.html()));
	        
	        Document doc4 = Jsoup.parse("<html><head><title>One</title></head><frameset></frameset></html>");
	        assertEquals("<html><head><title>One</title></head><frameset></frameset></html>", stripNewlines(doc4.html()));
	        
	        Document doc5 = Jsoup.parse("<html><head><title>One</title></head><frameset></frameset></html><noframes>");
	        assertEquals("<html><head><title>One</title></head><frameset></frameset><noframes></noframes></html>", stripNewlines(doc5.html()));
	        
	        Document doc6 = Jsoup.parse("<html><head><title>One</title></head><frameset></frameset></html><othertypes>");
	        assertEquals("<html><head><title>One</title></head><frameset></frameset></html>", stripNewlines(doc6.html()));
	        
	        Document doc7 = Jsoup.parse("<html><head><title>One</title></head><frameset></frameset></html></othertypes>");
	        assertEquals("<html><head><title>One</title></head><frameset></frameset></html>", stripNewlines(doc7.html()));
	    }
	    
	    
	    @Test public void testNullContextforParseFragment() {
	        String html = "<ol><li>One</li></ol><p>Two</p>";
	        List<Node> nodes = Parser.parseFragment(html, null, "http://example.com/");
	        assertEquals(1, nodes.size()); 
	        assertEquals("html", nodes.get(0).nodeName());
	        assertEquals("<html> <head></head> <body> <ol> <li>One</li> </ol> <p>Two</p> </body> </html>", StringUtil.normaliseWhitespace(nodes.get(0).outerHtml()));
	    }

	
	   
}