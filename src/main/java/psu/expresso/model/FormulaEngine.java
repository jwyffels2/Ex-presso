package psu.expresso.model;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.awt.Point;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;

/**
 * Evaluates “=A1 + SUM(B1:B3)”-style formulas,
 * now with support for unquoted ranges like A1:B3.
 */
public class FormulaEngine {
    // matches single cell refs (A1, B12, AA100, etc.)
    private static final Pattern REF_PATTERN   = Pattern.compile("([A-Za-z]+\\d+)");
    // matches ranges (A1:B3, C5:D10), optionally with spaces around the colon
    private static final Pattern RANGE_PATTERN = Pattern.compile("([A-Za-z]+\\d+)\\s*:\\s*([A-Za-z]+\\d+)");

    private final SpreadsheetModel<Object> model;

    public FormulaEngine(SpreadsheetModel<Object> model) {
        this.model = model;
        FormulaFunctions.init(model);
    }

    /**
     * Install a formula into (row,col).  Whenever any dependency changes,
     * we re-run and store the result in displayValue.
     */
    public void applyFormula(String rawFormula, int targetRow, int targetCol) {
        CellDataModel<Object> target = model.getOrCreateCell(targetRow, targetCol);

        // 1) store the user's raw text
        target.setValue(rawFormula);

        // 2) re-evaluate on any update (raw text or any observed cell)
        target.OnUpdate(src ->
                Platform.runLater(() -> evaluateAndStore(target))
        );

        // 3) observe each single‐cell ref so updates cascade
        String expr = rawFormula.startsWith("=")
                ? rawFormula.substring(1)
                : rawFormula;
        for (String ref : extractRefs(expr)) {
            Point p = CellRef.parse(ref);
            target.observe(model.getOrCreateCell(p.x, p.y));
        }

        // 4) initial evaluation
        evaluateAndStore(target);
    }

    /** Find all single refs (A1, B2, …) in the expression. */
    private Set<String> extractRefs(String expr) {
        Matcher m = REF_PATTERN.matcher(expr);
        Set<String> out = new LinkedHashSet<>();
        while (m.find()) out.add(m.group(1).toUpperCase());
        return out;
    }

    /**
     * Turns "A1:B3" into the Groovy string literal "'A1:B3'".
     * That way SUM('A1:B3') passes a real String into SUM().
     */
    private String quoteRanges(String expr) {
        Matcher m = RANGE_PATTERN.matcher(expr);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            // group(1) is start ref, group(2) is end ref
            String start = m.group(1).toUpperCase();
            String end   = m.group(2).toUpperCase();
            String quoted = "'" + start + ":" + end + "'";
            m.appendReplacement(sb, quoted);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Build and run the Groovy script, then write the
     * result into target.setDisplayValue(...).
     */
    private void evaluateAndStore(CellDataModel<Object> target) {
        Object raw = target.getValue();
        // non-formula → echo it straight through
        if (!(raw instanceof String s) || !s.startsWith("=")) {
            target.setDisplayValue(raw);
            return;
        }

        // 1) strip "="
        String expr = s.substring(1);

        // 2) convert any A1:B3 → "'A1:B3'"
        expr = quoteRanges(expr);

        // 3) build the script with static imports for SUM, AVG, etc.
        String script =
                "import static psu.expresso.model.FormulaFunctions.*\n" +
                        "return (" + expr + ")";

        // 4) bind each single‐cell ref name to its displayValue
        Binding binding = new Binding();
        for (String ref : extractRefs(expr)) {
            Point p = CellRef.parse(ref);
            Object dv = model.getCellIfExists(p.x, p.y)
                    .map(CellDataModel::getDisplayValue)
                    .orElse(null);
            binding.setVariable(ref, dv);
        }

        // 5) evaluate and catch errors
        Object result;
        try {
            result = new GroovyShell(binding).evaluate(script);
        } catch (Exception ex) {
            result = "#ERROR";
        }

        // 6) store into displayValue (fires downstream observers)
        target.setDisplayValue(result);
    }
}
