package br.com.zanata.flexrithm;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {

    private enum EstadoApp {
        PARADO, TRABALHANDO, DESCANSO, DESCANSO_PAUSADO
    }

    private int cicloTrabalhoSegundos = 30 * 60;
    private int ganhoDescansoSegundos = 5 * 60;
    private int limiteMaximoDescansoSegundos = 60 * 60;

    private Stage primaryStage;
    private Timeline trabalhoTimeline, descansoTimeline;
    private int tempoSessaoAtualSegundos = 0;
    private int saldoDescansoSegundos = 0;
    private EstadoApp estadoAtual = EstadoApp.PARADO;
    private Map<Conquista, Boolean> estadoConquistas = new EnumMap<>(Conquista.class);
    private Map<String, DadosDiarios> estadoHistorico = new HashMap<>();

    private Label timerLabel, saldoDescansoLabel;
    private Button iniciarTrabalhoButton, usarDescansoButton, configuracoesButton, finalizarTrabalhoButton,
            conquistasButton, relatoriosButton;

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
        finalizarTrabalhoButton = new Button("Finalizar Trabalho");
        conquistasButton = new Button("Conquistas");
        relatoriosButton = new Button("Relatórios");

        iniciarTrabalhoButton.setOnAction(e -> toggleTrabalho());
        usarDescansoButton.setOnAction(e -> toggleDescanso());
        configuracoesButton.setOnAction(e -> abrirTelaConfiguracoes());
        finalizarTrabalhoButton.setOnAction(e -> finalizarTrabalho());
        conquistasButton.setOnAction(e -> abrirTelaConquistas());
        relatoriosButton.setOnAction(e -> abrirTelaRelatorios());

        setupTrabalhoTimer();
        setupDescansoTimer();

        VBox root = new VBox(15, timerLabel, saldoDescansoLabel, iniciarTrabalhoButton, usarDescansoButton,
                finalizarTrabalhoButton, configuracoesButton, conquistasButton, relatoriosButton);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 400, 450);
        primaryStage.setTitle("Flexrithm");
        primaryStage.setScene(scene);
        primaryStage.show();

        atualizarUI();
    }

    @Override
    public void stop() {
        atualizarHistoricoComSessaoAtual();
        salvarDados();
        System.out.println("Aplicativo fechado, dados salvos!");
    }

    private void abrirTelaRelatorios() {
        Stage relatoriosStage = new Stage();
        relatoriosStage.initModality(Modality.APPLICATION_MODAL);
        relatoriosStage.initOwner(primaryStage);
        relatoriosStage.setTitle("Relatório de Produtividade");

        TextArea relatorioTextArea = new TextArea();
        relatorioTextArea.setEditable(false);
        relatorioTextArea.setFont(Font.font("Monospaced", 14));

        StringBuilder relatorioTexto = new StringBuilder("--- Tempo de Trabalho (Últimos 7 dias) ---\n\n");
        DateTimeFormatter formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (int i = 0; i < 7; i++) {
            LocalDate dia = LocalDate.now().minusDays(i);
            String diaFormatado = dia.format(formatadorData);
            String chaveDoMapa = dia.format(DateTimeFormatter.ISO_LOCAL_DATE);

            DadosDiarios dadosDoDia = estadoHistorico.get(chaveDoMapa);
            int segundosTrabalhados = (dadosDoDia != null) ? dadosDoDia.totalTrabalhoSegundos : 0;

            relatorioTexto.append(String.format("%s: %s\n", diaFormatado, formatarTempoTrabalho(segundosTrabalhados)));
        }

        relatorioTextArea.setText(relatorioTexto.toString());

        VBox layout = new VBox(relatorioTextArea);
        Scene scene = new Scene(layout, 400, 250);
        relatoriosStage.setScene(scene);
        relatoriosStage.showAndWait();
    }

    private void abrirTelaConquistas() {
        Stage conquistasStage = new Stage();
        conquistasStage.initModality(Modality.APPLICATION_MODAL);
        conquistasStage.initOwner(primaryStage);
        conquistasStage.setTitle("Suas Conquistas");
        ListView<Conquista> listView = new ListView<>();
        listView.getItems().addAll(Conquista.values());
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Conquista conquista, boolean empty) {
                super.updateItem(conquista, empty);
                if (empty || conquista == null) {
                    setGraphic(null);
                } else {
                    boolean desbloqueada = estadoConquistas.getOrDefault(conquista, false);
                    Label nomeConquista = new Label((desbloqueada ? "[✓] " : "[ ] ") + conquista.getNome());
                    nomeConquista.setFont(Font.font("System", FontWeight.BOLD, 14));
                    Label descricaoConquista = new Label(conquista.getDescricao());
                    descricaoConquista.setWrapText(true);
                    VBox conquistaLayout = new VBox(5, nomeConquista, descricaoConquista);
                    setGraphic(conquistaLayout);
                }
            }
        });
        VBox layout = new VBox(listView);
        Scene scene = new Scene(layout, 350, 250);
        conquistasStage.setScene(scene);
        conquistasStage.showAndWait();
    }

    private void finalizarTrabalho() {
        if ((estadoAtual == EstadoApp.PARADO || estadoAtual == EstadoApp.DESCANSO_PAUSADO)
                && tempoSessaoAtualSegundos > 0) {
            atualizarHistoricoComSessaoAtual(); // Move o tempo da sessão para o histórico e zera a sessão.
            salvarDados(); // Salva o novo estado.
            atualizarUI(); // Atualiza a tela.
        }
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
        Label limiteLabel = new Label("Limite máx. de descanso (min):");
        TextField limiteTextField = new TextField(String.valueOf(limiteMaximoDescansoSegundos / 60));
        Button salvarButton = new Button("Salvar");
        salvarButton.setOnAction(e -> {
            try {
                int trabalhoMin = Integer.parseInt(trabalhoTextField.getText());
                int descansoMin = Integer.parseInt(descansoTextField.getText());
                int limiteMin = Integer.parseInt(limiteTextField.getText());
                this.cicloTrabalhoSegundos = trabalhoMin * 60;
                this.ganhoDescansoSegundos = descansoMin * 60;
                this.limiteMaximoDescansoSegundos = limiteMin * 60;
                settingsStage.close();
            } catch (NumberFormatException ex) {
                System.out.println("Erro: Por favor, insira apenas números.");
            }
        });
        grid.add(trabalhoLabel, 0, 0);
        grid.add(trabalhoTextField, 1, 0);
        grid.add(descansoLabel, 0, 1);
        grid.add(descansoTextField, 1, 1);
        grid.add(limiteLabel, 0, 2);
        grid.add(limiteTextField, 1, 2);
        grid.add(salvarButton, 1, 3);
        Scene settingsScene = new Scene(grid);
        settingsStage.setScene(settingsScene);
        settingsStage.showAndWait();
    }

    private void setupTrabalhoTimer() {
        trabalhoTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            tempoSessaoAtualSegundos++;
            timerLabel.setText(formatarTempoTrabalho(tempoSessaoAtualSegundos));

            if (tempoSessaoAtualSegundos > 0 && cicloTrabalhoSegundos > 0
                    && tempoSessaoAtualSegundos % cicloTrabalhoSegundos == 0) {
                if (saldoDescansoSegundos < limiteMaximoDescansoSegundos) {
                    int novoSaldo = saldoDescansoSegundos + ganhoDescansoSegundos;
                    saldoDescansoSegundos = Math.min(novoSaldo, limiteMaximoDescansoSegundos);
                    atualizarUI();
                }
            }

            int totalHoje = getTotalTrabalhoHoje() + tempoSessaoAtualSegundos;
            if (totalHoje == 3600) {
                verificarEdesbloquearConquista(Conquista.FOCADO_POR_1_HORA);
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

    private int getTotalTrabalhoHoje() {
        String hoje = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return estadoHistorico.getOrDefault(hoje, new DadosDiarios()).totalTrabalhoSegundos;
    }

    private void atualizarHistoricoComSessaoAtual() {
        if (tempoSessaoAtualSegundos > 0) {
            String hoje = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            DadosDiarios dadosDeHoje = estadoHistorico.getOrDefault(hoje, new DadosDiarios());
            dadosDeHoje.totalTrabalhoSegundos += tempoSessaoAtualSegundos;
            estadoHistorico.put(hoje, dadosDeHoje);
            tempoSessaoAtualSegundos = 0;
        }
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
                    atualizarHistoricoComSessaoAtual();
                    trabalhoTimeline.pause();
                    estadoAtual = EstadoApp.DESCANSO;
                    timerLabel.setText(formatarTempoDescanso(saldoDescansoSegundos));
                    verificarEdesbloquearConquista(Conquista.PIONEIRO_DO_DESCANSO);
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

        boolean podeFinalizar = tempoSessaoAtualSegundos > 0 &&
                (estadoAtual == EstadoApp.PARADO || estadoAtual == EstadoApp.DESCANSO_PAUSADO);
        finalizarTrabalhoButton.setDisable(!podeFinalizar);

        switch (estadoAtual) {
            case PARADO:
                iniciarTrabalhoButton.setText("Iniciar Trabalho");
                iniciarTrabalhoButton.setDisable(false);
                usarDescansoButton.setText("Usar Descanso");
                usarDescansoButton.setDisable(saldoDescansoSegundos <= 0);
                timerLabel.setTextFill(Color.BLACK);
                timerLabel.setText(formatarTempoTrabalho(tempoSessaoAtualSegundos));
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

    private void verificarEdesbloquearConquista(Conquista conquista) {
        if (!estadoConquistas.getOrDefault(conquista, false)) {
            estadoConquistas.put(conquista, true);
            Platform.runLater(() -> mostrarNotificacaoConquista(conquista));
            salvarDados();
        }
    }

    private void mostrarNotificacaoConquista(Conquista conquista) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Conquista Desbloqueada!");
        alert.setHeaderText(conquista.getNome());
        alert.setContentText(conquista.getDescricao());
        alert.showAndWait();
    }

    private void salvarDados() {
        DadosApp dados = new DadosApp();
        dados.saldoDescansoSegundos = this.saldoDescansoSegundos;
        dados.cicloTrabalhoSegundos = this.cicloTrabalhoSegundos;
        dados.ganhoDescansoSegundos = this.ganhoDescansoSegundos;
        dados.limiteMaximoDescansoSegundos = this.limiteMaximoDescansoSegundos;
        dados.historicoDiario = this.estadoHistorico;

        // Limpa o mapa antigo e preenche com o estado atual
        dados.conquistasDesbloqueadas = new HashMap<>();
        for (Map.Entry<Conquista, Boolean> entry : estadoConquistas.entrySet()) {
            dados.conquistasDesbloqueadas.put(entry.getKey().name(), entry.getValue());
        }

        try (FileWriter writer = new FileWriter(CAMINHO_ARQUIVO_SAVE)) {
            gson.toJson(dados, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void carregarDados() {
        try (FileReader reader = new FileReader(CAMINHO_ARQUIVO_SAVE)) {
            DadosApp dados = gson.fromJson(reader, DadosApp.class);
            if (dados != null) {
                this.tempoSessaoAtualSegundos = 0; 
                this.saldoDescansoSegundos = dados.saldoDescansoSegundos;
                this.cicloTrabalhoSegundos = dados.cicloTrabalhoSegundos > 0 ? dados.cicloTrabalhoSegundos : 30 * 60;
                this.ganhoDescansoSegundos = dados.ganhoDescansoSegundos > 0 ? dados.ganhoDescansoSegundos : 5 * 60;
                this.limiteMaximoDescansoSegundos = (dados.limiteMaximoDescansoSegundos > 0)
                        ? dados.limiteMaximoDescansoSegundos
                        : 60 * 60;

                if (dados.historicoDiario != null) {
                    this.estadoHistorico = dados.historicoDiario;
                }
                if (dados.conquistasDesbloqueadas != null) {
                    for (Map.Entry<String, Boolean> entry : dados.conquistasDesbloqueadas.entrySet()) {
                        try {
                            Conquista c = Conquista.valueOf(entry.getKey());
                            estadoConquistas.put(c, entry.getValue());
                        } catch (IllegalArgumentException e) {
                            /* Ignora */ }
                    }
                }
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