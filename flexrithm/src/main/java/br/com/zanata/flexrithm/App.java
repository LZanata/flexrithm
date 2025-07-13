package br.com.zanata.flexrithm;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
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

    private enum EstadoApp {
        PARADO, TRABALHANDO, DESCANSO, DESCANSO_PAUSADO
    }

    private int cicloTrabalhoSegundos = 30 * 60;
    private int ganhoDescansoSegundos = 5 * 60;

    private Stage primaryStage;
    private Timeline trabalhoTimeline;
    private Timeline descansoTimeline;
    private int tempoTrabalhoSegundos = 0;
    private int saldoDescansoSegundos = 0;
    private EstadoApp estadoAtual = EstadoApp.PARADO;

    private Label timerLabel, saldoDescansoLabel;
    private Button iniciarTrabalhoButton, usarDescansoButton, configuracoesButton;

    // --- Persistência de Dados ---
    private final Gson gson = new Gson();
    private final String CAMINHO_ARQUIVO_SAVE = System.getProperty("user.home") + "/flexrithm_dados.json";

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        carregarDados();

        timerLabel = new Label();
        timerLabel.setFont(new Font("Arial", 48));
        saldoDescansoLabel = new Label();
        iniciarTrabalhoButton = new Button();
        usarDescansoButton = new Button();
        configuracoesButton = new Button("Configurações");

        iniciarTrabalhoButton.setOnAction(e -> toggleTrabalho());
        usarDescansoButton.setOnAction(e -> toggleDescanso());
        configuracoesButton.setOnAction(e -> abrirTelaConfiguracoes());

        setupTrabalhoTimer();
        setupDescansoTimer();

        VBox root = new VBox(20, timerLabel, saldoDescansoLabel, iniciarTrabalhoButton, usarDescansoButton, configuracoesButton);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 400, 350);
        primaryStage.setTitle("Flexrithm");
        primaryStage.setScene(scene);
        primaryStage.show();

        atualizarUI();
    }

    @Override
    public void stop() {
        salvarDados();
        System.out.println("Aplicativo fechado, dados salvos!");
    }

    private void abrirTelaConfiguracoes() {
        Stage settingsStage = new Stage();
        settingsStage.initModality(Modality.APPLICATION_MODAL);
        settingsStage.initOwner(primaryStage);
        settingsStage.setTitle("Configurações do Flexrithm");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);
        Label trabalhoLabel = new Label("A cada (minutos de trabalho):");
        TextField trabalhoTextField = new TextField(String.valueOf(cicloTrabalhoSegundos / 60));
        Label descansoLabel = new Label("Ganhar (minutos de descanso):");
        TextField descansoTextField = new TextField(String.valueOf(ganhoDescansoSegundos / 60));
        Button salvarButton = new Button("Salvar");
        salvarButton.setOnAction(e -> {
            try {
                int trabalhoMin = Integer.parseInt(trabalhoTextField.getText());
                int descansoMin = Integer.parseInt(descansoTextField.getText());
                this.cicloTrabalhoSegundos = trabalhoMin * 60;
                this.ganhoDescansoSegundos = descansoMin * 60;
                settingsStage.close();
            } catch (NumberFormatException ex) {
                System.out.println("Erro: Por favor, insira apenas números.");
            }
        });
        grid.add(trabalhoLabel, 0, 0);
        grid.add(trabalhoTextField, 1, 0);
        grid.add(descansoLabel, 0, 1);
        grid.add(descansoTextField, 1, 1);
        grid.add(salvarButton, 1, 2);
        Scene settingsScene = new Scene(grid);
        settingsStage.setScene(settingsScene);
        settingsStage.showAndWait();
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
            timerLabel.setText(formatarTempoDescanso(saldoDescansoSegundos));
            if (saldoDescansoSegundos <= 0) {
                descansoTimeline.stop();
                estadoAtual = EstadoApp.PARADO;
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
        } else if (estadoAtual == EstadoApp.DESCANSO_PAUSADO) {
            descansoTimeline.stop();
            trabalhoTimeline.play();
            estadoAtual = EstadoApp.TRABALHANDO;
        }
        atualizarUI();
    }

    private void toggleDescanso() {
        switch (estadoAtual) {
            case PARADO:
                if (saldoDescansoSegundos > 0) {
                    trabalhoTimeline.pause();
                    estadoAtual = EstadoApp.DESCANSO;
                    timerLabel.setText(formatarTempoDescanso(saldoDescansoSegundos));
                    descansoTimeline.play();
                }
                break;
            case DESCANSO:
                descansoTimeline.pause();
                estadoAtual = EstadoApp.DESCANSO_PAUSADO;
                break;
            case DESCANSO_PAUSADO:
                descansoTimeline.play();
                estadoAtual = EstadoApp.DESCANSO;
                break;
            default:
                break;
        }
        atualizarUI();
    }

    private void atualizarUI() {
        saldoDescansoLabel.setText("Descanso acumulado: " + formatarTempoDescanso(saldoDescansoSegundos));
        switch (estadoAtual) {
            case PARADO:
                iniciarTrabalhoButton.setText("Iniciar Trabalho");
                iniciarTrabalhoButton.setDisable(false);
                usarDescansoButton.setText("Usar Descanso");
                usarDescansoButton.setDisable(saldoDescansoSegundos <= 0);
                timerLabel.setTextFill(Color.BLACK);
                timerLabel.setText(formatarTempoTrabalho(tempoTrabalhoSegundos));
                break;
            case TRABALHANDO:
                iniciarTrabalhoButton.setText("Pausar Trabalho");
                iniciarTrabalhoButton.setDisable(false);
                usarDescansoButton.setDisable(true);
                timerLabel.setTextFill(Color.BLACK);
                break;
            case DESCANSO:
                iniciarTrabalhoButton.setDisable(true);
                usarDescansoButton.setText("Pausar Descanso");
                usarDescansoButton.setDisable(false);
                timerLabel.setTextFill(Color.GREEN);
                break;
            case DESCANSO_PAUSADO:
                iniciarTrabalhoButton.setText("Retomar Trabalho");
                iniciarTrabalhoButton.setDisable(false);
                usarDescansoButton.setText("Retomar Descanso");
                usarDescansoButton.setDisable(false);
                timerLabel.setTextFill(Color.ORANGE);
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

    private void salvarDados() {
        DadosApp dados = new DadosApp();
        dados.tempoTrabalhoSegundos = this.tempoTrabalhoSegundos;
        dados.saldoDescansoSegundos = this.saldoDescansoSegundos;
        dados.cicloTrabalhoSegundos = this.cicloTrabalhoSegundos;
        dados.ganhoDescansoSegundos = this.ganhoDescansoSegundos;
        try (FileWriter writer = new FileWriter(CAMINHO_ARQUIVO_SAVE)) {
            gson.toJson(dados, writer);
        } catch (IOException e) {
            System.err.println("Falha ao salvar os dados.");
            e.printStackTrace();
        }
    }

    private void carregarDados() {
        try (FileReader reader = new FileReader(CAMINHO_ARQUIVO_SAVE)) {
            DadosApp dados = gson.fromJson(reader, DadosApp.class);
            if (dados != null) {
                this.tempoTrabalhoSegundos = dados.tempoTrabalhoSegundos;
                this.saldoDescansoSegundos = dados.saldoDescansoSegundos;
                this.cicloTrabalhoSegundos = dados.cicloTrabalhoSegundos;
                this.ganhoDescansoSegundos = dados.ganhoDescansoSegundos;
                System.out.println("Dados carregados com sucesso!");
            }
        } catch (IOException e) {
            System.out.println("Nenhum arquivo de save encontrado. Usando valores padrão.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}