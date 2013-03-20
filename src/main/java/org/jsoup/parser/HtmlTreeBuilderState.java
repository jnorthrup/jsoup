package org.jsoup.parser;

import org.jsoup.helper.DescendableLinkedList;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * The Tree Builder's current state. Each state embodies the processing for the state, and transitions to other states.
 */
enum HtmlTreeBuilderState {
    Initial {
        boolean process(Token t, HtmlTreeBuilder tb) {
            if (isWhitespace(t)) {
                return true; // ignore whitespace
            }
            if (t.isComment()) {
                tb.insert(t.asComment());
            } else if (t.isDoctype()) {
                // todo: parse error check on expected doctypes
                // todo: quirk state check on doctype ids
                Token.Doctype d = t.asDoctype();
                DocumentType doctype = new DocumentType(d.getName(), d.getPublicIdentifier(), d.getSystemIdentifier(), tb.getBaseUri());
                tb.getDocument().appendChild(doctype);
                if (d.isForceQuirks())
                    tb.getDocument().quirksMode(Document.QuirksMode.quirks);
                tb.transition(BeforeHtml);
            } else {
                // todo: check not iframe srcdoc
                tb.transition(BeforeHtml);
                return tb.process(t); // re-process token
            }
            return true;
        }
    },
    BeforeHtml {
        boolean process(Token t, HtmlTreeBuilder tb) {
            if (t.isDoctype()) {
                tb.error(this);
                return false;
            }
            if (t.isComment()) {
                tb.insert(t.asComment());
            } else if (isWhitespace(t)) {
                return true; // ignore whitespace
            } else if (t.isStartTag() && "html".equals(t.asStartTag().name())) {
                tb.insert(t.asStartTag());
                tb.transition(BeforeHead);
            } else {
                if (t.isEndTag() && Arrays.asList("head", "body", "html", "br").contains(t.asEndTag().name())) {
                    return anythingElse(t, tb);
                } else if (t.isEndTag()) {
                    tb.error(this);
                    return false;
                } else {
                    return anythingElse(t, tb);
                }
            }
            return true;
        }

        private boolean anythingElse(Token t, HtmlTreeBuilder tb) {
            tb.insert("html");
            tb.transition(BeforeHead);
            return tb.process(t);
        }
    },
    BeforeHead {
        boolean process(Token t, HtmlTreeBuilder tb) {
            if (isWhitespace(t)) {
                return true;
            }
            if (t.isComment()) {
                tb.insert(t.asComment());
            } else if (t.isDoctype()) {
                tb.error(this);
                return false;
            } else if (t.isStartTag() && "html".equals(t.asStartTag().name())) {
                return InBody.process(t, tb); // does not transition
            } else if (t.isStartTag() && "head".equals(t.asStartTag().name())) {
                Element head = tb.insert(t.asStartTag());
                tb.setHeadElement(head);
                tb.transition(InHead);
            } else {
                if (t.isEndTag() && Arrays.asList("head", "body", "html", "br").contains(t.asEndTag().name())) {
                    tb.process(new Token.StartTag("head"));
                    return tb.process(t);
                } else if (t.isEndTag()) {
                    tb.error(this);
                    return false;
                } else {
                    tb.process(new Token.StartTag("head"));
                    return tb.process(t);
                }
            }
            return true;
        }
    },
    InHead {
        boolean process(Token t, HtmlTreeBuilder tb) {
            if (isWhitespace(t)) {
                tb.insert(t.asCharacter());
                return true;
            }
            switch (t.type) {
                case Comment:
                    tb.insert(t.asComment());
                    break;
                case Doctype:
                    tb.error(this);
                    return false;
                case StartTag:
                    Token.StartTag start = t.asStartTag();
                    String name = start.name();
                    if ("html".equals(name)) {
                        return InBody.process(t, tb);
                    }
                    if (Arrays.asList("base", "basefont", "bgsound", "command", "link").contains(name)) {
                        Element el = tb.insertEmpty(start);
                        // jsoup special: update base the frist time it is seen
                        if ("base".equals(name) && el.hasAttr("href"))
                            tb.maybeSetBaseUri(el);
                    } else if ("meta".equals(name)) {
                        Element meta = tb.insertEmpty(start);
                        // todo: charset switches
                    } else if ("title".equals(name)) {
                        handleRcData(start, tb);
                    } else {
                        if (Arrays.asList("noframes", "style").contains(name)) {
                            handleRawtext(start, tb);
                        } else if ("noscript".equals(name)) {
                            // else if noscript && scripting flag = true: rawtext (jsoup doesn't run script, to handle as noscript)
                            tb.insert(start);
                            tb.transition(InHeadNoscript);
                        } else if ("script".equals(name)) {
                            // skips some script rules as won't execute them
                            tb.insert(start);
                            tb.tokeniser.transition(TokeniserState.ScriptData);
                            tb.markInsertionMode();
                            tb.transition(Text);
                        } else if ("head".equals(name)) {
                            tb.error(this);
                            return false;
                        } else {
                            return anythingElse(t, tb);
                        }
                    }
                    break;
                case EndTag:
                    Token.EndTag end = t.asEndTag();
                    name = end.name();
                    if ("head".equals(name)) {
                        tb.pop();
                        tb.transition(AfterHead);
                    } else {
                        if (Arrays.asList("body", "html", "br").contains(name)) {
                            return anythingElse(t, tb);
                        } else {
                            tb.error(this);
                            return false;
                        }
                    }
                    break;
                default:
                    return anythingElse(t, tb);
            }
            return true;
        }

        private boolean anythingElse(Token t, TreeBuilder tb) {
            tb.process(new Token.EndTag("head"));
            return tb.process(t);
        }
    },
    InHeadNoscript {
        boolean process(Token t, HtmlTreeBuilder tb) {
            if (t.isDoctype()) {
                tb.error(this);
            } else if (t.isStartTag() && "html".equals(t.asStartTag().name())) {
                return tb.process(t, InBody);
            } else if (t.isEndTag() && "noscript".equals(t.asEndTag().name())) {
                tb.pop();
                tb.transition(InHead);
            } else {
                if (isWhitespace(t) || t.isComment() || t.isStartTag() && Arrays.asList("basefont", "bgsound", "link", "meta", "noframes", "style").contains(t.asStartTag().name())) {
                    return tb.process(t, InHead);
                } else if (t.isEndTag() && "br".equals(t.asEndTag().name())) {
                    return anythingElse(t, tb);
                } else {
                    if (t.isStartTag() && Arrays.asList("head", "noscript").contains(t.asStartTag().name()) || t.isEndTag()) {
                        tb.error(this);
                        return false;
                    } else {
                        return anythingElse(t, tb);
                    }
                }
            }
            return true;
        }

        private boolean anythingElse(Token t, HtmlTreeBuilder tb) {
            tb.error(this);
            tb.process(new Token.EndTag("noscript"));
            return tb.process(t);
        }
    },
    AfterHead {
        boolean process(Token t, HtmlTreeBuilder tb) {
            if (isWhitespace(t)) {
                tb.insert(t.asCharacter());
            } else if (t.isComment()) {
                tb.insert(t.asComment());
            } else if (t.isDoctype()) {
                tb.error(this);
            } else if (t.isStartTag()) {
                Token.StartTag startTag = t.asStartTag();
                String name = startTag.name();
                if ("html".equals(name)) {
                    return tb.process(t, InBody);
                } else if ("body".equals(name)) {
                    tb.insert(startTag);
                    tb.framesetOk(false);
                    tb.transition(InBody);
                } else if ("frameset".equals(name)) {
                    tb.insert(startTag);
                    tb.transition(InFrameset);
                } else {
                    if (Arrays.asList("base", "basefont", "bgsound", "link", "meta", "noframes", "script", "style", "title").contains(name)) {
                        tb.error(this);
                        Element head = tb.getHeadElement();
                        tb.push(head);
                        tb.process(t, InHead);
                        tb.removeFromStack(head);
                    } else if ("head".equals(name)) {
                        tb.error(this);
                        return false;
                    } else {
                        anythingElse(t, tb);
                    }
                }
            } else if (t.isEndTag()) {
                if (Arrays.asList("body", "html").contains(t.asEndTag().name())) {
                    anythingElse(t, tb);
                } else {
                    tb.error(this);
                    return false;
                }
            } else {
                anythingElse(t, tb);
            }
            return true;
        }

        private boolean anythingElse(Token t, HtmlTreeBuilder tb) {
            tb.process(new Token.StartTag("body"));
            tb.framesetOk(true);
            return tb.process(t);
        }
    },
    InBody {
        boolean process(Token t, HtmlTreeBuilder tb) {
            switch (t.type) {
                case Character:
                    Token.Character c = t.asCharacter();
                    if (c.getData().equals(nullString)) {
                        // todo confirm that check
                        tb.error(this);
                        return false;
                    }
                    if (isWhitespace(c)) {
                        tb.reconstructFormattingElements();
                        tb.insert(c);
                    } else {
                        tb.reconstructFormattingElements();
                        tb.insert(c);
                        tb.framesetOk(false);
                    }
                    break;
                case Comment:
                    tb.insert(t.asComment());
                    break;
                case Doctype:
                    tb.error(this);
                    return false;
                case StartTag:
                    Token.StartTag startTag = t.asStartTag();
                    String name = startTag.name();
                    if ("html".equals(name)) {
                        tb.error(this);
                        // merge attributes onto real html
                        Element html = tb.getStack().getFirst();
                        for (Attribute attribute : startTag.getAttributes()) {
                            if (!html.hasAttr(attribute.getKey()))
                                html.attributes().put(attribute);
                        }
                    } else {
                        if (Arrays.asList("base", "basefont", "bgsound", "command", "link", "meta", "noframes", "script", "style", "title").contains(name)) {
                            return tb.process(t, InHead);
                        } else if ("body".equals(name)) {
                            tb.error(this);
                            LinkedList<Element> stack = tb.getStack();
                            if (stack.size() == 1 || stack.size() > 2 && !"body".equals(stack.get(1).nodeName())) {
                                // only in fragment case
                                return false; // ignore
                            } else {
                                tb.framesetOk(false);
                                Element body = stack.get(1);
                                for (Attribute attribute : startTag.getAttributes()) {
                                    if (!body.hasAttr(attribute.getKey()))
                                        body.attributes().put(attribute);
                                }
                            }
                        } else if ("frameset".equals(name)) {
                            tb.error(this);
                            LinkedList<Element> stack = tb.getStack();
                            if (stack.size() == 1 || stack.size() > 2 && !"body".equals(stack.get(1).nodeName())) {
                                // only in fragment case
                                return false; // ignore
                            } else if (!tb.framesetOk()) {
                                return false; // ignore frameset
                            } else {
                                Element second = stack.get(1);
                                if (second.parent() != null)
                                    second.remove();
                                // pop up to html element
                                while (stack.size() > 1)
                                    stack.removeLast();
                                tb.insert(startTag);
                                tb.transition(InFrameset);
                            }
                        } else {
                            if (Arrays.asList("address", "article", "aside", "blockquote", "center", "details", "dir", "div", "dl", "fieldset", "figcaption", "figure", "footer", "header", "hgroup", "menu", "nav", "ol", "p", "section", "summary", "ul").contains(name)) {
                                if (tb.inButtonScope("p")) {
                                    tb.process(new Token.EndTag("p"));
                                }
                                tb.insert(startTag);
                            } else {
                                if (Arrays.asList("h1", "h2", "h3", "h4", "h5", "h6").contains(name)) {
                                    if (tb.inButtonScope("p")) {
                                        tb.process(new Token.EndTag("p"));
                                    }
                                    if (Arrays.asList("h1", "h2", "h3", "h4", "h5", "h6").contains(tb.currentElement().nodeName())) {
                                        tb.error(this);
                                        tb.pop();
                                    }
                                    tb.insert(startTag);
                                } else {
                                    if (Arrays.asList("pre", "listing").contains(name)) {
                                        if (tb.inButtonScope("p")) {
                                            tb.process(new Token.EndTag("p"));
                                        }
                                        tb.insert(startTag);
                                        // todo: ignore LF if next token
                                        tb.framesetOk(false);
                                    } else if ("form".equals(name)) {
                                        if (tb.getFormElement() != null) {
                                            tb.error(this);
                                            return false;
                                        }
                                        if (tb.inButtonScope("p")) {
                                            tb.process(new Token.EndTag("p"));
                                        }
                                        Element form = tb.insert(startTag);
                                        tb.setFormElement(form);
                                    } else if ("li".equals(name)) {
                                        tb.framesetOk(false);
                                        LinkedList<Element> stack = tb.getStack();
                                        for (int i = stack.size() - 1; i > 0; i--) {
                                            Element el = stack.get(i);
                                            if ("li".equals(el.nodeName())) {
                                                tb.process(new Token.EndTag("li"));
                                                break;
                                            }
                                            if (tb.isSpecial(el) && !Arrays.asList("address", "div", "p").contains(el.nodeName()))
                                                break;
                                        }
                                        if (tb.inButtonScope("p")) {
                                            tb.process(new Token.EndTag("p"));
                                        }
                                        tb.insert(startTag);
                                    } else {
                                        if (Arrays.asList("dd", "dt").contains(name)) {
                                            tb.framesetOk(false);
                                            LinkedList<Element> stack = tb.getStack();
                                            for (int i = stack.size() - 1; i > 0; i--) {
                                                Element el = stack.get(i);
                                                if (Arrays.asList("dd", "dt").contains(el.nodeName())) {
                                                    tb.process(new Token.EndTag(el.nodeName()));
                                                    break;
                                                }
                                                if (tb.isSpecial(el) && !Arrays.asList("address", "div", "p").contains(el.nodeName()))
                                                    break;
                                            }
                                            if (tb.inButtonScope("p")) {
                                                tb.process(new Token.EndTag("p"));
                                            }
                                            tb.insert(startTag);
                                        } else if ("plaintext".equals(name)) {
                                            if (tb.inButtonScope("p")) {
                                                tb.process(new Token.EndTag("p"));
                                            }
                                            tb.insert(startTag);
                                            tb.tokeniser.transition(TokeniserState.PLAINTEXT); // once in, never gets out
                                        } else if ("button".equals(name)) {
                                            if (tb.inButtonScope("button")) {
                                                // close and reprocess
                                                tb.error(this);
                                                tb.process(new Token.EndTag("button"));
                                                tb.process(startTag);
                                            } else {
                                                tb.reconstructFormattingElements();
                                                tb.insert(startTag);
                                                tb.framesetOk(false);
                                            }
                                        } else if ("a".equals(name)) {
                                            if (tb.getActiveFormattingElement("a") != null) {
                                                tb.error(this);
                                                tb.process(new Token.EndTag("a"));

                                                // still on stack?
                                                Element remainingA = tb.getFromStack("a");
                                                if (remainingA != null) {
                                                    tb.removeFromActiveFormattingElements(remainingA);
                                                    tb.removeFromStack(remainingA);
                                                }
                                            }
                                            tb.reconstructFormattingElements();
                                            Element a = tb.insert(startTag);
                                            tb.pushActiveFormattingElements(a);
                                        } else {
                                            if (Arrays.asList("b", "big", "code", "em", "font", "i", "s", "small", "strike", "strong", "tt", "u").contains(name)) {
                                                tb.reconstructFormattingElements();
                                                Element el = tb.insert(startTag);
                                                tb.pushActiveFormattingElements(el);
                                            } else if ("nobr".equals(name)) {
                                                tb.reconstructFormattingElements();
                                                if (tb.inScope("nobr")) {
                                                    tb.error(this);
                                                    tb.process(new Token.EndTag("nobr"));
                                                    tb.reconstructFormattingElements();
                                                }
                                                Element el = tb.insert(startTag);
                                                tb.pushActiveFormattingElements(el);
                                            } else {
                                                if (Arrays.asList("applet", "marquee", "object").contains(name)) {
                                                    tb.reconstructFormattingElements();
                                                    tb.insert(startTag);
                                                    tb.insertMarkerToFormattingElements();
                                                    tb.framesetOk(false);
                                                } else if ("table".equals(name)) {
                                                    if (tb.getDocument().quirksMode() != Document.QuirksMode.quirks && tb.inButtonScope("p")) {
                                                        tb.process(new Token.EndTag("p"));
                                                    }
                                                    tb.insert(startTag);
                                                    tb.framesetOk(false);
                                                    tb.transition(InTable);
                                                } else {
                                                    if (Arrays.asList("area", "br", "embed", "img", "keygen", "wbr").contains(name)) {
                                                        tb.reconstructFormattingElements();
                                                        tb.insertEmpty(startTag);
                                                        tb.framesetOk(false);
                                                    } else if ("input".equals(name)) {
                                                        tb.reconstructFormattingElements();
                                                        Element el = tb.insertEmpty(startTag);
                                                        if (!"hidden".equalsIgnoreCase(el.attr("type")))
                                                            tb.framesetOk(false);
                                                    } else {
                                                        if (Arrays.asList("param", "source", "track").contains(name)) {
                                                            tb.insertEmpty(startTag);
                                                        } else if ("hr".equals(name)) {
                                                            if (tb.inButtonScope("p")) {
                                                                tb.process(new Token.EndTag("p"));
                                                            }
                                                            tb.insertEmpty(startTag);
                                                            tb.framesetOk(false);
                                                        } else if ("image".equals(name)) {
                                                            // we're not supposed to ask.
                                                            startTag.name("img");
                                                            return tb.process(startTag);
                                                        } else if ("isindex".equals(name)) {
                                                            // how much do we care about the early 90s?
                                                            tb.error(this);
                                                            if (tb.getFormElement() != null)
                                                                return false;

                                                            tb.tokeniser.acknowledgeSelfClosingFlag();
                                                            tb.process(new Token.StartTag("form"));
                                                            if (startTag.attributes.hasKey("action")) {
                                                                Element form = tb.getFormElement();
                                                                form.attr("action", startTag.attributes.get("action"));
                                                            }
                                                            tb.process(new Token.StartTag("hr"));
                                                            tb.process(new Token.StartTag("label"));
                                                            // hope you like english.
                                                            String prompt = startTag.attributes.hasKey("prompt") ?
                                                                    startTag.attributes.get("prompt") :
                                                                    "This is a searchable index. Enter search keywords: ";

                                                            tb.process(new Token.Character(prompt));

                                                            // input
                                                            Attributes inputAttribs = new Attributes();
                                                            for (Attribute attr : startTag.attributes) {
                                                                if (!Arrays.asList("name", "action", "prompt").contains(attr.getKey()))
                                                                    inputAttribs.put(attr);
                                                            }
                                                            inputAttribs.put("name", "isindex");
                                                            tb.process(new Token.StartTag("input", inputAttribs));
                                                            tb.process(new Token.EndTag("label"));
                                                            tb.process(new Token.StartTag("hr"));
                                                            tb.process(new Token.EndTag("form"));
                                                        } else if ("textarea".equals(name)) {
                                                            tb.insert(startTag);
                                                            // todo: If the next token is a U+000A LINE FEED (LF) character token, then ignore that token and move on to the next one. (Newlines at the start of textarea elements are ignored as an authoring convenience.)
                                                            tb.tokeniser.transition(TokeniserState.Rcdata);
                                                            tb.markInsertionMode();
                                                            tb.framesetOk(false);
                                                            tb.transition(Text);
                                                        } else if ("xmp".equals(name)) {
                                                            if (tb.inButtonScope("p")) {
                                                                tb.process(new Token.EndTag("p"));
                                                            }
                                                            tb.reconstructFormattingElements();
                                                            tb.framesetOk(false);
                                                            handleRawtext(startTag, tb);
                                                        } else if ("iframe".equals(name)) {
                                                            tb.framesetOk(false);
                                                            handleRawtext(startTag, tb);
                                                        } else if ("noembed".equals(name)) {
                                                            // also handle noscript if script enabled
                                                            handleRawtext(startTag, tb);
                                                        } else if ("select".equals(name)) {
                                                            tb.reconstructFormattingElements();
                                                            tb.insert(startTag);
                                                            tb.framesetOk(false);

                                                            HtmlTreeBuilderState state = tb.state();
                                                            if (state == InTable || state == InCaption || state == InTableBody || state == InRow || state == InCell)
                                                                tb.transition(InSelectInTable);
                                                            else
                                                                tb.transition(InSelect);
                                                        } else {
                                                            if (Arrays.asList("option").contains("optgroup")) {
                                                                if ("option".equals(tb.currentElement().nodeName()))
                                                                    tb.process(new Token.EndTag("option"));
                                                                tb.reconstructFormattingElements();
                                                                tb.insert(startTag);
                                                            } else {
                                                                if (Arrays.asList("rt").contains("rp")) {
                                                                    if (tb.inScope("ruby")) {
                                                                        tb.generateImpliedEndTags();
                                                                        if (!"ruby".equals(tb.currentElement().nodeName())) {
                                                                            tb.error(this);
                                                                            tb.popStackToBefore("ruby"); // i.e. close up to but not include name
                                                                        }
                                                                        tb.insert(startTag);
                                                                    }
                                                                } else if ("math".equals(name) || "svg".equals(name)) {
                                                                    tb.reconstructFormattingElements();
                                                                    // todo: handle A start tag whose tag name is "math" (i.e. foreign, mathml)
                                                                    tb.insert(startTag);
                                                                    tb.tokeniser.acknowledgeSelfClosingFlag();
                                                                } else {
                                                                    if (Arrays.asList("caption", "col", "colgroup", "frame", "head", "tbody", "td", "tfoot", "th", "thead", "tr").contains(name)) {
                                                                        tb.error(this);
                                                                        return false;
                                                                    } else {
                                                                        tb.reconstructFormattingElements();
                                                                        tb.insert(startTag);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;

                case EndTag:
                    Token.EndTag endTag = t.asEndTag();
                    name = endTag.name();
                    if ("body".equals(name)) {
                        if (tb.inScope("body")) {
                            // todo: error if stack contains something not dd, dt, li, optgroup, option, p, rp, rt, tbody, td, tfoot, th, thead, tr, body, html
                            tb.transition(AfterBody);
                        } else {
                            tb.error(this);
                            return false;
                        }
                    } else if ("html".equals(name)) {
                        boolean notIgnored = tb.process(new Token.EndTag("body"));
                        if (notIgnored)
                            return tb.process(endTag);
                    } else {
                        if (Arrays.asList("address", "article", "aside", "blockquote", "button", "center", "details", "dir", "div", "dl", "fieldset", "figcaption", "figure", "footer", "header", "hgroup", "listing", "menu", "nav", "ol", "pre", "section", "summary", "ul").contains(name)) {
                            // todo: refactor these lookups
                            if (tb.inScope(name)) {
                                tb.generateImpliedEndTags();
                                if (!tb.currentElement().nodeName().equals(name))
                                    tb.error(this);
                                tb.popStackToClose(name);
                            } else {
                                // nothing to close
                                tb.error(this);
                                return false;
                            }
                        } else if ("form".equals(name)) {
                            Element currentForm = tb.getFormElement();
                            tb.setFormElement(null);
                            if (currentForm == null || !tb.inScope(name)) {
                                tb.error(this);
                                return false;
                            } else {
                                tb.generateImpliedEndTags();
                                if (!tb.currentElement().nodeName().equals(name))
                                    tb.error(this);
                                // remove currentForm from stack. will shift anything under up.
                                tb.removeFromStack(currentForm);
                            }
                        } else if ("p".equals(name)) {
                            if (tb.inButtonScope(name)) {
                                tb.generateImpliedEndTags(name);
                                if (!tb.currentElement().nodeName().equals(name))
                                    tb.error(this);
                                tb.popStackToClose(name);
                            } else {
                                tb.error(this);
                                tb.process(new Token.StartTag(name)); // if no p to close, creates an empty <p></p>
                                return tb.process(endTag);
                            }
                        } else if ("li".equals(name)) {
                            if (tb.inListItemScope(name)) {
                                tb.generateImpliedEndTags(name);
                                if (!tb.currentElement().nodeName().equals(name))
                                    tb.error(this);
                                tb.popStackToClose(name);
                            } else {
                                tb.error(this);
                                return false;
                            }
                        } else {
                            if (!Arrays.asList("dd", "dt").contains(name)) {
                                if (Arrays.asList("h1", "h2", "h3", "h4", "h5", "h6").contains(name)) {
                                    if (tb.inScope(new String[]{"h1", "h2", "h3", "h4", "h5", "h6"})) {
                                        tb.generateImpliedEndTags(name);
                                        if (!tb.currentElement().nodeName().equals(name))
                                            tb.error(this);
                                        tb.popStackToClose("h1", "h2", "h3", "h4", "h5", "h6");
                                    } else {
                                        tb.error(this);
                                        return false;
                                    }
                                } else // *sigh*
                                    if (!"sarcasm".equals(name)) {
                                        if (Arrays.asList("a", "b", "big", "code", "em", "font", "i", "nobr", "s", "small", "strike", "strong", "tt", "u").contains(name))
                                            for (int i = 0; i < 8; i++) {
                                                Element formatEl = tb.getActiveFormattingElement(name);
                                                if (formatEl == null)
                                                    return anyOtherEndTag(t, tb);
                                                if (!tb.onStack(formatEl)) {
                                                    tb.error(this);
                                                    tb.removeFromActiveFormattingElements(formatEl);
                                                    return true;
                                                }
                                                if (!tb.inScope(formatEl.nodeName())) {
                                                    tb.error(this);
                                                    return false;
                                                } else if (!tb.currentElement().equals(formatEl))
                                                    tb.error(this);

                                                Element furthestBlock = null;
                                                Element commonAncestor = null;
                                                boolean seenFormattingElement = false;
                                                LinkedList<Element> stack = tb.getStack();
                                                // the spec doesn't limit to < 64, but in degenerate cases (9000+ stack depth) this prevents
                                                // run-aways
                                                for (int si = 0; si < stack.size() && si < 64; si++) {
                                                    Element el = stack.get(si);
                                                    if (el.equals(formatEl)) {
                                                        commonAncestor = stack.get(si - 1);
                                                        seenFormattingElement = true;
                                                    } else if (seenFormattingElement && tb.isSpecial(el)) {
                                                        furthestBlock = el;
                                                        break;
                                                    }
                                                }
                                                if (furthestBlock == null) {
                                                    tb.popStackToClose(formatEl.nodeName());
                                                    tb.removeFromActiveFormattingElements(formatEl);
                                                    return true;
                                                }

                                                // todo: Let a bookmark note the position of the formatting element in the list of active formatting elements relative to the elements on either side of it in the list.
                                                // does that mean: int pos of format el in list?
                                                Element node = furthestBlock;
                                                Element lastNode = furthestBlock;
                                                for (int j = 0; j < 3; j++) {
                                                    if (tb.onStack(node))
                                                        node = tb.aboveOnStack(node);
                                                    if (tb.isInActiveFormattingElements(node)) {
                                                        if (node.equals(formatEl)) break;
                                                        else {

                                                            Element replacement = new Element(Tag.valueOf(node.nodeName()), tb.getBaseUri());
                                                            tb.replaceActiveFormattingElement(node, replacement);
                                                            tb.replaceOnStack(node, replacement);
                                                            node = replacement;

                                                            if (lastNode.equals(furthestBlock)) {
                                                                // todo: move the aforementioned bookmark to be immediately after the new node in the list of active formatting elements.
                                                                // not getting how this bookmark both straddles the element above, but is inbetween here...
                                                            }
                                                            if (lastNode.parent() != null)
                                                                lastNode.remove();
                                                            node.appendChild(lastNode);

                                                            lastNode = node;
                                                        }
                                                    } else { // note no bookmark check
                                                        tb.removeFromStack(node);
                                                    }
                                                }

                                                if (Arrays.asList("table", "tbody", "tfoot", "thead", "tr").contains(commonAncestor.nodeName())) {
                                                    if (lastNode.parent() != null)
                                                        lastNode.remove();
                                                    tb.insertInFosterParent(lastNode);
                                                } else {
                                                    if (lastNode.parent() != null)
                                                        lastNode.remove();
                                                    commonAncestor.appendChild(lastNode);
                                                }

                                                Element adopter = new Element(Tag.valueOf(name), tb.getBaseUri());
                                                Node[] childNodes = furthestBlock.childNodes().toArray(new Node[furthestBlock.childNodeSize()]);
                                                for (Node childNode : childNodes) {
                                                    adopter.appendChild(childNode); // append will reparent. thus the clone to avoid concurrent mod.
                                                }
                                                furthestBlock.appendChild(adopter);
                                                tb.removeFromActiveFormattingElements(formatEl);
                                                // todo: insert the new element into the list of active formatting elements at the position of the aforementioned bookmark.
                                                tb.removeFromStack(formatEl);
                                                tb.insertOnStackAfter(furthestBlock, adopter);
                                            }
                                        else {
                                            if (Arrays.asList("applet", "marquee", "object").contains(name)) {
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
                                            } else if ("br".equals(name)) {
                                                tb.error(this);
                                                tb.process(new Token.StartTag("br"));
                                                return false;
                                            } else {
                                                return anyOtherEndTag(t, tb);
                                            }
                                        }
                                    }
                                    // Adoption Agency Algorithm.
                                    else {
                                        return anyOtherEndTag(t, tb);
                                    }
                            } else {
                                if (tb.inScope(name)) {
                                    tb.generateImpliedEndTags(name);
                                    if (!tb.currentElement().nodeName().equals(name))
                                        tb.error(this);
                                    tb.popStackToClose(name);
                                } else {
                                    tb.error(this);
                                    return false;
                                }
                            }
                        }
                    }


                    break;
                case EOF:
                    // todo: error if stack contains something not dd, dt, li, p, tbody, td, tfoot, th, thead, tr, body, html
                    // stop parsing
                    break;
            }
            return true;
        }

        boolean anyOtherEndTag(Token t, HtmlTreeBuilder tb) {
            String name = t.asEndTag().name();
            DescendableLinkedList<Element> stack = tb.getStack();
            Iterator<Element> it = stack.descendingIterator();
            while (it.hasNext()) {
                Element node = it.next();
                if (node.nodeName().equals(name)) {
                    tb.generateImpliedEndTags(name);
                    if (!name.equals(tb.currentElement().nodeName()))
                        tb.error(this);
                    tb.popStackToClose(name);
                    break;
                } else {
                    if (tb.isSpecial(node)) {
                        tb.error(this);
                        return false;
                    }
                }
            }
            return true;
        }
    },
    Text {
        // in script, style etc. normally treated as data tags
        boolean process(Token t, HtmlTreeBuilder tb) {
            if (t.isCharacter()) {
                tb.insert(t.asCharacter());
            } else if (t.isEOF()) {
                tb.error(this);
                // if current node is script: already started
                tb.pop();
                tb.transition(tb.originalState());
                return tb.process(t);
            } else if (t.isEndTag()) {
                // if: An end tag whose tag name is "script" -- scripting nesting level, if evaluating scripts
                tb.pop();
                tb.transition(tb.originalState());
            }
            return true;
        }
    },
    InTable {
        boolean process(Token t, HtmlTreeBuilder tb) {
            if (t.isCharacter()) {
                tb.newPendingTableCharacters();
                tb.markInsertionMode();
                tb.transition(InTableText);
                return tb.process(t);
            }
            if (t.isComment()) {
                tb.insert(t.asComment());
                return true;
            }
            if (t.isDoctype()) {
                tb.error(this);
                return false;
            } else if (t.isStartTag()) {
                Token.StartTag startTag = t.asStartTag();
                String name = startTag.name();
                if ("caption".equals(name)) {
                    tb.clearStackToTableContext();
                    tb.insertMarkerToFormattingElements();
                    tb.insert(startTag);
                    tb.transition(InCaption);
                } else if ("colgroup".equals(name)) {
                    tb.clearStackToTableContext();
                    tb.insert(startTag);
                    tb.transition(InColumnGroup);
                } else if ("col".equals(name)) {
                    tb.process(new Token.StartTag("colgroup"));
                    return tb.process(t);
                } else {
                    if (Arrays.asList("tbody", "tfoot", "thead").contains(name)) {
                        tb.clearStackToTableContext();
                        tb.insert(startTag);
                        tb.transition(InTableBody);
                    } else {
                        if (Arrays.asList("td", "th", "tr").contains(name)) {
                            tb.process(new Token.StartTag("tbody"));
                            return tb.process(t);
                        } else if ("table".equals(name)) {
                            tb.error(this);
                            boolean processed = tb.process(new Token.EndTag("table"));
                            if (processed) // only ignored if in fragment
                                return tb.process(t);
                        } else {
                            if (Arrays.asList("style", "script").contains(name)) {
                                return tb.process(t, InHead);
                            } else if ("input".equals(name)) {
                                if (!"hidden".equalsIgnoreCase(startTag.attributes.get("type"))) {
                                    return anythingElse(t, tb);
                                } else {
                                    tb.insertEmpty(startTag);
                                }
                            } else if ("form".equals(name)) {
                                tb.error(this);
                                if (tb.getFormElement() != null)
                                    return false;
                                else {
                                    Element form = tb.insertEmpty(startTag);
                                    tb.setFormElement(form);
                                }
                            } else {
                                return anythingElse(t, tb);
                            }
                        }
                    }
                }
            } else if (t.isEndTag()) {
                Token.EndTag endTag = t.asEndTag();
                String name = endTag.name();

                if ("table".equals(name)) {
                    if (!tb.inTableScope(name)) {
                        tb.error(this);
                        return false;
                    } else {
                        tb.popStackToClose("table");
                    }
                    tb.resetInsertionMode();
                } else {
                    if (Arrays.asList("body", "caption", "col", "colgroup", "html", "tbody", "td", "tfoot", "th", "thead", "tr").contains(name)) {
                        tb.error(this);
                        return false;
                    } else {
                        return anythingElse(t, tb);
                    }
                }
            } else if (t.isEOF()) {
                if ("html".equals(tb.currentElement().nodeName()))
                    tb.error(this);
                return true; // stops parsing
            }
            return anythingElse(t, tb);
        }

        boolean anythingElse(Token t, HtmlTreeBuilder tb) {
            tb.error(this);
            boolean processed = true;
            if (Arrays.asList("table", "tbody", "tfoot", "thead", "tr").contains(tb.currentElement().nodeName())) {
                tb.setFosterInserts(true);
                processed = tb.process(t, InBody);
                tb.setFosterInserts(false);
            } else {
                processed = tb.process(t, InBody);
            }
            return processed;
        }
    },
    InTableText {
        boolean process(Token t, HtmlTreeBuilder tb) {
            switch (t.type) {
                case Character:
                    Token.Character c = t.asCharacter();
                    if (c.getData().equals(nullString)) {
                        tb.error(this);
                        return false;
                    }
                    tb.getPendingTableCharacters().add(c);
                    break;
                default:
                    if (!tb.getPendingTableCharacters().isEmpty()) {
                        for (Token.Character character : tb.getPendingTableCharacters()) {
                            if (isWhitespace(character)) tb.insert(character);
                            else {
                                // InTable anything else section:
                                tb.error(this);
                                if (Arrays.asList("table", "tbody", "tfoot", "thead", "tr").contains(tb.currentElement().nodeName())) {
                                    tb.setFosterInserts(true);
                                    tb.process(character, InBody);
                                    tb.setFosterInserts(false);
                                } else {
                                    tb.process(character, InBody);
                                }
                            }
                        }
                        tb.newPendingTableCharacters();
                    }
                    tb.transition(tb.originalState());
                    return tb.process(t);
            }
            return true;
        }
    },
    InCaption {
        boolean process(Token t, HtmlTreeBuilder tb) {
            if (t.isEndTag() && "caption".equals(t.asEndTag().name())) {
                Token.EndTag endTag = t.asEndTag();
                String name = endTag.name();
                if (tb.inTableScope(name)) {
                    tb.generateImpliedEndTags();
                    if (!"caption".equals(tb.currentElement().nodeName()))
                        tb.error(this);
                    tb.popStackToClose("caption");
                    tb.clearFormattingElementsToLastMarker();
                    tb.transition(InTable);
                } else {
                    tb.error(this);
                    return false;
                }
            } else {
                if (t.isStartTag() && Arrays.asList("caption", "col", "colgroup", "tbody", "td", "tfoot", "th", "thead", "tr").contains(t.asStartTag().name()) ||
                        t.isEndTag() && "table".equals(t.asEndTag().name())
                        ) {
                    tb.error(this);
                    boolean processed = tb.process(new Token.EndTag("caption"));
                    if (processed)
                        return tb.process(t);
                } else {
                    if (t.isEndTag() && Arrays.asList("body", "col", "colgroup", "html", "tbody", "td", "tfoot", "th", "thead", "tr").contains(t.asEndTag().name())) {
                        tb.error(this);
                        return false;
                    } else {
                        return tb.process(t, InBody);
                    }
                }
            }
            return true;
        }
    },
    InColumnGroup {
        boolean process(Token t, HtmlTreeBuilder tb) {
            if (isWhitespace(t)) {
                tb.insert(t.asCharacter());
                return true;
            }
            switch (t.type) {
                case Comment:
                    tb.insert(t.asComment());
                    break;
                case Doctype:
                    tb.error(this);
                    break;
                case StartTag:
                    Token.StartTag startTag = t.asStartTag();
                    String name = startTag.name();
                    if ("html".equals(name))
                        return tb.process(t, InBody);
                    if ("col".equals(name))
                        tb.insertEmpty(startTag);
                    else
                        return anythingElse(t, tb);
                    break;
                case EndTag:
                    Token.EndTag endTag = t.asEndTag();
                    name = endTag.name();
                    if ("colgroup".equals(name)) {
                        if ("html".equals(tb.currentElement().nodeName())) { // frag case
                            tb.error(this);
                            return false;
                        } else {
                            tb.pop();
                            tb.transition(InTable);
                        }
                    } else
                        return anythingElse(t, tb);
                    break;
                case EOF:
                    return "html".equals(tb.currentElement().nodeName()) ? true : anythingElse(t, tb);
                default:
                    return anythingElse(t, tb);
            }
            return true;
        }

        private boolean anythingElse(Token t, TreeBuilder tb) {
            boolean processed = tb.process(new Token.EndTag("colgroup"));
            return !processed || tb.process(t);
        }
    },
    InTableBody {
        boolean process(Token t, HtmlTreeBuilder tb) {
            switch (t.type) {
                case StartTag:
                    Token.StartTag startTag = t.asStartTag();
                    String name = startTag.name();
                    if ("tr".equals(name)) {
                        tb.clearStackToTableBodyContext();
                        tb.insert(startTag);
                        tb.transition(InRow);
                    } else {
                        if (Arrays.asList("th", "td").contains(name)) {
                            tb.error(this);
                            tb.process(new Token.StartTag("tr"));
                            return tb.process(startTag);
                        } else {
                            return Arrays.asList("caption", "col", "colgroup", "tbody", "tfoot", "thead").contains(name) ? exitTableBody(t, tb) : anythingElse(t, tb);
                        }
                    }
                    break;
                case EndTag:
                    Token.EndTag endTag = t.asEndTag();
                    name = endTag.name();
                    if (Arrays.asList("tbody", "tfoot", "thead").contains(name)) {
                        if (tb.inTableScope(name)) {
                            tb.clearStackToTableBodyContext();
                            tb.pop();
                            tb.transition(InTable);
                        } else {
                            tb.error(this);
                            return false;
                        }
                    } else if ("table".equals(name)) {
                        return exitTableBody(t, tb);
                    } else {
                        if (Arrays.asList("body", "caption", "col", "colgroup", "html", "td", "th", "tr").contains(name)) {
                            tb.error(this);
                            return false;
                        } else
                            return anythingElse(t, tb);
                    }
                    break;
                default:
                    return anythingElse(t, tb);
            }
            return true;
        }

        private boolean exitTableBody(Token t, HtmlTreeBuilder tb) {
            if (!(tb.inTableScope("tbody") || tb.inTableScope("thead") || tb.inScope("tfoot"))) {
                // frag case
                tb.error(this);
                return false;
            }
            tb.clearStackToTableBodyContext();
            tb.process(new Token.EndTag(tb.currentElement().nodeName())); // tbody, tfoot, thead
            return tb.process(t);
        }

        private boolean anythingElse(Token t, HtmlTreeBuilder tb) {
            return tb.process(t, InTable);
        }
    },
    InRow {
        boolean process(Token t, HtmlTreeBuilder tb) {
            if (t.isStartTag()) {
                Token.StartTag startTag = t.asStartTag();
                String name = startTag.name();

                if (Arrays.asList("th", "td").contains(name)) {
                    tb.clearStackToTableRowContext();
                    tb.insert(startTag);
                    tb.transition(InCell);
                    tb.insertMarkerToFormattingElements();
                } else {
                    return Arrays.asList("caption", "col", "colgroup", "tbody", "tfoot", "thead", "tr").contains(name) ? handleMissingTr(t, tb) : anythingElse(t, tb);
                }
            } else if (t.isEndTag()) {
                Token.EndTag endTag = t.asEndTag();
                String name = endTag.name();

                if ("tr".equals(name)) {
                    if (!tb.inTableScope(name)) {
                        tb.error(this); // frag
                        return false;
                    }
                    tb.clearStackToTableRowContext();
                    tb.pop(); // tr
                    tb.transition(InTableBody);
                } else if ("table".equals(name)) {
                    return handleMissingTr(t, tb);
                } else {
                    if (Arrays.asList("tbody", "tfoot", "thead").contains(name)) {
                        if (!tb.inTableScope(name)) {
                            tb.error(this);
                            return false;
                        }
                        tb.process(new Token.EndTag("tr"));
                        return tb.process(t);
                    } else {
                        if (Arrays.asList("body", "caption", "col", "colgroup", "html", "td", "th").contains(name)) {
                            tb.error(this);
                            return false;
                        } else {
                            return anythingElse(t, tb);
                        }
                    }
                }
            } else {
                return anythingElse(t, tb);
            }
            return true;
        }

        private boolean anythingElse(Token t, HtmlTreeBuilder tb) {
            return tb.process(t, InTable);
        }

        private boolean handleMissingTr(Token t, TreeBuilder tb) {
            boolean processed = tb.process(new Token.EndTag("tr"));
            return processed ? tb.process(t) : false;
        }
    },
    InCell {
        boolean process(Token t, HtmlTreeBuilder tb) {
            if (t.isEndTag()) {
                Token.EndTag endTag = t.asEndTag();
                String name = endTag.name();

                if (Arrays.asList("td", "th").contains(name)) {
                    if (!tb.inTableScope(name)) {
                        tb.error(this);
                        tb.transition(InRow); // might not be in scope if empty: <td /> and processing fake end tag
                        return false;
                    }
                    tb.generateImpliedEndTags();
                    if (!tb.currentElement().nodeName().equals(name))
                        tb.error(this);
                    tb.popStackToClose(name);
                    tb.clearFormattingElementsToLastMarker();
                    tb.transition(InRow);
                } else {
                    if (Arrays.asList("body", "caption", "col", "colgroup", "html").contains(name)) {
                        tb.error(this);
                        return false;
                    } else {
                        if (Arrays.asList("table", "tbody", "tfoot", "thead", "tr").contains(name)) {
                            if (!tb.inTableScope(name)) {
                                tb.error(this);
                                return false;
                            }
                            closeCell(tb);
                            return tb.process(t);
                        } else {
                            return anythingElse(t, tb);
                        }
                    }
                }
            } else {
                if (t.isStartTag() &&
                        Arrays.asList("caption", "col", "colgroup", "tbody", "td", "tfoot", "th", "thead", "tr").contains(t.asStartTag().name())) {
                    if (!(tb.inTableScope("td") || tb.inTableScope("th"))) {
                        tb.error(this);
                        return false;
                    }
                    closeCell(tb);
                    return tb.process(t);
                } else {
                    return anythingElse(t, tb);
                }
            }
            return true;
        }

        private boolean anythingElse(Token t, HtmlTreeBuilder tb) {
            return tb.process(t, InBody);
        }

        private void closeCell(HtmlTreeBuilder tb) {
            if (tb.inTableScope("td"))
                tb.process(new Token.EndTag("td"));
            else
                tb.process(new Token.EndTag("th")); // only here if th or td in scope
        }
    },
    InSelect {
        boolean process(Token t, HtmlTreeBuilder tb) {
            switch (t.type) {
                case Character:
                    Token.Character c = t.asCharacter();
                    if (c.getData().equals(nullString)) {
                        tb.error(this);
                        return false;
                    }
                    tb.insert(c);
                    break;
                case Comment:
                    tb.insert(t.asComment());
                    break;
                case Doctype:
                    tb.error(this);
                    return false;
                case StartTag:
                    Token.StartTag start = t.asStartTag();
                    String name = start.name();
                    if ("html".equals(name))
                        return tb.process(start, InBody);
                    if ("option".equals(name)) {
                        tb.process(new Token.EndTag("option"));
                        tb.insert(start);
                    } else if ("optgroup".equals(name)) {
                        if ("option".equals(tb.currentElement().nodeName()))
                            tb.process(new Token.EndTag("option"));
                        else if ("optgroup".equals(tb.currentElement().nodeName()))
                            tb.process(new Token.EndTag("optgroup"));
                        tb.insert(start);
                    } else if ("select".equals(name)) {
                        tb.error(this);
                        return tb.process(new Token.EndTag("select"));
                    } else {
                        if (Arrays.asList("input", "keygen", "textarea").contains(name)) {
                            tb.error(this);
                            if (!tb.inSelectScope("select"))
                                return false; // frag
                            tb.process(new Token.EndTag("select"));
                            return tb.process(start);
                        } else return "script".equals(name) ? tb.process(t, InHead) : anythingElse(tb);
                    }
                    break;
                case EndTag:
                    Token.EndTag end = t.asEndTag();
                    name = end.name();
                    switch (name) {
                        case "optgroup":
                            if ("option".equals(tb.currentElement().nodeName()) && tb.aboveOnStack(tb.currentElement()) != null && "optgroup".equals(tb.aboveOnStack(tb.currentElement()).nodeName()))
                                tb.process(new Token.EndTag("option"));
                            if ("optgroup".equals(tb.currentElement().nodeName()))
                                tb.pop();
                            else
                                tb.error(this);
                            break;
                        case "option":
                            if ("option".equals(tb.currentElement().nodeName()))
                                tb.pop();
                            else
                                tb.error(this);
                            break;
                        case "select":
                            if (!tb.inSelectScope(name)) {
                                tb.error(this);
                                return false;
                            }
                            tb.popStackToClose(name);
                            tb.resetInsertionMode();
                            break;
                        default:
                            return anythingElse(tb);
                    }
                    break;
                case EOF:
                    if (!"html".equals(tb.currentElement().nodeName()))
                        tb.error(this);
                    break;
                default:
                    return anythingElse(tb);
            }
            return true;
        }

        private boolean anythingElse(HtmlTreeBuilder tb) {
            tb.error(this);
            return false;
        }
    },
    InSelectInTable {
        boolean process(Token t, HtmlTreeBuilder tb) {
            if (t.isStartTag() && Arrays.asList("caption", "table", "tbody", "tfoot", "thead", "tr", "td", "th").contains(t.asStartTag().name())) {
                tb.error(this);
                tb.process(new Token.EndTag("select"));
                return tb.process(t);
            } else {
                if (t.isEndTag() && Arrays.asList("caption", "table", "tbody", "tfoot", "thead", "tr", "td", "th").contains(t.asEndTag().name())) {
                    tb.error(this);
                    if (tb.inTableScope(t.asEndTag().name())) {
                        tb.process(new Token.EndTag("select"));
                        return tb.process(t);
                    } else
                        return false;
                } else {
                    return tb.process(t, InSelect);
                }
            }
        }
    },
    AfterBody {
        boolean process(Token t, HtmlTreeBuilder tb) {
            if (isWhitespace(t)) {
                return tb.process(t, InBody);
            }
            if (t.isComment()) {
                tb.insert(t.asComment()); // into html node
            } else if (t.isDoctype()) {
                tb.error(this);
                return false;
            } else if (t.isStartTag() && "html".equals(t.asStartTag().name())) {
                return tb.process(t, InBody);
            } else if (t.isEndTag() && "html".equals(t.asEndTag().name())) {
                if (tb.isFragmentParsing()) {
                    tb.error(this);
                    return false;
                } else {
                    tb.transition(AfterAfterBody);
                }
            } else if (t.isEOF()) {
                // chillax! we're done
            } else {
                tb.error(this);
                tb.transition(InBody);
                return tb.process(t);
            }
            return true;
        }
    },
    InFrameset {
        boolean process(Token t, HtmlTreeBuilder tb) {
            if (isWhitespace(t)) {
                tb.insert(t.asCharacter());
            } else if (t.isComment()) {
                tb.insert(t.asComment());
            } else if (t.isDoctype()) {
                tb.error(this);
                return false;
            } else if (t.isStartTag()) {
                Token.StartTag start = t.asStartTag();
                String name = start.name();
                switch (name) {
                    case "html":
                        return tb.process(start, InBody);
                    case "frameset":
                        tb.insert(start);
                        break;
                    case "frame":
                        tb.insertEmpty(start);
                        break;
                    case "noframes":
                        return tb.process(start, InHead);
                    default:
                        tb.error(this);
                        return false;
                }
            } else if (t.isEndTag() && "frameset".equals(t.asEndTag().name())) {
                if ("html".equals(tb.currentElement().nodeName())) { // frag
                    tb.error(this);
                    return false;
                } else {
                    tb.pop();
                    if (!tb.isFragmentParsing() && !"frameset".equals(tb.currentElement().nodeName())) {
                        tb.transition(AfterFrameset);
                    }
                }
            } else if (t.isEOF()) {
                if (!"html".equals(tb.currentElement().nodeName())) {
                    tb.error(this);
                    return true;
                }
            } else {
                tb.error(this);
                return false;
            }
            return true;
        }
    },
    AfterFrameset {
        boolean process(Token t, HtmlTreeBuilder tb) {
            if (isWhitespace(t)) {
                tb.insert(t.asCharacter());
            } else if (t.isComment()) {
                tb.insert(t.asComment());
            } else if (t.isDoctype()) {
                tb.error(this);
                return false;
            } else if (t.isStartTag() && "html".equals(t.asStartTag().name())) {
                return tb.process(t, InBody);
            } else if (t.isEndTag() && "html".equals(t.asEndTag().name())) {
                tb.transition(AfterAfterFrameset);
            } else if (t.isStartTag() && "noframes".equals(t.asStartTag().name())) {
                return tb.process(t, InHead);
            } else if (t.isEOF()) {
                // cool your heels, we're complete
            } else {
                tb.error(this);
                return false;
            }
            return true;
        }
    },
    AfterAfterBody {
        boolean process(Token t, HtmlTreeBuilder tb) {
            if (t.isComment()) {
                tb.insert(t.asComment());
            } else if (t.isDoctype() || isWhitespace(t) || t.isStartTag() && "html".equals(t.asStartTag().name())) {
                return tb.process(t, InBody);
            } else if (t.isEOF()) {
                // nice work chuck
            } else {
                tb.error(this);
                tb.transition(InBody);
                return tb.process(t);
            }
            return true;
        }
    },
    AfterAfterFrameset {
        boolean process(Token t, HtmlTreeBuilder tb) {
            if (t.isComment()) {
                tb.insert(t.asComment());
            } else if (t.isDoctype() || isWhitespace(t) || t.isStartTag() && "html".equals(t.asStartTag().name())) {
                return tb.process(t, InBody);
            } else if (t.isEOF()) {
                // nice work chuck
            } else if (t.isStartTag() && "noframes".equals(t.asStartTag().name())) {
                return tb.process(t, InHead);
            } else {
                tb.error(this);
                return false;
            }
            return true;
        }
    },
     ForeignContent {
        boolean process(Token t, HtmlTreeBuilder tb) {
            return true;
            // todo: implement. Also; how do we get here?
        }
    };

    private static final String nullString = String.valueOf('\u0000');

    abstract boolean process(Token t, HtmlTreeBuilder tb);

    private static boolean isWhitespace(Token t) {
        if (t.isCharacter()) {
            String data = t.asCharacter().getData();
            // todo: this checks more than spec - "\t", "\n", "\f", "\r", " "
            for (int i = 0; i < data.length(); i++) {
                char c = data.charAt(i);
                if (!StringUtil.isWhitespace(c))
                    return false;
            }
            return true;
        }
        return false;
    }

    private static void handleRcData(Token.StartTag startTag, HtmlTreeBuilder tb) {
        tb.insert(startTag);
        tb.tokeniser.transition(TokeniserState.Rcdata);
        tb.markInsertionMode();
        tb.transition(Text);
    }

    private static void handleRawtext(Token.StartTag startTag, HtmlTreeBuilder tb) {
        tb.insert(startTag);
        tb.tokeniser.transition(TokeniserState.Rawtext);
        tb.markInsertionMode();
        tb.transition(Text);
    }
}
