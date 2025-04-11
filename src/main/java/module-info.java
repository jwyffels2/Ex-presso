module psu.excel {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires org.apache.groovy;

    opens psu.excel to javafx.fxml;
    exports psu.excel;
    exports psu.excel.model;

}