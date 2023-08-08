package com.example.assigment01deepspaceanalyser;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.util.*;


public class Controller {
    @FXML
    ImageView view;
    Stage stage;
    @FXML
    ImageView blackAndWhiteView;
    @FXML
    Label fileName;
    @FXML
    Label fileSize;

    public WritableImage ii ;

    public void open(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose image file");
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            file.toURI();
            //the image selected is displayed in all the image views once it is selected
            Image image = new Image(file.toURI().toString());
            view.setImage(image);

            blackAndWhiteView.setImage(image);
            blackAndWhite(blackAndWhiteView,128); //set to 128 as this is the midpoint between 0 and 255

            // Display the file name

            fileName.setText(file.getName());

            // Display its size in kilobytes

            fileSize.setText(file.length() + "KB");
        }


    }

    public int[] blackAndWhite(ImageView imageView, int threshold) {
        //Assign width and height values from imageView
        int width = (int) imageView.getImage().getWidth();
        int height = (int) imageView.getImage().getHeight();
        //create a new writable image with its width and height the same as the imageview
        ii = new WritableImage(width,height);

        int[] imageArray = new int[height * width];

        // convert to black and white
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = imageView.getImage().getPixelReader().getArgb(x, y);

                int a = (p >> 24) & 255;
                int r = (p >> 16) & 255;
                int g = (p >> 8) & 255;
                int b = p & 255;

                // calculate average
                int avg = (r + g + b) / 3;

                // set pixel to black or white depending on intensity value
                int bw;
                if (avg < threshold) {
                    bw = 0; // black
                    // store black or white value
                    imageArray[y * width + x] = -1;
                } else {
                    bw = 255; // white
                    // store black or white value
                    imageArray[y * width + x] = y * width + x;
                }

                // replace RGB value with black or white
                p = (a << 24) | (bw << 16) | (bw << 8) | bw;


                ii.getPixelWriter().setArgb(x, y, p);


            }
        }
        //Once the image is selected after the black and white conversion
        //preform the analysis on it
        imageView.setImage(ii);
        imageAnalysis();
        printImageArray();
        return imageArray;
    }

    public void union(int[] imageArray, int a, int b) {
        imageArray[find(imageArray,b)]=find(imageArray,a); //The root of b is made reference a
    }

    // finds the root of each of the pixels
    public int find(int[] imageArray, int data) {
        if(imageArray[data]==data) {
            return data;
        }
        else{
            return find(imageArray, imageArray[data]);
        }
    }

        int[] imageArray;


    public void imageAnalysis() {
        // create array the size of the width multiplied by the height
        imageArray = new int[(int) blackAndWhiteView.getImage().getHeight() * (int) blackAndWhiteView.getImage().getWidth()];

        // go through pixel by pixel, if black {-1}, if white {row*width+column}
        // iterate through the image pixel by pixel using a PixelReader object
        PixelReader pixelReader = blackAndWhiteView.getImage().getPixelReader();
        for (int i = 0; i < blackAndWhiteView.getImage().getHeight(); i++) {
            for (int j = 0; j < blackAndWhiteView.getImage().getWidth(); j++) {
                // get the color of the current pixel
                Color getColor = pixelReader.getColor(j, i);
                // if the pixel is white, set the corresponding index in imageArray to its position
                if (getColor.equals(Color.WHITE)) {
                    imageArray[(i * (int) blackAndWhiteView.getImage().getWidth()) + j] = (i * (int) blackAndWhiteView.getImage().getWidth()) + j;
                } else {
                    // if the pixel is black, set the corresponding index in imageArray to -1
                    imageArray[(i * (int) blackAndWhiteView.getImage().getWidth()) + j] = -1;
                }
            }
        }

        // perform union operation to group adjacent white pixels into connected components
        int right;
        int down;
        for (int data = 0; data < imageArray.length; data++) {
            if ((data + 1) < imageArray.length) {
                right = data + 1;
            } else {
                right = imageArray.length - 1; // goes to the very end +1 then does -1 once you run out of pixels
            }

            // checks if down is possible
            if ((data + (int) blackAndWhiteView.getImage().getWidth()) < imageArray.length) {
                down = data + (int) blackAndWhiteView.getImage().getWidth();
            } else {
                down = imageArray.length - 1; // goes to the very end
            }

            // perform union operation on adjacent white pixels
            if (imageArray[data] == -1) {
                continue;
            }
            if ((data + 1) % blackAndWhiteView.getImage().getWidth() != 0) {
                if (imageArray[right] != -1) {
                    union(imageArray, data, right);
                }
            }
            if ((data + 1) % blackAndWhiteView.getImage().getWidth() != 1) {
                if (imageArray[down] != -1) {
                    union(imageArray, data, down);
                }
            }
        }

        // obtain the positions of the center pixels of the connected components
        getCirclePositions(imageArray);
    }


    // Define a method to print the contents of an array of Image objects to the console
    public void printImageArray(){
        // Use the Arrays.toString() method to convert the imageArray to a string, and print the resulting string to the console
        System.out.println(Arrays.toString(imageArray));
    }

    // Define a LinkedList object to store the areas of circles that will be drawn
    LinkedList<Double> circles = new LinkedList<>();

    // Define a method to draw circles over a circular area defined by minX, minY, maxX, maxY parameters
    public void drawCircles(double minX, double minY, double maxX, double maxY) {
        //This lays out the bounds of my image view so the circles haven't drawn out of bounds of the image
        Bounds bounds = blackAndWhiteView.getLayoutBounds();
        double xScale = bounds.getWidth() / blackAndWhiteView.getImage().getWidth();
        double yScale = bounds.getHeight() / blackAndWhiteView.getImage().getHeight();

        minX *= xScale;
        maxX *= xScale;
        minY *= yScale;
        maxY *= yScale;

        // Create a new Circle object
        Circle circle = new Circle();

        // Calculate the center of the circle as the midpoint of the circular area
        circle.setCenterX((minX + maxX) / 2);
        circle.setCenterY((minY + maxY) / 2);

        // Calculate the radius of the circle as half the length of the longer side of the circular area
        circle.setRadius(Math.max(maxX - minX, maxY - minY) / 2);

        // Set the fill color of the circle to transparent and the stroke color to blue with a width of 1
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.BLUE);
        circle.setStrokeWidth(1);

        // Set the layout position of the circle to match that of a black-and-white view
        circle.setTranslateX(blackAndWhiteView.getLayoutX());
        circle.setTranslateY(blackAndWhiteView.getLayoutY());

        // Add the circle to the parent Pane
        ((Pane)blackAndWhiteView.getParent()).getChildren().add(circle);

        // Calculates the area of a circle using
        // the formula area = pi * radius^2 and then add it to the circles list
        double area = Math.PI * circle.getRadius() * circle.getRadius();
        circles.add(area);

        // Sort the list of circle areas in ascending order
        Collections.sort(circles);
        // Add a tooltip to the circle showing its position in the list and its area
        for (int i = 0; i < circles.size(); i++) {
            System.out.println(circles.get(i));
            Tooltip.install(circle, new Tooltip("Celestial Object Number: " + (i + 1) + "\nEstimated Size (pixel units): " + circles.get(i)));
        }
    }


    // Initialize noiseReduction to 0 and create an ArrayList called disjointSetValues
    int noiseReduction = 0;
    List<Integer> disjointSetValues = new ArrayList<>();

    // Define a method to get the positions of circles in an image array
    public void getCirclePositions(int[] imageArray) {
        // Remove any existing circles from the blackAndWhiteView parent AnchorPane
        ((AnchorPane)blackAndWhiteView.getParent()).getChildren().removeIf(c->c instanceof Circle);

        // Loop through the imageArray and find the first instance of an element that is not equal to -1
        // and add it to the List disjointSetValues if it is not already present
        for(int i = 0; i < imageArray.length; i++){
            if(imageArray[i] != -1 && !disjointSetValues.contains(find(imageArray, i))){
                disjointSetValues.add(find(imageArray, i));
            }
        }

        // Loop through the values in disjointSetValues
        for(int i = 0; i < disjointSetValues.size(); i++) {
            int rootValue = disjointSetValues.get(i);
            // Initialize minX, minY, maxX, and maxY to the dimensions of the blackAndWhiteView image
            int minX = (int) blackAndWhiteView.getImage().getWidth();
            int minY = (int) blackAndWhiteView.getImage().getHeight();
            int maxX = 0;
            int maxY = 0;

            // Loop through the imageArray and find the minimum and maximum x and y values for each element that
            // is equal to the current rootValue
            for (int j = 0; j < imageArray.length; j++) {
                if (imageArray[j] != -1) {
                    int root = find(imageArray, j);

                    if (rootValue == root) {
                        int rootX = j % (int) blackAndWhiteView.getImage().getWidth();
                        int rootY = j / (int) blackAndWhiteView.getImage().getWidth();

                        if (rootX < minX) minX = rootX;
                        if (rootY < minY) minY = rootY;
                        if (rootX > maxX) maxX = rootX;
                        if (rootY > maxY) maxY = rootY;
                    }
                }
            }

            // Noise Reduction
            // If the area of the component is greater than the noiseReduction value, call drawCircles

            if ((((maxX - minX)*(maxY - minY)) > noiseReduction)) {
                drawCircles(minX, minY, maxX, maxY);
            }
        }
    }


    //This method shows the total amount of celestial objects in the image
    public void totalCelestialObjects(ActionEvent event){
        System.out.println( "The total number of celestial objects in this image is " + disjointSetValues.size());
    }


    public void colorDisjointSets(ActionEvent event) {
        // The variable that will hold the current pixel's position in the 1-dimensional array
        int position = 0;
        PixelWriter pixelWriter = ii.getPixelWriter();
        // Loop through the rows and columns of the ImageView
        for (int j = 0; j < blackAndWhiteView.getFitHeight(); j++) {
            for (int i = 0; i < blackAndWhiteView.getFitWidth(); i++) {
                // Calculate the position of the current pixel in the 1-dimensional array
                position = (int) (j * blackAndWhiteView.getFitWidth() + i);
                // If the pixel has a root (i.e., is not -1), color it
                if (imageArray[position] != -1) {
                    int root = find(imageArray, position); // Find the root of the current pixel's set
                    Random rgb = new Random(root);
                    double r = rgb.nextDouble(); // Generate a random number between 0 and 1 for the red component of the color
                    double g = rgb.nextDouble(); // Generate a random number between 0 and 1 for the green component of the color
                    double b = rgb.nextDouble(); // Generate a random number between 0 and 1 for the blue component of the color
                    // Create a new Color object using the randomly generated RGB values
                    Color randomColor = new Color(r, g, b, 1.0);
                    pixelWriter.setColor(i, j, randomColor);
                }
            }
        }
    }


