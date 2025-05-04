/** Code: Luke Stine
 * A test class for the Filter design.
 */

package psu.expresso.model;

import java.util.ArrayList;
import java.util.List;

public class FilterTest {
    public static void main(String[] args) { // Main function to test out the Filtering design.

        List<CellDataModel<?>> column = new ArrayList<>(); // Creates a list representing a column of cells

        // Creates sample column of cells.
        column.add(createCell("Apple"));
        column.add(createCell("Banana"));
        column.add(createCell("Orange"));
        column.add(createCell("Grape"));

        // Creates a filter to find cells with the value "Apple".
        CellFilterIF filter = new ValueCellFilter("Apple");

        System.out.println("Filtering cells with value 'Apple':");

        // Applies filtering to the cells.
        for (CellDataModel<?> cell : column) {
            if (filter.filter(cell)) {
                System.out.println("Matched Cell: " + cell.getValue());
            }
        }
    }

    // A utility to create and initialize a Cell with a given value.
    private static <T> CellDataModel<T> createCell(T value) {
        CellDataModel<T> cell = new CellDataModel<>();
        cell.setValue(value);
        return cell;
    }
}
