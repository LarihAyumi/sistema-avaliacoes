package br.uel.avaliacoes.dao;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Repository
public class RelatorioDAO {

    private final DataSource ds;

    public RelatorioDAO(DataSource ds) {
        this.ds = ds;
    }

    private List<Map<String, Object>> consultar(
            String sql, Integer id) throws SQLException {

        List<Map<String, Object>> lista = new ArrayList<>();

        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            if (id != null) {
                ps.setInt(1, id);
            }

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int n = md.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> linha = new LinkedHashMap<>();

                    for (int i = 1; i <= n; i++) {
                        linha.put(
                            md.getColumnLabel(i),
                            rs.getObject(i)
                        );
                    }

                    lista.add(linha);
                }
            }
        }

        return lista;
    }

    public List<Map<String, Object>> desempenho()
            throws SQLException {

        String sql = """
            WITH notas AS (
                SELECT
                    p.id_participacao,
                    p.id_avaliacao,
                    COALESCE(SUM(r.nota_obtida), 0) AS nota,
                    COUNT(r.id_resposta) AS respostas
                FROM participacao p
                LEFT JOIN resposta r
                    ON r.id_participacao = p.id_participacao
                GROUP BY p.id_participacao, p.id_avaliacao
            )
            SELECT
                a.id_avaliacao,
                a.titulo,
                COUNT(n.id_participacao) AS alunos,
                COUNT(n.id_participacao) FILTER (
                    WHERE n.respostas > 0
                ) AS responderam,
                ROUND(AVG(n.nota), 2) AS media,
                MAX(n.nota) AS maior_nota,
                MIN(n.nota) AS menor_nota
            FROM avaliacao a
            LEFT JOIN notas n
                ON n.id_avaliacao = a.id_avaliacao
            WHERE a.inicio >= '2026-01-01'
            GROUP BY a.id_avaliacao, a.titulo
            ORDER BY a.id_avaliacao
            """;

        return consultar(sql, null);
    }

    public List<Map<String, Object>> evolucao(int id)
            throws SQLException {

        String sql = """
            WITH notas AS (
                SELECT
                    p.id_usuario,
                    p.id_avaliacao,
                    COALESCE(SUM(r.nota_obtida), 0) AS nota
                FROM participacao p
                LEFT JOIN resposta r
                    ON r.id_participacao = p.id_participacao
                GROUP BY p.id_usuario, p.id_avaliacao
            )
            SELECT
                u.id_usuario,
                u.nome,
                a.titulo,
                a.inicio,
                n.nota AS nota_aluno,
                (
                    SELECT ROUND(AVG(n2.nota), 2)
                    FROM notas n2
                    WHERE n2.id_avaliacao = n.id_avaliacao
                ) AS media_avaliacao
            FROM notas n
            JOIN usuario u
                ON u.id_usuario = n.id_usuario
            JOIN avaliacao a
                ON a.id_avaliacao = n.id_avaliacao
            WHERE u.id_usuario = ?
              AND a.inicio >= '2026-01-01'
            ORDER BY a.inicio, a.id_avaliacao
            """;

        return consultar(sql, id);
    }

    public List<Map<String, Object>> questoes()
            throws SQLException {

        String sql = """
            WITH erros AS (
                SELECT
                    q.id_questao,
                    q.enunciado,
                    COUNT(*) AS total_respostas,
                    COUNT(*) FILTER (
                        WHERE alt.correta = FALSE
                    ) AS total_erros
                FROM resposta r
                JOIN item_avaliacao i
                    ON i.id_item = r.id_item
                JOIN questao q
                    ON q.id_questao = i.id_questao
                JOIN tipo_questao t
                    ON t.id_tipo = q.id_tipo
                JOIN alternativa alt
                    ON alt.id_alternativa = r.id_alternativa
                WHERE t.descricao IN ('objetiva')
                  AND q.enunciado LIKE '%'
                GROUP BY q.id_questao, q.enunciado
                HAVING COUNT(*) >= 1
            ),
            ranking AS (
                SELECT *,
                    ROUND(
                        100.0 * total_erros / total_respostas, 2
                    ) AS percentual_erros
                FROM erros
            )
            SELECT
                RANK() OVER (
                    ORDER BY percentual_erros DESC
                ) AS posicao,
                id_questao,
                enunciado,
                total_respostas,
                total_erros,
                percentual_erros
            FROM ranking
            ORDER BY posicao, total_respostas DESC, id_questao
            FETCH FIRST 10 ROWS ONLY
            """;

        return consultar(sql, null);
    }
}
