package psu.expresso.model;

import java.util.function.Consumer;

/**
 * A concrete CellDataModel class that holds a value of type Type.
 * This class is observable and can both notify and observe other cells.
 */
public class CellDataModel<Type> extends Observable<CellDataModel<?>> implements CellDataModelIF<Type> {

    private Type value;

    // A lambda that an observer can set to define how it updates when notified.
    private Consumer<CellDataModel<?>> updateLambda;

    /**
     * Sets the cell's value and notifies observers of the change.
     */
    public void setValue(Type value) {
        this.value = value;
        // Notify all observers that this cell's value has changed.
        notifyObservers();
    }

    /**
     * Gets the cell's value.
     */
    public Type getValue() {
        return value;
    }

    /**
     * Sets the update function for this cell. If this cell is an observing another cell,
     * this lambda will be executed when it is notified.
     *
     * @param lambda a Consumer that accepts a CellDataModel<?> as its input.
     */
    public void OnUpdate(Consumer<CellDataModel<?>> lambda) {
        this.updateLambda = lambda;
    }

    /**
     * Called by the observable when a change occurs.
     * When this cell is notified, its update lambda is executed if one is defined.
     *
     * @param observer the observer being notified (i.e. this cell's update lambda will be called).
     */
    @Override
    protected void notifyObserver(CellDataModel<?> observer) {
        if (observer.updateLambda == null) {
            // Fallback behavior: simply print a message.
            System.out.println("Notifying observer: " + observer);
            return;
        }
        observer.updateLambda.accept(this);
    }

    @Override
    public String toString() {
        return "CellDataModel [value=" + value + "]";
    }
}
