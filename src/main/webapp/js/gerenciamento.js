

'use strict';

/* ──────────────────────────────────────────
   Estado global
────────────────────────────────────────── */
let produtoAtual = null;
let todosProdutos = [];

/* ──────────────────────────────────────────
   Inicialização
────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
    carregarProdutos();
    inicializarBusca();
    inicializarSidebar();
    inicializarCalculoTotal();
    fecharModalAoClicarFora();
});

/* ──────────────────────────────────────────
   Carregamento de Produtos
────────────────────────────────────────── */
async function carregarProdutos() {
    mostrarEstado('carregando');

    try {
        const res = await fetch('../api/estoque');

        if (!res.ok) throw new Error(`Servidor retornou status ${res.status}`);

        const dados = await res.json();
        todosProdutos = dados;

        if (!dados || dados.length === 0) {
            mostrarEstado('vazio');
            return;
        }

        renderizarTabela(dados);
        mostrarEstado('tabela');

    } catch (err) {
        console.error('[gerenciamento.js] Erro ao carregar produtos:', err);
        document.getElementById('mensagemErro').textContent = `Erro: ${err.message}`;
        mostrarEstado('erro');
    }
}

/* ──────────────────────────────────────────
   Renderização da Tabela
────────────────────────────────────────── */
function renderizarTabela(produtos) {
    const tbody = document.getElementById('corpoTabelaGerenciamento');
    tbody.innerHTML = '';

    if (!produtos || produtos.length === 0) {
        tbody.innerHTML = `
            <tr class="sem-dados">
                <td colspan="7">Nenhum produto encontrado para esta busca.</td>
            </tr>`;
        return;
    }

    produtos.forEach(produto => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><code style="font-size:0.8rem; color: var(--text-secondary);">${escapeHtml(produto.codigoBarras)}</code></td>
            <td><strong>${escapeHtml(produto.nomeProduto)}</strong></td>
            <td>${produto.quantidade}</td>
            <td>R$ ${parseFloat(produto.valor).toFixed(2)}</td>
            <td>R$ ${parseFloat(produto.total).toFixed(2)}</td>
            <td><span class="badge badge-${produto.status.toLowerCase()}">${produto.status}</span></td>
            <td>
                <button class="btn-gerenciar" aria-label="Gerenciar ${escapeHtml(produto.nomeProduto)}">
                    ⚙ Gerenciar
                </button>
            </td>
        `;

        // Associa o clique ao produto correto (sem usar onclick inline)
        tr.querySelector('.btn-gerenciar').addEventListener('click', () => abrirModal(produto));
        tbody.appendChild(tr);
    });
}

/* ──────────────────────────────────────────
   Controle de Estados da UI
────────────────────────────────────────── */
function mostrarEstado(estado) {
    document.getElementById('estadoCarregando').style.display = estado === 'carregando' ? 'flex' : 'none';
    document.getElementById('estadoVazio').style.display      = estado === 'vazio'      ? 'flex' : 'none';
    document.getElementById('estadoErro').style.display       = estado === 'erro'       ? 'flex' : 'none';
    document.getElementById('tabelaWrapper').style.display    = estado === 'tabela'     ? 'block' : 'none';
}

/* ──────────────────────────────────────────
   Busca / Filtro Local
────────────────────────────────────────── */
function inicializarBusca() {
    const input = document.getElementById('buscaProduto');
    if (!input) return;

    input.addEventListener('input', () => {
        const termo = input.value.trim().toLowerCase();

        if (todosProdutos.length === 0) return;

        const filtrados = todosProdutos.filter(p =>
            p.nomeProduto.toLowerCase().includes(termo) ||
            p.codigoBarras.toLowerCase().includes(termo) ||
            (p.fabricante && p.fabricante.toLowerCase().includes(termo)) ||
            (p.marca && p.marca.toLowerCase().includes(termo))
        );

        renderizarTabela(filtrados);
        mostrarEstado('tabela');
    });
}

/* ──────────────────────────────────────────
   Sidebar: destacar link ativo no scroll
────────────────────────────────────────── */
function inicializarSidebar() {
    const links = document.querySelectorAll('.sidebar-link');
    const secoes = document.querySelectorAll('.secao');

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const id = entry.target.id.replace('secao-', '');
                links.forEach(link => {
                    const isAtivo = link.dataset.secao === id;
                    link.classList.toggle('ativo-sidebar', isAtivo);
                });
            }
        });
    }, { threshold: 0.4 });

    secoes.forEach(secao => observer.observe(secao));
}

/* ──────────────────────────────────────────
   Cálculo automático do Total (edição)
────────────────────────────────────────── */
function inicializarCalculoTotal() {
    const qty = document.getElementById('edit-quantidade');
    const val = document.getElementById('edit-valor');
    const tot = document.getElementById('edit-total');

    if (!qty || !val || !tot) return;

    const recalcular = () => {
        const q = parseFloat(qty.value) || 0;
        const v = parseFloat(val.value) || 0;
        tot.value = (q * v).toFixed(2);
    };

    qty.addEventListener('input', recalcular);
    val.addEventListener('input', recalcular);
}

/* ──────────────────────────────────────────
   Modal: Abrir / Fechar / Abas
────────────────────────────────────────── */
function abrirModal(produto) {
    produtoAtual = produto;

    // Preencher header do modal
    document.getElementById('modalNomeProduto').textContent = produto.nomeProduto;

    // Preencher aba Detalhes
    document.getElementById('det-codigo').textContent        = produto.codigoBarras  || '—';
    document.getElementById('det-fabricante').textContent    = produto.fabricante    || '—';
    document.getElementById('det-marca').textContent         = produto.marca         || '—';
    document.getElementById('det-quantidade').textContent    = produto.quantidade    ?? '—';
    document.getElementById('det-valor').textContent         = `R$ ${parseFloat(produto.valor).toFixed(2)}`;
    document.getElementById('det-total').textContent         = `R$ ${parseFloat(produto.total).toFixed(2)}`;
    document.getElementById('det-dataFabricacao').textContent = formatarData(produto.dataFabricacao);
    document.getElementById('det-dataVencimento').textContent = formatarData(produto.dataVencimento);
    document.getElementById('det-status').innerHTML = `<span class="badge badge-${produto.status.toLowerCase()}">${produto.status}</span>`;

    // Preencher aba Editar
    document.getElementById('edit-codigoBarras').value    = produto.codigoBarras  || '';
    document.getElementById('edit-nomeProduto').value     = produto.nomeProduto   || '';
    document.getElementById('edit-fabricante').value      = produto.fabricante    || '';
    document.getElementById('edit-marca').value           = produto.marca         || '';
    document.getElementById('edit-quantidade').value      = produto.quantidade    ?? '';
    document.getElementById('edit-valor').value           = parseFloat(produto.valor).toFixed(2);
    document.getElementById('edit-total').value           = parseFloat(produto.total).toFixed(2);
    document.getElementById('edit-dataFabricacao').value  = produto.dataFabricacao || '';
    document.getElementById('edit-dataVencimento').value  = produto.dataVencimento || '';
    document.getElementById('edit-status').value          = produto.status         || 'ENTRADA';

    // Preencher aba Excluir
    document.getElementById('excluir-nome').textContent = produto.nomeProduto;

    // Limpar feedbacks anteriores
    ocultarFeedback('edit-feedback');
    ocultarFeedback('excluir-feedback');

    // Mostrar modal na aba Detalhes
    mudarAba('detalhes');
    document.getElementById('modalOverlay').classList.add('ativo');
    document.body.style.overflow = 'hidden';
}

function fecharModal() {
    document.getElementById('modalOverlay').classList.remove('ativo');
    document.body.style.overflow = '';
    produtoAtual = null;
}

function fecharModalAoClicarFora() {
    document.getElementById('modalOverlay').addEventListener('click', (e) => {
        if (e.target === document.getElementById('modalOverlay')) {
            fecharModal();
        }
    });
}

function mudarAba(aba) {
    document.querySelectorAll('.aba-conteudo').forEach(el => el.classList.remove('ativo'));
    document.querySelectorAll('.btn-aba').forEach(el => el.classList.remove('ativo'));
    document.getElementById(`aba-${aba}`).classList.add('ativo');
    document.querySelector(`[data-aba="${aba}"]`).classList.add('ativo');
}

/* ──────────────────────────────────────────
   CRUD: Salvar Edição
────────────────────────────────────────── */
async function salvarEdicao() {
    if (!produtoAtual) return;

    const btnSalvar = document.getElementById('btnSalvar');
    btnSalvar.disabled = true;
    btnSalvar.textContent = 'Salvando...';
    ocultarFeedback('edit-feedback');

    const params = new URLSearchParams();
    params.append('codigoBarras',    document.getElementById('edit-codigoBarras').value);
    params.append('nomeProduto',     document.getElementById('edit-nomeProduto').value);
    params.append('fabricante',      document.getElementById('edit-fabricante').value);
    params.append('marca',           document.getElementById('edit-marca').value);
    params.append('quantidade',      document.getElementById('edit-quantidade').value);
    params.append('valor',           document.getElementById('edit-valor').value);
    params.append('total',           document.getElementById('edit-total').value);
    params.append('dataFabricacao',  document.getElementById('edit-dataFabricacao').value);
    params.append('dataVencimento',  document.getElementById('edit-dataVencimento').value);
    params.append('status',          document.getElementById('edit-status').value);

    try {
        const res = await fetch('../api/produtos/atualizar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        });

        if (!res.ok) throw new Error(`Status ${res.status}`);

        mostrarFeedback('edit-feedback', 'sucesso', '✓ Produto atualizado com sucesso!');

        // Recarrega a lista e fecha após breve delay
        await carregarProdutos();
        setTimeout(fecharModal, 1200);

    } catch (err) {
        console.error('[gerenciamento.js] Erro ao atualizar produto:', err);
        mostrarFeedback('edit-feedback', 'erro', `Erro ao salvar: ${err.message}`);
    } finally {
        btnSalvar.disabled = false;
        btnSalvar.textContent = 'Salvar Alterações';
    }
}

/* ──────────────────────────────────────────
   CRUD: Confirmar Exclusão
────────────────────────────────────────── */
async function confirmarExclusao() {
    if (!produtoAtual) return;

    const btnExcluir = document.getElementById('btnExcluir');
    btnExcluir.disabled = true;
    btnExcluir.textContent = 'Excluindo...';
    ocultarFeedback('excluir-feedback');

    const codigo = encodeURIComponent(produtoAtual.codigoBarras);

    try {
        const res = await fetch(`../api/produtos/excluir?codigoBarras=${codigo}`, {
            method: 'DELETE'
        });

        if (!res.ok) throw new Error(`Status ${res.status}`);

        mostrarFeedback('excluir-feedback', 'sucesso', '✓ Produto excluído com sucesso!');

        await carregarProdutos();
        setTimeout(fecharModal, 1200);

    } catch (err) {
        console.error('[gerenciamento.js] Erro ao excluir produto:', err);
        mostrarFeedback('excluir-feedback', 'erro', `Erro ao excluir: ${err.message}`);
    } finally {
        btnExcluir.disabled = false;
        btnExcluir.textContent = 'Excluir Produto';
    }
}

/* ──────────────────────────────────────────
   Utilitários
────────────────────────────────────────── */
function mostrarFeedback(elementId, tipo, mensagem) {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.textContent = mensagem;
    el.className = `feedback ${tipo}`;
    el.style.display = 'block';
}

function ocultarFeedback(elementId) {
    const el = document.getElementById(elementId);
    if (el) el.style.display = 'none';
}

function formatarData(dataStr) {
    if (!dataStr) return '—';
    try {
        const [ano, mes, dia] = dataStr.split('-');
        return `${dia}/${mes}/${ano}`;
    } catch {
        return dataStr;
    }
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

// Expõe funções chamadas no HTML (abas e botões do modal)
window.mudarAba          = mudarAba;
window.fecharModal       = fecharModal;
window.salvarEdicao      = salvarEdicao;
window.confirmarExclusao = confirmarExclusao;
window.carregarProdutos  = carregarProdutos;