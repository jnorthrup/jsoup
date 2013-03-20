package org.jsoup.select;

import org.jsoup.helper.Validate;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Evaluates that an element matches the selector.
 */
public abstract class Evaluator {
    Evaluator() {
    }

    /**
     * Test if the element meets the evaluator's requirements.
     *
     * @param root    Root of the matching subtree
     * @param element tested element
     */
    public abstract boolean matches(Element root, Element element);

    /**
     * Evaluator for tag name
     */
    public static class Tag extends Evaluator {
        private String tagName;

        public Tag(String tagName) {
            this.tagName = tagName;
        }

        @Override
        public boolean matches(Element root, Element element) {
            return element.tagName().equals(tagName);
        }

        @Override
        public String toString() {
            return String.format("%s", tagName);
        }
    }

    /**
     * Evaluator for element id
     */
    public static class Id extends Evaluator {
        private String id;

        public Id(String id) {
            this.id = id;
        }

        @Override
        public boolean matches(Element root, Element element) {
            return id.equals(element.id());
        }

        @Override
        public String toString() {
            return String.format("#%s", id);
        }

    }

    /**
     * Evaluator for element class
     */
    public static class Class extends Evaluator {
        private String className;

        public Class(String className) {
            this.className = className;
        }

        @Override
        public boolean matches(Element root, Element element) {
            return element.hasClass(className);
        }

        @Override
        public String toString() {
            return String.format(".%s", className);
        }

    }

    /**
     * Evaluator for attribute name matching
     */
    public static class Attribute extends Evaluator {
        private String key;

        public Attribute(String key) {
            this.key = key;
        }

        @Override
        public boolean matches(Element root, Element element) {
            return element.hasAttr(key);
        }

        @Override
        public String toString() {
            return String.format("[%s]", key);
        }

    }

    /**
     * Evaluator for attribute name prefix matching
     */
    public static class AttributeStarting extends Evaluator {
        private String keyPrefix;

        public AttributeStarting(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }

        @Override
        public boolean matches(Element root, Element element) {
            List<org.jsoup.nodes.Attribute> values = element.attributes().asList();
            for (org.jsoup.nodes.Attribute attribute : values) {
                if (attribute.getKey().startsWith(keyPrefix))
                    return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("[^%s]", keyPrefix);
        }

    }

    /**
     * Evaluator for attribute name/value matching
     */
    public static class AttributeWithValue extends Evaluator.AttributeKeyPair {
        public AttributeWithValue(String key, String value) {
            super(key, value);
        }

        @Override
        public boolean matches(Element root, Element element) {
            return element.hasAttr(key) && value.equalsIgnoreCase(element.attr(key));
        }

        @Override
        public String toString() {
            return String.format("[%s=%s]", key, value);
        }

    }

    /**
     * Evaluator for attribute name != value matching
     */
    public static class AttributeWithValueNot extends Evaluator.AttributeKeyPair {
        public AttributeWithValueNot(String key, String value) {
            super(key, value);
        }

        @Override
        public boolean matches(Element root, Element element) {
            return !value.equalsIgnoreCase(element.attr(key));
        }

        @Override
        public String toString() {
            return String.format("[%s!=%s]", key, value);
        }

    }

    /**
     * Evaluator for attribute name/value matching (value prefix)
     */
    public static class AttributeWithValueStarting extends Evaluator.AttributeKeyPair {
        public AttributeWithValueStarting(String key, String value) {
            super(key, value);
        }

        @Override
        public boolean matches(Element root, Element element) {
            return element.hasAttr(key) && element.attr(key).toLowerCase().startsWith(value); // value is lower case already
        }

        @Override
        public String toString() {
            return String.format("[%s^=%s]", key, value);
        }

    }

    /**
     * Evaluator for attribute name/value matching (value ending)
     */
    public static class AttributeWithValueEnding extends Evaluator.AttributeKeyPair {
        public AttributeWithValueEnding(String key, String value) {
            super(key, value);
        }

        @Override
        public boolean matches(Element root, Element element) {
            return element.hasAttr(key) && element.attr(key).toLowerCase().endsWith(value); // value is lower case
        }

        @Override
        public String toString() {
            return String.format("[%s$=%s]", key, value);
        }

    }

    /**
     * Evaluator for attribute name/value matching (value containing)
     */
    public static class AttributeWithValueContaining extends Evaluator.AttributeKeyPair {
        public AttributeWithValueContaining(String key, String value) {
            super(key, value);
        }

