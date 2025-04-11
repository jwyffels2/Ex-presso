module psu.expresso {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires org.apache.groovy;

    opens psu.expresso to javafx.fxml;
    exports psu.expresso;
    exports psu.expresso.model;

}