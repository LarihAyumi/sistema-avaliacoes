async function enviar(url, dados) {
    const res = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(dados)
    });

    if (!res.ok) {
        let detalhe = "";

        try {
            const erro = await res.json();
            detalhe = erro.message || erro.detail || "";
        } catch (_) {}

        throw new Error(
            "Erro HTTP " + res.status +
            (detalhe ? ": " + detalhe : "")
        );
    }

    return await res.json();
}

function mensagem(id, texto, erro = false) {
    const el = document.getElementById(id);
    el.textContent = texto;
    el.style.color = erro ? "#a23e53" : "#5c433d";
}

const tipo = document.getElementById("tipo");
const alternativas = document.getElementById("alternativas");

tipo.addEventListener("change", () => {
    alternativas.hidden = tipo.value !== "1";
});

document.getElementById("formQuestao")
    .addEventListener("submit", async (e) => {
        e.preventDefault();

        const idTipo = Number(tipo.value);

        const dados = {
            enunciado: document.getElementById("enunciado")
                .value.trim(),
            id_tipo: idTipo,
            alternativas: []
        };

        if (idTipo === 1) {
            const linhas = document.querySelectorAll(
                "#listaAlternativas .alternativa"
            );

            dados.alternativas = Array.from(linhas)
                .map(linha => ({
                    texto: linha.querySelector(
                        'input[type="text"]'
                    ).value.trim(),
                    correta: linha.querySelector(
                        'input[type="radio"]'
                    ).checked
                }));

            if (dados.alternativas.some(a => !a.texto)) {
                mensagem(
                    "msgQuestao",
                    "Preencha todas as alternativas.",
                    true
                );
                return;
            }

            const textos = dados.alternativas.map(
                a => a.texto.toLowerCase()
            );

            if (new Set(textos).size !== textos.length) {
                mensagem(
                    "msgQuestao",
                    "As alternativas devem ser diferentes.",
                    true
                );
                return;
            }
        }

        try {
            const r = await enviar("/questoes", dados);

            mensagem(
                "msgQuestao",
                "Questão cadastrada! ID: " + r.id_questao
            );

            e.target.reset();
            alternativas.hidden = false;

        } catch (erro) {
            mensagem("msgQuestao", erro.message, true);
        }
    });

document.getElementById("formAvaliacao")
    .addEventListener("submit", async (e) => {
        e.preventDefault();

        const dados = {
            titulo: document.getElementById("titulo")
                .value.trim(),
            descricao: document.getElementById("descricao")
                .value.trim(),
            inicio: document.getElementById("inicio").value,
            fim: document.getElementById("fim").value,
            id_criador: Number(
                document.getElementById("criador").value
            )
        };

        if (dados.inicio >= dados.fim) {
            mensagem(
                "msgAvaliacao",
                "A data final deve ser posterior à inicial.",
                true
            );
            return;
        }

        try {
            const r = await enviar("/avaliacoes", dados);

            mensagem(
                "msgAvaliacao",
                "Avaliação cadastrada! ID: " + r.id_avaliacao
            );

            document.getElementById("avItem").value =
                r.id_avaliacao;

            document.getElementById("avAluno").value =
                r.id_avaliacao;

            e.target.reset();

        } catch (erro) {
            mensagem("msgAvaliacao", erro.message, true);
        }
    });

document.getElementById("formItem")
    .addEventListener("submit", async (e) => {
        e.preventDefault();

        const id = Number(
            document.getElementById("avItem").value
        );

        const dados = {
            id_questao: Number(
                document.getElementById("questaoItem").value
            ),
            valor: Number(
                document.getElementById("valorItem").value
            )
        };

        try {
            const r = await enviar(
                "/avaliacoes/" + id + "/questoes",
                dados
            );

            mensagem(
                "msgItem",
                "Questão adicionada! Item ID: " + r.id_item
            );

        } catch (erro) {
            mensagem("msgItem", erro.message, true);
        }
    });

document.getElementById("formAluno")
    .addEventListener("submit", async (e) => {
        e.preventDefault();

        const id = Number(
            document.getElementById("avAluno").value
        );

        const dados = {
            id_usuario: Number(
                document.getElementById("usuarioAluno").value
            )
        };

        try {
            const r = await enviar(
                "/avaliacoes/" + id + "/alunos",
                dados
            );

            mensagem(
                "msgAluno",
                "Aluno adicionado! Participação ID: " +
                r.id_participacao
            );

        } catch (erro) {
            mensagem("msgAluno", erro.message, true);
        }
    });
