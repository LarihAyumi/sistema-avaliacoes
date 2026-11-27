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
        document.getElementById(id),
        {
            type: tipo,
            data: {
                labels,
                datasets
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        labels: {
                            color: "#5c433d",
                            font: {
                                family: "Arial",
                                size: 13
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        ticks: {
                            color: "#7b5e57"
                        },
                        grid: {
                            color: "#f1d4dc"
                        }
                    },
                    y: {
                        beginAtZero: true,
                        ticks: {
                            color: "#7b5e57"
                        },
                        grid: {
                            color: "#f1d4dc"
                        }
                    }
                }
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
        [
            {
                label: "Média das avaliações",
                data: dados.map(d => d.media),
                backgroundColor: "#d9a7b8",
                borderColor: "#b97a8c",
                borderWidth: 1,
                borderRadius: 7
            }
        ]
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
                borderColor: "#c88ea2",
                backgroundColor: "#c88ea2",
                borderWidth: 3,
                pointRadius: 5,
                pointHoverRadius: 7,
                tension: 0.3,
                fill: false
            },
            {
                label: "Média da avaliação",
                data: dados.map(d => d.media_avaliacao),
                borderColor: "#7b5e57",
                backgroundColor: "#7b5e57",
                borderWidth: 3,
                pointRadius: 5,
                pointHoverRadius: 7,
                tension: 0.3,
                fill: false
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
        [
            {
                label: "Percentual de erros (%)",
                data: dados.map(d => d.percentual_erros),
                backgroundColor: "#b97a8c",
                borderColor: "#7b5e57",
                borderWidth: 1,
                borderRadius: 7
            }
        ]
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
