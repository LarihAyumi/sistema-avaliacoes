let idParticipacao = null;
let idAvaliacao = null;

async function consultar(url) {
    const res = await fetch(url);

    if (!res.ok) {
        throw new Error("Erro HTTP " + res.status);
    }

    return res.json();
}

async function enviar(url, dados) {
    const res = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(dados)
    });

    if (!res.ok) {
        let msg = "Erro HTTP " + res.status;

        if (res.status === 409) {
            msg = "Esta questão já foi respondida.";
        } else if (res.status === 400) {
            msg = "Resposta inválida.";
        }

        throw new Error(msg);
    }

    return res.json();
}

function mensagem(id, texto, erro = false) {
    const el = document.getElementById(id);
    el.textContent = texto;
    el.style.color = erro ? "#a23e53" : "#5c433d";
}

function criarQuestao(item, questao, respondida) {
    const div = document.createElement("div");
    div.className = "questao-aluno";

    const titulo = document.createElement("h3");
    titulo.textContent = questao.enunciado;
    div.appendChild(titulo);

    const valor = document.createElement("p");
    valor.textContent = "Valor: " + item.valor + " pontos";
    div.appendChild(valor);

    if (respondida) {
        const aviso = document.createElement("p");
        aviso.className = "mensagem";
        aviso.textContent = "Resposta já enviada.";
        div.appendChild(aviso);
        return div;
    }

    const form = document.createElement("form");

    if (Number(questao.id_tipo) === 1) {
        for (const alt of questao.alternativas || []) {
            const linha = document.createElement("label");
            linha.className = "alternativa-aluno";

            const radio = document.createElement("input");
            radio.type = "radio";
            radio.name = "resposta";
            radio.value = alt.id_alternativa;
            radio.required = true;

            const texto = document.createElement("span");
            texto.textContent = alt.texto;

            linha.append(radio, texto);
            form.appendChild(linha);
        }
    } else {
        const texto = document.createElement("textarea");
        texto.name = "texto_livre";
        texto.placeholder = "Digite sua resposta...";
        texto.required = true;
        form.appendChild(texto);
    }

    const botao = document.createElement("button");
    botao.type = "submit";
    botao.textContent = "Enviar resposta";
    form.appendChild(botao);

    const aviso = document.createElement("p");
    aviso.className = "mensagem";
    aviso.setAttribute("role", "status");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const dados = {
            id_item: item.id_item
        };

        if (Number(questao.id_tipo) === 1) {
            const marcada = form.querySelector(
                'input[name="resposta"]:checked'
            );

            if (!marcada) {
                aviso.textContent = "Selecione uma alternativa.";
                return;
            }

            dados.id_alternativa = Number(marcada.value);
        } else {
            const texto = form.querySelector(
                'textarea[name="texto_livre"]'
            ).value.trim();

            if (!texto) {
                aviso.textContent = "Digite sua resposta.";
                return;
            }

            dados.texto_livre = texto;
        }

        botao.disabled = true;

        try {
            await enviar(
                "/participacoes/" +
                idParticipacao + "/respostas",
                dados
            );

            aviso.textContent = "Resposta enviada com sucesso.";
            aviso.style.color = "#5c433d";

            for (const campo of form.elements) {
                campo.disabled = true;
            }

            await carregarNota();

        } catch (erro) {
            aviso.textContent = erro.message;
            aviso.style.color = "#a23e53";
            botao.disabled = false;
        }
    });

    div.append(form, aviso);
    return div;
}

async function carregarAvaliacao() {
    const p = Number(
        document.getElementById("participacao").value
    );

    const a = Number(
        document.getElementById("avaliacao").value
    );

    if (!Number.isInteger(p) || p < 1 ||
        !Number.isInteger(a) || a < 1) {
        mensagem("msgBusca", "Informe IDs válidos.", true);
        return;
    }

    document.getElementById("secQuestoes").hidden = true;
    document.getElementById("secNota").hidden = true;
    mensagem("msgBusca", "Carregando...");

    try {
        const [participantes, itens, questoes, respostas] =
            await Promise.all([
                consultar("/avaliacoes/" + a + "/alunos"),
                consultar("/avaliacoes/" + a + "/questoes"),
                consultar("/questoes"),
                consultar("/participacoes/" + p + "/respostas")
            ]);

        const pertence = participantes.some(
            x => Number(x.id_participacao) === p
        );

        if (!pertence) {
            throw new Error(
                "A participação não pertence à avaliação."
            );
        }

        idParticipacao = p;
        idAvaliacao = a;

        const lista = document.getElementById("listaQuestoes");
        lista.replaceChildren();

        const respondidos = new Set(
            respostas.map(r => Number(r.id_item))
        );

        for (const item of itens) {
            const questao = questoes.find(
                q => Number(q.id_questao) ===
                     Number(item.id_questao)
            );

            if (!questao) {
                const aviso = document.createElement("p");
                aviso.textContent =
                    "Questão não encontrada: " + item.id_questao;
                lista.appendChild(aviso);
                continue;
            }

            lista.appendChild(
                criarQuestao(
                    item,
                    questao,
                    respondidos.has(Number(item.id_item))
                )
            );
        }

        if (!itens.length) {
            lista.textContent =
                "Esta avaliação ainda não possui questões.";
        }

        document.getElementById("secQuestoes").hidden = false;
        document.getElementById("secNota").hidden = false;

        mensagem("msgBusca", "Avaliação carregada.");
        await carregarNota();

    } catch (erro) {
        mensagem("msgBusca", erro.message, true);
    }
}

async function carregarNota() {
    if (!idParticipacao) return;

    try {
        const nota = await consultar(
            "/participacoes/" + idParticipacao + "/nota"
        );

        const div = document.getElementById("resultadoNota");

        div.replaceChildren();

        const total = document.createElement("h3");
        total.textContent =
            "Nota registrada: " +
            Number(nota.nota_total).toFixed(2) +
            " / " +
            Number(nota.valor_total).toFixed(2);

        const pendentes = document.createElement("p");
        pendentes.textContent =
            "Respostas aguardando correção: " + nota.pendentes;

        const observacao = document.createElement("p");
        observacao.textContent =
            "Nota parcial: questões sem resposta ou sem " +
            "correção podem alterar o resultado.";

        div.append(total, pendentes, observacao);

        mensagem("msgNota", "");

    } catch (erro) {
        mensagem("msgNota", erro.message, true);
    }
}

document.getElementById("formBusca")
    .addEventListener("submit", async (e) => {
        e.preventDefault();
        await carregarAvaliacao();
    });

document.getElementById("btnNota")
    .addEventListener("click", carregarNota);

document.addEventListener("DOMContentLoaded", carregarAvaliacao);
