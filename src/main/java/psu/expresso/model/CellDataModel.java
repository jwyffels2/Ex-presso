// src/main/java/psu/expresso/model/CellDataModel.java
package psu.expresso.model;

import java.util.function.Consumer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Holds:
 *  • value:        raw input (formula or literal)
 *  • displayValue: computed result for display
 */
public class CellDataModel<T>
        extends Observable<CellDataModel<T>>
        implements CellDataModelIF<T>, DecoratorIF
{
    private final ObjectProperty<T> value        = new SimpleObjectProperty<>();
    private final ObjectProperty<T> displayValue = new SimpleObjectProperty<>();
    private Consumer<CellDataModel<T>> updateLambda;

    @Override
    public T getValue() {
        return value.get();
    }
    @Override
    public void setValue(T newValue) {
        value.set(newValue);
        notifyObservers();
    }
    public ObjectProperty<T> valueProperty() {
        return value;
    }

    public T getDisplayValue() {
        return displayValue.get();
    }
    public void setDisplayValue(T dv) {
        displayValue.set(dv);
        notifyObservers();
    }
    public ObjectProperty<T> displayValueProperty() {
        return displayValue;
    }

    public void OnUpdate(Consumer<CellDataModel<T>> lambda) {
        this.updateLambda = lambda;
    }
    @Override
    protected void notifyObserver(CellDataModel<T> obs) {
        if (obs.updateLambda != null) {
            obs.updateLambda.accept(this);
        }
    }

    @Override
    public String toString() {
        return "Cell{" + getDisplayValue() + "}";
    }

    @Override
    public String displayValue() {
        T dv = getDisplayValue();
        return dv != null ? dv.toString() : "";
    }
}
