package br.uel.avaliacoes.model;

import java.math.BigDecimal;

public record Nota(
    Integer id_participacao,
    BigDecimal nota_total,
    BigDecimal valor_total,
    Long pendentes
) {}
