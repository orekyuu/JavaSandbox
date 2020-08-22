module java.sandbox.javacv.main {
    requires javafx.controls;
    requires javafx.graphics;
    requires org.bytedeco.opencv;
    requires org.bytedeco.javacv.platform;

    opens org.orekyuu.javacv;
}