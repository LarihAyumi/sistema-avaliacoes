package br.uel.avaliacoes.dao;

import br.uel.avaliacoes.model.Resposta;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RespostaDAO {

    private final DataSource ds;

    public RespostaDAO(DataSource ds) {
        this.ds = ds;
    }

    public int cadastrar(int id, Resposta r) throws SQLException {
        if (r.id_item() == null) {
            throw new IllegalArgumentException("Item obrigatorio");
        }

        boolean objetiva = r.id_alternativa() != null;
        boolean discursiva = r.texto_livre() != null
            && !r.texto_livre().isBlank();

        if (objetiva == discursiva) {
            throw new IllegalArgumentException(
                "Informe alternativa ou texto livre"
            );
        }

        String sql = """
            INSERT INTO resposta
                (id_participacao, id_item,
                 id_alternativa, texto_livre)
            VALUES (?, ?, ?, ?)
            RETURNING id_resposta
            """;

        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, r.id_item());

            if (objetiva) {
                ps.setInt(3, r.id_alternativa());
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setNull(3, Types.INTEGER);
                ps.setString(4, r.texto_livre());
            }

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("id_resposta");
            }
        }
    }

    public List<Resposta> listar(int id) throws SQLException {
        List<Resposta> lista = new ArrayList<>();

        String sql = """
            SELECT id_resposta, id_participacao, id_item,
                   id_alternativa, texto_livre, nota_obtida
            FROM resposta
            WHERE id_participacao = ?
            ORDER BY id_item
            """;

        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer alt = (Integer) rs.getObject("id_alternativa");

                    lista.add(new Resposta(
                        rs.getInt("id_resposta"),
                        rs.getInt("id_participacao"),
                        rs.getInt("id_item"),
                        alt,
                        rs.getString("texto_livre"),
                        rs.getBigDecimal("nota_obtida")
                    ));
                }
            }
        }

        return lista;
    }
}
