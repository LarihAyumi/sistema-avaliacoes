package br.uel.avaliacoes.controller;

import br.uel.avaliacoes.dao.RespostaDAO;
import br.uel.avaliacoes.model.Resposta;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/participacoes/{id}/respostas")
public class RespostaController {

    private final RespostaDAO dao;

    public RespostaController(RespostaDAO dao) {
        this.dao = dao;
    }

    @GetMapping
    public List<Resposta> listar(
        @PathVariable int id
    ) throws SQLException {
        return dao.listar(id);
    }

    @PostMapping
    public ResponseEntity<Map<String, Integer>> cadastrar(
        @PathVariable int id,
        @RequestBody Resposta r
    ) throws SQLException {

        try {
            int idResposta = dao.cadastrar(id, r);

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("id_resposta", idResposta));

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta questao ja foi respondida"
                );
            }

            if ("23503".equals(e.getSQLState())) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Participacao, item ou alternativa inexistente"
                );
            }

            if ("P0001".equals(e.getSQLState())) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
                );
            }

            throw e;
        }
    }
}
