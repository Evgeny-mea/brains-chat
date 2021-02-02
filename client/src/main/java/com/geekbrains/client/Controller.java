package com.geekbrains.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    TextArea textArea;

    @FXML
    TextField msgField, loginField;

    @FXML
    HBox msgPanel, authPanel;

    @FXML

    PasswordField passField;

    @FXML
    ListView<String> clientsList;

    private boolean authenticated;
    private String nickname;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
        clientsList.setVisible(authenticated);
        clientsList.setManaged(authenticated);
        if (!authenticated) {
            nickname = "";
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthenticated(false);
        clientsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String nickname = clientsList.getSelectionModel().getSelectedItem();
                msgField.setText("/w " + nickname + " ");
                msgField.requestFocus();
                msgField.selectEnd();
            }
        });
        linkCallbacks();
    }

    public void sendAuth() {
        Network.sendAuth(loginField.getText(), passField.getText());
        loginField.clear();
        passField.clear();
    }

    public void sendMsg() {
        if (Network.sendMsg(msgField.getText())) {
            msgField.clear();
            msgField.requestFocus();
        }
    }

    public void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
            alert.showAndWait();
        });
    }

    public void linkCallbacks() {
        Network.setCallOnException(args -> showAlert(args[0].toString()));
        Network.setCallOnCloseConnection(args -> setAuthenticated(false));
        Network.setCallOnAuthenticated(args -> {
            setAuthenticated(true);
            nickname = args[0].toString();
        });
        Network.setCallOnMsgReceived(args -> {
            String msg = args[0].toString();
            if (msg.startsWith("/")) {
                if (msg.startsWith("/clients ")) {
                    String[] tokens = msg.split("\\s");
                    Platform.runLater(() -> {
                        clientsList.getItems().clear();
                        for (int i = 1; i < tokens.length; i++) {
                            clientsList.getItems().add(tokens[i]);
                        }
                    });
                }
            } else {
                textArea.appendText(msg + "\n");
            }
        });
    }
}