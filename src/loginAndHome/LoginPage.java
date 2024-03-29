package loginAndHome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

import static java.lang.System.exit;

public class LoginPage implements Initializable {
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private ChoiceBox<String> bankName;

    private static Statement sqlStatement;

    public static String usernameString;
    public static String passwordString;
    public static String bankNameString;
    public static String bankCodeString;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        login(bankName);
    }

    static void login(ChoiceBox<String> bankName) {
        try {
            sqlStatement = databaseConnection.Connect.connectBankDB();
            @SuppressWarnings("SqlDialectInspection")
            String query = "select distinct bank_branch_name from employees_details;";
            ResultSet sqlResult = sqlStatement.executeQuery(query);
            while (sqlResult.next()) {
                bankName.getItems().add(sqlResult.getString("bank_branch_name"));
            }
            bankName.getSelectionModel().selectFirst();
        } catch (com.mysql.cj.jdbc.exceptions.CommunicationsException e) {
            Alert internetProblem = new Alert(Alert.AlertType.ERROR);
            internetProblem.setContentText("Connection Failed due to poor internet connection");
            internetProblem.showAndWait();
            exit(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void LogInOnAction() {
        if (!validateUserLoginInput(username, password, bankName)) {
            ResetOnAction();
            return;
        }

        usernameString = username.getText();
        passwordString = password.getText();
        bankNameString = bankName.getValue();

        try {
            String query = "select bank_branch_code from employees_details where bank_branch_name=\'" + bankNameString + "\'";
            ResultSet sqlResult = sqlStatement.executeQuery(query);
            if (sqlResult.next()) {
                bankCodeString = sqlResult.getString("bank_branch_code");
            }
        } catch (com.mysql.cj.jdbc.exceptions.CommunicationsException e) {
            Alert internetProblem = new Alert(Alert.AlertType.ERROR);
            internetProblem.setContentText("Connection Failed due to poor internet connection");
            internetProblem.showAndWait();
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }

        //noinspection Duplicates
        try {
            Pane newPane = FXMLLoader.load(getClass().getResource("homePage.fxml"));
            Main.primaryStage.setScene(new Scene(newPane, 1000, 600));
            Main.primaryStage.setMaximized(true);
            Main.primaryStage.show();
        } catch (Exception e) {
            Alert homePageNotLoad = new Alert(Alert.AlertType.ERROR);
            homePageNotLoad.setContentText("Home Page could not be loaded");
            homePageNotLoad.showAndWait();
        }
    }

    static boolean validateUserLoginInput(TextField username, PasswordField password, ChoiceBox<String> bankName) {
        if (username.getText().isEmpty() || password.getText().isEmpty()) {
            Alert emptyFields = new Alert(Alert.AlertType.ERROR);
            emptyFields.setContentText("Some Fields are missing");
            emptyFields.show();
            return false;
        }

        try {
            String query = "select username, password, bank_branch_name from employees_details";
            ResultSet sqlResult = sqlStatement.executeQuery(query);
            while (sqlResult.next()) {
                if (sqlResult.getString("username").equals(username.getText()) &&
                sqlResult.getString("password").equals(password.getText()) &&
                sqlResult.getString("bank_branch_name").equals(bankName.getValue())) {
                    return true;
                }
            }
        } catch (com.mysql.cj.jdbc.exceptions.CommunicationsException e) {
            Alert internetProblem = new Alert(Alert.AlertType.ERROR);
            internetProblem.setContentText("Connection Failed due to poor internet connection");
            internetProblem.showAndWait();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Alert invalidInput = new Alert(Alert.AlertType.ERROR);
        invalidInput.setContentText("Invalid credentials");
        invalidInput.show();

        return false;
    }

    @FXML
    public void ResetOnAction() {
        username.clear();
        password.clear();
        bankName.getSelectionModel().selectFirst();
    }
}