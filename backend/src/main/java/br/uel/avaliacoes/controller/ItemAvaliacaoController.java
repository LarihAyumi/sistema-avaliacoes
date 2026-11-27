package br.uel.avaliacoes.controller;

import br.uel.avaliacoes.dao.ItemAvaliacaoDAO;
import br.uel.avaliacoes.model.ItemAvaliacao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/avaliacoes/{id}/questoes")
public class ItemAvaliacaoController {

    private final ItemAvaliacaoDAO dao;

    public ItemAvaliacaoController(ItemAvaliacaoDAO dao) {
        this.dao = dao;
    }

    @GetMapping
    public List<ItemAvaliacao> listar(
        @PathVariable int id
    ) throws SQLException {
        return dao.listar(id);
    }

    @PostMapping
    public ResponseEntity<Map<String, Integer>> cadastrar(
        @PathVariable int id,
        @RequestBody ItemAvaliacao item
    ) throws SQLException {

        try {
            ItemAvaliacao novo = new ItemAvaliacao(
                null,
                id,
                item.id_questao(),
                item.valor()
            );

            int idItem = dao.cadastrar(novo);

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("id_item", idItem));

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, e.getMessage()
            );
        }
    }
}
