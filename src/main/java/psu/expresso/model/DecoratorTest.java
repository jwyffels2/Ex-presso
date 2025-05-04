/**
 * Code: Luke Stine
 * A testing file for the Decorator Pattern on a Cell object with font and color styling.
 */

package psu.expresso.model;

public class DecoratorTest {
    public static void main(String[] args) {
        // Creates a simple cell with both its value and display value.
        CellDataModel<String> cell = new CellDataModel<>();
        cell.setValue("Hello, World!");
        cell.setDisplayValue("Hello, World!");

        // Decorates the cell with font and color.
        DecoratorIF styled = new FontStyleDecorator(cell, "Arial", "Blue");

        // Displays both the original and styled output.
        System.out.println("Original cell display: " + cell.displayValue());
        System.out.println("Styled cell display: " + styled.displayValue());
    }
}
