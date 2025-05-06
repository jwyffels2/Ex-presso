/** Code: Luke Stine
 * Tests the Builder Pattern to create a styled CellDataModel.
 */

package psu.expresso.model;

public class BuilderTest {
    public static void main(String[] args) {

        // Use builder to construct and decorate a cell.
        DecoratorIF styledCell = new CellBuilder<String>()
                .withValue("Builder Cell")
                .applyDecorator(cell -> new FontStyleDecorator(cell, "Arial", "Blue"))
                .applyDecorator(cell -> new BorderDecorator(cell))
                .build();

        // Prints result with border for the styled cell.
        System.out.println("Styled cell: " + styledCell.displayValue());
    }
}
