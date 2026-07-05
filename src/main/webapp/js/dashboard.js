async function carregarEstoque() {
    try {
        const response = await fetch("../api/estoque");

        if (!response.ok) throw new Error(`Status ${response.status}`);

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
                    <td>${htmlBadgeNivel(item)}</td>
                </tr>`;
            tabela.innerHTML += linha;
        });

    } catch (erro) {
        console.error("Erro ao carregar os produtos:", erro);
    }
}

function obterNivel(produto) {
    if (produto.nivel) {
        return produto.nivel;
    }
    const qtd = Number(produto.quantidade) || 0;
    const min = Number(produto.quantidadeMinima) || 0;
    if (min > 0 && qtd < min) {
        return 'BAIXO';
    }
    return 'NORMAL';
}

function formatarNivel(nivel) {
    if (nivel === 'BAIXO') return 'Baixo';
    return 'Normal';
}

function classeBadgeNivel(nivel) {
    return nivel === 'BAIXO' ? 'baixo' : 'normal';
}

function htmlBadgeNivel(produto) {
    const nivel = obterNivel(produto);
    return `<span class="badge badge-${classeBadgeNivel(nivel)}">${formatarNivel(nivel)}</span>`;
}

async function carregarResumo() {
    try {
        const response = await fetch("../api/resumo");

        if (!response.ok) throw new Error(`Status ${response.status}`);

        const dados = await response.json();

        document.getElementById("cardEntrada").innerHTML = dados.entrada;
        document.getElementById("cardSaida").innerHTML   = dados.saida;
        document.getElementById("cardTotal").innerHTML   = dados.total;

    } catch (erro) {
        console.error("Erro na consulta dos dados:", erro);
    }
}

window.onload = () => {
    carregarEstoque();
    carregarResumo();
};