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

public class DataEntryInterface extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Data Entry Interface");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        String[] labelNames = {
                "Game Title", "Game ID", "Game Score",
                "Player ID", "Player and Game ID",
                "First Name", "Last Name", "Address",
                "Postal Code", "Province", "Phone Number"
        };

        Label[] labels = new Label[labelNames.length];
        TextField[] textFields = new TextField[labelNames.length];

        for (int i = 0; i < labelNames.length; i++) {
            String labelText = labelNames[i] + ":";
            labels[i] = new Label(labelText);
            textFields[i] = new TextField();

            grid.addRow(i, labels[i], textFields[i]);
        }

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            insertData(
                    textFields[0].getText(), textFields[1].getText(),
                    textFields[3].getText(), textFields[4].getText(),
                    textFields[5].getText(), textFields[6].getText(),
                    textFields[7].getText(), textFields[8].getText(),
                    textFields[9].getText(), textFields[10].getText(),
                    textFields[2].getText()
            );
        });

        grid.add(submitButton, 1, labelNames.length);

        Scene scene = new Scene(grid, 400, 450);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private void insertData(String gameTitle, String gameId, String playerId, String playerAndGameId,
                            String firstName, String lastName, String address, String postalCode,
                            String province, String phoneNumber, String gameScoreId) {
        Connection connection = null;
        try {
            connection = GameManager.connectToDatabase();
            connection.setAutoCommit(false); // Disable auto-commit to control transactions

            // Insert into game table
            String insertGameQuery = "INSERT INTO game (game_id, game_title) VALUES (?, ?)";
            try (PreparedStatement gameStatement = connection.prepareStatement(insertGameQuery)) {
                gameStatement.setInt(1, Integer.parseInt(gameId));
                gameStatement.setString(2, gameTitle);
                gameStatement.executeUpdate();
            }

            // Insert into player table
            String insertPlayerQuery = "INSERT INTO player (player_id, first_name, last_name, address, postal_code, province, phone_number) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement playerStatement = connection.prepareStatement(insertPlayerQuery)) {
                playerStatement.setInt(1, Integer.parseInt(playerId));
                playerStatement.setString(2, firstName);
                playerStatement.setString(3, lastName);
                playerStatement.setString(4, address);
                playerStatement.setString(5, postalCode);
                playerStatement.setString(6, province);
                playerStatement.setString(7, phoneNumber);
                playerStatement.executeUpdate();
            }

            // Insert into playerandgame table
            String insertPlayerAndGameQuery = "INSERT INTO playerandgame (player_game_id, game_id, player_id, player_date, score) " +
                    "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement playerAndGameStatement = connection.prepareStatement(insertPlayerAndGameQuery)) {
                playerAndGameStatement.setInt(1, Integer.parseInt(playerAndGameId));
                playerAndGameStatement.setInt(2, Integer.parseInt(gameId));
                playerAndGameStatement.setInt(3, Integer.parseInt(playerId));
                playerAndGameStatement.setDate(4, new java.sql.Date(System.currentTimeMillis()));
                playerAndGameStatement.setInt(5, Integer.parseInt(gameScoreId));
                playerAndGameStatement.executeUpdate();
            }

            connection.commit(); // Commit the transaction if all statements are successful
            showAlert("Success", "Data successfully inserted into all tables.");
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (connection != null) {
                    connection.rollback(); // Rollback the transaction in case of any exception
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }finally {
                String errorMessage = e.getMessage();
                String firstLine = errorMessage.split("\n")[0];
                showAlert("Error", "Failed to insert data." + firstLine);
            }

        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true); // Restore auto-commit to its default state
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
