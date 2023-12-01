//MuhammadVohra-Course:COMP228
package exercise1;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class GameManager extends Application {

    public static final String DB_URL = "jdbc:oracle:thin:@199.212.26.208:1521:SQLD";
    public static final String USER = "COMP228_F23_piy_33";
    public static final String PASSWORD = "password";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Button updateButton = new Button("Update");
        Button insertButton = new Button("Insert");
        Button displayButton = new Button("Display Data");

        updateButton.setStyle("-fx-font-size: 15pt;");
        insertButton.setStyle("-fx-font-size: 15pt;");
        displayButton.setStyle("-fx-font-size: 15pt;");

        javafx.scene.text.Text updateHeading = new javafx.scene.text.Text("Update the existing player information.");
        javafx.scene.text.Text insertHeading = new javafx.scene.text.Text("Insert game and player information into the database.");
        javafx.scene.text.Text displayHeading = new javafx.scene.text.Text("Open to see all the data.");

        updateHeading.setStyle("-fx-font-size: 16pt;");
        insertHeading.setStyle("-fx-font-size: 16pt;");
        displayHeading.setStyle("-fx-font-size: 16pt;");

        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(updateHeading, updateButton, insertHeading, insertButton, displayHeading, displayButton);

        Scene scene = new Scene(vbox, 800, 400);

        primaryStage.setTitle("Update, Insert, or Display Data");
        primaryStage.setScene(scene);

        updateButton.setOnAction(e -> openUpdatePlayerInterface());
        insertButton.setOnAction(e -> openDataEntryInterface());
        displayButton.setOnAction(e -> openDataDisplay());

        primaryStage.show();
    }


    private void openDataEntryInterface() {
        DataEntryInterface dataEntryInterface = new DataEntryInterface();
        dataEntryInterface.start(new Stage());
    }

    private void openUpdatePlayerInterface() {
        UpdatePlayerInterface updatePlayerInterface = new UpdatePlayerInterface();
        updatePlayerInterface.start(new Stage());
    }

    private void openDataDisplay() {
        DataDisplayInterface openDataDisplay = new DataDisplayInterface();
        openDataDisplay.start(new Stage());
    }

    public static Connection connectToDatabase() throws SQLException {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            return DriverManager.getConnection(DB_URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("Oracle JDBC driver not found");
        }
    }
}
