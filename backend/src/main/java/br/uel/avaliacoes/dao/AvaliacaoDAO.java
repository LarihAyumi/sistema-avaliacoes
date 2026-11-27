package br.uel.avaliacoes.dao;

import br.uel.avaliacoes.model.Avaliacao;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AvaliacaoDAO {

    private final DataSource ds;

    public AvaliacaoDAO(DataSource ds) {
        this.ds = ds;
    }

    public int cadastrar(Avaliacao a) throws SQLException {
        if (a.titulo() == null || a.titulo().isBlank()) {
            throw new IllegalArgumentException("Titulo obrigatorio");
        }

        if (a.descricao() == null) {
            throw new IllegalArgumentException("Descricao obrigatoria");
        }

        if (a.inicio() == null || a.fim() == null ||
            !a.inicio().isBefore(a.fim())) {
            throw new IllegalArgumentException("Periodo invalido");
        }

        if (a.id_criador() == null) {
            throw new IllegalArgumentException("Professor obrigatorio");
        }

        String sql = """
            INSERT INTO avaliacao
            (titulo, descricao, inicio, fim, id_criador)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id_avaliacao
            """;

        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, a.titulo());
            ps.setString(2, a.descricao());
            ps.setTimestamp(3, Timestamp.valueOf(a.inicio()));
            ps.setTimestamp(4, Timestamp.valueOf(a.fim()));
            ps.setInt(5, a.id_criador());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("id_avaliacao");
            }
        }
    }

    public List<Avaliacao> listar() throws SQLException {
        List<Avaliacao> lista = new ArrayList<>();

        String sql = """
            SELECT id_avaliacao, titulo, descricao,
                   inicio, fim, id_criador
            FROM avaliacao
            ORDER BY id_avaliacao
            """;

        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Avaliacao(
                    rs.getInt("id_avaliacao"),
                    rs.getString("titulo"),
                    rs.getString("descricao"),
                    rs.getTimestamp("inicio").toLocalDateTime(),
                    rs.getTimestamp("fim").toLocalDateTime(),
                    rs.getInt("id_criador")
                ));
            }
        }

        return lista;
    }
}
