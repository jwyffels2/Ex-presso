/**
 * Code: Luke Stine
 * A decorator that applies font style and color to a cell's displayed value.
 */

package psu.expresso.model;

public class FontStyleDecorator implements DecoratorIF {

    private final DecoratorIF wrappedCell;
    private final String font;
    private final String color;

    /**
     * Constructs a decorator with font and color styles which can be applied to a cell.
     *
     * @param cell  The original cell to decorate.
     * @param font  Font style of the cell (e.g., "Arial").
     * @param color Text color of the cell (e.g., "blue").
     */
    public FontStyleDecorator(DecoratorIF cell, String font, String color) {
        this.wrappedCell = cell;
        this.font = font;
        this.color = color;
    }

    /**
     * Returns a styled string combining font and color around the cell's display value.
     *
     * @return A formatted string.
     */
    @Override
    public String displayValue() {
        // Optionally, return decorated value with added styling
        return "<span style='font-family:" + font + "; color:" + color + ";'>" + wrappedCell.displayValue() + "</span>";
    }
}
