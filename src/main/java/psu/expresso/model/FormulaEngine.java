package psu.expresso.model;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.awt.Point;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Installs and evaluates "=A1 + SUM(B1:B3)"-style formulas.
 *
 * Now properly clears out old observer links (via unobserve),
 * then wires up new ones, so SELF_OBSERVATION and CYCLE_DETECTED
 * are caught every time.
 */
public class FormulaEngine {
    private static final Pattern REF_PATTERN   = Pattern.compile("([A-Za-z]+\\d+)");
    private static final Pattern RANGE_PATTERN = Pattern.compile("([A-Za-z]+\\d+)\\s*:\\s*([A-Za-z]+\\d+)");
    private final SpreadsheetModel<Object> model;

    public FormulaEngine(SpreadsheetModel<Object> model) {
        this.model = model;
        FormulaFunctions.init(model);
    }

    /**
     * Apply the formula text into the target cell (row,col), clear out
     * any old observer links, then re‐wire to exactly the new dependencies.
     */
    public void applyFormula(String rawFormula, int targetRow, int targetCol) {
        CellDataModel<Object> target = model.getOrCreateCell(targetRow, targetCol);

        // ─── 1) Unobserve all old refs from the previous formula ───────────
        Object prev = target.getValue();
        if (prev instanceof String prevS && prevS.startsWith("=")) {
            String oldExpr = prevS.substring(1);
            for (String oldRef : extractRefs(oldExpr)) {
                Point pOld = CellRef.parse(oldRef);
                model.getCellIfExists(pOld.x, pOld.y)
                        .ifPresent(src -> target.unobserve(src));
            }
        }

        // ─── 2) Store the raw formula and hook re‐evaluation ──────────────
        target.setValue(rawFormula);
        target.OnUpdate(src ->
                Platform.runLater(() -> evaluateAndStore(target))
        );

        // ─── 3) Observe each new dependency, catching real errors ─────────
        String expr = rawFormula.startsWith("=")
                ? rawFormula.substring(1)
                : rawFormula;

        for (String ref : extractRefs(expr)) {
            Point p = CellRef.parse(ref);
            CellDataModel<Object> source = model.getOrCreateCell(p.x, p.y);

            Observable.ErrorCodes code = target.observe(source);
            switch (code) {
                case SUCCESS:
                case DUPLICATE:
                    // OK: either new link, or we already had it
                    break;

                case SELF_OBSERVATION:
                    popup("Formula Error",
                            "Cell cannot reference itself (" + ref + ").");
                    model.removeCell(targetRow, targetCol);
                    return;

                case CYCLE_DETECTED:
                    popup("Formula Error",
                            "That reference would introduce a cycle at " + ref + ".");
                    model.removeCell(targetRow, targetCol);
                    return;

                default:
                    popup("Observer Error",
                            "Cannot watch cell " + ref + ": " + code);
                    model.removeCell(targetRow, targetCol);
                    return;
            }
        }

        // ─── 4) Initial evaluation ────────────────────────────────────────
        evaluateAndStore(target);
    }

    /** Find all A1‐style single references in the expression. */
    private Set<String> extractRefs(String expr) {
        Matcher m = REF_PATTERN.matcher(expr);
        Set<String> out = new LinkedHashSet<>();
        while (m.find()) {
            out.add(m.group(1).toUpperCase());
        }
        return out;
    }

    /** Wrap any A1:B3 into "'A1:B3'" so SUM sees it as a literal range. */
    private String quoteRanges(String expr) {
        Matcher m = RANGE_PATTERN.matcher(expr);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String start = m.group(1).toUpperCase();
            String end   = m.group(2).toUpperCase();
            m.appendReplacement(sb, "'" + start + ":" + end + "'");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Evaluate the formula in target.getValue() with Groovy,
     * write the result into target.setDisplayValue(...).
     */
    private void evaluateAndStore(CellDataModel<Object> target) {
        Object raw = target.getValue();
        if (!(raw instanceof String s) || !s.startsWith("=")) {
            // not a formula → echo it
            target.setDisplayValue(raw);
            return;
        }

        String expr   = quoteRanges(s.substring(1));
        String script =
                "import static psu.expresso.model.FormulaFunctions.*\n" +
                        "return (" + expr + ")";

        Binding binding = new Binding();
        for (String ref : extractRefs(expr)) {
            Point p = CellRef.parse(ref);
            Object dv = model.getCellIfExists(p.x, p.y)
                    .map(CellDataModel::getDisplayValue)
                    .orElse(null);
            binding.setVariable(ref, dv);
        }

        Object result;
        try {
            result = new GroovyShell(binding).evaluate(script);
        } catch (Exception ex) {
            result = "#ERROR";
        }

        target.setDisplayValue(result);
    }

    /** Show an alert on the FX thread. */
    private void popup(String title, String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }
}
