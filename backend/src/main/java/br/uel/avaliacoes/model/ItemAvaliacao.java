package br.uel.avaliacoes.model;

import java.math.BigDecimal;

public record ItemAvaliacao(
    Integer id_item,
    Integer id_avaliacao,
    Integer id_questao,
    BigDecimal valor
) {}
