-- RELATORIO 1: Desempenho por avaliacao
-- JOIN, LEFT JOIN, SUM, AVG, COUNT, GROUP BY, WHERE e ORDER BY
WITH notas AS (
    SELECT
        p.id_participacao,
        p.id_avaliacao,
        COALESCE(SUM(r.nota_obtida), 0) AS nota,
        COUNT(r.id_resposta) AS respostas
    FROM participacao p
    LEFT JOIN resposta r
        ON r.id_participacao = p.id_participacao
    GROUP BY p.id_participacao, p.id_avaliacao
)
SELECT
    a.id_avaliacao,
    a.titulo,
    COUNT(n.id_participacao) AS alunos,
    COUNT(n.id_participacao) FILTER (
        WHERE n.respostas > 0
    ) AS responderam,
    ROUND(AVG(n.nota), 2) AS media,
    MAX(n.nota) AS maior_nota,
    MIN(n.nota) AS menor_nota
FROM avaliacao a
LEFT JOIN notas n
    ON n.id_avaliacao = a.id_avaliacao
WHERE a.inicio >= '2026-01-01'
GROUP BY a.id_avaliacao, a.titulo
ORDER BY a.id_avaliacao;



-- RELATORIO 2: Evolucao do aluno
-- JOIN, LEFT JOIN, SUM, subconsulta, WHERE e ORDER BY
WITH notas AS (
    SELECT
        p.id_usuario,
        p.id_avaliacao,
        COALESCE(SUM(r.nota_obtida), 0) AS nota
    FROM participacao p
    LEFT JOIN resposta r
        ON r.id_participacao = p.id_participacao
    GROUP BY p.id_usuario, p.id_avaliacao
)
SELECT
    u.id_usuario,
    u.nome,
    a.titulo,
    a.inicio,
    n.nota AS nota_aluno,
    (
        SELECT ROUND(AVG(n2.nota), 2)
        FROM notas n2
        WHERE n2.id_avaliacao = n.id_avaliacao
    ) AS media_avaliacao
FROM notas n
JOIN usuario u ON u.id_usuario = n.id_usuario
JOIN avaliacao a ON a.id_avaliacao = n.id_avaliacao
WHERE u.id_usuario = 3
  AND a.inicio >= '2026-01-01'
ORDER BY a.inicio, a.id_avaliacao;



-- RELATORIO 3: Questoes mais erradas
-- JOIN, COUNT, GROUP BY, HAVING, RANK, IN, LIKE e ORDER BY
WITH erros AS (
    SELECT
        q.id_questao,
        q.enunciado,
        COUNT(*) AS total_respostas,
        COUNT(*) FILTER (
            WHERE alt.correta = FALSE
        ) AS total_erros
    FROM resposta r
    JOIN item_avaliacao i
        ON i.id_item = r.id_item
    JOIN questao q
        ON q.id_questao = i.id_questao
    JOIN tipo_questao t
        ON t.id_tipo = q.id_tipo
    JOIN alternativa alt
        ON alt.id_alternativa = r.id_alternativa
    WHERE t.descricao IN ('objetiva')
      AND q.enunciado LIKE '%'
    GROUP BY q.id_questao, q.enunciado
    HAVING COUNT(*) >= 1
),
ranking AS (
    SELECT
        *,
        ROUND(
            100.0 * total_erros / total_respostas, 2
        ) AS percentual_erros
    FROM erros
)
SELECT
    RANK() OVER (
        ORDER BY percentual_erros DESC
    ) AS posicao,
    id_questao,
    enunciado,
    total_respostas,
    total_erros,
    percentual_erros
FROM ranking
ORDER BY posicao, total_respostas DESC, id_questao
FETCH FIRST 10 ROWS ONLY;
