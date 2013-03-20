package org.jsoup.nodes;

import org.jsoup.helper.StringUtil;
import org.jsoup.helper.Validate;
import org.jsoup.parser.Parser;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 The base, abstract Node model. Elements, Documents, Comments etc are all Node instances.

 @author Jonathan Hedley, jonathan@hedley.net */
public abstract class Node implements Cloneable {
    Node parentNode;
    List<Node> childNodes;
    Attributes attributes;
    String baseUri;
    int siblingIndex;

    /**
     Create a new Node.
     @param baseUri base URI
     @param attributes attributes (not null, but may be empty)
     */
    Node(String baseUri, Attributes attributes) {
        Validate.notNull(baseUri);
        Validate.notNull(attributes);

        childNodes = new ArrayList<>(4);
        this.baseUri = baseUri.trim();
        this.attributes = attributes;
    }

    Node(String baseUri) {
        this(baseUri, new Attributes());
    }

    /**
     * Default constructor. Doesn't setup base uri, children, or attributes; use with caution.
     */
    Node() {
        childNodes = Collections.emptyList();
        attributes = null;
    }

    /**
     Get the node name of this node. Use for debugging purposes and not logic switching (for that, use instanceof).
     @return node name
     */
    public abstract String nodeName();

    /**
     * Get an attribute's value by its key.
     * <p/>
     * To get an absolute URL from an attribute that may be a relative URL, prefix the key with <code><b>abs</b></code>,
     * which is a shortcut to the {@link #absUrl} method.
     * E.g.: <blockquote><code>String url = a.attr("abs:href");</code></blockquote>
     * @param attributeKey The attribute key.
     * @return The attribute, or empty string if not present (to avoid nulls).
     * @see #attributes()
     * @see #hasAttr(String)
     * @see #absUrl(String)
     */
    public String attr(String attributeKey) {
        Validate.notNull(attributeKey);

        if (attributes.hasKey(attributeKey))
            return attributes.get(attributeKey);
        else
            return attributeKey.toLowerCase().startsWith("abs:") ? absUrl(attributeKey.substring("abs:".length())) : "";
    }

    /**
     * Get all of the element's attributes.
     * @return attributes (which implements iterable, in same order as presented in original HTML).
     */
    public Attributes attributes() {
        return attributes;
    }

    /**
     * Set an attribute (key=value). If the attribute already exists, it is replaced.
     * @param attributeKey The attribute key.
     * @param attributeValue The attribute value.
     * @return this (for chaining)
     */
    public Node attr(String attributeKey, String attributeValue) {
        attributes.put(attributeKey, attributeValue);
        return this;
    }

    /**
     * Test if this element has an attribute.
     * @param attributeKey The attribute key to check.
     * @return true if the attribute exists, false if not.
     */
    public boolean hasAttr(String attributeKey) {
        Validate.notNull(attributeKey);

        if (attributeKey.toLowerCase().startsWith("abs:")) {
            String key = attributeKey.substring("abs:".length());
            if (attributes.hasKey(key) && absUrl(key) != null && !absUrl(key).isEmpty())
                return true;
        }
        return attributes.hasKey(attributeKey);
    }

    /**
     * Remove an attribute from this element.
     * @param attributeKey The attribute to remove.
     * @return this (for chaining)
     */
    public Node removeAttr(String attributeKey) {
        Validate.notNull(attributeKey);
        attributes.remove(attributeKey);
        return this;
    }

    /**
     Get the base URI of this node.
     @return base URI
     */
    public String baseUri() {
        return baseUri;
    }

    /**
     Update the base URI of this node and all of its descendants.
     @param baseUri base URI to set
     */
    public void setBaseUri(final String baseUri) {
        Validate.notNull(baseUri);

        traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                node.baseUri = baseUri;
            }

