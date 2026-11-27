package br.uel.avaliacoes.dao;

import br.uel.avaliacoes.model.Nota;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;

@Repository
public class NotaDAO {

    private final DataSource ds;

    public NotaDAO(DataSource ds) {
        this.ds = ds;
    }

    public void corrigir(int id, BigDecimal nota)
            throws SQLException {

        if (nota == null ||
            nota.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "Nota invalida"
            );
        }

        String sql = """
            UPDATE resposta r
            SET nota_obtida = ?
            FROM item_avaliacao i
            JOIN questao q ON q.id_questao = i.id_questao
            JOIN tipo_questao t ON t.id_tipo = q.id_tipo
            WHERE r.id_resposta = ?
              AND r.id_item = i.id_item
              AND t.descricao = 'discursiva'
              AND ? <= i.valor
            """;

        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setBigDecimal(1, nota);
            ps.setInt(2, id);
            ps.setBigDecimal(3, nota);

            int n = ps.executeUpdate();

            if (n == 0) {
                throw new IllegalArgumentException(
                    "Resposta discursiva inexistente ou nota acima do valor"
                );
            }
        }
    }

    public Nota calcular(int id) throws SQLException {

        String sql = """
            SELECT
                p.id_participacao,
                COALESCE(SUM(r.nota_obtida), 0) AS nota_total,
                COALESCE((
                    SELECT SUM(i.valor)
                    FROM item_avaliacao i
                    WHERE i.id_avaliacao = p.id_avaliacao
                ), 0) AS valor_total,
                COUNT(*) FILTER (
                    WHERE r.id_resposta IS NOT NULL
                      AND r.nota_obtida IS NULL
                ) AS pendentes
            FROM participacao p
            LEFT JOIN resposta r
              ON r.id_participacao = p.id_participacao
            WHERE p.id_participacao = ?
            GROUP BY p.id_participacao, p.id_avaliacao
            """;

        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                        "Participacao inexistente"
                    );
                }

                return new Nota(
                    rs.getInt("id_participacao"),
                    rs.getBigDecimal("nota_total"),
                    rs.getBigDecimal("valor_total"),
                    rs.getLong("pendentes")
                );
            }
        }
    }
}
