async function filtroEstoque() {
    try {
        const nome = document.getElementById("pesquisarNome").value;
        const tipo = document.getElementById("tipoMovimentacao").value;
        const data = document.getElementById("filtroData").value;

        const url = `http://localhost:8080/api/estoque?nome=${encodeURIComponent(nome)}&tipo=${encodeURIComponent(tipo)}&data=${encodeURIComponent(data)}`;
        const response = await fetch(url);
        const dados = await response.json();

        const tabela = document.getElementById("corpoTabela");
        tabela.innerHTML = "";

        const filtrados = dados.filter(item => {
            const matchNome = nome === "" || item.nomeProduto.toLowerCase().includes(nome);
            const matchTipo = tipo === "" || item.status === tipo;
            const matchData = data === "" || item.dataFabricacao === data;

            return matchNome && matchTipo && matchData;
        });

        filtrados.forEach(item => {
            const linha = `
            <tr>
                <td>${item.codigoBarras}</td>
                <td>${item.nomeProduto}</td>
                <td>${item.fabricante}</td>
                <td>${item.marca}</td>
                <td>${item.dataFabricacao}</td>
                <td>${item.dataVencimento}</td>
                <td>${item.quantidade}</td>
                <td>${parseFloat(item.valor).toFixed(2)}</td>
                <td>${parseFloat(item.total).toFixed(2)}</td>
                <td>${item.status}</td>
            </tr>
        `;
            tabela.innerHTML += linha;
        });

    } catch (erro) {
        console.error("Erro ao filtrar", erro);
    }
}

document.getElementById("btnPesquisar").addEventListener("click", filtroEstoque);