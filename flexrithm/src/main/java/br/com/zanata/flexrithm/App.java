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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {

    // --- Enum para controle de estado ---
    private enum EstadoApp { PARADO, TRABALHANDO, DESCANSO }

    // --- Constantes de Configuração ---
    private static final int CICLO_TRABALHO_SEGUNDOS = 10; // A CADA 10 SEGUNDOS PARA TESTE
    private static final int GANHO_DESCANSO_SEGUNDOS = 15; // GANHA 15 SEGUNDOS PARA TESTE

    // --- Variáveis de Estado ---
    private Timeline trabalhoTimeline;
    private Timeline descansoTimeline;
    private int tempoTrabalhoSegundos = 0;
    private int saldoDescansoSegundos = 0;
    private EstadoApp estadoAtual = EstadoApp.PARADO;

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
        
        // --- Configurar Ações ---
        iniciarTrabalhoButton.setOnAction(e -> toggleTrabalho());
        usarDescansoButton.setOnAction(e -> iniciarModoDescanso());

        // --- Configurar os Timers ---
        setupTrabalhoTimer();
        setupDescansoTimer();

        // --- Montar o Layout ---
        VBox root = new VBox(20, timerLabel, saldoDescansoLabel, iniciarTrabalhoButton, usarDescansoButton);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Flexrithm");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        atualizarUI(); // Atualiza a UI para o estado inicial
    }
    
    // Timer para contar o tempo de trabalho (para cima)
    private void setupTrabalhoTimer() {
        trabalhoTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            tempoTrabalhoSegundos++;
            timerLabel.setText(formatarTempoTrabalho(tempoTrabalhoSegundos));

            if (tempoTrabalhoSegundos > 0 && tempoTrabalhoSegundos % CICLO_TRABALHO_SEGUNDOS == 0) {
                saldoDescansoSegundos += GANHO_DESCANSO_SEGUNDOS;
                atualizarUI();
            }
        }));
        trabalhoTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    // Timer para contar o tempo de descanso (para baixo)
    private void setupDescansoTimer() {
        descansoTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            saldoDescansoSegundos--;
            timerLabel.setText(formatarTempoDescanso(saldoDescansoSegundos));
            
            // Se o descanso acabar, volta ao estado parado
            if (saldoDescansoSegundos <= 0) {
                descansoTimeline.stop();
                estadoAtual = EstadoApp.PARADO;
                timerLabel.setText(formatarTempoTrabalho(tempoTrabalhoSegundos)); // Mostra o tempo de trabalho novamente
                atualizarUI();
            }
        }));
        descansoTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    // Controla o início e pausa do trabalho
    private void toggleTrabalho() {
        if (estadoAtual == EstadoApp.TRABALHANDO) {
            trabalhoTimeline.pause();
            estadoAtual = EstadoApp.PARADO;
        } else if (estadoAtual == EstadoApp.PARADO) {
            trabalhoTimeline.play();
            estadoAtual = EstadoApp.TRABALHANDO;
        }
        atualizarUI();
    }

    // Ativa o modo de descanso
    private void iniciarModoDescanso() {
        // Só pode usar o descanso se tiver saldo e não estiver trabalhando/descansando
        if (saldoDescansoSegundos > 0 && estadoAtual == EstadoApp.PARADO) {
            trabalhoTimeline.pause(); // Garante que o timer de trabalho está pausado
            estadoAtual = EstadoApp.DESCANSO;
            descansoTimeline.play();
            atualizarUI();
        }
    }

    // Atualiza a aparência e estado dos botões e labels
    private void atualizarUI() {
        saldoDescansoLabel.setText("Descanso acumulado: " + formatarTempoDescanso(saldoDescansoSegundos));

        switch (estadoAtual) {
            case PARADO:
                iniciarTrabalhoButton.setText("Iniciar Trabalho");
                iniciarTrabalhoButton.setDisable(false);
                usarDescansoButton.setDisable(saldoDescansoSegundos <= 0); // Desativa se não tiver saldo
                timerLabel.setTextFill(Color.BLACK);
                break;
            case TRABALHANDO:
                iniciarTrabalhoButton.setText("Pausar Trabalho");
                iniciarTrabalhoButton.setDisable(false);
                usarDescansoButton.setDisable(true); // Não pode usar descanso enquanto trabalha
                timerLabel.setTextFill(Color.BLACK);
                break;
            case DESCANSO:
                iniciarTrabalhoButton.setDisable(true); // Não pode trabalhar enquanto descansa
                usarDescansoButton.setDisable(true);
                timerLabel.setTextFill(Color.GREEN); // Muda a cor para indicar o descanso
                break;
        }
    }

    private String formatarTempoTrabalho(int totalSegundos) {
        int horas = totalSegundos / 3600;
        int minutos = (totalSegundos % 3600) / 60;
        int segundos = totalSegundos % 60;
        return String.format("%02d:%02d:%02d", horas, minutos, segundos);
    }
    
    private String formatarTempoDescanso(int totalSegundos) {
        int minutos = totalSegundos / 60;
        int segundos = totalSegundos % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }

    public static void main(String[] args) {
        launch(args);
    }
}