package org.jsoup.parser;

import org.jsoup.helper.Validate;

import java.util.Locale;

/**
 CharacterReader consumes tokens off a string. To replace the old TokenQueue.
 */
class CharacterReader {
    static final char EOF = (char) -1;

    private final char[] input;
    private final int length;
    private int pos;
    private int mark;

    CharacterReader(String input) {
        Validate.notNull(input);
        this.input = input.toCharArray();
        length = this.input.length;
    }

    int pos() {
        return pos;
    }

    boolean isEmpty() {
        return pos >= length;
    }

    char current() {
        return isEmpty() ? EOF : input[pos];
    }

    char consume() {
        char val = isEmpty() ? EOF : input[pos];
        pos++;
        return val;
    }

    void unconsume() {
        pos--;
    }

    void advance() {
        pos++;
    }

    void mark() {
        mark = pos;
    }

    void rewindToMark() {
        pos = mark;
    }

// --Commented out by Inspection START (3/20/13 10:02 AM):
//    String consumeAsString() {
//        String result = new String(input, pos, 1);
//        pos++;
//        return result;
//    }
// --Commented out by Inspection STOP (3/20/13 10:02 AM)

    /**
     * Returns the number of characters between the current position and the next instance of the input char
     * @param c scan target
     * @return offset between current position and next instance of target. -1 if not found.
     */
    int nextIndexOf(char c) {
        // doesn't handle scanning for surrogates
        for (int i = pos; i < length; i++) {
            if (c == input[i])
                return i - pos;
        }
        return -1;
    }

    /**
     * Returns the number of characters between the current position and the next instance of the input sequence
     *
     * @param seq scan target
     * @return offset between current position and next instance of target. -1 if not found.
     */
    int nextIndexOf(CharSequence seq) {
        // doesn't handle scanning for surrogates
        char startChar = seq.charAt(0);
        int offset = pos;
        while (offset < length) {
            // scan to first instance of startchar:
            if (startChar != input[offset])
                while(++offset < length && startChar != input[offset]);
            if (offset < length) {
                int i = offset + 1;
                int last = i + seq.length()-1;
                for (int j = 1; i < last && seq.charAt(j) == input[i]; i++, j++);
                if (i == last) // found full sequence
                    return offset - pos;
            }
            offset++;
        }
        return -1;
    }

    String consumeTo(char c) {
        int offset = nextIndexOf(c);
        if (offset == -1) {
            return consumeToEnd();
        } else {
            String consumed = new String(input, pos, offset);
            pos += offset;
            return consumed;
        }
    }

    String consumeTo(CharSequence seq) {
        int offset = nextIndexOf(seq);
        if (offset == -1) {
            return consumeToEnd();
        } else {
            String consumed = new String(input, pos, offset);
            pos += offset;
            return consumed;
        }
    }

    String consumeToAny(char... chars) {


        int start;
        OUTER:
        for (start = pos; pos < length; pos++) {
            for (char aChar : chars) {
                if (input[pos] == aChar)
                    break OUTER;
            }

        }

        return pos > start ? new String(input, start, pos - start) : "";
    }

    String consumeToEnd() {
        String data = new String(input, pos, length-pos);
        pos = length;
        return data;
    }

    String consumeLetterSequence() {
        int start = pos;
        while (pos < length) {
            char c = input[pos];
            if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')
                pos++;
            else
                break;
        }

        return new String(input, start, pos - start);
    }

    String consumeLetterThenDigitSequence() {
        int start = pos;
        while (pos < length) {
            char c = input[pos];
            if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')
                pos++;
            else
                break;
        }
        while (!isEmpty()) {
            char c = input[pos];
            if (c >= '0' && c <= '9')
                pos++;
            else
                break;
        }

        return new String(input, start, pos - start);
    }

    String consumeHexSequence() {
        int start = pos;
        while (pos < length) {
            char c = input[pos];
            if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f')
                pos++;
            else
                break;
        }
        return new String(input, start, pos - start);
    }

    String consumeDigitSequence() {
        int start = pos;
        while (pos < length) {
            char c = input[pos];
            if (c >= '0' && c <= '9')
                pos++;
            else
                break;
        }
        return new String(input, start, pos - start);
    }

    boolean matches(char c) {
        return !isEmpty() && input[pos] == c;

    }

    boolean matches(CharSequence seq) {
        int scanLength = seq.length();
        if (scanLength > length - pos)
            return false;

        for (int offset = 0; offset < scanLength; offset++)
            if (seq.charAt(offset) != input[pos+offset])
                return false;
        return true;
    }

    boolean matchesIgnoreCase(CharSequence seq) {
        int scanLength = seq.length();
        if (scanLength > length - pos)
            return false;

        for (int offset = 0; offset < scanLength; offset++) {
            char upScan = Character.toUpperCase(seq.charAt(offset));
            char upTarget = Character.toUpperCase(input[pos + offset]);
            if (upScan != upTarget)
                return false;
        }
        return true;
    }

    boolean matchesAny(char... seq) {
        if (isEmpty())
            return false;

        char c = input[pos];
        for (char seek : seq) {
            if (seek == c)
                return true;
        }
        return false;
    }

    boolean matchesLetter() {
        if (isEmpty())
            return false;
        char c = input[pos];
        return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z';
    }

    boolean matchesDigit() {
        if (isEmpty())
            return false;
        char c = input[pos];
        return c >= '0' && c <= '9';
    }

    boolean matchConsume(String seq) {
        if (matches(seq)) {
            pos += seq.length();
            return true;
        } else {
            return false;
        }
    }

    boolean matchConsumeIgnoreCase(String seq) {
        if (matchesIgnoreCase(seq)) {
            pos += seq.length();
            return true;
        } else {
            return false;
        }
    }

    boolean containsIgnoreCase(String seq) {
        // used to check presence of </title>, </style>. only finds consistent case.
        String loScan = seq.toLowerCase(Locale.ENGLISH);
        String hiScan = seq.toUpperCase(Locale.ENGLISH);
        return nextIndexOf(loScan) > -1 || nextIndexOf(hiScan) > -1;
    }

    @Override
    public String toString() {
        return new String(input, pos, length - pos);
    }
}
