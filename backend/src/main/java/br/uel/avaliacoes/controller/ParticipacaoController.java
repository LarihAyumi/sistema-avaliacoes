package br.uel.avaliacoes.controller;

import br.uel.avaliacoes.dao.ParticipacaoDAO;
import br.uel.avaliacoes.model.Participacao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/avaliacoes/{id}/alunos")
public class ParticipacaoController {

    private final ParticipacaoDAO dao;

    public ParticipacaoController(ParticipacaoDAO dao) {
        this.dao = dao;
    }

    @GetMapping
    public List<Participacao> listar(
        @PathVariable int id
    ) throws SQLException {
        return dao.listar(id);
    }

    @PostMapping
    public ResponseEntity<Map<String, Integer>> cadastrar(
        @PathVariable int id,
        @RequestBody Participacao p
    ) throws SQLException {

        try {
            Participacao novo = new Participacao(
                null,
                p.id_usuario(),
                id,
                "pendente"
            );

            int idParticipacao = dao.cadastrar(novo);

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("id_participacao", idParticipacao));

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, e.getMessage()
            );

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Aluno ja vinculado a esta avaliacao"
                );
            }

            if ("23503".equals(e.getSQLState())) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Aluno ou avaliacao inexistente"
                );
            }

            if ("P0001".equals(e.getSQLState())) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Somente alunos podem participar"
                );
            }

            throw e;
        }
    }
}
