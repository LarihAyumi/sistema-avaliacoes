# Sistema de Avaliações Acadêmicas

Integrantes
- Bruna Yokoshiro
- Larissa Ayumi Costa

Tema escolhido
Avaliação de aprendizagem (provas, simulados e quizzes).

Particularidade
Cada questão possui um valor dentro da avaliação. As questões objetivas são corrigidas automaticamente, enquanto as questões de texto livre são corrigidas pelo professor.
O sistema registra a nota obtida em cada questão e calcula a nota total de cada aluno.

# ---- COMO RODAR O SISTEMA ----

# Pré-requisitos
É necessário possuir:
- Java instalado, compatível com a versão configurada no projeto Maven;
- PostgreSQL instalado e em execução;
- Um navegador web;
- Acesso à internet para carregar o Chart.js pela CDN.


# Criar o banco de dados
No terminal, execute:
   psql -U postgres -c "CREATE DATABASE sistema_avaliacoes;"

Depois, a partir da raiz do projeto:
   psql -U postgres -d sistema_avaliacoes -v ON_ERROR_STOP=1 -f database/schema.sql
   psql -U postgres -d sistema_avaliacoes -v ON_ERROR_STOP=1 -f database/carga.sql

Esses comandos devem ser executados em um banco novo, pois o arquivo de carga insere os dados iniciais de teste.

# Configurar a conexão
O arquivo backend/src/main/resources/application.properties utiliza:
   spring.application.name=avaliacoes
   server.port=8080
   spring.datasource.url=jdbc:postgresql://localhost:5432/sistema_avaliacoes
   spring.datasource.username=postgres
   spring.datasource.password=${DB_PASSWORD}

A senha do PostgreSQL deve ser informada pela variável de ambiente DB_PASSWORD, sem ser incluída no código-fonte.

# Iniciar o backend
No PowerShell, execute:
   cd backend
   $env:DB_PASSWORD = Read-Host "Senha do PostgreSQL"
   .\mvnw.cmd spring-boot:run

Depois da inicialização, acesse:
http://localhost:8080/



# ---- Descrição dos relatórios -----

Páginas da aplicação:
Relatórios (/): Exibição de tabelas e gráficos
Área do professor(/professor.html): Cadastro de questões, avaliações e participantes
Área do aluno (/aluno.html): Visualização de questões e envio de respostas
Correção (/correcao.html): Correção discursiva e consulta de notas

As telas utilizam uma API REST desenvolvida em Java, com acesso ao PostgreSQL através de JDBC.

# Relatórios SQL

O sistema possui três relatórios:
1. Desempenho por avaliação 
Apresenta a quantidade de alunos associados, quantos possuem respostas, a média das notas e as maiores e menores notas registradas.
Os resultados são apresentados em tabela e gráfico de barras.

2. Evolução do aluno
Mostra as notas de um aluno em diferentes avaliações e permite compará-las com a média de cada avaliação.
Os resultados são apresentados em tabela e gráfico de linhas.

3. Questões mais erradas
Identifica as questões objetivas com maior percentual de respostas incorretas, considerando os registros disponíveis no banco.
Os resultados são apresentados em tabela e gráfico de barras.
As consultas utilizam recursos SQL como JOIN, LEFT JOIN, GROUP BY, HAVING, COUNT, SUM, AVG, subconsultas, filtros, ordenação e funções de ranking.

# Endpoints principais

GET / POST (/questoes): Consultar e cadastrar questões

GET / POST (/avaliacoes): Consultar e cadastrar avaliações

GET / POST (/avaliacoes/{id}/questoes): Consultar e associar questões

GET / POST (/avaliacoes/{id}/alunos): Consultar e associar alunos

GET / POST (/participacoes/{id}/respostas): Consultar e registrar respostas

PATCH (/respostas/{id}/nota): Corrigir questão discursiva

GET (/participacoes/{id}/nota): Consultar nota total

GET (/relatorios/desempenho): Relatório de desempenho

GET (/relatorios/evolucao/{id}): Relatório de evolução

GET (/relatorios/questoes): Relatório das questões mais erradas



# Dados de teste

O projeto contém dados fictícios para permitir a demonstração das funcionalidades.
Alguns registros utilizados nos testes foram:
- Professor de ID 1.
- Aluno de ID 3.
- Avaliação de ID 6 — Prova de Banco de Dados.
- Participação de ID 91.

Esses identificadores correspondem ao banco utilizado durante o desenvolvimento. Em uma instalação nova, os dados e IDs disponíveis dependem dos scripts executados.

# Observações

A implementação atual inclui as funcionalidades principais de cadastro, realização de avaliações, correção de respostas e geração de relatórios.
As interfaces ainda utilizam identificadores numéricos para selecionar usuários e avaliações. A aplicação também necessita de autenticação, autorização e proteção do gabarito das questões objetivas antes de ser utilizada em um ambiente real.

# Documentação

Diagrama Entidade-Relacionamento: docs/DER.png
Esquema relacional: docs/esquema-relacional.md
Criação das tabelas: database/schema.sql
Carga inicial: database/carga.sql
Consultas dos relatórios: database/relatorios.sql