            public void tail(Node node, int depth) {
            }
        });
    }

    /**
     * Get an absolute URL from a URL attribute that may be relative (i.e. an <code>&lt;a href></code> or
     * <code>&lt;img src></code>).
     * <p/>
     * E.g.: <code>String absUrl = linkEl.absUrl("href");</code>
     * <p/>
     * If the attribute value is already absolute (i.e. it starts with a protocol, like
     * <code>http://</code> or <code>https://</code> etc), and it successfully parses as a URL, the attribute is
     * returned directly. Otherwise, it is treated as a URL relative to the element's {@link #baseUri}, and made
     * absolute using that.
     * <p/>
     * As an alternate, you can use the {@link #attr} method with the <code>abs:</code> prefix, e.g.:
     * <code>String absUrl = linkEl.attr("abs:href");</code>
     *
     * @param attributeKey The attribute key
     * @return An absolute URL if one could be made, or an empty string (not null) if the attribute was missing or
     * could not be made successfully into a URL.
     * @see #attr
     * @see URL#URL(URL, String)
     */
    public String absUrl(String attributeKey) {
        Validate.notEmpty(attributeKey);

        String relUrl = attr(attributeKey);
        if (hasAttr(attributeKey)) {
            URL base;
            try {
                try {
                    base = new URL(baseUri);
                } catch (MalformedURLException e) {
                    // the base is unsuitable, but the attribute may be abs on its own, so try that
                    URL abs = new URL(relUrl);
                    return abs.toExternalForm();
                }
                // workaround: java resolves '//path/file + ?foo' to '//path/?foo', not '//path/file?foo' as desired
                if (!relUrl.isEmpty() && relUrl.charAt(0) == '?')
                    relUrl = base.getPath() + relUrl;
                URL abs = new URL(base, relUrl);
                return abs.toExternalForm();
            } catch (MalformedURLException e) {
                return "";
            }
        } else {
            return ""; // nothing to make absolute with
        }
    }

    /**
     Get a child node by its 0-based index.
     @param index index of child node
     @return the child node at this index. Throws a {@code IndexOutOfBoundsException} if the index is out of bounds.
     */
    public Node childNode(int index) {
        return childNodes.get(index);
    }

    /**
     Get this node's children. Presented as an unmodifiable list: new children can not be added, but the child nodes
     themselves can be manipulated.
     @return list of children. If no children, returns an empty list.
     */
    public List<Node> childNodes() {
        return Collections.unmodifiableList(childNodes);
    }

    /**
     * Returns a deep copy of this node's children. Changes made to these nodes will not be reflected in the original
     * nodes
     * @return a deep copy of this node's children
     */
    public List<Node> childNodesCopy() {
        List<Node> children = new ArrayList<>(childNodes.size());
        for (Node node : childNodes) {
            children.add(node.clone());
        }
        return children;
    }

    /**
     * Get the number of child nodes that this node holds.
     * @return the number of child nodes that this node holds.
     */
    public final int childNodeSize() {
        return childNodes.size();
    }
    
    Node[] childNodesAsArray() {
        return childNodes.toArray(new Node[childNodeSize()]);
    }

    /**
     Gets this node's parent node.
     @return parent node; or null if no parent.
     */
    public Node parent() {
        return parentNode;
    }
    
    /**
     * Gets the Document associated with this Node. 
     * @return the Document associated with this Node, or null if there is no such Document.
     */
    public Document ownerDocument() {
        if (this instanceof Document)
            return (Document) this;
        else return parentNode == null ? null : parentNode.ownerDocument();
    }
    
    /**
     * Remove (delete) this node from the DOM tree. If this node has children, they are also removed.
     */
    public void remove() {
        Validate.notNull(parentNode);
        parentNode.removeChild(this);
    }

    /**
     * Insert the specified HTML into the DOM before this node (i.e. as a preceding sibling).
     * @param html HTML to add before this node
     * @return this node, for chaining
     * @see #after(String)
     */
    Node before(String html) {
        addSiblingHtml(siblingIndex(), html);
        return this;
    }

    /**
     * Insert the specified node into the DOM before this node (i.e. as a preceding sibling).
     * @param node to add before this node
     * @return this node, for chaining
     * @see #after(Node)
     */
    Node before(Node node) {
        Validate.notNull(node);
        Validate.notNull(parentNode);

        parentNode.addChildren(siblingIndex(), node);
        return this;
    }

    /**
     * Insert the specified HTML into the DOM after this node (i.e. as a following sibling).
     * @param html HTML to add after this node
     * @return this node, for chaining
     * @see #before(String)
     */
    public Node after(String html) {
        addSiblingHtml(siblingIndex()+1, html);
        return this;
    }

    /**
     * Insert the specified node into the DOM after this node (i.e. as a following sibling).
     * @param node to add after this node
     * @return this node, for chaining
     * @see #before(Node)
     */
    Node after(Node node) {
        Validate.notNull(node);
        Validate.notNull(parentNode);

        parentNode.addChildren(siblingIndex()+1, node);
        return this;
    }

    private void addSiblingHtml(int index, String html) {
        Validate.notNull(html);
        Validate.notNull(parentNode);

        Element context = parent() instanceof Element ? (Element) parent() : null;        
        List<Node> nodes = Parser.parseFragment(html, context, baseUri());
        parentNode.addChildren(index, nodes.toArray(new Node[nodes.size()]));
    }

    /**
     Wrap the supplied HTML around this node.
     @param html HTML to wrap around this element, e.g. {@code <div class="head"></div>}. Can be arbitrarily deep.
     @return this node, for chaining.
     */
    public Node wrap(String html) {
        Validate.notEmpty(html);

        Element context = parent() instanceof Element ? (Element) parent() : null;
        List<Node> wrapChildren = Parser.parseFragment(html, context, baseUri());
        Node wrapNode = wrapChildren.get(0);
        if (wrapNode == null || !(wrapNode instanceof Element)) // nothing to wrap with; noop
            return null;

        Element wrap = (Element) wrapNode;
        Element deepest = getDeepChild(wrap);
        parentNode.replaceChild(this, wrap);
        deepest.addChildren(this);

        // remainder (unbalanced wrap, like <div></div><p></p> -- The <p> is remainder
        if (!wrapChildren.isEmpty()) {

            for (int i = 0, wrapChildrenSize = wrapChildren.size(); i < wrapChildrenSize; i++) {
                Node remainder = wrapChildren.get(i);
                remainder.parentNode.removeChild(remainder);
                wrap.appendChild(remainder);
            }
        }
        return this;
    }

    /**
     * Removes this node from the DOM, and moves its children up into the node's parent. This has the effect of dropping
     * the node but keeping its children.
     * <p/>
     * For example, with the input html:<br/>
     * {@code <div>One <span>Two <b>Three</b></span></div>}<br/>
     * Calling {@code element.unwrap()} on the {@code span} element will result in the html:<br/>
     * {@code <div>One Two <b>Three</b></div>}<br/>
     * and the {@code "Two "} {@link TextNode} being returned.
     * @return the first child of this node, after the node has been unwrapped. Null if the node had no children.
     * @see #remove()
     * @see #wrap(String)
     */
    public Node unwrap() {
        Validate.notNull(parentNode);

        int index = siblingIndex;
        Node firstChild = childNodes.isEmpty() ? null : childNodes.get(0);
        parentNode.addChildren(index, childNodesAsArray());
        remove();

        return firstChild;
    }

    private static Element getDeepChild(Element el) {
        List<Element> children = el.children();
        return children.isEmpty() ? el : getDeepChild(children.get(0));
    }
    
    /**
     * Replace this node in the DOM with the supplied node.
     * @param in the node that will will replace the existing node.
     */
    public void replaceWith(Node in) {
        Validate.notNull(in);
        Validate.notNull(parentNode);
        parentNode.replaceChild(this, in);
    }

    void setParentNode(Node parentNode) {
        if (this.parentNode != null)
            this.parentNode.removeChild(this);
        this.parentNode = parentNode;
    }

    void replaceChild(Node out, Node in) {
        Validate.isTrue(out.parentNode.equals(this));
        Validate.notNull(in);
        if (in.parentNode != null)
            in.parentNode.removeChild(in);
        
        Integer index = out.siblingIndex();
        childNodes.set(index, in);
        in.parentNode = this;
        in.siblingIndex = index;
        out.parentNode = null;
    }

    void removeChild(Node out) {
        Validate.isTrue(out.parentNode.equals(this));
        int index = out.siblingIndex();
        childNodes.remove(index);
        reindexChildren();
        out.parentNode = null;
    }

    void addChildren(Node... children) {
        //most used. short circuit addChildren(int), which hits reindex children and array copy
        for (Node child: children) {
            reparentChild(child);
            childNodes.add(child);
            child.siblingIndex = childNodes.size() - 1;
        }
    }

    void addChildren(int index, Node... children) {
        Validate.noNullElements(children);
        for (int i = children.length - 1; i >= 0; i--) {
            Node in = children[i];
            reparentChild(in);
            childNodes.add(index, in);
        }
        reindexChildren();
    }

    private void reparentChild(Node child) {
        if (child.parentNode != null)
            child.parentNode.removeChild(child);
        child.setParentNode(this);
    }
    
    private void reindexChildren() {
        for (int i = 0; i < childNodes.size(); i++) {
            childNodes.get(i).siblingIndex = i;
        }
    }
    
    /**
     Retrieves this node's sibling nodes. Similar to {@link #childNodes()  node.parent.childNodes()}, but does not
     include this node (a node is not a sibling of itself).
     @return node siblings. If the node has no parent, returns an empty list.
     */
    public List<Node> siblingNodes() {
        if (parentNode == null)
            return Collections.emptyList();

        List<Node> nodes = parentNode.childNodes;
        List<Node> siblings = new ArrayList<>(nodes.size() - 1);
        for (Node node: nodes)
            if (!node.equals(this))
                siblings.add(node);
        return siblings;
    }

    /**
     Get this node's next sibling.
     @return next sibling, or null if this is the last sibling
     */
    public Node nextSibling() {
        if (parentNode == null)
            return null; // root
        
        List<Node> siblings = parentNode.childNodes;
        Integer index = siblingIndex();
        Validate.notNull(index);
        return siblings.size() > index + 1 ? siblings.get(index + 1) : null;
    }

    /**
     Get this node's previous sibling.
     @return the previous sibling, or null if this is the first sibling
     */
    public Node previousSibling() {
        if (parentNode == null)
            return null; // root

        List<Node> siblings = parentNode.childNodes;
        Integer index = siblingIndex();
        Validate.notNull(index);
        return index > 0 ? siblings.get(index - 1) : null;
    }

    /**
     * Get the list index of this node in its node sibling list. I.e. if this is the first node
     * sibling, returns 0.
     * @return position in node sibling list
     * @see Element#elementSiblingIndex()
     */
    public int siblingIndex() {
        return siblingIndex;
    }
    
