package psu.expresso.model;

public class ObserverTest {
    public static void main(String[] args) {
        // Create the subscribedCell cell (cellDataModelA) that holds a String value.
        // Think of cellDataModelA as a cell that publishes its updates to any subscribers.
        CellDataModel<String> cellDataModelA = new CellDataModel<>();
        cellDataModelA.setValue("Initial Value A");

        // Create the subscriber cell (cellDataModelB).
        // cellDataModelB will subscribe to cellDataModelA so that it gets notified when cellDataModelA changes.
        CellDataModel<String> cellDataModelB = new CellDataModel<>();
        cellDataModelB.setValue("Initial Value B");

        // Define cellDataModelB's update callback via the OnUpdate method.
        // This lambda acts as a subscription handler. When a subscribed cell publishes a new value,
        // this callback is executed: it prints the subscribedCell’s new value and updates cellDataModelB accordingly.
        cellDataModelB.OnUpdate(subscribedCellDataModel -> {
            System.out.println("cellDataModelB received update: subscribedCellDataModel's new value is: " + subscribedCellDataModel.getValue());
            // Optionally, cellDataModelB can update its own state based on the subscribedCellDataModel's new value.
            cellDataModelB.setValue("Updated from " + subscribedCellDataModel.getValue());
        });

        // cellDataModelB subscribes to cellDataModelA.
        // This means cellDataModelB will now receive notifications (updates) whenever cellDataModelA's value changes.
        var subscribeResult = cellDataModelB.observe(cellDataModelA);
        // Changing cellDataModelA's value will trigger cellDataModelB's update callback.
        cellDataModelA.setValue("New Value A");

        // At this point, cellDataModelB's update callback should have been invoked and its value updated.
        System.out.println("cellDataModelB's updated value: " + cellDataModelB.getValue());

        // Now cellDataModelB decides it no longer wants to be updated by cellDataModelA. It unsubscribes.
        var unsubscribeResult = cellDataModelB.unobserve(cellDataModelA);
        System.out.println("cellDataModelB has unsubscribed from cellDataModelA: " + unsubscribeResult);

        // Changing cellDataModelA's value now will not notify cellDataModelB, as it is no longer subscribed.
        System.out.println("Changing cellDataModelA's value to 'Another New Value A' (cellDataModelB should not update)");
        cellDataModelA.setValue("Another New Value A");
    }
}
