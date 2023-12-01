package exercise1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DriverManager;

public class DataEntryInterface extends Application {

    private Connection connection;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Data Entry Interface");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        Label gameTitleLabel = new Label("Game Title:");
        TextField gameTitleField = new TextField();

        Label gameIdLabel = new Label("Game ID:");
        TextField gameIdField = new TextField();

        Label gameScoreIdLabel = new Label("Game Score:");
        TextField gameScoreIdField = new TextField();

        Label playerIdLabel = new Label("Player ID: ");
        TextField playerIdField = new TextField();

        Label playerAndGameIdLabel = new Label("Player and Game ID: ");
        TextField playerAndGameIdField = new TextField();


        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField();

        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField();

        Label addressLabel = new Label("Address:");
        TextField addressField = new TextField();

        Label postalCodeLabel = new Label("Postal Code:");
        TextField postalCodeField = new TextField();

        Label provinceLabel = new Label("Province:");
        TextField provinceField = new TextField();

        Label phoneNumberLabel = new Label("Phone Number:");
        TextField phoneNumberField = new TextField();


        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> insertData(gameTitleField.getText(), gameIdField.getText(),
                playerIdField.getText(), playerAndGameIdField.getText(),
                firstNameField.getText(), lastNameField.getText(), addressField.getText(),
                postalCodeField.getText(), provinceField.getText(), phoneNumberField.getText(),
                gameScoreIdField.getText()));


        grid.add(gameTitleLabel, 0, 0);
        grid.add(gameTitleField, 1, 0);
        grid.add(gameIdLabel, 0, 1);
        grid.add(gameIdField, 1, 1);
        grid.add(gameScoreIdLabel, 0, 2);
        grid.add(gameScoreIdField, 1, 2);
        grid.add(playerIdLabel, 0, 3);
        grid.add(playerIdField, 1, 3);
        grid.add(playerAndGameIdLabel, 0, 4);
        grid.add(playerAndGameIdField, 1, 4);
        grid.add(firstNameLabel, 0, 5);
        grid.add(firstNameField, 1, 5);
        grid.add(lastNameLabel, 0, 6);
        grid.add(lastNameField, 1, 6);
        grid.add(addressLabel, 0, 7);
        grid.add(addressField, 1, 7);
        grid.add(postalCodeLabel, 0, 8);
        grid.add(postalCodeField, 1, 8);
        grid.add(provinceLabel, 0, 9);
        grid.add(provinceField, 1, 9);
        grid.add(phoneNumberLabel, 0, 10);
        grid.add(phoneNumberField, 1, 10);
        grid.add(submitButton, 1, 11);


        Scene scene = new Scene(grid, 400, 450);
        primaryStage.setScene(scene);


        primaryStage.show();
    }

    private void insertData(String gameTitle, String gameId, String playerId, String playerAndGameId,
                            String firstName, String lastName, String address, String postalCode,
                            String province, String phoneNumber, String gameScoreId) {
        try {

            connection = connectToDatabase();

            String insertGameQuery = "INSERT INTO game (game_id, game_title) VALUES (?, ?)";
            PreparedStatement gameStatement = connection.prepareStatement(insertGameQuery);
            gameStatement.setInt(1, Integer.parseInt(gameId));
            gameStatement.setString(2, gameTitle);
            gameStatement.executeUpdate();


            String insertPlayerQuery = "INSERT INTO player (player_id, first_name, last_name, address, postal_code, province, phone_number) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement playerStatement = connection.prepareStatement(insertPlayerQuery);
            playerStatement.setInt(1, Integer.parseInt(playerId));
            playerStatement.setString(2, firstName);
            playerStatement.setString(3, lastName);
            playerStatement.setString(4, address);
            playerStatement.setString(5, postalCode);
            playerStatement.setString(6, province);
            playerStatement.setString(7, phoneNumber);
            playerStatement.executeUpdate();


            String insertPlayerAndGameQuery = "INSERT INTO playerandgame (player_game_id, game_id, player_id, player_date, score) " +
                    "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement playerAndGameStatement = connection.prepareStatement(insertPlayerAndGameQuery);
            playerAndGameStatement.setInt(1, Integer.parseInt(playerAndGameId));
            playerAndGameStatement.setInt(2, Integer.parseInt(gameId));
            playerAndGameStatement.setInt(3, Integer.parseInt(playerId));
            playerAndGameStatement.setDate(4, new java.sql.Date(System.currentTimeMillis()));
            playerAndGameStatement.setInt(5, Integer.parseInt(gameScoreId));
            playerAndGameStatement.executeUpdate();

            connection.commit();
            showAlert("Success", "Data successfully inserted into all tables.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to insert data. Please check your input and try again.");
        } finally {

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private Connection connectToDatabase() throws SQLException {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            Connection conn = DriverManager.getConnection(GameManager.DB_URL, GameManager.USER, GameManager.PASSWORD);

            conn.setAutoCommit(false);

            return conn;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("Oracle JDBC driver not found");
        }
    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