//////////////------ Slider Code ------/////////////////////////////////////


    @FXML
    Slider hueSlider;
    @FXML
    Slider hueSlider1;

    @FXML
    Slider brightnessSlider;
    @FXML
    Slider brightnessSlider1;

    @FXML
    Slider saturationSlider;
    @FXML
    Slider saturationSlider1;
    @FXML
    Slider noiseSlider;


    ColorAdjust colorAdjust = new ColorAdjust();

    public void hueChanger(double value) {
        try {

            colorAdjust.setHue(value);

            view.setEffect(colorAdjust);
            blackAndWhiteView.setEffect(colorAdjust);

        }
        catch(Exception e){
            System.out.println("Error:" + e);
            System.out.println(hueSlider.getValue());
        }

    }

    public void hueSlider(){
        hueChanger(hueSlider.getValue());
    }

    public void hueSlider1(){
        hueChanger(hueSlider1.getValue());
    }


    public void brightnessChanger(double value){

        colorAdjust.setBrightness(value);

        view.setEffect(colorAdjust);
        blackAndWhiteView.setEffect(colorAdjust);
    }

    public void brightnessSlider(){
        brightnessChanger(brightnessSlider.getValue());
        brightnessChanger(brightnessSlider1.getValue());

    }


    public void saturationChanger(double value){

        colorAdjust.setSaturation(value);

        view.setEffect(colorAdjust);
        blackAndWhiteView.setEffect(colorAdjust);

    }

    public void saturationSlider(){
        saturationChanger(saturationSlider.getValue());
        saturationChanger(saturationSlider1.getValue());
    }
    public void reset(ActionEvent actionEvent){

        colorAdjust.setHue(0);
        colorAdjust.setSaturation(0);
        colorAdjust.setBrightness(0);

        hueSlider.setValue(0);
        hueSlider1.setValue(0);
        saturationSlider.setValue(0);
        brightnessSlider.setValue(0);
        noiseSlider.setValue(0);
    }

    public void exit(ActionEvent event) {
        System.exit(1);
    }

}