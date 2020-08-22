package org.orekyuu.javacv;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Main extends Application {

    public static void main(String[] args) {
        launch(Main.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane border = new BorderPane();

        Button selectImage = new Button("画像を選択");

        HBox bottomBox = new HBox(selectImage);
        bottomBox.setAlignment(Pos.CENTER);
        border.setBottom(bottomBox);

        ImageView originalView = new ImageView();
        originalView.setFitWidth(300);
        originalView.setFitHeight(300);
        originalView.setPreserveRatio(true);
        border.setTop(originalView);

        ArrayList<ImageView> steps = new ArrayList<>();
        for (int i = 0; i < BookFinder.STEPS; i++) {
            ImageView view = new ImageView();
            view.setFitHeight(300);
            view.setFitWidth(300);
            view.setPreserveRatio(true);
            steps.add(view);
        }
        HBox centerBox = new HBox(steps.toArray(new ImageView[]{}));
        centerBox.setAlignment(Pos.CENTER);
        border.setCenter(centerBox);

        primaryStage.setScene(new Scene(border));
        primaryStage.centerOnScreen();
        primaryStage.setTitle("画像テスト");
        primaryStage.show();

        selectImage.setOnAction(e -> {
            var chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpeg", "*.jpg", "*.png"));
            File file = chooser.showOpenDialog(primaryStage);

            BookFinder finder = new BookFinder(file.getAbsolutePath());
            finder.process();
            originalView.setImage(mat2Image(finder.getOriginal(), file));

            for (int i = 0; i < BookFinder.STEPS; i++) {
                Mat mat = finder.steps().get(i);
                Image image = mat2Image(mat, file);
                steps.get(i).setImage(image);
            }

            if (finder.getResult() != null) {
                Stage stage = new Stage(StageStyle.UTILITY);
                ImageView imageView = new ImageView(mat2Image(finder.getResult(), file));
                stage.setScene(new Scene(new VBox(imageView)));
                stage.centerOnScreen();
                stage.show();
            }
        });
    }

    private Image mat2Image(Mat mat, File file) {

        String[] split = file.getName().split("\\.");
        String ext = split[split.length - 1];

        byte[] buff = new byte[mat.createBuffer().capacity()];
        opencv_imgcodecs.imencode("." + ext, mat, buff);
        return new Image(new ByteArrayInputStream(buff));
    }
}
