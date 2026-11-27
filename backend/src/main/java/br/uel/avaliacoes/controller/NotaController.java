package br.uel.avaliacoes.controller;

import br.uel.avaliacoes.dao.NotaDAO;
import br.uel.avaliacoes.model.Nota;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

@RestController
public class NotaController {

    private final NotaDAO dao;

    public NotaController(NotaDAO dao) {
        this.dao = dao;
    }

    @PatchMapping("/respostas/{id}/nota")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void corrigir(
        @PathVariable int id,
        @RequestBody Map<String, BigDecimal> body
    ) throws SQLException {

        try {
            dao.corrigir(id, body.get("nota"));

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        }
    }

    @GetMapping("/participacoes/{id}/nota")
    public Nota calcular(
        @PathVariable int id
    ) throws SQLException {

        try {
            return dao.calcular(id);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                e.getMessage()
            );
        }
    }
}
