package org.orekyuu.javacv;

import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

public class BookFinder {

    final int height = 640;
    final int width = 480;
    private final String path;

    private Mat original;
    private Mat resized = new Mat();
    private Mat grayscale = new Mat();
    private Mat gaussianBlur = new Mat();


    public BookFinder(String path) {
        this.path = path;
    }

    public void process() {
        original = opencv_imgcodecs.imread(path);
        // resize
        opencv_imgproc.resize(original, resized, new Size(width, height));
        // グレースケール変換
        opencv_imgproc.cvtColor(resized, grayscale, opencv_imgproc.COLOR_BGR2GRAY);
        // ブラー
        opencv_imgproc.GaussianBlur(grayscale, gaussianBlur, new Size(5, 5), 1);
    }

    public Mat getOriginal() {
        return original;
    }

    public Mat getResized() {
        return resized;
    }

    public Mat getGrayscale() {
        return grayscale;
    }

    public Mat getGaussianBlur() {
        return gaussianBlur;
    }
}
