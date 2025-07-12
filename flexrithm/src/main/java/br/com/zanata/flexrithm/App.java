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

    // --- Constantes de Configuração ---
    private static final int CICLO_TRABALHO_SEGUNDOS = 10; // Apenas 10 segundos para testar
    private static final int GANHO_DESCANSO_SEGUNDOS = 5;  // Ganha 5 segundos de descanso

    // --- Variáveis de Estado ---
    private Timeline timeline;
    private int tempoTrabalhoSegundos = 0;
    private int saldoDescansoSegundos = 0;
    private boolean isTimerRunning = false;

    // --- Componentes da Interface ---
    private Label timerLabel;
    private Label saldoDescansoLabel;
    private Button iniciarTrabalhoButton;
    private Button usarDescansoButton;

    @Override
    public void start(Stage primaryStage) {
        timerLabel = new Label("00:00:00");
        timerLabel.setFont(new Font("Arial", 48));

        saldoDescansoLabel = new Label("Descanso acumulado: 00:00");

        iniciarTrabalhoButton = new Button("Iniciar Trabalho");
        usarDescansoButton = new Button("Usar Descanso");
        
        iniciarTrabalhoButton.setOnAction(e -> toggleTimer());
        usarDescansoButton.setOnAction(e -> System.out.println("Botão 'Usar Descanso' clicado!"));

        setupTimer();

        VBox root = new VBox(20, timerLabel, saldoDescansoLabel, iniciarTrabalhoButton, usarDescansoButton);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Flexrithm");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void setupTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            // Incrementa o tempo de trabalho
            tempoTrabalhoSegundos++;
            timerLabel.setText(formatarTempoTrabalho(tempoTrabalhoSegundos));

            // Verifica se um ciclo de trabalho foi completado
            if (tempoTrabalhoSegundos > 0 && tempoTrabalhoSegundos % CICLO_TRABALHO_SEGUNDOS == 0) {
                saldoDescansoSegundos += GANHO_DESCANSO_SEGUNDOS;
                saldoDescansoLabel.setText("Descanso acumulado: " + formatarTempoDescanso(saldoDescansoSegundos));
                System.out.println("Parabéns! Você ganhou " + (GANHO_DESCANSO_SEGUNDOS / 60) + " minutos de descanso!");
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void toggleTimer() {
        if (isTimerRunning) {
            timeline.pause();
            iniciarTrabalhoButton.setText("Retomar Trabalho");
        } else {
            timeline.play();
            iniciarTrabalhoButton.setText("Pausar Trabalho");
        }
        isTimerRunning = !isTimerRunning;
    }

    private String formatarTempoTrabalho(int totalSegundos) {
        int horas = totalSegundos / 3600;
        int minutos = (totalSegundos % 3600) / 60;
        int segundos = totalSegundos % 60;
        return String.format("%02d:%02d:%02d", horas, minutos, segundos);
    }

    // Formata o tempo de descanso (sem horas)
    private String formatarTempoDescanso(int totalSegundos) {
        int minutos = totalSegundos / 60;
        int segundos = totalSegundos % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }

    public static void main(String[] args) {
        launch(args);
    }
}