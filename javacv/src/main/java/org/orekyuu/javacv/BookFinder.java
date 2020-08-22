package org.orekyuu.javacv;

import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;

import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.BORDER_CONSTANT;
import static org.bytedeco.opencv.global.opencv_core.CV_8U;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.opencv_core.Mat.ones;

public class BookFinder {

    public static final int STEPS = 6;
    final int maxheight = 640;
    final int maxwidth = 480;
    private final String path;

    private Mat original;
    private Mat resized = new Mat();
    private Mat grayscale = new Mat();
    private Mat gaussianBlur = new Mat();
    private Mat cannyBlur = new Mat();
    private Mat dilate = new Mat();
    private Mat erosion = new Mat();
    private Mat resultAll;
    private Mat result;

    public BookFinder(String path) {
        this.path = path;
    }

    public void process() {
        original = opencv_imgcodecs.imread(path);
        // resize
        int width = maxwidth;
        int height = maxheight;
        int imageWidth = original.arrayWidth();
        int imageHeight = original.arrayHeight();

        if (width > (maxheight * imageWidth / imageHeight)) {
            width = (int)((float)(maxheight * imageWidth) / (float)(imageHeight) + 0.5);
            if (width <= 0) {
                width = 1;
            }
        } else if (height > maxwidth * imageHeight / imageWidth){
            height = (int)((float)(maxwidth * imageHeight) / (float)(imageWidth) + 0.5);
            if (height <= 0) {
                height = 1;
            }
        }
        opencv_imgproc.resize(original, resized, new Size(width, height));
        // グレースケール変換
        opencv_imgproc.cvtColor(resized, grayscale, opencv_imgproc.COLOR_BGR2GRAY);
        // ブラー
        opencv_imgproc.GaussianBlur(grayscale, gaussianBlur, new Size(5, 5), 1);
        opencv_imgproc.Canny(gaussianBlur, cannyBlur, 200, 100);
        opencv_imgproc.dilate(cannyBlur, dilate, ones(5, 5, CV_8U).asMat(), new Point(-1, -1), 2, BORDER_CONSTANT, new Scalar(morphologyDefaultBorderValue()));
        opencv_imgproc.erode(dilate, erosion, ones(5, 5, CV_8U).asMat(), new Point(-1, -1), 1, BORDER_CONSTANT, new Scalar(morphologyDefaultBorderValue()));

        Mat imageContour = new Mat();
        Mat imageBigContour = new Mat();
        resized.copyTo(imageContour);
        resized.copyTo(imageBigContour);
        // all
        {
            MatVector contours = new MatVector();
            findContours(erosion, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
            drawContours(imageContour, contours, -1, new Scalar(0, 255, 0, 255));
            result = imageContour;
        }
        // biggest
        {
            MatVector contours = new MatVector();
            findContours(erosion, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

            double maxArea = 0;
            Mat max = null;
            for (Mat mat : contours.get()) {
                double area = contourArea(mat);
                if (area > 5000) {
                    double peri = arcLength(mat, true);
                    Mat approx = new Mat();
                    approxPolyDP(mat, approx, 0.02 * peri, true);
                    if(area > maxArea) {
                        max = approx;
                        maxArea = area;
                    }
                }
            }
            if (max != null) {
                MatVector vector = new MatVector().push_back(max);
                drawContours(imageBigContour, vector, -1, new Scalar(0, 255, 0, 255));
                result = imageBigContour;
            }

        }

    }

    public Mat getOriginal() {
        return original;
    }

    public List<Mat> steps() {
        return List.of(resized, grayscale, gaussianBlur, cannyBlur, dilate, erosion);
    }

    public Mat getResult() {
        return result;
    }
}
