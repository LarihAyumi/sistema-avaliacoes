package br.uel.avaliacoes.model;

public record Participacao(
    Integer id_participacao,
    Integer id_usuario,
    Integer id_avaliacao,
    String status
) {}