        @Override
        public boolean matches(Element root, Element element) {
            return element.hasAttr(key) && element.attr(key).toLowerCase().contains(value); // value is lower case
        }

        @Override
        public String toString() {
            return String.format("[%s*=%s]", key, value);
        }

    }

    /**
     * Evaluator for attribute name/value matching (value regex matching)
     */
    public static class AttributeWithValueMatching extends Evaluator {
        String key;
        Pattern pattern;

        public AttributeWithValueMatching(String key, Pattern pattern) {
            this.key = key.trim().toLowerCase();
            this.pattern = pattern;
        }

        @Override
        public boolean matches(Element root, Element element) {
            return element.hasAttr(key) && pattern.matcher(element.attr(key)).find();
        }

        @Override
        public String toString() {
            return String.format("[%s~=%s]", key, pattern.toString());
        }

    }

    /**
     * Abstract evaluator for attribute name/value matching
     */
    public abstract static class AttributeKeyPair extends Evaluator {
        String key;
        String value;

        public AttributeKeyPair(String key, String value) {
            Validate.notEmpty(key);
            Validate.notEmpty(value);

            this.key = key.trim().toLowerCase();
            this.value = value.trim().toLowerCase();
        }
    }

    /**
     * Evaluator for any / all element matching
     */
    public static class AllElements extends Evaluator {

        @Override
        public boolean matches(Element root, Element element) {
            return true;
        }

        @Override
        public String toString() {
            return "*";
        }
    }

    /**
     * Evaluator for matching by sibling index number (e < idx)
     */
    public static class IndexLessThan extends Evaluator.IndexEvaluator {
        public IndexLessThan(int index) {
            super(index);
        }

        @Override
        public boolean matches(Element root, Element element) {
            return element.elementSiblingIndex() < index;
        }

        @Override
        public String toString() {
            return String.format(":lt(%d)", index);
        }

    }

    /**
     * Evaluator for matching by sibling index number (e > idx)
     */
    public static class IndexGreaterThan extends Evaluator.IndexEvaluator {
        public IndexGreaterThan(int index) {
            super(index);
        }

        @Override
        public boolean matches(Element root, Element element) {
            return element.elementSiblingIndex() > index;
        }

        @Override
        public String toString() {
            return String.format(":gt(%d)", index);
        }

    }

    /**
     * Evaluator for matching by sibling index number (e = idx)
     */
    public static class IndexEquals extends Evaluator.IndexEvaluator {
        public IndexEquals(int index) {
            super(index);
        }

        @Override
        public boolean matches(Element root, Element element) {
            return element.elementSiblingIndex() == index;
        }

        @Override
        public String toString() {
            return String.format(":eq(%d)", index);
        }

    }

    /**
     * Abstract evaluator for sibling index matching
     *
     * @author ant
     */
    public abstract static class IndexEvaluator extends Evaluator {
        int index;

        public IndexEvaluator(int index) {
            this.index = index;
        }
    }

    /**
     * Evaluator for matching Element (and its descendants) text
     */
    public static class ContainsText extends Evaluator {
        private String searchText;

        public ContainsText(String searchText) {
            this.searchText = searchText.toLowerCase();
        }

        @Override
        public boolean matches(Element root, Element element) {
            return element.text().toLowerCase().contains(searchText);
        }

        @Override
        public String toString() {
            return String.format(":contains(%s", searchText);
        }
    }

    /**
     * Evaluator for matching Element's own text
     */
    public static class ContainsOwnText extends Evaluator {
        private String searchText;

        public ContainsOwnText(String searchText) {
            this.searchText = searchText.toLowerCase();
        }

        @Override
        public boolean matches(Element root, Element element) {
            return element.ownText().toLowerCase().contains(searchText);
        }

        @Override
        public String toString() {
            return String.format(":containsOwn(%s", searchText);
        }
    }

    /**
     * Evaluator for matching Element (and its descendants) text with regex
     */
    public static class Matches extends Evaluator {
        private Pattern pattern;

        public Matches(Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public boolean matches(Element root, Element element) {
            Matcher m = pattern.matcher(element.text());
            return m.find();
        }

        @Override
        public String toString() {
            return String.format(":matches(%s", pattern);
        }
    }

    /**
     * Evaluator for matching Element's own text with regex
     */
    public static class MatchesOwn extends Evaluator {
        private Pattern pattern;

        public MatchesOwn(Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public boolean matches(Element root, Element element) {
            Matcher m = pattern.matcher(element.ownText());
            return m.find();
        }

        @Override
        public String toString() {
            return String.format(":matchesOwn(%s", pattern);
        }
    }
}