// --Commented out by Inspection START (3/20/13 10:02 AM):
//    protected void setSiblingIndex(int siblingIndex) {
//        this.siblingIndex = siblingIndex;
//    }
// --Commented out by Inspection STOP (3/20/13 10:02 AM)

    /**
     * Perform a depth-first traversal through this node and its descendants.
     * @param nodeVisitor the visitor callbacks to perform on each node
     * @return this node, for chaining
     */
    public Node traverse(NodeVisitor nodeVisitor) {
        Validate.notNull(nodeVisitor);
        NodeTraversor traversor = new NodeTraversor(nodeVisitor);
        traversor.traverse(this);
        return this;
    }

    /**
     Get the outer HTML of this node.
     @return HTML
     */
    public String outerHtml() {
        StringBuilder accum = new StringBuilder(128);
        outerHtml(accum);
        return accum.toString();
    }

    void outerHtml(StringBuilder accum) {
        new NodeTraversor(new Node.OuterHtmlVisitor(accum, getOutputSettings())).traverse(this);
    }

    // if this node has no document (or parent), retrieve the default output settings
    private Document.OutputSettings getOutputSettings() {
        return ownerDocument() != null ? ownerDocument().outputSettings() : new Document("").outputSettings();
    }

    /**
     Get the outer HTML of this node.
     @param accum accumulator to place HTML into
     */
    abstract void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out);

    abstract void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out);

    public String toString() {
        return outerHtml();
    }

    void indent(StringBuilder accum, int depth, Document.OutputSettings out) {
        accum.append('\n').append(StringUtil.padding(depth * out.indentAmount()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // todo: have nodes hold a child index, compare against that and parent (not children)
        return false;
    }

    @Override
    public int hashCode() {
        int result = parentNode != null ? parentNode.hashCode() : 0;
        // not children, or will block stack as they go back up to parent)
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        return result;
    }

    /**
     * Create a stand-alone, deep copy of this node, and all of its children. The cloned node will have no siblings or
     * parent node. As a stand-alone object, any changes made to the clone or any of its children will not impact the
     * original node.
     * <p>
     * The cloned node may be adopted into another Document or node structure using {@link Element#appendChild(Node)}.
     * @return stand-alone cloned node
     */
    @Override
    public Node clone() {
        return doClone(null); // splits for orphan
    }

    Node doClone(Node parent) {
        Node clone;
        try {
            clone = (Node) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        clone.parentNode = parent; // can be null, to create an orphan split
        clone.siblingIndex = parent == null ? 0 : siblingIndex;
        clone.attributes = attributes != null ? attributes.clone() : null;
        clone.baseUri = baseUri;
        clone.childNodes = new ArrayList<>(childNodes.size());
        for (Node child: childNodes)
            clone.childNodes.add(child.doClone(clone)); // clone() creates orphans, doClone() keeps parent

        return clone;
    }

    private static class OuterHtmlVisitor implements NodeVisitor {
        private StringBuilder accum;
        private Document.OutputSettings out;

        OuterHtmlVisitor(StringBuilder accum, Document.OutputSettings out) {
            this.accum = accum;
            this.out = out;
        }

        public void head(Node node, int depth) {
            node.outerHtmlHead(accum, depth, out);
        }

        public void tail(Node node, int depth) {
            if (!"#text".equals(node.nodeName())) // saves a void hit.
                node.outerHtmlTail(accum, depth, out);
        }
    }
}
