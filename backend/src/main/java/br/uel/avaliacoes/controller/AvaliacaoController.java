package br.uel.avaliacoes.controller;

import br.uel.avaliacoes.dao.AvaliacaoDAO;
import br.uel.avaliacoes.model.Avaliacao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoDAO dao;

    public AvaliacaoController(AvaliacaoDAO dao) {
        this.dao = dao;
    }

    @GetMapping
    public List<Avaliacao> listar() throws SQLException {
        return dao.listar();
    }

    @PostMapping
    public ResponseEntity<Map<String, Integer>> cadastrar(
        @RequestBody Avaliacao a
    ) throws SQLException {
        try {
            int id = dao.cadastrar(a);

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("id_avaliacao", id));

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, e.getMessage()
            );
        }
    }
}
