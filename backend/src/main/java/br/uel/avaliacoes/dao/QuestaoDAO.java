package br.uel.avaliacoes.dao;

import br.uel.avaliacoes.model.Questao;
import br.uel.avaliacoes.model.Questao.Alternativa;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class QuestaoDAO {

    private final DataSource ds;

    public QuestaoDAO(DataSource ds) {
        this.ds = ds;
    }

    public int cadastrar(Questao q) throws SQLException {
        try (Connection cn = ds.getConnection()) {
            cn.setAutoCommit(false);

            try {
                String tipo;

                try (PreparedStatement ps = cn.prepareStatement(
                    "SELECT descricao FROM tipo_questao WHERE id_tipo = ?"
                )) {
                    ps.setInt(1, q.id_tipo());

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Tipo inexistente");
                        }
                        tipo = rs.getString(1);
                    }
                }

                List<Alternativa> al = q.alternativas() == null
                    ? List.of() : q.alternativas();

                if (q.enunciado() == null ||
                    q.enunciado().isBlank()) {
                    throw new IllegalArgumentException("Enunciado obrigatorio");
                }

                if (tipo.equals("objetiva")) {
                    if (al.size() < 2) {
                        throw new IllegalArgumentException(
                            "Questao objetiva precisa de pelo menos 2 alternativas"
                        );
                    }

                    long certas = al.stream()
                        .filter(Alternativa::correta).count();

                    if (certas != 1) {
                        throw new IllegalArgumentException(
                            "Informe exatamente uma alternativa correta"
                        );
                    }

                    for (Alternativa a : al) {
                        if (a.texto() == null || a.texto().isBlank()) {
                            throw new IllegalArgumentException(
                                "Texto da alternativa obrigatorio"
                            );
                        }
                    }
                } else if (tipo.equals("discursiva")) {
                    if (!al.isEmpty()) {
                        throw new IllegalArgumentException(
                            "Questao discursiva nao aceita alternativas"
                        );
                    }
                } else {
                    throw new IllegalArgumentException("Tipo nao suportado");
                }

                int id;

                String sql = """
                    INSERT INTO questao (enunciado, id_tipo)
                    VALUES (?, ?)
                    RETURNING id_questao
                    """;

                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setString(1, q.enunciado());
                    ps.setInt(2, q.id_tipo());

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        id = rs.getInt(1);
                    }
                }

                sql = """
                    INSERT INTO alternativa (id_questao, texto, correta)
                    VALUES (?, ?, ?)
                    """;

                if (tipo.equals("objetiva")) {
                    try (PreparedStatement ps = cn.prepareStatement(sql)) {
                        for (Alternativa a : al) {
                            ps.setInt(1, id);
                            ps.setString(2, a.texto());
                            ps.setBoolean(3, a.correta());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }

                cn.commit();
                return id;

            } catch (Exception e) {
                cn.rollback();

                if (e instanceof SQLException se) {
                    throw se;
                }
                if (e instanceof RuntimeException re) {
                    throw re;
                }
                throw new SQLException(e);
            }
        }
    }

    public List<Questao> listar() throws SQLException {
        List<Questao> lista = new ArrayList<>();

        String sql = """
            SELECT id_questao, enunciado, id_tipo
            FROM questao
            ORDER BY id_questao
            """;

        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_questao");

                List<Alternativa> al = new ArrayList<>();

                String sqlAlt = """
                    SELECT id_alternativa, texto, correta
                    FROM alternativa
                    WHERE id_questao = ?
                    ORDER BY id_alternativa
                    """;

                try (PreparedStatement pa = cn.prepareStatement(sqlAlt)) {
                    pa.setInt(1, id);

                    try (ResultSet ra = pa.executeQuery()) {
                        while (ra.next()) {
                            al.add(new Alternativa(
                                ra.getInt("id_alternativa"),
                                ra.getString("texto"),
                                ra.getBoolean("correta")
                            ));
                        }
                    }
                }

                lista.add(new Questao(
                    id,
                    rs.getString("enunciado"),
                    rs.getInt("id_tipo"),
                    al
                ));
            }
        }

        return lista;
    }
}
