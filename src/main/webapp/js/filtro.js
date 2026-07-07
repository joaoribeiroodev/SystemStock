async function filtroEstoque() {
    try {
        const nome = document.getElementById("pesquisarNome").value;
        const tipo = document.getElementById("tipoMovimentacao").value;
        const data = document.getElementById("filtroData").value;

        const url = `../api/estoque?nome=${encodeURIComponent(nome)}&tipo=${encodeURIComponent(tipo)}&data=${encodeURIComponent(data)}`;
        const response = await fetch(url);
        const dados = await response.json();

        if (typeof definirItensDashboard === "function") {
            definirItensDashboard(dados);
        }

    } catch (erro) {
        console.error("Erro ao filtrar", erro);
    }
}

document.getElementById("btnPesquisar").addEventListener("click", filtroEstoque);
