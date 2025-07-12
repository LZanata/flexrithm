package br.com.zanata.flexrithm;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Classe principal do Flexrithm
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        
        // Label para o display do tempo
        Label timerLabel = new Label("00:00:00");
        timerLabel.setFont(new Font("Arial", 48)); // Deixa a fonte do timer bem grande

        // Label para o saldo de descanso
        Label saldoDescansoLabel = new Label("Descanso acumulado: 00:00");

        // Botões de ação
        Button iniciarTrabalhoButton = new Button("Iniciar Trabalho");
        Button usarDescansoButton = new Button("Usar Descanso");
        
        // VBox para empilhar os elementos verticalmente
        VBox root = new VBox();
        root.setSpacing(20); // Espaçamento de 20 pixels entre os elementos
        root.setAlignment(Pos.CENTER); // Centraliza tudo
        root.setPadding(new Insets(20)); // Adiciona uma margem interna de 20 pixels

        // Adiciona todos os componentes ao VBox
        root.getChildren().addAll(timerLabel, saldoDescansoLabel, iniciarTrabalhoButton, usarDescansoButton);
        
        // Cria a cena com o layout (definindo o tamanho da janela)
        Scene scene = new Scene(root, 400, 300);

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