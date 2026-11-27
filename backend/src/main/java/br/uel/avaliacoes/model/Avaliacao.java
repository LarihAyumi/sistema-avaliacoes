package br.uel.avaliacoes.model;

import java.time.LocalDateTime;

public record Avaliacao(
    Integer id_avaliacao,
    String titulo,
    String descricao,
    LocalDateTime inicio,
    LocalDateTime fim,
    Integer id_criador
) {}
