package ru.geekbrains.CLIENT;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Controller  {

    Socket socket;
    DataInputStream input;
    DataOutputStream output;

    final String IP_ADDRESS = "localhost";
    final int PORT = 9192;

    @FXML
    TextArea chatArea;
    @FXML
    TextField textField;
    @FXML
    Button btn1;
    @FXML
    HBox upperPanel;
    @FXML
    HBox bottomPanel;
    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;
    @FXML
    ListView<String> clientsList;

    private boolean isAuthorized;

    List<TextArea> textAreas;

    private void setAuthrized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;

        if (!isAuthorized) {
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
            clientsList.setVisible(false);
            clientsList.setManaged(false);
        } else {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
            clientsList.setVisible(true);
            clientsList.setManaged(true);
        }
    }

    public void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            textAreas = new ArrayList<>();
            textAreas.add(chatArea);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String str = input.readUTF();
                            if (str.startsWith("/authok")) {
                                setAuthrized(true);
                                break;
                            } else {
                                chatArea.appendText(str + "\n");
                            }
                        }

                        while (true) {
                          String str = input.readUTF();
                          if (str.startsWith("/")) {
                              if (str.equalsIgnoreCase("/clientClose")) break;

                              if (str.startsWith("/clientslist ")) {
                                  String[] tokens = str.split(" ");
                                  Platform.runLater(() -> {
                                      clientsList.getItems().clear();
                                      for (int i = 1; i < tokens.length; i++) {
                                          clientsList.getItems().add(tokens[i]);
                                      }
                                  });
                              }

                          } else {
                              chatArea.appendText(str + "\n");
                          }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        setAuthrized(false);
                    }
                }
            }).start();

    } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg () {
        try {
            output.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
            }
        }

     public void Dispose() {
//    sending close session msg to server
        try {
            if (output != null) {
                output.writeUTF("/end");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAyth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            output.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void selectClient(MouseEvent mouseEvent) {
        if(mouseEvent.getClickCount() == 2) {
            MiniStage ms = new MiniStage(clientsList.getSelectionModel().getSelectedItem(), output, textAreas);
            ms.show();
        }
    }
}
