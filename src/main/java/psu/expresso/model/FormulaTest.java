package psu.expresso.model;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import javafx.application.Platform;

import java.awt.*;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormulaTest {
    public static void main(String[] args) {
        // Create an instance of a CellDataModel and set an initial value.
        CellDataModel<String> cellDataModelA = new CellDataModel<>();
        cellDataModelA.setValue("Initial Value");

        // Create a binding and expose the cell with a variable name.
        Binding binding = new Binding();
        binding.setVariable("cellA", cellDataModelA);

        // Initialize GroovyShell with the binding.
        GroovyShell shell = new GroovyShell(binding);

        // Groovy script that reads the cell value and updates it.
        String script =
                "println \"CellA's initial value: \" + cellDataModelA.value\n" +
                        "cellA.value= 'Updated via Groovy!'\n" +
                        "return cellDataModelA.value";

        // Execute the script.
        Object updatedValue = shell.evaluate(script);

        // Output the new cell value.
        System.out.println("CellA's new value: " + updatedValue);
    }

    public static class FormulaEngine {
        private static final Pattern REF_PATTERN =
                Pattern.compile("([A-Za-z]+\\d+)");
        private final SpreadsheetModel<Object> model;

        public FormulaEngine(SpreadsheetModel<Object> model) {
            this.model = model;
        }

        /**
         * Apply a formula (e.g. "=A1 * B2 + 5") into the target cell.
         * Sets up observers on each referenced cell so that changes re-run the formula.
         */
        public void applyFormula(String rawFormula, int targetRow, int targetCol) {
            // strip leading '=' if present
            String expr = rawFormula.startsWith("=")
                    ? rawFormula.substring(1)
                    : rawFormula;

            // find all distinct refs like "A1", "B2"
            Matcher m = REF_PATTERN.matcher(expr);
            Set<String> refs = new LinkedHashSet<>();
            while (m.find()) refs.add(m.group(1));

            // create a Groovy Binding and shell
            Binding binding = new Binding();
            GroovyShell shell = new GroovyShell(binding);

            // install observers and bind variables
            for (String ref : refs) {
                Point p = CellRef.parse(ref);
                CellDataModel<Object> cell = model.getOrCreateCell(p.x, p.y);
                // bind variable name in Groovy to the cell
                binding.setVariable(ref, cell);

                // when that cell updates, re-eval the formula on FX thread
                cell.OnUpdate(src -> Platform.runLater(() ->
                        evaluateAndSet(expr, refs, targetRow, targetCol)
                ));
            }

            // do the initial calculation now
            evaluateAndSet(expr, refs, targetRow, targetCol);
        }

        /**
         * Actually runs shell.evaluate(), writes the result into the target cell.
         */
        private void evaluateAndSet(String expr, Set<String> refs, int row, int col) {
            // rebuild the binding each time so Groovy sees current values:
            Binding binding = new Binding();
            GroovyShell shell = new GroovyShell(binding);
            for (String ref : refs) {
                Point p = CellRef.parse(ref);
                binding.setVariable(ref, model.getOrCreateCell(p.x, p.y));
            }
            // wrap the expression to return the cell’s .value
            String script =
                    refs.stream()
                            .map(r -> "// bind " + r + "\n")
                            .reduce("", String::concat)
                            + "return (" + expr + ")";

            Object result;
            try {
                result = shell.evaluate(script);
            } catch (Exception ex) {
                result = "#ERROR";
            }

            // write into the target
            CellDataModel<Object> target = model.getOrCreateCell(row, col);
            target.setValue(result);
        }
    }
}
