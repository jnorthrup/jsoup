package org.jsoup.parser;

import org.jsoup.helper.Validate;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;

/**
 * Parse tokens for the Tokeniser.
 */
abstract class Token {
    Token.TokenType type;

    private Token() {
    }
    
    String tokenType() {
        return getClass().getSimpleName();
    }

    static class Doctype extends Token {
        final StringBuilder name = new StringBuilder();
        final StringBuilder publicIdentifier = new StringBuilder();
        final StringBuilder systemIdentifier = new StringBuilder();
        boolean forceQuirks;

        Doctype() {
            type = Token.TokenType.Doctype;
        }

        String getName() {
            return name.toString();
        }

        String getPublicIdentifier() {
            return publicIdentifier.toString();
        }

        public String getSystemIdentifier() {
            return systemIdentifier.toString();
        }

        public boolean isForceQuirks() {
            return forceQuirks;
        }
    }

    abstract static class Tag extends Token {
        String tagName;
        private String pendingAttributeName; // attribute names are generally caught in one hop, not accumulated
        private StringBuilder pendingAttributeValue; // but values are accumulated, from e.g. & in hrefs

        boolean selfClosing;
        Attributes attributes; // start tags get attributes on construction. End tags get attributes on first new attribute (but only for parser convenience, not used).

        void newAttribute() {
            if (attributes == null)
                attributes = new Attributes();

            if (pendingAttributeName != null) {
                Attribute attribute;
                attribute = pendingAttributeValue == null ? new Attribute(pendingAttributeName, "") : new Attribute(pendingAttributeName, pendingAttributeValue.toString());
                attributes.put(attribute);
            }
            pendingAttributeName = null;
            if (pendingAttributeValue != null)
                pendingAttributeValue.delete(0, pendingAttributeValue.length());
        }

        void finaliseTag() {
            // finalises for emit
            if (pendingAttributeName != null) {
                // todo: check if attribute name exists; if so, drop and error
                newAttribute();
            }
        }

        String name() {
            Validate.isFalse(tagName.isEmpty());
            return tagName;
        }

        Token.Tag name(String name) {
            tagName = name;
            return this;
        }

        boolean isSelfClosing() {
            return selfClosing;
        }

        @SuppressWarnings("TypeMayBeWeakened")
        Attributes getAttributes() {
            return attributes;
        }

        // these appenders are rarely hit in not null state-- caused by null chars.
        void appendTagName(String append) {
            tagName = tagName == null ? append : tagName + append;
        }

        void appendTagName(char append) {
            appendTagName(String.valueOf(append));
        }

        void appendAttributeName(String append) {
            pendingAttributeName = pendingAttributeName == null ? append : pendingAttributeName + append;
        }

        void appendAttributeName(char append) {
            appendAttributeName(String.valueOf(append));
        }

        void appendAttributeValue(String append) {
            pendingAttributeValue = pendingAttributeValue == null ? new StringBuilder(append) : pendingAttributeValue.append(append);
        }

        void appendAttributeValue(char append) {
            appendAttributeValue(String.valueOf(append));
        }
    }

    static class StartTag extends Token.Tag {
        StartTag() {
            attributes = new Attributes();
            type = Token.TokenType.StartTag;
        }

        StartTag(String name) {
            this();
            tagName = name;
        }

        StartTag(String name, Attributes attributes) {
            this();
            tagName = name;
            this.attributes = attributes;
        }

        @Override
        public String toString() {
            return attributes != null && attributes.size() > 0 ? '<' + name() + ' ' + attributes.toString() + '>' : '<' + name() + '>';
        }
    }

    static class EndTag extends Token.Tag{
        EndTag() {
            type = Token.TokenType.EndTag;
        }

        EndTag(String name) {
            this();
            tagName = name;
        }

        @Override
        public String toString() {
            return "</" + name() + '>';
        }
    }

    static class Comment extends Token {
        final StringBuilder data = new StringBuilder();
        boolean bogus;

        Comment() {
            type = Token.TokenType.Comment;
        }

        String getData() {
            return data.toString();
        }

        @Override
        public String toString() {
            return "<!--" + getData() + "-->";
        }
    }

    static class Character extends Token {
        private final String data;

        Character(String data) {
            type = Token.TokenType.Character;
            this.data = data;
        }

        String getData() {
            return data;
        }

        @Override
        public String toString() {
            return data;
        }
    }

    static class EOF extends Token {
        EOF() {
            type = Token.TokenType.EOF;
        }
    }

    boolean isDoctype() {
        return type == Token.TokenType.Doctype;
    }

    Token.Doctype asDoctype() {
        return (Token.Doctype) this;
    }

    boolean isStartTag() {
        return type == Token.TokenType.StartTag;
    }

    Token.StartTag asStartTag() {
        return (Token.StartTag) this;
    }

    boolean isEndTag() {
        return type == Token.TokenType.EndTag;
    }

    Token.EndTag asEndTag() {
        return (Token.EndTag) this;
    }

    boolean isComment() {
        return type == Token.TokenType.Comment;
    }

    Token.Comment asComment() {
        return (Token.Comment) this;
    }

    boolean isCharacter() {
        return type == Token.TokenType.Character;
    }

    Token.Character asCharacter() {
        return (Token.Character) this;
    }

    boolean isEOF() {
        return type == Token.TokenType.EOF;
    }

    enum TokenType {
        Doctype,
        StartTag,
        EndTag,
        Comment,
        Character,
        EOF
    }
}
