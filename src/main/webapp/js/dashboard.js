async function carregarEstoque() {
    try {
        const response = await fetch("http://localhost:8080/api/estoque");
        const dados = await response.json();

        const tabela = document.getElementById("corpoTabela");
        tabela.innerHTML = "";

        dados.forEach(item => {
            const linha = `
                    <tr>
                        <td>${item.codigoBarras}</td>
                        <td>${item.nomeProduto}</td>
                        <td>${item.fabricante}</td>
                        <td>${item.marca}</td>
                        <td>${item.dataFabricacao}</td>
                        <td>${item.dataVencimento}</td>
                        <td>${item.quantidade}</td>
                        <td>${item.valor}</td>
                        <td>${item.total}</td>
                        <td>${item.status}</td>
                    </tr>
                    `;
            tabela.innerHTML += linha;
        });
    } catch (erro) {
        console.log("Erro ao carregar os produtos", erro);
    }
}

async function carregarResumo() {
    try {
        const response = await fetch("http://localhost:8080/api/resumo");
        const dados = await response.json();

        document.getElementById("cardEntrada").innerHTML = dados.entrada;
        document.getElementById("cardSaida").innerHTML = dados.saida;
        document.getElementById("cardTotal").innerHTML = dados.total;
    } catch (erro) {
        console.log("Erro na consulta dos dados", erro);
    }
}

window.onload = () => {
    carregarEstoque();
    carregarResumo();
}; 