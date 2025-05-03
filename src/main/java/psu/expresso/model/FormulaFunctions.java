package psu.expresso.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Spreadsheet functions exposed to Groovy formulas.
 * Call init(model) once so these can look up cell values.
 */
public class FormulaFunctions {
    private static SpreadsheetModel<Object> model;

    /** Must be called once at startup by FormulaEngine. */
    public static void init(SpreadsheetModel<Object> m) {
        model = m;
        FormulaArg.init(m);   // ← initialize the FormulaArg static model
    }

    /** Parse + expand every raw arg into a flat list of FormulaArg. */
    private static List<FormulaArg> flatten(Object[] rawArgs) {
        List<FormulaArg> out = new ArrayList<>();
        for (Object o : rawArgs) {
            out.addAll(FormulaArg.parse(o).expand());
        }
        return out;
    }

    /**
     * Folds (reduces) a sequence of formula arguments into a single result.
     * <p>
     * Think of an <em>accumulator</em> as a “running total” (or more generally, a running
     * result) that starts with your {@code identity} value and is updated on each iteration.
     * <ol>
     *   <li>We first turn the raw Groovy inputs into a flat list of {@link FormulaArg}
     *       via {@link #flatten(Object[])}, expanding ranges into single-cell refs.</li>
     *   <li>We initialize our <em>accumulator</em> to {@code identity}.  This is the value
     *       that will be combined with each argument in turn.</li>
     *   <li>On each argument, we call {@code fn.apply(accumulator, arg)}, which returns
     *       a new accumulator.  Conceptually you can imagine a bucket holding the
     *       partial result; each call to {@code fn} pours in the next bit and hands
     *       you back a fresh bucket to continue with.</li>
     *   <li>Once every argument has been processed, we return the final accumulator.</li>
     * </ol>
     *
     * <pre>{@code
     * // SUM example: identity=0.0, fn adds each numeric arg
     * double total = fold(rawArgs, 0.0, (sum, arg) -> {
     *     Number n = arg.asNumber();
     *     return sum + (n == null ? 0 : n.doubleValue());
     * });
     * }</pre>
     *
     * @param rawArgs   the original varargs passed from the Groovy formula
     *                  (e.g. cell-refs like "A1", ranges like "B1:B3", or literals)
     * @param identity  the initial “running result” (accumulator) before processing any args;
     *                  for example:
     *                  <ul>
     *                    <li>0.0 for SUM (because 0 + x = x)</li>
     *                    <li>0 for COUNT</li>
     *                    <li>new StringBuilder() for CONCAT</li>
     *                  </ul>
     * @param fn        a function that takes the current accumulator and one {@link FormulaArg}
     *                  and returns the updated accumulator.  It defines <em>how</em> each
     *                  argument is combined into the running result.
     * @param <R>       the type of the accumulator (and the method’s return value)
     * @return          the final accumulated result after processing every argument
     */
    private static <R> R fold(Object[] rawArgs,
                              R identity,
                              BiFunction<R,FormulaArg,R> fn) {
        R acc = identity;
        for (FormulaArg arg : flatten(rawArgs)) {
            acc = fn.apply(acc, arg);
        }
        return acc;
    }


    /** SUM of all numeric arguments. */
    public static double SUM(Object... rawArgs) {
        return fold(rawArgs, 0.0, (sum,arg) -> {
            Number n = arg.asNumber();
            return sum + (n == null ? 0 : n.doubleValue());
        });
    }

    /** COUNT of all numeric arguments. */
    public static int COUNT(Object... rawArgs) {
        return fold(rawArgs, 0, (cnt,arg) ->
                cnt + (arg.asNumber() != null ? 1 : 0)
        );
    }

    /** AVERAGE of all numeric arguments (0 if none). */
    public static double AVERAGE(Object... rawArgs) {
        double sum   = SUM(rawArgs);
        int    count = COUNT(rawArgs);
        return count == 0 ? 0 : sum / count;
    }

    /** CONCAT all args into a single string. */
    public static String CONCAT(Object... rawArgs) {
        return fold(rawArgs, new StringBuilder(), (sb,arg) -> {
            sb.append(arg.asText());
            return sb;
        }).toString();
    }

    public static double MAX(Object... rawArgs) {
        return fold(rawArgs, Double.NEGATIVE_INFINITY, (m,arg) -> {
            Number n = arg.asNumber();
            return (n == null) ? m : Math.max(m, n.doubleValue());
        });
    }


    // … add MIN, MEDIAN, TEXTJOIN, etc. using the same fold pattern …
}
