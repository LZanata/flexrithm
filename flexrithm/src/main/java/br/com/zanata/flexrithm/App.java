package br.com.zanata.flexrithm;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Classe principal do Flexrithm
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Cria um texto simples
        Label label = new Label("Olá, Flexrithm!");

        // Cria um layout e adiciona o texto a ele
        StackPane root = new StackPane();
        root.getChildren().add(label);

        // Cria a "cena" com o layout (definindo o tamanho da janela)
        Scene scene = new Scene(root, 640, 480);

        // Configura a janela principal (chamada de "Stage")
        primaryStage.setTitle("Flexrithm");
        primaryStage.setScene(scene);

        // Mostra a janela
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}