package com.example.assigment01deepspaceanalyser;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ControllerTest {
    private Controller controller;

    @BeforeEach
    void setUp() {
        controller = new Controller();
        controller.view = new ImageView();
        controller.blackAndWhiteView = new ImageView();
        controller.stage = new Stage();
        controller.fileName = new Label();
        controller.fileSize = new Label();
    }

    @AfterEach
    void tearDown() {
        controller = null;
        controller.view = null;
        controller.blackAndWhiteView = null;
        controller.stage = null;
        controller.fileName = null;
        controller.fileSize = null;


    }

    @Test
    void testOpen() throws IOException {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(controller.stage);
        if (file != null) {
            file.toURI();
            Image image = new Image(file.toURI().toString(), 512, 512, false, true);
            controller.view.setImage(image);
            controller.blackAndWhiteView.setImage(image);
            controller.blackAndWhite(controller.blackAndWhiteView, 128);
            controller.fileName.setText(file.getName());
            controller.fileSize.setText(file.length() + "KB");

            Assertions.assertNotNull(controller.view.getImage());
            Assertions.assertNotNull(controller.blackAndWhiteView.getImage());
            assertEquals("image.jpg", controller.fileName.getText());
            Assertions.assertTrue(controller.fileSize.getText().endsWith("KB"));
        }
    }

    @Test
    void testBlackAndWhite() {
        int[] imageArray = controller.blackAndWhite(controller.blackAndWhiteView, 128);
        Assertions.assertNotNull(imageArray);
        assertEquals(262144, imageArray.length);
    }

    @Test
    void testPrintImageArray() {
        // create a new ByteArrayOutputStream to capture the output of System.out.println
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // create an instance of your class
        Controller controller = new Controller();

        // call the printImageArray method
        controller.printImageArray();

        // assert that the output matches the expected output
        String expectedOutput = Arrays.toString(controller.imageArray) + "\n"; // assuming imageArray is an instance variable
        assertEquals(expectedOutput, outContent.toString());
    }

}