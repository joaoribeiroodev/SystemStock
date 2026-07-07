const ITENS_POR_PAGINA = 10;
let itensDashboard = [];
let paginaAtualDashboard = 1;

async function carregarEstoque() {
    try {
        const response = await fetch("../api/estoque");

        if (!response.ok) throw new Error(`Status ${response.status}`);

        const dados = await response.json();
        definirItensDashboard(dados);

    } catch (erro) {
        console.error("Erro ao carregar os produtos:", erro);
    }
}

function linhaTabela(item) {
    return `
        <tr>
            <td>${escapeHtml(item.codigoBarras)}</td>
            <td>${escapeHtml(item.nomeProduto)}</td>
            <td>${escapeHtml(item.fabricante)}</td>
            <td>${escapeHtml(item.marca)}</td>
            <td>${escapeHtml(item.dataFabricacao)}</td>
            <td>${escapeHtml(item.dataVencimento)}</td>
            <td>${escapeHtml(String(item.quantidade))}</td>
            <td>${escapeHtml(item.valor)}</td>
            <td>${escapeHtml(item.total)}</td>
            <td>${htmlBadgeNivel(item)}</td>
        </tr>`;
}

function renderizarPaginaDashboard() {
    const tabela = document.getElementById("corpoTabela");
    if (!tabela) return;

    const totalPaginas = Math.max(1, Math.ceil(itensDashboard.length / ITENS_POR_PAGINA));
    if (paginaAtualDashboard > totalPaginas) {
        paginaAtualDashboard = totalPaginas;
    }

    const inicio = (paginaAtualDashboard - 1) * ITENS_POR_PAGINA;
    const fim = inicio + ITENS_POR_PAGINA;
    const paginaItens = itensDashboard.slice(inicio, fim);

    tabela.innerHTML = paginaItens.map(linhaTabela).join("");
    atualizarControlesPaginacao(totalPaginas);
}

function atualizarControlesPaginacao(totalPaginas) {
    const select = document.getElementById("pagina");
    const btnVoltar = document.getElementById("btnVoltar");
    const btnProximo = document.getElementById("btnProximo");

    if (!select || !btnVoltar || !btnProximo) return;

    select.innerHTML = "";
    for (let i = 1; i <= totalPaginas; i++) {
        const opt = document.createElement("option");
        opt.value = String(i);
        opt.textContent = String(i);
        if (i === paginaAtualDashboard) {
            opt.selected = true;
        }
        select.appendChild(opt);
    }

    btnVoltar.disabled = paginaAtualDashboard <= 1;
    btnProximo.disabled = paginaAtualDashboard >= totalPaginas;
}

function definirItensDashboard(dados) {
    itensDashboard = Array.isArray(dados) ? dados : [];
    paginaAtualDashboard = 1;
    renderizarPaginaDashboard();
}

function inicializarPaginacao() {
    const select = document.getElementById("pagina");
    const btnVoltar = document.getElementById("btnVoltar");
    const btnProximo = document.getElementById("btnProximo");

    if (!select || !btnVoltar || !btnProximo) return;

    select.addEventListener("change", function (e) {
        paginaAtualDashboard = parseInt(e.target.value, 10) || 1;
        renderizarPaginaDashboard();
    });

    btnVoltar.addEventListener("click", function () {
        if (paginaAtualDashboard > 1) {
            paginaAtualDashboard--;
            renderizarPaginaDashboard();
        }
    });

    btnProximo.addEventListener("click", function () {
        const totalPaginas = Math.max(1, Math.ceil(itensDashboard.length / ITENS_POR_PAGINA));
        if (paginaAtualDashboard < totalPaginas) {
            paginaAtualDashboard++;
            renderizarPaginaDashboard();
        }
    });
}

window.definirItensDashboard = definirItensDashboard;

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

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
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
    inicializarPaginacao();
    carregarEstoque();
    carregarResumo();
};
