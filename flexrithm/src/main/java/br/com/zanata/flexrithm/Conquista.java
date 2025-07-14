package br.com.zanata.flexrithm;

public enum Conquista {
    PIONEIRO_DO_DESCANSO("Pioneiro do Descanso", "Use o modo de descanso pela primeira vez."),
    FOCADO_POR_1_HORA("Foco de Aço", "Trabalhe por 1 hora sem parar."),
    ROTINA_DE_ACO("Rotina de Aço", "Use o aplicativo por 5 dias seguidos.");

    private final String nome;
    private final String descricao;

    Conquista(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }
}