package psu.excel.model;

public class ObserverTest {
    public static void main(String[] args) {
        // Create the subscribedCell cell (cellA) that holds a String value.
        // Think of cellA as a cell that publishes its updates to any subscribers.
        Cell<String> cellA = new Cell<>();
        cellA.setValue("Initial Value A");

        // Create the subscriber cell (cellB).
        // cellB will subscribe to cellA so that it gets notified when cellA changes.
        Cell<String> cellB = new Cell<>();
        cellB.setValue("Initial Value B");

        // Define cellB's update callback via the OnUpdate method.
        // This lambda acts as a subscription handler. When a subscribed cell publishes a new value,
        // this callback is executed: it prints the subscribedCell’s new value and updates cellB accordingly.
        cellB.OnUpdate(subscribedCell -> {
            System.out.println("cellB received update: subscribedCell's new value is: " + subscribedCell.getValue());
            // Optionally, cellB can update its own state based on the subscribedCell's new value.
            cellB.setValue("Updated from " + subscribedCell.getValue());
        });

        // cellB subscribes to cellA.
        // This means cellB will now receive notifications (updates) whenever cellA's value changes.
        var subscribeResult = cellB.observe(cellA);
        // Changing cellA's value will trigger cellB's update callback.
        cellA.setValue("New Value A");

        // At this point, cellB's update callback should have been invoked and its value updated.
        System.out.println("cellB's updated value: " + cellB.getValue());

        // Now cellB decides it no longer wants to be updated by cellA. It unsubscribes.
        var unsubscribeResult = cellB.unobserve(cellA);
        System.out.println("cellB has unsubscribed from cellA: " + unsubscribeResult);

        // Changing cellA's value now will not notify cellB, as it is no longer subscribed.
        System.out.println("Changing cellA's value to 'Another New Value A' (cellB should not update)");
        cellA.setValue("Another New Value A");
    }
}
