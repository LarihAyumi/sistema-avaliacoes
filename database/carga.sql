BEGIN;

INSERT INTO tipo_questao (descricao)
VALUES ('objetiva'), ('discursiva');

INSERT INTO usuario (nome, email, senha, papel)
VALUES ('Professora Ana', 'ana.prof@exemplo.com', 'HASH_DE_TESTE', 'professor'),
       ('Professor Bruno', 'bruno.prof@exemplo.com', 'HASH_DE_TESTE', 'professor');

INSERT INTO usuario (nome, email, senha, papel)
SELECT 'Aluno ' || lpad(n::text, 2, '0'),
       'aluno' || lpad(n::text, 2, '0') || '@exemplo.com',
       'HASH_DE_TESTE', 'aluno'
FROM generate_series(1, 18) AS n;

-- Cada bloco de seis questões contém quatro objetivas e duas discursivas.
INSERT INTO questao (enunciado, id_tipo)
SELECT 'Questao de teste ' || n || ': conteudo de Banco de Dados I',
       CASE WHEN n % 3 = 0 THEN 2 ELSE 1 END
FROM generate_series(1, 30) AS n;

-- Para cada questão objetiva, quatro alternativas, sendo a primeira correta.
INSERT INTO alternativa (id_questao, texto, correta)
SELECT q.id_questao, 'Opcao ' || x.n || ' da questao ' || q.id_questao,
       x.n = 1
FROM questao q
CROSS JOIN generate_series(1, 4) AS x(n)
WHERE q.id_tipo = 1;

INSERT INTO avaliacao (titulo, descricao, inicio, fim, id_criador)
SELECT 'Avaliacao ' || n,
       'Avaliacao de exemplo com questoes objetivas e discursivas',
       TIMESTAMP '2026-11-01 14:00' + (n - 1) * INTERVAL '1 day',
       TIMESTAMP '2026-11-01 16:00' + (n - 1) * INTERVAL '1 day',
       CASE WHEN n % 2 = 0 THEN 2 ELSE 1 END
FROM generate_series(1, 5) AS n;

-- Seis questões por avaliação, valor de 2 pontos por questão.
INSERT INTO item_avaliacao (id_avaliacao, id_questao, valor)
SELECT ((id_questao - 1) / 6) + 1, id_questao, 2.00
FROM questao;

-- Todos os alunos são designados; 3 por aval permanecem pendentes.
INSERT INTO participacao (id_usuario, id_avaliacao, status)
SELECT u.id_usuario, a.id_avaliacao,
       CASE WHEN u.id_usuario <= 16 THEN 'concluida' ELSE 'pendente' END
FROM usuario u CROSS JOIN avaliacao a
WHERE u.papel = 'aluno';

-- Respostas das 15 primeiras pessoas da turma: 75 participações, 450 respostas.
-- Quest objetivas: desempenho variado; discursivas: correção ilustrativa.
INSERT INTO resposta (id_participacao, id_item, id_alternativa, texto_livre, nota_obtida)
SELECT p.id_participacao, i.id_item,
       CASE WHEN q.id_tipo = 1 THEN (
           SELECT al.id_alternativa FROM alternativa al
           WHERE al.id_questao = q.id_questao
             AND al.texto = 'Opcao ' ||
                 CASE WHEN (p.id_usuario + i.id_item) % 4 = 0 THEN 2 ELSE 1 END
                 || ' da questao ' || q.id_questao
       ) ELSE NULL END,
       CASE WHEN q.id_tipo = 2
            THEN 'Resposta discursiva ilustrativa do aluno ' || p.id_usuario
            ELSE NULL END,
       CASE WHEN q.id_tipo = 2
            THEN CASE WHEN (p.id_usuario + i.id_item) % 5 = 0 THEN 1.00 ELSE 2.00 END
            ELSE NULL END
FROM participacao p
JOIN item_avaliacao i ON i.id_avaliacao = p.id_avaliacao
JOIN questao q ON q.id_questao = i.id_questao
WHERE p.status = 'concluida';

COMMIT;

-- Conferência rapida: 20 usuarios, 5 avaliacoes, 30 questoes, 90 participacoes, 450 respostas.
SELECT 'usuario' AS tabela, count(*) AS quantidade FROM usuario
UNION ALL SELECT 'avaliacao', count(*) FROM avaliacao
UNION ALL SELECT 'questao', count(*) FROM questao
UNION ALL SELECT 'participacao', count(*) FROM participacao
UNION ALL SELECT 'resposta', count(*) FROM resposta;
