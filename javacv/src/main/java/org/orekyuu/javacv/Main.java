package org.orekyuu.javacv;

import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.bytedeco.opencv.opencv_core.Mat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;

public class Main extends Application {

    public static void main(String[] args) {
        launch(Main.class, args);
    }

    SimpleObjectProperty<Image> resultImage = new SimpleObjectProperty<>(null);
    File file;
    ArrayList<ImageView> steps = new ArrayList<>();
    ImageView originalView;
    Slider value1;
    Slider value2;

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane border = new BorderPane();

        Button selectImage = new Button("画像を選択");

        value1 = new Slider(0, 255, 100);
        value2 = new Slider(0, 255, 100);
        value1.valueProperty().addListener((observable, oldValue, newValue) -> onChanged());
        value2.valueProperty().addListener((observable, oldValue, newValue) -> onChanged());

        VBox toolBox = new VBox(selectImage, value1, value2);
        toolBox.setAlignment(Pos.CENTER);
        border.setBottom(toolBox);

        originalView = new ImageView();
        originalView.setFitWidth(300);
        originalView.setFitHeight(300);
        originalView.setPreserveRatio(true);
        border.setTop(originalView);

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
            file = chooser.showOpenDialog(primaryStage);

            onChanged();

            Stage stage = new Stage(StageStyle.UTILITY);
            ImageView imageView = new ImageView();
            imageView.imageProperty().bind(resultImage);
            stage.setScene(new Scene(new VBox(imageView)));
            stage.centerOnScreen();
            stage.show();
        });
    }



    private void onChanged() {
        BookFinder finder = new BookFinder(file.getAbsolutePath());
        finder.process(value1.getValue(), value2.getValue());
        originalView.setImage(mat2Image(finder.getResized(), file));
        resultImage.setValue(mat2Image(finder.getResult(), file));
        for (int i = 0; i < BookFinder.STEPS; i++) {
            Image image = mat2Image(finder.steps().get(i), file);
            steps.get(i).setImage(image);
        }
    }

    private Image mat2Image(IplImage img, File file) {
        if (img == null) {
            return null;
        }

        String[] split = file.getName().split("\\.");
        String ext = split[split.length - 1];

        OpenCVFrameConverter.ToMat converter2Mat = new OpenCVFrameConverter.ToMat();
        Frame frame = converter2Mat.convert(img);
        Mat mat = converter2Mat.convert(frame);

        byte[] buff = new byte[mat.createBuffer().capacity()];
        opencv_imgcodecs.imencode("." + ext, mat, buff);
        return new Image(new ByteArrayInputStream(buff));
    }
}
