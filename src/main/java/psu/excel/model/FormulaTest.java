package psu.excel.model;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class FormulaTest {
    public static void main(String[] args) {
        // Create an instance of a Cell and set an initial value.
        Cell<String> cellA = new Cell<>();
        cellA.setValue("Initial Value");

        // Create a binding and expose the cell with a variable name.
        Binding binding = new Binding();
        binding.setVariable("cellA", cellA);

        // Initialize GroovyShell with the binding.
        GroovyShell shell = new GroovyShell(binding);

        // Groovy script that reads the cell value and updates it.
        String script =
                "println \"CellA's initial value: \" + cellA.value\n" +
                        "cellA.value= 'Updated via Groovy!'\n" +
                        "return cellA.value";

        // Execute the script.
        Object updatedValue = shell.evaluate(script);

        // Output the new cell value.
        System.out.println("CellA's new value: " + updatedValue);
    }
}
