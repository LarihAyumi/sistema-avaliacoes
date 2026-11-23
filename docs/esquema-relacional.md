Esquema relacional

  -- CONVENÇÕES --
- **PK**: chave primaria
- **FK**: chave estrangeira
- **UQ**: restricao de unicidade
- **NULL**: atributo opcional

 -- RELAÇÕES --
**USUARIO**(
  **id_usuario** PK, nome, email UQ, senha, papel
)

**TIPO_QUESTAO**(
  **id_tipo** PK, descricao UQ
)

**QUESTAO**(
  **id_questao** PK, enunciado, id_tipo FK -> TIPO_QUESTAO(id_tipo)
)

**ALTERNATIVA**(
  **id_alternativa** PK, id_questao FK -> QUESTAO(id_questao), texto, correta
)

**AVALIACAO**(
  **id_avaliacao** PK, titulo, descricao, inicio, fim, id_criador FK -> USUARIO(id_usuario)
)

**ITEM_AVALIACAO**(
  **id_item** PK, id_avaliacao FK -> AVALIACAO(id_avaliacao), id_questao FK -> QUESTAO(id_questao), valor, UQ(id_avaliacao, id_questao)
)

**PARTICIPACAO**(
  **id_participacao** PK, id_usuario FK -> USUARIO(id_usuario), id_avaliacao FK -> AVALIACAO(id_avaliacao), status, UQ(id_usuario, id_avaliacao)
)

**RESPOSTA**(
  **id_resposta** PK, id_participacao FK -> PARTICIPACAO(id_participacao), id_item FK -> ITEM_AVALIACAO(id_item), id_alternativa FK -> ALTERNATIVA(id_alternativa) NULL, texto_livre NULL, nota_obtida NULL, UQ(id_participacao, id_item)
)



 -- TRADUÇÕES DOS RELACIONAMENTOS DO DER --

| Relacionamento | Cardinalidade | Mapeamento |
|---|---|---|
| USUARIO cria AVALIACAO | 1:N | `AVALIACAO.id_criador` |
| TIPO_QUESTAO classifica QUESTAO | 1:N | `QUESTAO.id_tipo` |
| QUESTAO possui ALTERNATIVA | 1:N | `ALTERNATIVA.id_questao` |
| AVALIACAO inclui ITEM_AVALIACAO | 1:N | `ITEM_AVALIACAO.id_avaliacao` |
| QUESTAO e utilizada em ITEM_AVALIACAO | 1:N | `ITEM_AVALIACAO.id_questao` |
| USUARIO faz PARTICIPACAO | 1:N | `PARTICIPACAO.id_usuario` |
| AVALIACAO recebe PARTICIPACAO | 1:N | `PARTICIPACAO.id_avaliacao` |
| PARTICIPACAO registra RESPOSTA | 1:N | `RESPOSTA.id_participacao` |
| ITEM_AVALIACAO recebe RESPOSTA | 1:N | `RESPOSTA.id_item` |
| RESPOSTA seleciona ALTERNATIVA | N:1, opcional | `RESPOSTA.id_alternativa` |




 -- DEPENDÊNCIAS FUNCIONAIS PRINCIPAIS -- 

- `id_usuario -> nome, email, senha, papel`
- `id_tipo -> descricao`
- `id_questao -> enunciado, id_tipo`
- `id_alternativa -> id_questao, texto, correta`
- `id_avaliacao -> titulo, descricao, inicio, fim, id_criador`
- `id_item -> id_avaliacao, id_questao, valor`
- `(id_avaliacao, id_questao) -> id_item, valor`
- `id_participacao -> id_usuario, id_avaliacao, status`
- `(id_usuario, id_avaliacao) -> id_participacao, status`
- `id_resposta -> id_participacao, id_item, id_alternativa, texto_livre, nota_obtida`
- `(id_participacao, id_item) -> id_resposta, id_alternativa, texto_livre, nota_obtida`




 -- JUSTIFICATIVA DA 3FN --

**1FN:** atributos atomicos, sem listas de alternativas ou respostas na mesma celula. Cada alternativa e resposta ocupa sua propria tupla.

**2FN:** nao ha atributo nao-chave dependente de apenas parte de chave candidata composta. Por exemplo, `valor` depende do par `(id_avaliacao, id_questao)` em ITEM_AVALIACAO, enquanto a resposta de um aluno a um item depende do par `(id_participacao, id_item)`.

**3FN:** assumindo as dependencias funcionais listadas e as regras do dominio, cada dependencia funcional nao trivial possui superchave como determinante. Dados de usuario, questao e avaliacao ficam em relacoes diferentes, evitando dependencias transitivas. A nota total nao e armazenada em PARTICIPACAO: sera calculada pela soma de RESPOSTA.nota_obtida para a participacao.