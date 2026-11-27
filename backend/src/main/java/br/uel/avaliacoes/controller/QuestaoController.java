package br.uel.avaliacoes.controller;

import br.uel.avaliacoes.dao.QuestaoDAO;
import br.uel.avaliacoes.model.Questao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/questoes")
public class QuestaoController {

    private final QuestaoDAO dao;

    public QuestaoController(QuestaoDAO dao) {
        this.dao = dao;
    }

    @GetMapping
    public List<Questao> listar() throws SQLException {
        return dao.listar();
    }

    @PostMapping
    public ResponseEntity<Map<String, Integer>> cadastrar(
        @RequestBody Questao q
    ) throws SQLException {
        try {
            int id = dao.cadastrar(q);

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("id_questao", id));

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, e.getMessage()
            );
        }
    }
}
