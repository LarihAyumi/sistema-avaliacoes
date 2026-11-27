package br.uel.avaliacoes.controller;

import br.uel.avaliacoes.dao.QuestaoDAO;
import br.uel.avaliacoes.model.Questao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
public List<Map<String, Object>> listar()
        throws SQLException {

    List<Map<String, Object>> lista = new ArrayList<>();

    for (Questao q : dao.listar()) {
        Map<String, Object> dados = new LinkedHashMap<>();

        dados.put("id_questao", q.id_questao());
        dados.put("enunciado", q.enunciado());
        dados.put("id_tipo", q.id_tipo());

        List<Map<String, Object>> alternativas =
            new ArrayList<>();

        if (q.alternativas() != null) {
            for (Questao.Alternativa a : q.alternativas()) {
                Map<String, Object> alt =
                    new LinkedHashMap<>();

                alt.put("id_alternativa", a.id_alternativa());
                alt.put("texto", a.texto());

                alternativas.add(alt);
            }
        }

        dados.put("alternativas", alternativas);
        lista.add(dados);
        }

        return lista;
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
