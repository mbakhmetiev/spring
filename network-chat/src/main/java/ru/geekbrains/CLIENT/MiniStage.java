package ru.geekbrains.CLIENT;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class MiniStage extends Stage {
    String nickTo;
    DataOutputStream out;
    List<TextArea> parentList;

    public MiniStage(String nickTo, DataOutputStream out, List<TextArea> parentList) {
        this.nickTo = nickTo;
        this.out = out;
        this.parentList = parentList;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/fxml/personal.fxml"));
            Parent root = fxmlLoader.load();
            setTitle("personal window");
            Scene scene = new Scene(root, 300, 400);
            setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
