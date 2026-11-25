BEGIN;

CREATE TABLE usuario (
    id_usuario INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    papel VARCHAR(20) NOT NULL CHECK (papel IN ('aluno', 'professor'))
);

CREATE TABLE tipo_questao (
    id_tipo INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    descricao VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE questao (
    id_questao INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    enunciado TEXT NOT NULL CHECK (length(btrim(enunciado)) > 0),
    id_tipo INTEGER NOT NULL REFERENCES tipo_questao(id_tipo)
);

CREATE TABLE alternativa (
    id_alternativa INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_questao INTEGER NOT NULL REFERENCES questao(id_questao),
    texto TEXT NOT NULL CHECK (length(btrim(texto)) > 0),
    correta BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (id_questao, texto)
);

-- No máx uma alternativa correta por questão.
CREATE UNIQUE INDEX uq_alternativa_correta
    ON alternativa (id_questao) WHERE correta = TRUE;

CREATE TABLE avaliacao (
    id_avaliacao INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL CHECK (length(btrim(titulo)) > 0),
    descricao TEXT NOT NULL,
    inicio TIMESTAMP NOT NULL,
    fim TIMESTAMP NOT NULL,
    id_criador INTEGER NOT NULL REFERENCES usuario(id_usuario),
    CHECK (inicio < fim)
);

CREATE TABLE item_avaliacao (
    id_item INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_avaliacao INTEGER NOT NULL REFERENCES avaliacao(id_avaliacao),
    id_questao INTEGER NOT NULL REFERENCES questao(id_questao),
    valor NUMERIC(7,2) NOT NULL CHECK (valor > 0),
    UNIQUE (id_avaliacao, id_questao)
);

CREATE TABLE participacao (
    id_participacao INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario INTEGER NOT NULL REFERENCES usuario(id_usuario),
    id_avaliacao INTEGER NOT NULL REFERENCES avaliacao(id_avaliacao),
    status VARCHAR(20) NOT NULL DEFAULT 'pendente'
        CHECK (status IN ('pendente', 'em_andamento', 'concluida')),
    UNIQUE (id_usuario, id_avaliacao)
);

CREATE TABLE resposta (
    id_resposta INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_participacao INTEGER NOT NULL REFERENCES participacao(id_participacao),
    id_item INTEGER NOT NULL REFERENCES item_avaliacao(id_item),
    id_alternativa INTEGER REFERENCES alternativa(id_alternativa),
    texto_livre TEXT,
    nota_obtida NUMERIC(7,2) CHECK (nota_obtida >= 0),
    UNIQUE (id_participacao, id_item),
    CHECK (
        (id_alternativa IS NOT NULL AND texto_livre IS NULL)
        OR (id_alternativa IS NULL AND texto_livre IS NOT NULL)
    )
);

-- Validações que exigem consultar outras relações.
CREATE OR REPLACE FUNCTION validar_resposta()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    v_avaliacao_p INTEGER;
    v_avaliacao_i INTEGER;
    v_questao INTEGER;
    v_tipo VARCHAR(50);
    v_valor NUMERIC(7,2);
    v_alternativa_questao INTEGER;
    v_correta BOOLEAN;
BEGIN
    SELECT id_avaliacao INTO v_avaliacao_p
    FROM participacao WHERE id_participacao = NEW.id_participacao;

    SELECT i.id_avaliacao, i.id_questao, i.valor, t.descricao
    INTO v_avaliacao_i, v_questao, v_valor, v_tipo
    FROM item_avaliacao i
    JOIN questao q ON q.id_questao = i.id_questao
    JOIN tipo_questao t ON t.id_tipo = q.id_tipo
    WHERE i.id_item = NEW.id_item;

    IF v_avaliacao_p IS DISTINCT FROM v_avaliacao_i THEN
        RAISE EXCEPTION 'O item nao pertence a avaliacao da participacao';
    END IF;

    IF v_tipo = 'objetiva' THEN
        IF NEW.id_alternativa IS NULL OR NEW.texto_livre IS NOT NULL THEN
            RAISE EXCEPTION 'Questao objetiva exige alternativa, sem texto livre';
        END IF;
        SELECT id_questao, correta
        INTO v_alternativa_questao, v_correta
        FROM alternativa WHERE id_alternativa = NEW.id_alternativa;
        IF v_alternativa_questao IS DISTINCT FROM v_questao THEN
            RAISE EXCEPTION 'Alternativa nao pertence a questao deste item';
        END IF;
        -- A nota objetiva e calculada automaticamente.
        NEW.nota_obtida := CASE WHEN v_correta THEN v_valor ELSE 0 END;
    ELSIF v_tipo = 'discursiva' THEN
        IF NEW.id_alternativa IS NOT NULL OR NEW.texto_livre IS NULL
           OR length(btrim(NEW.texto_livre)) = 0 THEN
            RAISE EXCEPTION 'Questao discursiva exige texto e nao aceita alternativa';
        END IF;
        -- NULL = ainda n corrigida pelo professor.
    ELSE
        RAISE EXCEPTION 'Tipo de questao nao suportado: %', v_tipo;
    END IF;

    IF NEW.nota_obtida > v_valor THEN
        RAISE EXCEPTION 'Nota maior que o valor do item';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_validar_resposta
BEFORE INSERT OR UPDATE ON resposta
FOR EACH ROW EXECUTE FUNCTION validar_resposta();

-- Atribuir uma avaliação apenas a alunos e permitir criacao só por professores.
CREATE OR REPLACE FUNCTION validar_papel_usuario()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    v_papel VARCHAR(20);
BEGIN
    IF TG_TABLE_NAME = 'avaliacao' THEN
        SELECT papel INTO v_papel
        FROM usuario
        WHERE id_usuario = NEW.id_criador;

        IF v_papel IS DISTINCT FROM 'professor' THEN
            RAISE EXCEPTION 'Apenas professores podem criar avaliacoes';
        END IF;

    ELSIF TG_TABLE_NAME = 'participacao' THEN
        SELECT papel INTO v_papel
        FROM usuario
        WHERE id_usuario = NEW.id_usuario;

        IF v_papel IS DISTINCT FROM 'aluno' THEN
            RAISE EXCEPTION 'Apenas alunos podem participar de avaliacoes';
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_criador_professor
BEFORE INSERT OR UPDATE OF id_criador ON avaliacao
FOR EACH ROW EXECUTE FUNCTION validar_papel_usuario();

CREATE TRIGGER trg_participante_aluno
BEFORE INSERT OR UPDATE OF id_usuario ON participacao
FOR EACH ROW EXECUTE FUNCTION validar_papel_usuario();
COMMIT;