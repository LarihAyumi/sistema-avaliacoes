package br.uel.avaliacoes.controller;

import br.uel.avaliacoes.dao.UsuarioDAO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TesteController {

    private final UsuarioDAO dao;

    public TesteController(UsuarioDAO dao) {
        this.dao = dao;
    }

    @GetMapping("/teste")
    public int testar() throws Exception {
        return dao.contarUsuarios();
    }
}
