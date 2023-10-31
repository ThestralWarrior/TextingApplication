module com.application.textingapplication {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens com.application.textingapplication to javafx.fxml;
    exports com.application.textingapplication;
}