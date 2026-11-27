let graficos = {};

function tabela(id, dados, colunas) {
    const div = document.getElementById(id);

    if (!dados.length) {
        div.textContent = "Nenhum registro encontrado.";
        return;
    }

    const tb = document.createElement("table");
    const head = document.createElement("thead");
    const tr = document.createElement("tr");

    for (const [campo, titulo] of colunas) {
        const th = document.createElement("th");
        th.textContent = titulo;
        tr.appendChild(th);
    }

    head.appendChild(tr);
    tb.appendChild(head);

    const body = document.createElement("tbody");

    for (const d of dados) {
        const tr = document.createElement("tr");

        for (const [campo] of colunas) {
            const td = document.createElement("td");
            td.textContent = d[campo] ?? "-";
            tr.appendChild(td);
        }

        body.appendChild(tr);
    }

    tb.appendChild(body);
    div.replaceChildren(tb);
}

function grafico(id, tipo, labels, datasets) {
    if (graficos[id]) {
        graficos[id].destroy();
    }

    graficos[id] = new Chart(
        document.getElementById(id), {
            type: tipo,
            data: {
                labels,
                datasets
            },
            options: {
                responsive: true,
                maintainAspectRatio: false
            }
        }
    );
}

async function consultar(url) {
    const res = await fetch(url);

    if (!res.ok) {
        throw new Error("Erro HTTP " + res.status);
    }

    return await res.json();
}

async function carregarDesempenho() {
    const dados = await consultar("/relatorios/desempenho");

    tabela("tabelaDesempenho", dados, [
        ["titulo", "Avaliação"],
        ["alunos", "Alunos"],
        ["responderam", "Responderam"],
        ["media", "Média"],
        ["maior_nota", "Maior nota"],
        ["menor_nota", "Menor nota"]
    ]);

    grafico(
        "graficoDesempenho",
        "bar",
        dados.map(d => d.titulo + " #" + d.id_avaliacao),
        [{
            label: "Média",
            data: dados.map(d => d.media),
            backgroundColor: "#365cc3"
        }]
    );
}

async function carregarEvolucao() {
    const id = document.getElementById("aluno").value;
    const dados = await consultar("/relatorios/evolucao/" + id);

    tabela("tabelaEvolucao", dados, [
        ["titulo", "Avaliação"],
        ["nota_aluno", "Nota do aluno"],
        ["media_avaliacao", "Média da avaliação"]
    ]);

    grafico(
        "graficoEvolucao",
        "line",
        dados.map(d => d.titulo),
        [
            {
                label: "Nota do aluno",
                data: dados.map(d => d.nota_aluno),
                borderColor: "#365cc3",
                backgroundColor: "#365cc3"
            },
            {
                label: "Média da avaliação",
                data: dados.map(d => d.media_avaliacao),
                borderColor: "#e59a32",
                backgroundColor: "#e59a32"
            }
        ]
    );
}

async function carregarQuestoes() {
    const dados = await consultar("/relatorios/questoes");

    tabela("tabelaQuestoes", dados, [
        ["posicao", "Posição"],
        ["id_questao", "Questão"],
        ["enunciado", "Enunciado"],
        ["total_respostas", "Respostas"],
        ["total_erros", "Erros"],
        ["percentual_erros", "Erros (%)"]
    ]);

    grafico(
        "graficoQuestoes",
        "bar",
        dados.map(d => "Questão " + d.id_questao),
        [{
            label: "Percentual de erros (%)",
            data: dados.map(d => d.percentual_erros),
            backgroundColor: "#d45b61"
        }]
    );
}

async function iniciar() {
    try {
        await Promise.all([
            carregarDesempenho(),
            carregarEvolucao(),
            carregarQuestoes()
        ]);
    } catch (e) {
        alert("Erro ao carregar relatórios: " + e.message);
        console.error(e);
    }
}

document.addEventListener("DOMContentLoaded", iniciar);
