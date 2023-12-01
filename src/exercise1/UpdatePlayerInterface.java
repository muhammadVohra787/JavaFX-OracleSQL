package exercise1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.*;

public class UpdatePlayerInterface extends Application {

    private Connection connection;
    private ListView<String> playerList;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Set up the main stage
        primaryStage.setTitle("Update Player Interface");

        // Create the main grid
        GridPane grid = createGrid();
        // Create a label for the heading
        Label headingLabel = createLabel("Double-click the element to extract information", 12);

        // Create a ListView for the player list
        playerList = new ListView<>();
        playerList.getItems().addAll("Dummy Item");
        // Populate the player list
        populatePlayerList(playerList);

        // Create an array of TextFields for player information
        TextField[] textFields = createTextFields(7);
        // Create an array of Labels for field descriptions
        Label[] labels = createLabels("Player ID:", "First Name:", "Last Name:", "Address:",
                "Postal Code:", "Province:", "Phone Number:");

        // Create a submit button and set its action
        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> updatePlayer(textFields[0].getText(), textFields[1].getText(),
                textFields[2].getText(), textFields[3].getText(), textFields[4].getText(),
                textFields[5].getText(), textFields[6].getText()));

        // Add a listener for the selected item in the player list
        playerList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> populatePlayerFields(newValue, textFields[0], textFields[1],
                        textFields[2], textFields[3], textFields[4], textFields[5], textFields[6]));

        // Add components to the grid
        grid.add(headingLabel, 0, 0, 2, 1);
        grid.add(playerList, 0, 1, 1, 8);

        for (int i = 0; i < labels.length; i++) {
            grid.add(labels[i], 1, i + 1);
            grid.add(textFields[i], 2, i + 1);
        }

        // Make playerIdField uneditable
        textFields[0].setEditable(false);

        grid.add(submitButton, 2, 8);

        // Create the scene and set it to the stage
        Scene scene = new Scene(grid, 600, 400);
        primaryStage.setScene(scene);
        // Show the stage
        primaryStage.show();
    }

    // Helper method to create a GridPane
    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);
        return grid;
    }

    // Helper method to create a styled label
    private Label createLabel(String text, double fontSize) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: " + fontSize + "pt;");
        return label;
    }

    // Helper method to create an array of TextFields
    private TextField[] createTextFields(int count) {
        TextField[] textFields = new TextField[count];
        for (int i = 0; i < count; i++) {
            textFields[i] = new TextField();
        }
        return textFields;
    }

    // Helper method to create an array of Labels
    private Label[] createLabels(String... texts) {
        Label[] labels = new Label[texts.length];
        for (int i = 0; i < texts.length; i++) {
            labels[i] = new Label(texts[i]);
        }
        return labels;
    }

    // Helper method to populate the player list from the database
    private void populatePlayerList(ListView<String> playerList) {
        try {
            connection = GameManager.connectToDatabase();
            if (connection != null) {
                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery("SELECT player_id, first_name, last_name FROM player Order by player_id");
                ResultSetMetaData rsmd = rs.getMetaData();
                playerList.getItems().clear();
                while (rs.next()) {
                    String playerId = rs.getString("player_id");
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String playerName = playerId + ": " + firstName + " " + lastName;

                    playerList.getItems().add(playerName);

                    System.out.println("Retrieved player: " + playerName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error fetching player data: " + e.getMessage());
        }
    }

    // Helper method to populate the player fields based on the selected player in the list
    private void populatePlayerFields(String selectedPlayer, TextField playerIdField, TextField firstNameField,
                                      TextField lastNameField, TextField addressField, TextField postalCodeField,
                                      TextField provinceField, TextField phoneNumberField) {
        if (selectedPlayer != null) {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+):");
            java.util.regex.Matcher matcher = pattern.matcher(selectedPlayer);

            if (matcher.find()) {
                String playerId = matcher.group(1).trim();

                try {
                    connection = GameManager.connectToDatabase();
                    String selectPlayerSQL = "SELECT * FROM player WHERE player_id = ?";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(selectPlayerSQL)) {
                        preparedStatement.setString(1, playerId);
                        ResultSet resultSet = preparedStatement.executeQuery();
                        if (resultSet.next()) {
                            playerIdField.setText(resultSet.getString("player_id"));
                            firstNameField.setText(resultSet.getString("first_name"));
                            lastNameField.setText(resultSet.getString("last_name"));
                            addressField.setText(resultSet.getString("address"));
                            postalCodeField.setText(resultSet.getString("postal_code"));
                            provinceField.setText(resultSet.getString("province"));
                            phoneNumberField.setText(resultSet.getString("phone_number"));
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Helper method to update player information in the database
    private void updatePlayer(String playerId, String firstName, String lastName, String address, String postalCode,
                              String province, String phoneNumber) {
        try {
            connection = GameManager.connectToDatabase();
            connection.setAutoCommit(false);

            String updatePlayerSQL = "UPDATE player SET first_name=?, last_name=?, address=?, postal_code=?, " +
                    "province=?, phone_number=? WHERE player_id=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(updatePlayerSQL)) {
                preparedStatement.setString(1, firstName);
                preparedStatement.setString(2, lastName);
                preparedStatement.setString(3, address);
                preparedStatement.setString(4, postalCode);
                preparedStatement.setString(5, province);
                preparedStatement.setString(6, phoneNumber);
                preparedStatement.setString(7, playerId);
                preparedStatement.executeUpdate();
            }

            connection.commit();
            showAlert("Success", "Player information successfully updated.");
            populatePlayerList(playerList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Error updating player information.");
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Helper method to show an information alert
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
