module com.madroakos.hikvisionhelper {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.madroakos.hikvisionhelper to javafx.fxml;
    exports com.madroakos.hikvisionhelper to javafx.graphics;
    exports com.madroakos.hikvisionhelper.mainPage to javafx.graphics;
    opens com.madroakos.hikvisionhelper.mainPage to javafx.fxml;
}