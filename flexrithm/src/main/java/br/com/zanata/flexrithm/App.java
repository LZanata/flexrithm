package br.com.zanata.flexrithm;

import javafx.application.Application;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {

    private enum EstadoApp { PARADO, TRABALHANDO, DESCANSO }

    // --- Configurações (agora são variáveis) ---
    private int cicloTrabalhoSegundos = 30 * 60; // Padrão: 30 min
    private int ganhoDescansoSegundos = 5 * 60;  // Padrão: 5 min

    // --- Variáveis de Estado ---
    private Stage primaryStage; // Guardar referência da janela principal
    private Timeline trabalhoTimeline;
    private Timeline descansoTimeline;
    private int tempoTrabalhoSegundos = 0;
    private int saldoDescansoSegundos = 0;
    private EstadoApp estadoAtual = EstadoApp.PARADO;

    // --- Componentes da Interface ---
    private Label timerLabel, saldoDescansoLabel;
    private Button iniciarTrabalhoButton, usarDescansoButton, configuracoesButton;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage; // Salva a referência da janela principal

        // --- Inicializar componentes ---
        timerLabel = new Label("00:00:00");
        timerLabel.setFont(new Font("Arial", 48));
        saldoDescansoLabel = new Label(); // Será atualizado pelo atualizarUI()
        iniciarTrabalhoButton = new Button();
        usarDescansoButton = new Button("Usar Descanso");
        configuracoesButton = new Button("Configurações");

        // --- Configurar Ações ---
        iniciarTrabalhoButton.setOnAction(e -> toggleTrabalho());
        usarDescansoButton.setOnAction(e -> iniciarModoDescanso());
        configuracoesButton.setOnAction(e -> abrirTelaConfiguracoes());

        // --- Configurar Timers ---
        setupTrabalhoTimer();
        setupDescansoTimer();

        // --- Montar Layout ---
        VBox root = new VBox(20, timerLabel, saldoDescansoLabel, iniciarTrabalhoButton, usarDescansoButton, configuracoesButton);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 400, 350); // Aumentei um pouco a altura
        primaryStage.setTitle("Flexrithm");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        atualizarUI();
    }
    
    // MÉTODO NOVO: Cria e exibe a janela de configurações
    private void abrirTelaConfiguracoes() {
        Stage settingsStage = new Stage();
        settingsStage.initModality(Modality.APPLICATION_MODAL); // Bloqueia a janela principal
        settingsStage.initOwner(primaryStage);
        settingsStage.setTitle("Configurações do Flexrithm");

        // Layout em Grade para organizar labels e campos de texto
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        // --- Componentes da nova janela ---
        Label trabalhoLabel = new Label("A cada (minutos de trabalho):");
        TextField trabalhoTextField = new TextField(String.valueOf(cicloTrabalhoSegundos / 60));

        Label descansoLabel = new Label("Ganhar (minutos de descanso):");
        TextField descansoTextField = new TextField(String.valueOf(ganhoDescansoSegundos / 60));

        Button salvarButton = new Button("Salvar");
        salvarButton.setOnAction(e -> {
            try {
                int trabalhoMin = Integer.parseInt(trabalhoTextField.getText());
                int descansoMin = Integer.parseInt(descansoTextField.getText());

                // Atualiza as variáveis de configuração (convertendo para segundos)
                this.cicloTrabalhoSegundos = trabalhoMin * 60;
                this.ganhoDescansoSegundos = descansoMin * 60;
                
                System.out.println("Configurações salvas!");
                settingsStage.close(); // Fecha a janela de configurações
            } catch (NumberFormatException ex) {
                // Se o usuário digitar texto em vez de número, apenas ignora
                System.out.println("Erro: Por favor, insira apenas números.");
            }
        });

        // Adiciona os componentes à grade
        grid.add(trabalhoLabel, 0, 0);
        grid.add(trabalhoTextField, 1, 0);
        grid.add(descansoLabel, 0, 1);
        grid.add(descansoTextField, 1, 1);
        grid.add(salvarButton, 1, 2);

        Scene settingsScene = new Scene(grid);
        settingsStage.setScene(settingsScene);
        settingsStage.showAndWait(); // Mostra a janela e espera ela ser fechada
    }

    private void setupTrabalhoTimer() {
        trabalhoTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            tempoTrabalhoSegundos++;
            timerLabel.setText(formatarTempoTrabalho(tempoTrabalhoSegundos));
            if (tempoTrabalhoSegundos > 0 && cicloTrabalhoSegundos > 0 && tempoTrabalhoSegundos % cicloTrabalhoSegundos == 0) {
                saldoDescansoSegundos += ganhoDescansoSegundos;
                atualizarUI();
            }
        }));
        trabalhoTimeline.setCycleCount(Timeline.INDEFINITE);
    }
    
    private void setupDescansoTimer() {
        descansoTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            saldoDescansoSegundos--;
            timerLabel.setText("Descanso: " + formatarTempoDescanso(saldoDescansoSegundos));
            if (saldoDescansoSegundos <= 0) {
                descansoTimeline.stop();
                estadoAtual = EstadoApp.PARADO;
                timerLabel.setText(formatarTempoTrabalho(tempoTrabalhoSegundos));
                atualizarUI();
            }
        }));
        descansoTimeline.setCycleCount(Timeline.INDEFINITE);
    }

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

    private void iniciarModoDescanso() {
        if (saldoDescansoSegundos > 0 && estadoAtual == EstadoApp.PARADO) {
            trabalhoTimeline.pause();
            estadoAtual = EstadoApp.DESCANSO;
            descansoTimeline.play();
            atualizarUI();
        }
    }

    private void atualizarUI() {
        saldoDescansoLabel.setText("Descanso acumulado: " + formatarTempoDescanso(saldoDescansoSegundos));
        switch (estadoAtual) {
            case PARADO:
                iniciarTrabalhoButton.setText("Iniciar Trabalho");
                iniciarTrabalhoButton.setDisable(false);
                usarDescansoButton.setDisable(saldoDescansoSegundos <= 0);
                timerLabel.setTextFill(Color.BLACK);
                break;
            case TRABALHANDO:
                iniciarTrabalhoButton.setText("Pausar Trabalho");
                iniciarTrabalhoButton.setDisable(false);
                usarDescansoButton.setDisable(true);
                timerLabel.setTextFill(Color.BLACK);
                break;
            case DESCANSO:
                iniciarTrabalhoButton.setDisable(true);
                usarDescansoButton.setDisable(true);
                timerLabel.setTextFill(Color.GREEN);
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