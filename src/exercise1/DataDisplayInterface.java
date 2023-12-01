package exercise1;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataDisplayInterface extends Application {

    private Connection connection;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Data Display Interface");

        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10, 10, 10, 10));

        TitledPane gameTitledPane = createTitledPane("Game", "game");
        TitledPane playerTitledPane = createTitledPane("Player", "player");
        TitledPane playerAndGameTitledPane = createTitledPane("Player and Game", "playerandgame");

        root.getChildren().addAll(gameTitledPane, playerTitledPane, playerAndGameTitledPane);

        Scene scene = new Scene(root, 800, 800);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private TitledPane createTitledPane(String title, String tableName) {
        GridPane grid = createGrid();

        TitledPane titledPane = new TitledPane(title, grid);
        titledPane.setCollapsible(false);
        populateGrid(grid, tableName);

        return titledPane;
    }

    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(10);
        return grid;
    }

    private void populateGrid(GridPane grid, String tableName) {
        try {
            connection = connectToDatabase();

            List<String> columnNames = getColumnNames(tableName);

            for (int i = 0; i < columnNames.size(); i++) {
                Label columnHeader = new Label(columnNames.get(i));
                grid.add(columnHeader, i, 0);
            }

            String query = "SELECT * FROM " + tableName;
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            int row = 1;
            while (resultSet.next()) {
                for (int i = 1; i <= columnNames.size(); i++) {
                    Label dataLabel = new Label(resultSet.getString(i));
                    grid.add(dataLabel, i - 1, row);
                }
                row++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private List<String> getColumnNames(String tableName) throws SQLException {
        List<String> columnNames = new ArrayList<>();

        String query = "SELECT column_name FROM all_tab_columns WHERE table_name = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, tableName.toUpperCase());

        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            columnNames.add(resultSet.getString("column_name"));
        }

        return columnNames;
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

    private void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}


