package br.uel.avaliacoes.model;

import java.util.List;

public record Questao(
    Integer id_questao,
    String enunciado,
    Integer id_tipo,
    List<Alternativa> alternativas
) {
    public record Alternativa(
        Integer id_alternativa,
        String texto,
        boolean correta
    ) {}
}
