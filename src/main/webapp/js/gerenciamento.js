
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
    inicializarMovimentacao();
    fecharModalAoClicarFora();
});

/* ──────────────────────────────────────────
   Nível de estoque (status na listagem)
────────────────────────────────────────── */
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

/* ──────────────────────────────────────────
   Validação de campos (> 0 e não vazios)
────────────────────────────────────────── */
function campoVazio(valor) {
    return valor === null || valor === undefined || String(valor).trim() === '';
}

function numeroInvalido(valor) {
    const n = Number(valor);
    return Number.isNaN(n) || n <= 0;
}

function validarFormularioEdicao() {
    const camposTexto = [
        { id: 'edit-nomeProduto', nome: 'Nome do Produto' },
        { id: 'edit-fabricante', nome: 'Fabricante' },
        { id: 'edit-marca', nome: 'Marca' },
        { id: 'edit-dataFabricacao', nome: 'Data de Fabricação' },
        { id: 'edit-dataVencimento', nome: 'Data de Vencimento' }
    ];

    for (const campo of camposTexto) {
        const el = document.getElementById(campo.id);
        if (!el || campoVazio(el.value)) {
            return `Preencha o campo "${campo.nome}".`;
        }
    }

    const quantidadeMinima = document.getElementById('edit-quantidadeMinima').value;
    const valor = document.getElementById('edit-valor').value;

    if (numeroInvalido(quantidadeMinima)) {
        return 'Quantidade mínima de estoque deve ser maior que zero.';
    }
    if (numeroInvalido(valor)) {
        return 'Valor unitário deve ser maior que zero.';
    }

    return null;
}

function validarFormularioMovimentacao() {
    const produtoInput = document.getElementById('mov-produto');
    const tipo = document.getElementById('mov-tipo').value;
    const quantidade = document.getElementById('mov-quantidade').value;
    const codigo = resolverCodigoBarrasMovimentacao(produtoInput.value);

    if (campoVazio(produtoInput.value) || !codigo) {
        return 'Selecione um produto válido da lista.';
    }
    if (campoVazio(tipo)) {
        return 'Selecione o tipo de movimentação.';
    }
    if (numeroInvalido(quantidade)) {
        return 'Quantidade deve ser maior que zero.';
    }

    document.getElementById('mov-codigoBarras').value = codigo;
    return null;
}

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

        atualizarListaProdutosMovimentacao(dados);

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

function atualizarListaProdutosMovimentacao(produtos) {
    const datalist = document.getElementById('listaProdutosMovimentacao');
    if (!datalist) return;

    datalist.innerHTML = '';
    (produtos || []).forEach(p => {
        const porCodigo = document.createElement('option');
        porCodigo.value = p.codigoBarras;

        const porNome = document.createElement('option');
        porNome.value = `${p.nomeProduto} (${p.codigoBarras})`;

        datalist.appendChild(porCodigo);
        datalist.appendChild(porNome);
    });
}

