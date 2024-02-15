module com.madroakos.hikvisionhelper {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.madroakos.hikvisionhelper to javafx.fxml;
    exports com.madroakos.hikvisionhelper to javafx.graphics;
}