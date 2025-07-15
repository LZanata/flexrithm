package br.com.zanata.flexrithm;

import java.util.HashMap;
import java.util.Map;

public class DadosApp {
    int saldoDescansoSegundos;
    int cicloTrabalhoSegundos;
    int ganhoDescansoSegundos;
    int limiteMaximoDescansoSegundos;
    Map<String, Boolean> conquistasDesbloqueadas = new HashMap<>();
    Map<String, DadosDiarios> historicoDiario = new HashMap<>();
}