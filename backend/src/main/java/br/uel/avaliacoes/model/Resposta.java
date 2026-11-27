package br.uel.avaliacoes.model;

import java.math.BigDecimal;

public record Resposta(
    Integer id_resposta,
    Integer id_participacao,
    Integer id_item,
    Integer id_alternativa,
    String texto_livre,
    BigDecimal nota_obtida
) {}
