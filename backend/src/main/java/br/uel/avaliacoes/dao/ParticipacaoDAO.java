package br.uel.avaliacoes.dao;

import br.uel.avaliacoes.model.Participacao;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ParticipacaoDAO {

    private final DataSource ds;

    public ParticipacaoDAO(DataSource ds) {
        this.ds = ds;
    }

    public int cadastrar(Participacao p) throws SQLException {
        if (p.id_usuario() == null || p.id_avaliacao() == null) {
            throw new IllegalArgumentException(
                "Aluno e avaliacao obrigatorios"
            );
        }

        String sql = """
            INSERT INTO participacao
                (id_usuario, id_avaliacao)
            VALUES (?, ?)
            RETURNING id_participacao
            """;

        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, p.id_usuario());
            ps.setInt(2, p.id_avaliacao());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("id_participacao");
            }
        }
    }

    public List<Participacao> listar(int id) throws SQLException {
        List<Participacao> lista = new ArrayList<>();

        String sql = """
            SELECT id_participacao, id_usuario,
                   id_avaliacao, status
            FROM participacao
            WHERE id_avaliacao = ?
            ORDER BY id_usuario
            """;

        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Participacao(
                        rs.getInt("id_participacao"),
                        rs.getInt("id_usuario"),
                        rs.getInt("id_avaliacao"),
                        rs.getString("status")
                    ));
                }
            }
        }

        return lista;
    }
}
