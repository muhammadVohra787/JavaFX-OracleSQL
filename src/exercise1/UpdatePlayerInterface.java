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
        primaryStage.setTitle("Update Player Interface");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        Label headingLabel = new Label("Double-click the element to extract information");
        headingLabel.setStyle("-fx-font-size: 12pt;");

        Label playerIdLabel = new Label("Player ID:");
        TextField playerIdField = new TextField();

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


        playerList = new ListView<>();


        playerList.getItems().addAll("Dummy Item");
        populatePlayerList(playerList);

        playerList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> populatePlayerFields(newValue, playerIdField, firstNameField, lastNameField,
                        addressField, postalCodeField, provinceField, phoneNumberField));

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> updatePlayer(playerIdField.getText(), firstNameField.getText(),
                lastNameField.getText(), addressField.getText(), postalCodeField.getText(),
                provinceField.getText(), phoneNumberField.getText()));

        grid.add(headingLabel, 0, 0, 2, 1);
        grid.add(playerList, 0, 1, 1, 8);
        grid.add(playerIdLabel, 1, 1);
        grid.add(playerIdField, 2, 1);
        grid.add(firstNameLabel, 1, 2);
        grid.add(firstNameField, 2, 2);
        grid.add(lastNameLabel, 1, 3);
        grid.add(lastNameField, 2, 3);
        grid.add(addressLabel, 1, 4);
        grid.add(addressField, 2, 4);
        grid.add(postalCodeLabel, 1, 5);
        grid.add(postalCodeField, 2, 5);
        grid.add(provinceLabel, 1, 6);
        grid.add(provinceField, 2, 6);
        grid.add(phoneNumberLabel, 1, 7);
        grid.add(phoneNumberField, 2, 7);
        grid.add(submitButton, 2, 8);

        Scene scene = new Scene(grid, 600, 400);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private void populatePlayerList(ListView<String> playerList) {
        try {
            connection = connectToDatabase();
            if (connection != null) {
                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery("SELECT player_id, first_name, last_name FROM player");
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
        } finally {
            closeConnection();
        }
    }

    private void populatePlayerFields(String selectedPlayer, TextField playerIdField, TextField firstNameField,
                                      TextField lastNameField, TextField addressField, TextField postalCodeField,
                                      TextField provinceField, TextField phoneNumberField) {
        if (selectedPlayer != null) {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+):");
            java.util.regex.Matcher matcher = pattern.matcher(selectedPlayer);

            if (matcher.find()) {
                String playerId = matcher.group(1).trim();

                try {
                    connection = connectToDatabase();
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
                } finally {
                    closeConnection();
                }
            }
        }
    }


    private void updatePlayer(String playerId, String firstName, String lastName, String address, String postalCode,
                              String province, String phoneNumber) {
        try {
            connection = connectToDatabase();
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
        } finally {
            closeConnection();
        }
    }

    private Connection connectToDatabase() throws SQLException {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            return DriverManager.getConnection(GameManager.DB_URL, GameManager.USER, GameManager.PASSWORD);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("Oracle JDBC driver not found");
        }
    }

    private void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
