package br.uel.avaliacoes.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

@Repository
public class UsuarioDAO {

    private final DataSource ds;

    public UsuarioDAO(DataSource ds) {
        this.ds = ds;
    }

    public int contarUsuarios() throws Exception {
        String sql = "SELECT COUNT(*) FROM usuario";

        try (Connection cn = ds.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            return rs.getInt(1);
        }
    }
}
