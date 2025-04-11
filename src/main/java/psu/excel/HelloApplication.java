package psu.excel;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Create a Label that says "Hello, JavaFX!"
        Label helloLabel = new Label("Hello, JavaFX!");
        // Create a layout container and add the label to it
        StackPane root = new StackPane(helloLabel);
        // Create a scene with the layout container, set its width and height
        Scene scene = new Scene(root, 400, 300);
        // Set the stage (window) title
        primaryStage.setTitle("Hello World JavaFX");
        // Add the scene to the stage
        primaryStage.setScene(scene);
        // Display the stage
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Launch the JavaFX application
        launch(args);
    }
}
