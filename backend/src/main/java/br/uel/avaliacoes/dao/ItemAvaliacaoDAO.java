package br.uel.avaliacoes.dao;

import br.uel.avaliacoes.model.ItemAvaliacao;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ItemAvaliacaoDAO {

    private final DataSource ds;

    public ItemAvaliacaoDAO(DataSource ds) {
        this.ds = ds;
    }

    public int cadastrar(ItemAvaliacao i) throws SQLException {
        if (i.id_avaliacao() == null || i.id_questao() == null) {
            throw new IllegalArgumentException(
                "Avaliacao e questao obrigatorias"
            );
        }

        if (i.valor() == null ||
            i.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "O valor deve ser maior que zero"
            );
        }

        String sql = """
            INSERT INTO item_avaliacao
                (id_avaliacao, id_questao, valor)
            VALUES (?, ?, ?)
            RETURNING id_item
            """;

        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, i.id_avaliacao());
            ps.setInt(2, i.id_questao());
            ps.setBigDecimal(3, i.valor());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("id_item");
            }
        }
    }

    public List<ItemAvaliacao> listar(int id) throws SQLException {
        List<ItemAvaliacao> lista = new ArrayList<>();

        String sql = """
            SELECT id_item, id_avaliacao, id_questao, valor
            FROM item_avaliacao
            WHERE id_avaliacao = ?
            ORDER BY id_item
            """;

        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ItemAvaliacao(
                        rs.getInt("id_item"),
                        rs.getInt("id_avaliacao"),
                        rs.getInt("id_questao"),
                        rs.getBigDecimal("valor")
                    ));
                }
            }
        }

        return lista;
    }
}