function resolverCodigoBarrasMovimentacao(texto) {
    if (!texto || !texto.trim()) return null;

    const termo = texto.trim().toLowerCase();

    const porCodigo = todosProdutos.find(p => p.codigoBarras.toLowerCase() === termo);
    if (porCodigo) return porCodigo.codigoBarras;

    const porLabel = todosProdutos.find(p =>
        `${p.nomeProduto} (${p.codigoBarras})`.toLowerCase() === termo
    );
    if (porLabel) return porLabel.codigoBarras;

    const porNomeExato = todosProdutos.find(p => p.nomeProduto.toLowerCase() === termo);
    if (porNomeExato) return porNomeExato.codigoBarras;

    const parciais = todosProdutos.filter(p =>
        p.codigoBarras.toLowerCase().includes(termo) ||
        p.nomeProduto.toLowerCase().includes(termo)
    );
    if (parciais.length === 1) return parciais[0].codigoBarras;

    return null;
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
        const nivel = obterNivel(produto);
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><code style="font-size:0.8rem; color: var(--text-secondary);">${escapeHtml(produto.codigoBarras)}</code></td>
            <td><strong>${escapeHtml(produto.nomeProduto)}</strong></td>
            <td>${produto.quantidade}</td>
            <td>R$ ${parseFloat(produto.valor).toFixed(2)}</td>
            <td>R$ ${parseFloat(produto.total).toFixed(2)}</td>
            <td><span class="badge badge-${classeBadgeNivel(nivel)}">${formatarNivel(nivel)}</span></td>
            <td>
                <button class="btn-gerenciar" aria-label="Gerenciar ${escapeHtml(produto.nomeProduto)}">
                    ⚙ Gerenciar
                </button>
            </td>
        `;

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

    links.forEach(link => {
        link.addEventListener('click', () => {
            links.forEach(l => l.classList.remove('ativo-sidebar'));
            link.classList.add('ativo-sidebar');
        });
    });

    const marcarSecaoVisivel = () => {
        const offset = 100;
        let secaoAtiva = secoes[0];

        secoes.forEach(secao => {
            if (secao.getBoundingClientRect().top <= offset) {
                secaoAtiva = secao;
            }
        });

        const id = secaoAtiva.id.replace('secao-', '');
        links.forEach(link => {
            link.classList.toggle('ativo-sidebar', link.dataset.secao === id);
        });
    };

    window.addEventListener('scroll', marcarSecaoVisivel, { passive: true });
    marcarSecaoVisivel();
}

/* ──────────────────────────────────────────
   Movimentação de estoque
────────────────────────────────────────── */
function inicializarMovimentacao() {
    const form = document.getElementById('formMovimentacao');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        await registrarMovimentacao();
    });

    form.addEventListener('reset', () => {
        ocultarFeedback('mov-feedback');
        document.getElementById('mov-codigoBarras').value = '';
    });
}

async function registrarMovimentacao() {
    const btn = document.getElementById('btnRegistrarMov');
    ocultarFeedback('mov-feedback');

    const erroValidacao = validarFormularioMovimentacao();
    if (erroValidacao) {
        mostrarFeedback('mov-feedback', 'erro', erroValidacao);
        return;
    }

    btn.disabled = true;
    btn.textContent = 'Registrando...';

    const params = new URLSearchParams();
    params.append('codigoBarras', document.getElementById('mov-codigoBarras').value);
    params.append('tipo', document.getElementById('mov-tipo').value);
    params.append('quantidade', document.getElementById('mov-quantidade').value);

    try {
        const res = await fetch('../api/movimentacao', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        });

        const dados = await res.json().catch(() => ({}));

        if (!res.ok) {
            throw new Error(dados.erro || `Status ${res.status}`);
        }

        mostrarFeedback('mov-feedback', 'sucesso', '✓ Movimentação registrada com sucesso!');
        document.getElementById('formMovimentacao').reset();
        document.getElementById('mov-codigoBarras').value = '';
        await carregarProdutos();

    } catch (err) {
        console.error('[gerenciamento.js] Erro ao registrar movimentação:', err);
        mostrarFeedback('mov-feedback', 'erro', err.message);
    } finally {
        btn.disabled = false;
        btn.textContent = 'Registrar';
    }
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

    val.addEventListener('input', recalcular);
}

/* ──────────────────────────────────────────
   Modal: Abrir / Fechar / Abas
────────────────────────────────────────── */
function abrirModal(produto) {
    produtoAtual = produto;
    const nivel = obterNivel(produto);

    document.getElementById('modalNomeProduto').textContent = produto.nomeProduto;

    document.getElementById('det-codigo').textContent           = produto.codigoBarras  || '—';
    document.getElementById('det-fabricante').textContent       = produto.fabricante    || '—';
    document.getElementById('det-marca').textContent            = produto.marca         || '—';
    document.getElementById('det-quantidade').textContent        = produto.quantidade    ?? '—';
    document.getElementById('det-quantidadeMinima').textContent  = produto.quantidadeMinima ?? '—';
    document.getElementById('det-valor').textContent              = `R$ ${parseFloat(produto.valor).toFixed(2)}`;
    document.getElementById('det-total').textContent            = `R$ ${parseFloat(produto.total).toFixed(2)}`;
    document.getElementById('det-dataFabricacao').textContent     = formatarData(produto.dataFabricacao);
    document.getElementById('det-dataVencimento').textContent    = formatarData(produto.dataVencimento);
    document.getElementById('det-status').innerHTML =
        `<span class="badge badge-${classeBadgeNivel(nivel)}">${formatarNivel(nivel)}</span>`;

    document.getElementById('edit-codigoBarras').value       = produto.codigoBarras  || '';
    document.getElementById('edit-nomeProduto').value          = produto.nomeProduto   || '';
    document.getElementById('edit-fabricante').value           = produto.fabricante    || '';
    document.getElementById('edit-marca').value                = produto.marca         || '';
    document.getElementById('edit-quantidade').value           = produto.quantidade    ?? '';
    document.getElementById('edit-quantidadeMinima').value     = produto.quantidadeMinima ?? '';
    document.getElementById('edit-valor').value                = parseFloat(produto.valor).toFixed(2);
    document.getElementById('edit-total').value                = parseFloat(produto.total).toFixed(2);
    document.getElementById('edit-dataFabricacao').value       = produto.dataFabricacao || '';
    document.getElementById('edit-dataVencimento').value       = produto.dataVencimento || '';

    document.getElementById('excluir-nome').textContent = produto.nomeProduto;

    ocultarFeedback('edit-feedback');
    ocultarFeedback('excluir-feedback');

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

    ocultarFeedback('edit-feedback');

    const erroValidacao = validarFormularioEdicao();
    if (erroValidacao) {
        mostrarFeedback('edit-feedback', 'erro', erroValidacao);
        return;
    }

    const btnSalvar = document.getElementById('btnSalvar');
    btnSalvar.disabled = true;
    btnSalvar.textContent = 'Salvando...';

    const params = new URLSearchParams();
    params.append('codigoBarras',       document.getElementById('edit-codigoBarras').value);
    params.append('nomeProduto',        document.getElementById('edit-nomeProduto').value);
    params.append('fabricante',         document.getElementById('edit-fabricante').value);
    params.append('marca',              document.getElementById('edit-marca').value);
    params.append('quantidadeMinima',   document.getElementById('edit-quantidadeMinima').value);
    params.append('valor',              document.getElementById('edit-valor').value);
    params.append('dataFabricacao',     document.getElementById('edit-dataFabricacao').value);
    params.append('dataVencimento',     document.getElementById('edit-dataVencimento').value);

    try {
        const res = await fetch('../api/produtos/atualizar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        });

        const dados = await res.json().catch(() => ({}));

        if (!res.ok) {
            throw new Error(dados.erro || `Status ${res.status}`);
        }

        mostrarFeedback('edit-feedback', 'sucesso', '✓ Produto atualizado com sucesso!');

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

window.mudarAba          = mudarAba;
window.fecharModal       = fecharModal;
window.salvarEdicao      = salvarEdicao;
window.confirmarExclusao = confirmarExclusao;
window.carregarProdutos  = carregarProdutos;
