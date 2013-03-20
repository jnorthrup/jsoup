package org.jsoup.select;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Base combining (and, or) evaluator.
 */
abstract class CombiningEvaluator extends Evaluator {
    final List<Evaluator> evaluators;

    CombiningEvaluator() {
        evaluators = new ArrayList<>();
    }

    CombiningEvaluator(Collection<Evaluator> evaluators) {
        this();
        this.evaluators.addAll(evaluators);
    }

    Evaluator rightMostEvaluator() {
        return evaluators.isEmpty() ? null : evaluators.get(evaluators.size() - 1);
    }
    
    void replaceRightMostEvaluator(Evaluator replacement) {
        evaluators.set(evaluators.size() - 1, replacement);
    }

    static final class And extends CombiningEvaluator {
        And(Collection<Evaluator> evaluators) {
            super(evaluators);
        }

        And(Evaluator... evaluators) {
            this(Arrays.asList(evaluators));
        }

        @Override
        public boolean matches(Element root, Element node) {
            for (Evaluator s : evaluators) {
                if (!s.matches(root, node))
                    return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return StringUtil.join(" ", evaluators);
        }
    }

    static final class Or extends CombiningEvaluator {
// --Commented out by Inspection START (3/20/13 10:02 AM):
//        /**
//         * Create a new Or evaluator. The initial evaluators are ANDed together and used as the first clause of the OR.
//         * @param evaluators initial OR clause (these are wrapped into an AND evaluator).
//         */
//        Or(Collection<Evaluator> evaluators) {
//            if (evaluators.size() > 1)
//                this.evaluators.add(new CombiningEvaluator.And(evaluators));
//            else // 0 or 1
//                this.evaluators.addAll(evaluators);
//        }
// --Commented out by Inspection STOP (3/20/13 10:02 AM)

        Or() {
        }

        public void add(Evaluator e) {
            evaluators.add(e);
        }

        @Override
        public boolean matches(Element root, Element node) {
            for (Evaluator s : evaluators) {
                if (s.matches(root, node))
                    return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format(":or%s", evaluators);
        }
    }
}
