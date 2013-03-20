package org.jsoup.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.parser.*;

public class NodeTest {

    @Test
	public void absUrl() {
		Tag tag = Tag.valueOf("a");
		// Testing for relative href and absolute href
		Attributes attributes = new Attributes();
		attributes.put("relHref", "/example");
		attributes.put("absHref", "http://google.com/");

		// No URI provided
		Element hrefElNoUri = new Element(tag, "", attributes);
		assertEquals("", hrefElNoUri.absUrl("relHref"));
		assertEquals("http://google.com/", hrefElNoUri.absUrl("absHref"));

		// Valid URI (valid protocol)
		Element hrefElValidUri = new Element(tag, "https://google.com/",
				attributes);
		assertEquals("https://google.com/example",
				hrefElValidUri.absUrl("relHref"));
		assertEquals("http://google.com/", hrefElValidUri.absUrl("absHref"));

		// False protocol
		Element hrefElInvalidUri = new Element(tag, "lol://google.com/",
				attributes);
		assertEquals("", hrefElInvalidUri.absUrl("relHref"));
		assertEquals("http://google.com/", hrefElInvalidUri.absUrl("absHref"));
	}
	
	@Test
	public void absUrlWithNoAttribute() {
		Tag tag = Tag.valueOf("a");
		Attributes attributes = new Attributes();

		Element hrefElNoUri = new Element(tag, "", attributes);
		assertEquals("", hrefElNoUri.absUrl("href"));
	}
	
	@Test
	public void absUrlWithExclamationMarkInAttributeAndBaseURI() {
		Tag tag = Tag.valueOf("a");
		// Testing for relative href and absolute href
		Attributes attributes = new Attributes();
		attributes.put("href", "?example");
		//attributes.put("absHref", "http://google.com/");

		Element hrefWithUri = new Element(tag, "https://google.com",
				attributes);
		assertEquals("https://google.com/?example", hrefWithUri.absUrl("href"));
	}

	@Test
	public void insertAfter() {
		Tag tagSibling = Tag.valueOf("a");
		Element sibling = new Element(tagSibling, "");

		Document docBefore = Jsoup.parseBodyFragment("<div><p></p></div>");
		Document docAfter = Jsoup
				.parseBodyFragment("<div><p></p><a></a></div>");

		docBefore.select("p").first().after(sibling);
		assertEquals(docAfter.body().html(), docBefore.body().html());
	}

	@Test
	public void insertAfterString() {
		Document docBefore = Jsoup.parseBodyFragment("<div><p></p></div>");
		Document docAfter = Jsoup.parseBodyFragment("<div><p></p><br></div>");

		docBefore.select("p").first().after("<br>");
		assertEquals(docAfter.body().html(), docBefore.body().html());
	}

	@Test
	public void getAttr() {
		// No base URI
		Tag tag = Tag.valueOf("a");
		Attributes attributes = new Attributes();
		attributes.put("title", "testing");
		Node nodeWithoutURI = new Element(tag, "", attributes);
		assertEquals("testing", nodeWithoutURI.attr("title"));
		assertEquals("", nodeWithoutURI.attr("abs:title"));

		// Base URI
		tag = Tag.valueOf("a");
		attributes = new Attributes();
		attributes.put("href", "testing");
		Node nodeWithURI = new Element(tag, "http://testing.com/", attributes);
		assertEquals("", nodeWithURI.attr("title"));
		assertEquals("http://testing.com/testing", nodeWithURI.attr("abs:href"));
	}

	@Test
	public void setAttr() {
		Tag tag = Tag.valueOf("a");
		Attributes attributes = new Attributes();
		Node node = new Element(tag, "", attributes);
		// Empty attribute
		assertEquals("", node.attr("title"));
		// Standard attribute insertion
		node.attr("title", "testing");
		assertEquals("testing", node.attr("title"));
		// Erasing insertion
		node.attr("title", "noMoreTesting");
		assertEquals("noMoreTesting", node.attr("title"));
	}

	@Test
	public void getAttributes() {
		Tag tag = Tag.valueOf("a");
		Attributes attributes = new Attributes();
		Node node = new Element(tag, "", attributes);
		// No attributes
		Attributes attributesTest = new Attributes();
		assertEquals(attributesTest, node.attributes());
		// One attributes
		node.attr("title", "testing");
		attributesTest.put("title", "testing");
		assertEquals(attributes, node.attributes());
		// Several attributes
		node.attr("class", "testing");
		node.attr("align", "testing");
		attributesTest.put("class", "testing");
		attributesTest.put("align", "testing");
		assertEquals(attributesTest, node.attributes());
	}

