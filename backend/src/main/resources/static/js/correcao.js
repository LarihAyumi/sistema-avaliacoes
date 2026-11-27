let idAvaliacao = null;
let idParticipacao = null;
let itens = [];
let questoes = [];

async function requisitar(url, metodo = "GET", dados = null) {
    const op = { method: metodo };

    if (dados !== null) {
        op.headers = {
            "Content-Type": "application/json"
        };
        op.body = JSON.stringify(dados);
    }

    const res = await fetch(url, op);

    if (!res.ok) {
        throw new Error("Erro HTTP " + res.status);
    }

    if (res.status === 204) {
        return null;
    }

    return res.json();
}

function mensagem(id, texto, erro = false) {
    const el = document.getElementById(id);
    el.textContent = texto;
    el.style.color = erro ? "#a23e53" : "#5c433d";
}

function texto(tag, conteudo, classe = "") {
    const el = document.createElement(tag);
    el.textContent = conteudo;

    if (classe) {
        el.className = classe;
    }

    return el;
}

function formatarNota(valor) {
    return Number(valor).toFixed(2).replace(".", ",");
}

async function buscarAvaliacao() {
    const id = Number(
        document.getElementById("avaliacao").value
    );

    if (!Number.isInteger(id) || id < 1) {
        mensagem("msgBusca", "Informe uma avaliação válida.", true);
        return;
    }

    document.getElementById("secAlunos").hidden = true;
    document.getElementById("secRespostas").hidden = true;
    document.getElementById("secResultado").hidden = true;

    mensagem("msgBusca", "Carregando...");

    try {
        const [participantes, dadosItens, dadosQuestoes] =
            await Promise.all([
                requisitar("/avaliacoes/" + id + "/alunos"),
                requisitar("/avaliacoes/" + id + "/questoes"),
                requisitar("/questoes")
            ]);

        idAvaliacao = id;
        idParticipacao = null;
        itens = dadosItens;
        questoes = dadosQuestoes;

        const select = document.getElementById("alunoCorrecao");
        select.replaceChildren();

        for (const p of participantes) {
            const op = document.createElement("option");
            op.value = p.id_participacao;
            op.textContent =
                "Aluno ID " + p.id_usuario +
                " — Participação " + p.id_participacao;

            select.appendChild(op);
        }

        document.getElementById("secAlunos").hidden = false;

        if (!participantes.length) {
            mensagem(
                "msgAluno",
                "Esta avaliação não possui participantes."
            );
        } else {
            mensagem("msgAluno", "");
            await carregarAluno();
        }

        mensagem(
            "msgBusca",
            "Avaliação " + id + " carregada."
        );

    } catch (erro) {
        mensagem("msgBusca", erro.message, true);
    }
}

async function carregarAluno() {
    const select = document.getElementById("alunoCorrecao");
    const id = Number(select.value);

    if (!Number.isInteger(id) || id < 1) {
        return;
    }

    document.getElementById("secRespostas").hidden = true;
    document.getElementById("secResultado").hidden = true;

    try {
        const respostas = await requisitar(
            "/participacoes/" + id + "/respostas"
        );

        idParticipacao = id;

        const lista = document.getElementById("listaRespostas");
        lista.replaceChildren();

        let quantidade = 0;

        for (const r of respostas) {
            if (r.texto_livre == null) {
                continue;
            }

            const item = itens.find(
                i => Number(i.id_item) === Number(r.id_item)
            );

            if (!item) {
                continue;
            }

            const questao = questoes.find(
                q => Number(q.id_questao) ===
                     Number(item.id_questao)
            );

            if (!questao || Number(questao.id_tipo) !== 2) {
                continue;
            }

            quantidade++;

            const card = document.createElement("div");
            card.className = "questao-aluno";

            card.appendChild(
                texto("h3", questao.enunciado)
            );

            card.appendChild(
                texto(
                    "p",
                    "Valor máximo: " +
                    formatarNota(item.valor) + " pontos"
                )
            );

            card.appendChild(
                texto("h4", "Resposta do aluno:")
            );

            card.appendChild(
                texto("p", r.texto_livre, "texto-resposta")
            );

            const form = document.createElement("form");

            const rotulo = texto(
                "label",
                "Nota da resposta:"
            );

            const nota = document.createElement("input");
            nota.type = "number";
            nota.min = "0";
            nota.max = String(item.valor);
            nota.step = "0.01";
            nota.required = true;

            if (r.nota_obtida != null) {
                nota.value = r.nota_obtida;
            }

            const botao = document.createElement("button");
            botao.type = "submit";
            botao.textContent = "Salvar nota";

            const aviso = texto("p", "", "mensagem");
            aviso.setAttribute("role", "status");

            form.append(rotulo, nota, botao, aviso);

            form.addEventListener("submit", async (e) => {
                e.preventDefault();

                const valor = Number(nota.value);
                const maximo = Number(item.valor);

                if (
                    nota.value.trim() === "" ||
                    !Number.isFinite(valor) ||
                    valor < 0 ||
                    valor > maximo ||
                    Math.round(valor * 100) !== valor * 100
                ) {
                    aviso.textContent =
                        "Informe uma nota válida, de 0 a " +
                        formatarNota(maximo) + ".";
                    aviso.style.color = "#a23e53";
                    return;
                }

                botao.disabled = true;

                try {
                    await requisitar(
                        "/respostas/" + r.id_resposta + "/nota",
                        "PATCH",
                        { nota: valor }
                    );

                    aviso.textContent =
                        "Nota salva com sucesso.";
                    aviso.style.color = "#5c433d";

                    await carregarNota();

                } catch (erro) {
                    aviso.textContent = erro.message;
                    aviso.style.color = "#a23e53";
                } finally {
                    botao.disabled = false;
                }
            });

            card.appendChild(form);
            lista.appendChild(card);
        }

        if (quantidade === 0) {
            lista.appendChild(
                texto(
                    "p",
                    "Este aluno ainda não enviou " +
                    "respostas discursivas."
                )
            );
        }

        document.getElementById("secRespostas").hidden = false;
        document.getElementById("secResultado").hidden = false;

        mensagem("msgAluno", "Respostas carregadas.");
        await carregarNota();

    } catch (erro) {
        mensagem("msgAluno", erro.message, true);
    }
}

async function carregarNota() {
    if (!idParticipacao) {
        return;
    }

    try {
        const r = await requisitar(
            "/participacoes/" + idParticipacao + "/nota"
        );

        const div = document.getElementById("resultadoNota");
        div.replaceChildren();

        div.appendChild(
            texto(
                "h3",
                formatarNota(r.nota_total) + " / " +
                formatarNota(r.valor_total) + " pontos"
            )
        );

        div.appendChild(
            texto(
                "p",
                "Respostas aguardando correção: " +
                r.pendentes
            )
        );

        div.appendChild(
            texto(
                "p",
                "O resultado pode ser parcial caso " +
                "existam questões ainda não respondidas."
            )
        );

        mensagem("msgNota", "");

    } catch (erro) {
        mensagem("msgNota", erro.message, true);
    }
}

document.getElementById("formBusca")
    .addEventListener("submit", (e) => {
        e.preventDefault();
        buscarAvaliacao();
    });

document.getElementById("btnCarregar")
    .addEventListener("click", carregarAluno);

document.getElementById("btnAtualizar")
    .addEventListener("click", carregarNota);

document.addEventListener("DOMContentLoaded", buscarAvaliacao);
