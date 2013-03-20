package org.jsoup.parser;

import java.util.ArrayList;

/**
 * A container for ParseErrors.
 * 
 * @author Jonathan Hedley
 */
class ParseErrorList extends ArrayList<ParseError>{
    private static final int INITIAL_CAPACITY = 16;
    private final int maxSize;
    
    private ParseErrorList(int initialCapacity, int maxSize) {
        super(initialCapacity);
        this.maxSize = maxSize;
    }
    
    boolean canAddError() {
        return size() < maxSize;
    }

// --Commented out by Inspection START (3/20/13 10:02 AM):
//    int getMaxSize() {
//        return maxSize;
//    }
// --Commented out by Inspection STOP (3/20/13 10:02 AM)

    static ParseErrorList noTracking() {
        return new ParseErrorList(0, 0);
    }
    
    static ParseErrorList tracking(int maxSize) {
        return new ParseErrorList(INITIAL_CAPACITY, maxSize);
    }
}
