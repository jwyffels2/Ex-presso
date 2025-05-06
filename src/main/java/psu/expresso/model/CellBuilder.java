/** Code: Luke Stine
 * Builder class for creating and configuring CellDataModel<T> instances with optional decorators.
 */

package psu.expresso.model;

import java.util.ArrayList;
import java.util.List;

public class CellBuilder<T> {

    private final CellDataModel<T> baseCell; // Base cell.
    private final List<DecoratorFactory> decorators = new ArrayList<>(); // Decorators.

    // Constructor creates a new base CellDataModel.
    public CellBuilder() {
        baseCell = new CellDataModel<>();
    }

    // Sets the value and display value for the cell.
    public CellBuilder<T> withValue(T value) {
        baseCell.setValue(value);
        baseCell.setDisplayValue(value);
        return this;
    }

    // Adds a decorator to apply during build().
    public CellBuilder<T> applyDecorator(DecoratorFactory factory) {
        decorators.add(factory);
        return this;
    }

    // Builds and returns the final decorated cell.
    public DecoratorIF build() {
        DecoratorIF result = baseCell;
        for (DecoratorFactory factory : decorators) {
            result = factory.decorate(result);
        }
        return result;
    }

    // Functional interface to generalize decorators.
    @FunctionalInterface
    public interface DecoratorFactory {
        DecoratorIF decorate(DecoratorIF cell);
    }
}
