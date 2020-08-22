package org.orekyuu.javacv;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class BookFinder {

    public static final int STEPS = 2;
    final int maxheight = 640;
    final int maxwidth = 480;
    private final String path;

    int resizedWidth;
    int resizedHeight;

    IplImage resized;
    IplImage filter;
    IplImage pointing;
    IplImage result;


    public BookFinder(String path) {
        this.path = path;
    }

    private void setResizedImageSize(IplImage original, int maxwidth, int maxheight) {
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
        resizedWidth = width;
        resizedHeight = height;
    }

    public void process(double threshold, double threshold2) {
        IplImage image = cvIplImage(opencv_imgcodecs.imread(path));
        setResizedImageSize(image, maxwidth, maxheight);
        resized = resize(image);
        filter = applyFilter(resized, threshold, threshold2);
        var seq = findLargestSquare(filter);
        if (seq != null) {
            result = transform(resized, seq);
        } else {
            result = null;
        }
    }

    private IplImage resize(IplImage image) {
        IplImage iplImage = cvCreateImage(cvSize(resizedWidth, resizedHeight), image.depth(), image.nChannels());
        cvResize(image, iplImage);
        return iplImage;
    }

    private IplImage applyFilter(IplImage image, double threshold, double threshold2) {
        IplImage gray = cvCreateImage(cvGetSize(image), IPL_DEPTH_8U, 1);
        cvCvtColor(image, gray, CV_BGR2GRAY);
        OpenCVFrameConverter.ToMat converter2Mat = new OpenCVFrameConverter.ToMat();
        Frame grayImageFrame = converter2Mat.convert(gray);
        Mat mat = converter2Mat.convert(grayImageFrame);

        GaussianBlur(mat, mat, new Size(5, 5), 0, 0, BORDER_DEFAULT);
        IplImage destImage = converter2Mat.convertToIplImage(grayImageFrame);
        cvErode(destImage, destImage);
        cvDilate(destImage, destImage);
        cvCanny(destImage, destImage, threshold, threshold2);
        return destImage;
    }

    private CvSeq findLargestSquare(IplImage image) {
        IplImage founded = cvCloneImage(image);
        IplImage resultImage = cvCloneImage(resized);

        CvMemStorage memory = CvMemStorage.create();
        CvSeq contours = new CvSeq();

        cvFindContours(founded, memory, contours,
                Loader.sizeof(CvContour.class), CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE, cvPoint(0, 0));

        int maxWidth = 0;
        int maxHeight = 0;
        double maxArea = 0;

        CvRect contour = null;
        CvSeq seqFounded = null;
        CvSeq nextSeq = new CvSeq();

        CvSeq result = null;
        for (nextSeq = contours; nextSeq != null; nextSeq = nextSeq.h_next()) {
            double area = cvContourArea(nextSeq);
            System.out.println("area: " + area);
            if (area > 300) {
                double peri = cvArcLength(nextSeq, CV_WHOLE_SEQ, 1);
                CvSeq poly = cvApproxPoly(nextSeq, Loader.sizeof(CvContour.class), memory, CV_POLY_APPROX_DP, 0.02 * peri);
                System.out.println(poly.total());
                if (area > 300 && area > maxArea && poly.total() == 4) {
                    contour = cvBoundingRect(nextSeq);
                    maxWidth = contour.width();
                    maxHeight = contour.height();
                    maxArea = area;
                    result = poly;
                    System.out.println(poly);
                }
            }
        }

        if (result != null) {
            for (int i = 0; i < result.total(); i++) {
                CvPoint v = new CvPoint(cvGetSeqElem(result, i));
                System.out.println(v);
                cvDrawCircle(resultImage, v, 5, CvScalar.GREEN, 20, 8, 0);
            }
            pointing = resultImage;
        }

        return result;
    }

    private IplImage transform(IplImage source, CvSeq contour) {
        IplImage wrap = cvCloneImage(source);

        CvPoint topRight = new CvPoint(cvGetSeqElem(contour, 0));
        CvPoint topLeft = new CvPoint(cvGetSeqElem(contour, 1));
        CvPoint bottomLeft = new CvPoint(cvGetSeqElem(contour, 2));
        CvPoint bottomRight = new CvPoint(cvGetSeqElem(contour, 3));

        int resultWidth = Math.max(topRight.x() - topLeft.x(),
                bottomRight.x() - bottomLeft.x());
        int resultHeight = Math.max(bottomRight.y() - topRight.y(),
                bottomLeft.y() - topLeft.y());
        resultWidth = Math.abs(resultWidth);
        resultHeight = Math.abs(resultHeight);

        float[] sourcePoints = {
                topLeft.x(), topLeft.y(),
                topRight.x(), topRight.y(),
                bottomLeft.x(), bottomLeft.y(),
                bottomRight.x(), bottomRight.y(),
        };
        float[] destinationPoints = {
                0, 0,
                resultWidth, 0,
                0, resultHeight,
                resultWidth, resultHeight,
        };

        CvMat mat = cvCreateMat(3, 3, CV_32FC1);
        cvGetPerspectiveTransform(sourcePoints, destinationPoints, mat);
        IplImage destImage = cvCloneImage(wrap);
        cvWarpPerspective(wrap, destImage, mat, CV_INTER_LINEAR, CvScalar.ZERO);
        return cropImage(destImage, 0, 0, resultWidth, resultHeight);
    }

    private IplImage cropImage(IplImage img, int x, int y, int resultWidth, int resultHeight) {
        cvSetImageROI(img, cvRect(x, y, resultWidth, resultHeight));
        IplImage dest = cvCloneImage(img);
        cvCopy(img, dest);
        return dest;
    }

    public IplImage getResized() {
        return resized;
    }

    public IplImage getResult() {
        return result;
    }

    public List<IplImage> steps() {
        ArrayList<IplImage> images = new ArrayList<>();
        images.add(filter);
        images.add(pointing);
        return images;
    }
}
