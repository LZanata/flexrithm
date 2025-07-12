package br.com.zanata.flexrithm;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {

    // --- Variáveis de Estado ---
    private Timeline timeline;
    private int tempoEmSegundos = 0;
    private boolean isTimerRunning = false;

    // --- Componentes da Interface ---
    private Label timerLabel;
    private Label saldoDescansoLabel;
    private Button iniciarTrabalhoButton;
    private Button usarDescansoButton;

    @Override
    public void start(Stage primaryStage) {
        // --- Inicializa os componentes ---
        timerLabel = new Label("00:00:00");
        timerLabel.setFont(new Font("Arial", 48));

        saldoDescansoLabel = new Label("Descanso acumulado: 00:00");

        iniciarTrabalhoButton = new Button("Iniciar Trabalho");
        usarDescansoButton = new Button("Usar Descanso");
        
        // --- Configura as Ações dos Botões ---
        iniciarTrabalhoButton.setOnAction(e -> toggleTimer());
        
        // Ação para o botão de usar descanso (será implementada no futuro)
        usarDescansoButton.setOnAction(e -> System.out.println("Botão 'Usar Descanso' clicado!"));

        // --- Configura o Timer ---
        setupTimer();

        // --- Monta o Layout da Tela ---
        VBox root = new VBox(20, timerLabel, saldoDescansoLabel, iniciarTrabalhoButton, usarDescansoButton);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Flexrithm");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void setupTimer() {
        // Cria uma timeline que executa a cada 1 segundo
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            tempoEmSegundos++;
            timerLabel.setText(formatarTempo(tempoEmSegundos));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE); // Repete para sempre
    }

    private void toggleTimer() {
        if (isTimerRunning) {
            timeline.pause();
            iniciarTrabalhoButton.setText("Retomar Trabalho");
        } else {
            timeline.play();
            iniciarTrabalhoButton.setText("Pausar Trabalho");
        }
        isTimerRunning = !isTimerRunning; // Inverte o estado
    }

    private String formatarTempo(int totalSegundos) {
        int horas = totalSegundos / 3600;
        int minutos = (totalSegundos % 3600) / 60;
        int segundos = totalSegundos % 60;
        return String.format("%02d:%02d:%02d", horas, minutos, segundos);
    }

    public static void main(String[] args) {
        launch(args);
    }
}