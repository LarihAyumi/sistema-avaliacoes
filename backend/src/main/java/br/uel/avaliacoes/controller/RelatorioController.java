package br.uel.avaliacoes.controller;

import br.uel.avaliacoes.dao.RelatorioDAO;

import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    private final RelatorioDAO dao;

    public RelatorioController(RelatorioDAO dao) {
        this.dao = dao;
    }

    @GetMapping("/desempenho")
    public List<Map<String, Object>> desempenho()
            throws SQLException {
        return dao.desempenho();
    }

    @GetMapping("/evolucao/{id}")
    public List<Map<String, Object>> evolucao(
            @PathVariable int id) throws SQLException {
        return dao.evolucao(id);
    }

    @GetMapping("/questoes")
    public List<Map<String, Object>> questoes()
            throws SQLException {
        return dao.questoes();
    }
}