	@Test
	public void baseUri() {
		Tag tag = Tag.valueOf("a");
		Attributes attributes = new Attributes();
		// No URI
		Node nodeNoURI = new Element(tag, "", attributes);
		assertEquals("", nodeNoURI.baseUri());
		// URI present
		Node nodeURI = new Element(tag, "/testing", attributes);
		assertEquals("/testing", nodeURI.baseUri());
	}

	@Test
	public void insertBefore() {
		Tag tagSibling = Tag.valueOf("a");
		Element sibling = new Element(tagSibling, "");

		Document docBefore = Jsoup.parseBodyFragment("<div><p></p></div>");
		Document docAfter = Jsoup
				.parseBodyFragment("<div><a></a><p></p></div>");

		docBefore.select("p").first().before(sibling);
		assertEquals(docAfter.body().html(), docBefore.body().html());
	}

	@Test
	public void insertBeforeString() {
		Document docBefore = Jsoup.parseBodyFragment("<div><p></p></div>");
		Document docAfter = Jsoup.parseBodyFragment("<div><br><p></p></div>");

		docBefore.select("p").first().before("<br>");
		assertEquals(docAfter.body().html(), docBefore.body().html());
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void childNode() {
		// No children
		Document noChild = Jsoup.parseBodyFragment("");
        assertNull(noChild.body().childNode(0));
		// Only one child
		Tag tag = Tag.valueOf("div");
		Document oneChild = Jsoup.parseBodyFragment("<div></div>");
		assertEquals(new Element(tag, "").toString(), oneChild.body()
				.childNode(0).toString());
		// i th child
		tag = Tag.valueOf("a");
		Document severalChildren = Jsoup
				.parseBodyFragment("<div></div><p></p><a></a>");
		assertEquals(new Element(tag, "").toString(), severalChildren.body()
				.childNode(2).toString());
		// IndexOutOfBoundsException raised
		severalChildren.body().childNode(3).toString();
	}

	@Test
	public void childNodes() {
		/* Tests both childNodes and childNodeSize functions */
		// No children
		Document noChild = Jsoup.parseBodyFragment("");
		assertEquals(0, noChild.body().childNodeSize());
		assertEquals(new ArrayList<Node>(), noChild.body().childNodes());
		// Some children
		Document children = Jsoup
				.parseBodyFragment("<div></div><p></p><a></a>");
		Collection<Node> listNodes = new ArrayList<>();
		listNodes.add(children.body().childNode(0));
		listNodes.add(children.body().childNode(1));
		listNodes.add(children.body().childNode(2));
		assertEquals(3, children.body().childNodeSize());
		assertEquals(listNodes, children.body().childNodes());
	}
	
	@Test
	public void childNodesCopy() {
		/* Tests both childNodes and childNodeSize functions */
		// No children
		Document noChild = Jsoup.parseBodyFragment("");
		assertEquals(0, noChild.body().childNodeSize());
		assertEquals(new ArrayList<Node>(), noChild.body().childNodes());
		// Some children
		Document children = Jsoup
				.parseBodyFragment("<div></div><p></p><a></a>");
		Collection<Node> listNodes = new ArrayList<>();
		listNodes.add(children.body().childNode(0));
		listNodes.add(children.body().childNode(1));
		listNodes.add(children.body().childNode(2));
		assertEquals(listNodes.toString(), children.body().childNodesCopy().toString());
	}

	@Test
	public void cloneNode() {
		// Testing the same content
		Node toCopy = Jsoup.parseBodyFragment("<div><p></p><a></a></div>")
				.body();
		Node copied = toCopy.clone();
		// Testing the same content for both the original and the copy
		assertEquals(copied.toString(), toCopy.toString());
		// Testing after modification
		copied.childNode(0).after("<a>testing</a>");
		assertNotSame(copied.toString(), toCopy.toString());
	}

	@Test
	public void hasAttr() {
		Tag tag = Tag.valueOf("a");
		Attributes attributes = new Attributes();
		Node node = new Element(tag, "", attributes);
		// Tests for no attributes
        assertFalse(node.hasAttr("title"));
		// Tests for one attribute
		attributes.put("title", "testing");
        assertTrue(node.hasAttr("title"));
	}
	
	@Test
	public void hasAttrWithSemicolonPrefix() {
		Tag tag = Tag.valueOf("a");
		Attributes attributes = new Attributes();
		attributes.put("title", "hello");
		// An attribute
		Node nodeWithAbsURIAndAttribute = new Element(tag, "https://google.com/", attributes);
        assertTrue(nodeWithAbsURIAndAttribute.hasAttr("abs:title"));
		Node nodeNoAbsURIAndAttribute = new Element(tag, "", attributes);
        assertFalse(nodeNoAbsURIAndAttribute.hasAttr("abs:title"));
		
		// No attribute
		attributes = new Attributes();
		Node nodeWithAbsURIAndNoAttribute = new Element(tag, "https://google.com/", attributes);
        assertFalse(nodeWithAbsURIAndNoAttribute.hasAttr("abs:title"));
		Node nodeNoAbsURIAndNoAttribute = new Element(tag, "", attributes);
        assertFalse(nodeNoAbsURIAndNoAttribute.hasAttr("abs:title"));
	}

	@Test
	public void nextSibling() {
		Tag tag = Tag.valueOf("a");
		Attributes attributes = new Attributes();
		Node node = new Element(tag, "", attributes);
		// Test for a stand alone node
        assertNull(node.nextSibling());
		// Test for a node that has another sibling after it
		Element siblings = Jsoup.parseBodyFragment("<div><p></p><a></a></div>")
				.body();
		assertEquals(siblings.select("a").first(), siblings.select("p").first()
				.nextSibling());
	}

	@Test
	public void outerHtml() {
		Tag tag = Tag.valueOf("a");
		Attributes attributes = new Attributes();
		Node node = new Element(tag, "", attributes);
		// Test for a stand alone node
		assertEquals("<a></a>", node.outerHtml());
		// Test for a node that has a father
		Element nodeWithFather = Jsoup.parseBodyFragment("<div><p></p></div>")
				.body().select("div").first();
		assertEquals("<div>\n <p></p>\n</div>", nodeWithFather.outerHtml());
	}

	@Test
	public void ownerDocument() {
		Tag tag = Tag.valueOf("a");
		Attributes attributes = new Attributes();
		Node node = new Element(tag, "", attributes);
		// Test for a stand alone node
        assertNull(node.ownerDocument());
		// Test for a node that has a father and a document ancestor
		Element nodeWithFatherAnddocumentAncestor = Jsoup.parseBodyFragment(
				"<div><p></p></div>").body();
		assertEquals(Jsoup.parseBodyFragment("<div><p></p></div>").toString(),
				nodeWithFatherAnddocumentAncestor.ownerDocument().toString());
		// Test for a node that has a father and no document ancestor
		Element nodeWithFatherAndNoDocumentAncestor = nodeWithFatherAnddocumentAncestor
				.clone();
        assertNull(nodeWithFatherAndNoDocumentAncestor.ownerDocument());
	}

	@Test
	public void parent() {
		// No parent
		Tag tag = Tag.valueOf("a");
		Node noParent = new Element(tag, "");
        assertNull(noParent.parent());

		// A parent
		Document parent = Jsoup.parseBodyFragment("<a></a>");
		Node child = parent.childNode(0);
		assertEquals(parent, child.parent());
	}

	@Test
	public void previousSibling() {
		Tag tag = Tag.valueOf("a");
		Attributes attributes = new Attributes();
		Node node = new Element(tag, "", attributes);
		// Test for a stand alone node
        assertNull(node.previousSibling());
		// Test for a node that has another sibling after it
		Element siblings = Jsoup.parseBodyFragment("<div><p></p><a></a></div>")
				.body();
		assertEquals(siblings.select("p").first(), siblings.select("a").first()
				.previousSibling());
        assertNull(siblings.select("p").first()
                .previousSibling());
	}

	@Test
	public void remove() {
		// remove an element
		Document docBefore = Jsoup
				.parseBodyFragment("<div><p></p><a></a></div>");
		Document docAfter = Jsoup.parseBodyFragment("<div><p></p></div>");

		docBefore.select("a").first().remove();
		assertEquals(docAfter.body().html(), docBefore.body().html());
	}

	@Test
	public void removeAttr() {
		// Remove attribute from empty node
		Tag tag = Tag.valueOf("a");
		Attributes attributes = new Attributes();
		Node node = new Element(tag, "", attributes);
		node.removeAttr("title");
		assertEquals("", node.attr("title"));

		// Remove attribute from single-attribute node
		node.attr("title", "test1");
		node.removeAttr("title");
		assertEquals("", node.attr("title"));

		// Remove attribute from multi-attribute node
		node.attr("title", "test2");
		node.attr("body", "test3");
		node.removeAttr("title");
		assertEquals("", node.attr("title"));
		assertEquals("test3", node.attr("body"));
	}

	@Test
	public void replaceWith() {
		// Replace "p" element with "a" element
		Tag tagA = Tag.valueOf("a");
		Element elementA = new Element(tagA, "");

		Document docBefore = Jsoup.parseBodyFragment("<div><p></p></div>");
		Document docAfter = Jsoup.parseBodyFragment("<div><a></a></div>");

		docBefore.select("p").first().replaceWith(elementA);

		assertEquals(docAfter.body().html(), docBefore.body().html());
	}

	@Test
	public void setBaseUri() {
		// No URI
		Node node = new Element(Tag.valueOf("a"), "");
		assertEquals("", node.baseUri());

		// Set the URI
		node.setBaseUri("/testing");
		assertEquals("/testing", node.baseUri());

		// Clear the URI
		node.setBaseUri("");
		assertEquals("", node.baseUri());
	}

	@Test(expected = NullPointerException.class)
	public void siblingIndex() {
		// index #1
		Document document = new Document("<a><div></div></a>");

		Tag tagSibling = Tag.valueOf("p");
		Node sibling = new Element(tagSibling, "");
		document.select("div").first().after(sibling);

		int siblingIndex = sibling.siblingIndex();
		assertEquals(1, siblingIndex);

		// no siblings
		Node unfoundSibling = new Element(Tag.valueOf("br"), "");
		int unfoundIndex = unfoundSibling.siblingIndex();
		assertEquals(0, unfoundIndex);
	}

	@Test
	public void siblingNodes() {
		// No siblings
		Document document = Jsoup.parseBodyFragment("<a><div></div></a>");

		Element div = document.select("div").first();
		List<Element> actual = div.siblingElements();
		assertEquals(0, actual.size());

		// A sibling
		Tag tagSibling = Tag.valueOf("p");
		Element expected = new Element(tagSibling, "");

		document.select("div").first().after(expected);
		div = document.select("div").first();
		actual = div.siblingElements();

		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}
	
	@Test
	public void siblingNodesWhiteBoxTesting() {
		// No siblings
		Tag tagSibling = Tag.valueOf("div");

		Element div = new Element(tagSibling, "");
		List<Node> actual = div.siblingNodes();
		assertEquals(new ArrayList<Node>(), actual);

		// A sibling
		tagSibling = Tag.valueOf("p");
		Element expected = new Element(tagSibling, "");

		Document document = Jsoup.parseBodyFragment("<a><div></div></a>");
		document.select("div").first().after(expected);
		div = document.select("div").first();
		actual = div.siblingNodes();
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void toStringTest() {
		Tag tag = Tag.valueOf("a");
		Node element = new Element(tag, "");

		String actual = element.toString();
		String expected = "<a></a>";

		assertEquals(expected, actual);
	}

	@Test
	public void unwrap() {
		// Normal wrap
		Document docBefore = Jsoup.parseBodyFragment("<div>1<a>2</a></div>");
		Document docAfter = Jsoup.parseBodyFragment("<div>12</div>");

		Element elementA = docBefore.select("a").first();
		elementA.unwrap();

		assertEquals(docAfter.body().html(), docBefore.body().html());
		
		// No parent unwrap
		docBefore = Jsoup.parseBodyFragment("<a></a>");
		docAfter = Jsoup.parseBodyFragment("");
		
		Element noParentElement = docBefore.select("a").first();
		noParentElement.unwrap();
		
		assertEquals(docAfter.body().html(), docBefore.body().html());
	}

	@Test
	public void wrap() {
		// Normal wrap
		Document docBefore = Jsoup.parseBodyFragment("<div>1<p>2</p></div>");
		Document docAfter = Jsoup
				.parseBodyFragment("<div>1<a><p>2</p></a></div>");
		Element elementP = docBefore.select("p").first();
		elementP.wrap("<a>");

		assertEquals(docAfter.body().html(), docBefore.body().html());
		
		// Two argument wrap
		docBefore = Jsoup.parseBodyFragment("<div></div>");
		docAfter = Jsoup.parseBodyFragment("<a><div></div><p></p></a>");
		elementP = docBefore.select("div").first();
		elementP.wrap("<a></a><p></p>");
		
		assertEquals(docAfter.body().html(), docBefore.body().html());
		
		// Invalid tag wrap
		docBefore = Jsoup.parseBodyFragment("<div></div>");
		docAfter = Jsoup.parseBodyFragment("<div></div>");
		elementP = docBefore.select("div").first();
		elementP.wrap("1");
		
		assertEquals(docAfter.body().html(), docBefore.body().html());
	}
	
	@Test(expected=NullPointerException.class)
	public void wrap_noParent() {
		// No parent wrap
		Tag tag = Tag.valueOf("a");
		Node noParentElement = new Element(Tag.valueOf("a"), "");
		try
		{
			noParentElement.wrap("<a>");
		}
		catch (NullPointerException e)
		{
			throw e;
		}
	}
}
