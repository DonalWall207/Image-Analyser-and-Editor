module com.example.assigment01deepspaceanalyser {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires org.junit.jupiter.api;
    //requires org.junit.platform.commons.util;


    opens com.example.assigment01deepspaceanalyser to javafx.fxml;
    exports com.example.assigment01deepspaceanalyser;
   // opens org.junit.platform.commons.util to javafx.fxml;
   // exports org.junit.platform.commons.util;
}