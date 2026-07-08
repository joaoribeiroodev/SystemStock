
'use strict';

let produtoAtual = null;
let todosProdutos = [];
let todasSolicitacoes = [];

document.addEventListener('DOMContentLoaded', async () => {
    configurarLimitesDataEdicao();
    try {
        await carregarProdutos();
    } catch (e) {
        console.warn('[gerenciamento.js] falha ao carregar produtos antes das solicitações:', e);
    }
    carregarSolicitacoes();
    inicializarBusca();
    inicializarSidebar();
    inicializarCalculoTotal();
    inicializarMovimentacao();
    inicializarModalMovimentacao();
    inicializarFiltroSolicitacoes();
    fecharModalAoClicarFora();
});


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


function formatarStatusSolicitacao(status) {
    const mapa = {
        PENDENTE: 'Pendente',
        EM_ANDAMENTO: 'Em andamento',
        ATENDIDA: 'Atendida',
        CANCELADA: 'Cancelada'
    };
    return mapa[status] || status;
}

function classeBadgeSolicitacao(status) {
    const mapa = {
        PENDENTE: 'pendente',
        EM_ANDAMENTO: 'em-andamento',
        ATENDIDA: 'atendida',
        CANCELADA: 'cancelada'
    };
    return mapa[status] || 'pendente';
}

function formatarDataHora(valor) {
    if (!valor) return '—';
    try {
        let data = String(valor).trim();
        if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}(:\d{2})?$/.test(data)) {
            data = data.replace(' ', 'T');
        }
        const d = new Date(data);
        if (Number.isNaN(d.getTime())) return valor;
        return d.toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
    } catch {
        return valor;
    }
}

async function carregarSolicitacoes() {
    const status = document.getElementById('filtroStatusSolicitacao')?.value || '';
    mostrarEstadoSolicitacoes('carregando');

    try {
        const qs = status ? `?status=${encodeURIComponent(status)}` : '';
        const res = await fetch(`../api/solicitacoes${qs}`);

        if (!res.ok) throw new Error(`Servidor retornou status ${res.status}`);

        const dados = await res.json();
        todasSolicitacoes = dados || [];

        atualizarBadgeSolicitacoesPendentes();

        if (todasSolicitacoes.length === 0) {
            mostrarEstadoSolicitacoes('vazio');
            return;
        }

        renderizarTabelaSolicitacoes(todasSolicitacoes);
        mostrarEstadoSolicitacoes('tabela');

    } catch (err) {
        console.error('[gerenciamento.js] Erro ao carregar solicitações:', err);
        const el = document.getElementById('solMensagemErro');
        if (el) el.textContent = `Erro: ${err.message}`;
        mostrarEstadoSolicitacoes('erro');
    }
}

function atualizarBadgeSolicitacoesPendentes() {
    const badge = document.getElementById('badgeSolicitacoesPendentes');
    if (!badge) return;

    const pendentes = todasSolicitacoes.filter(s => s.status === 'PENDENTE').length;
    if (pendentes > 0) {
        badge.textContent = String(pendentes);
        badge.style.display = 'inline-flex';
    } else {
        badge.style.display = 'none';
    }
}

function mostrarEstadoSolicitacoes(estado) {
    const carregando = document.getElementById('solEstadoCarregando');
    const vazio = document.getElementById('solEstadoVazio');
    const erro = document.getElementById('solEstadoErro');
    const tabela = document.getElementById('solTabelaWrapper');
    if (carregando) carregando.style.display = estado === 'carregando' ? 'flex' : 'none';
    if (vazio) vazio.style.display = estado === 'vazio' ? 'flex' : 'none';
    if (erro) erro.style.display = estado === 'erro' ? 'flex' : 'none';
    if (tabela) tabela.style.display = estado === 'tabela' ? 'block' : 'none';
}

function renderizarTabelaSolicitacoes(lista) {
    const tbody = document.getElementById('corpoTabelaSolicitacoes');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (!lista || lista.length === 0) {
        tbody.innerHTML = `
            <tr class="sem-dados">
                <td colspan="8">Nenhuma solicitação encontrada para este filtro.</td>
            </tr>`;
        return;
    }

    lista.forEach(sol => {
        const tr = document.createElement('tr');
        const aberta = sol.status === 'PENDENTE' || sol.status === 'EM_ANDAMENTO';

        tr.innerHTML = `
            <td>
                <strong>${escapeHtml(sol.nomeProduto)}</strong><br>
                <code style="font-size:0.75rem; color: var(--text-secondary);">${escapeHtml(sol.codigoBarras)}</code>
            </td>
            <td>${escapeHtml(localProdutoPorCodigo(sol.codigoBarras))}</td>
            <td>${sol.quantidadeAtual}</td>
            <td>${sol.quantidadeMinima}</td>
            <td>${sol.quantidadeSugerida}</td>
            <td><span class="badge badge-${classeBadgeSolicitacao(sol.status)}">${formatarStatusSolicitacao(sol.status)}</span></td>
            <td>${formatarDataHora(sol.criadoEm)}</td>
            <td class="acoes-solicitacao"></td>
        `;

        const tdAcoes = tr.querySelector('.acoes-solicitacao');
        if (aberta) {
            if (sol.status === 'PENDENTE') {
                tdAcoes.appendChild(criarBotaoSolicitacao('Em andamento', 'EM_ANDAMENTO', sol.id, 'btn-gerenciar'));
            }
            tdAcoes.appendChild(criarBotaoSolicitacao('Atendida', 'ATENDIDA', sol.id, 'btn-gerenciar'));
            tdAcoes.appendChild(criarBotaoSolicitacao('Cancelar', 'CANCELADA', sol.id, 'btn-gerenciar btn-cancelar-sol'));
        } else {
            tdAcoes.innerHTML = '<span style="color: var(--text-muted); font-size:0.8rem;">—</span>';
        }

        tbody.appendChild(tr);
    });
}

function criarBotaoSolicitacao(rotulo, novoStatus, id, classe) {
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = classe;
    btn.textContent = rotulo;
    btn.style.marginRight = '0.4rem';
    btn.addEventListener('click', () => atualizarStatusSolicitacao(id, novoStatus));
    return btn;
}

function localProdutoPorCodigo(codigoBarras) {
    const produto = todosProdutos.find(p => p.codigoBarras === codigoBarras);
    if (!produto) return '—';
    const partes = [produto.prateleira, produto.localArmazenamento].filter(Boolean);
    return partes.length ? partes.join(' — ') : '—';
}

async function atualizarStatusSolicitacao(id, novoStatus) {
    const rotulos = {
        EM_ANDAMENTO: 'marcar como Em andamento',
        ATENDIDA: 'marcar como Atendida',
        CANCELADA: 'cancelar'
    };

    if (novoStatus === 'CANCELADA' && !confirm('Tem certeza que deseja cancelar esta solicitação de compra?')) {
        return;
    }

    const params = new URLSearchParams();
    params.append('id', id);
    params.append('status', novoStatus);

    try {
        const res = await fetch('../api/solicitacoes/atualizar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        });

        const dados = await res.json().catch(() => ({}));

        if (!res.ok) {
            throw new Error(dados.erro || `Status ${res.status}`);
        }

        await carregarSolicitacoes();

    } catch (err) {
        console.error('[gerenciamento.js] Erro ao atualizar solicitação:', err);
        alert(`Não foi possível ${rotulos[novoStatus] || 'atualizar'} a solicitação: ${err.message}`);
    }
}

function inicializarFiltroSolicitacoes() {
    const select = document.getElementById('filtroStatusSolicitacao');
    if (!select) return;
    select.addEventListener('change', () => carregarSolicitacoes());
}


function campoVazio(valor) {
    return valor === null || valor === undefined || String(valor).trim() === '';
}

function numeroInvalido(valor) {
    const n = Number(valor);
    return Number.isNaN(n) || n <= 0;
}

function dataLocalISO(date) {
    const d = date || new Date();
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const dia = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${dia}`;
}

function hojeISO() {
    return dataLocalISO(new Date());
}

function amanhaISO() {
    const d = new Date();
    d.setDate(d.getDate() + 1);
    return dataLocalISO(d);
}

function configurarLimitesDataEdicao() {
    const fab = document.getElementById('edit-dataFabricacao');
    const ven = document.getElementById('edit-dataVencimento');
    if (fab) fab.max = hojeISO();
    if (ven) ven.min = amanhaISO();
}

function validarDatas(dataFabricacao, dataVencimento) {
    const hoje = hojeISO();

    if (dataFabricacao > hoje) {
        return 'Data de fabricação não pode ser posterior à data atual.';
    }
    if (dataVencimento <= hoje) {
        return 'Data de vencimento deve ser posterior à data atual.';
    }
    if (dataVencimento < dataFabricacao) {
        return 'Data de vencimento não pode ser anterior à data de fabricação.';
    }
    return null;
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

    const erroData = validarDatas(
        document.getElementById('edit-dataFabricacao').value,
        document.getElementById('edit-dataVencimento').value
    );
    if (erroData) return erroData;

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


function mostrarEstado(estado) {
    document.getElementById('estadoCarregando').style.display = estado === 'carregando' ? 'flex' : 'none';
    document.getElementById('estadoVazio').style.display      = estado === 'vazio'      ? 'flex' : 'none';
    document.getElementById('estadoErro').style.display       = estado === 'erro'       ? 'flex' : 'none';
    document.getElementById('tabelaWrapper').style.display    = estado === 'tabela'     ? 'block' : 'none';
}


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


function abrirModalMovimentacao(produto) {
    fecharModal();

    const form = document.getElementById('formMovimentacao');
    if (form) form.reset();

    document.getElementById('mov-codigoBarras').value = '';
    ocultarFeedback('mov-feedback');

    if (produto) {
        document.getElementById('mov-produto').value =
            `${produto.nomeProduto} (${produto.codigoBarras})`;
        document.getElementById('mov-codigoBarras').value = produto.codigoBarras;
    }

    document.getElementById('modalMovimentacaoOverlay').classList.add('ativo');
    document.body.style.overflow = 'hidden';

    setTimeout(() => {
        const input = document.getElementById('mov-produto');
        if (input) input.focus();
    }, 100);
}

function fecharModalMovimentacao() {
    document.getElementById('modalMovimentacaoOverlay').classList.remove('ativo');
    document.body.style.overflow = '';
}

function inicializarModalMovimentacao() {
    const btnToolbar = document.getElementById('btnAbrirMovimentacao');
    const btnSidebar = document.getElementById('sidebarAbrirMovimentacao');

    if (btnToolbar) {
        btnToolbar.addEventListener('click', () => abrirModalMovimentacao());
    }

    if (btnSidebar) {
        btnSidebar.addEventListener('click', (e) => {
            e.preventDefault();
            document.querySelectorAll('.sidebar-link').forEach(l => l.classList.remove('ativo-sidebar'));
            btnSidebar.classList.add('ativo-sidebar');
            abrirModalMovimentacao();
        });
    }
}

function inicializarMovimentacao() {
    const form = document.getElementById('formMovimentacao');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        await registrarMovimentacao();
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
        await carregarProdutos();
        await carregarSolicitacoes();
        setTimeout(fecharModalMovimentacao, 1200);

    } catch (err) {
        console.error('[gerenciamento.js] Erro ao registrar movimentação:', err);
        mostrarFeedback('mov-feedback', 'erro', err.message);
    } finally {
        btn.disabled = false;
        btn.textContent = 'Registrar';
    }
}

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


function abrirModal(produto) {
    fecharModalMovimentacao();
    produtoAtual = produto;
    const nivel = obterNivel(produto);

    document.getElementById('modalNomeProduto').textContent = produto.nomeProduto;

    document.getElementById('det-codigo').textContent           = produto.codigoBarras  || '—';
    document.getElementById('det-fabricante').textContent       = produto.fabricante    || '—';
    document.getElementById('det-marca').textContent            = produto.marca         || '—';
    document.getElementById('det-quantidade').textContent        = produto.quantidade    ?? '—';
    document.getElementById('det-quantidadeMinima').textContent  = produto.quantidadeMinima ?? '—';
    document.getElementById('det-prateleira').textContent         = produto.prateleira || '—';
    document.getElementById('det-localArmazenamento').textContent = produto.localArmazenamento || '—';
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
    document.getElementById('edit-prateleira').value           = produto.prateleira || '';
    document.getElementById('edit-localArmazenamento').value   = produto.localArmazenamento || '';
    configurarLimitesDataEdicao();

    document.getElementById('excluir-nome').textContent = produto.nomeProduto;
    resetConfirmacaoExclusao();

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

    document.getElementById('modalMovimentacaoOverlay').addEventListener('click', (e) => {
        if (e.target === document.getElementById('modalMovimentacaoOverlay')) {
            fecharModalMovimentacao();
        }
    });
}

function mudarAba(aba) {
    document.querySelectorAll('.aba-conteudo').forEach(el => el.classList.remove('ativo'));
    document.querySelectorAll('.btn-aba').forEach(el => el.classList.remove('ativo'));
    document.getElementById(`aba-${aba}`).classList.add('ativo');
    document.querySelector(`[data-aba="${aba}"]`).classList.add('ativo');

    if (aba === 'excluir') resetConfirmacaoExclusao();
}


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
    params.append('prateleira',         document.getElementById('edit-prateleira').value);
    params.append('localArmazenamento', document.getElementById('edit-localArmazenamento').value);

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
        await carregarSolicitacoes();
        setTimeout(fecharModal, 1200);

    } catch (err) {
        console.error('[gerenciamento.js] Erro ao atualizar produto:', err);
        mostrarFeedback('edit-feedback', 'erro', `Erro ao salvar: ${err.message}`);
    } finally {
        btnSalvar.disabled = false;
        btnSalvar.textContent = 'Salvar Alterações';
    }
}

function resetConfirmacaoExclusao() {
    const checkbox = document.getElementById('excluir-confirmar');
    const btnExcluir = document.getElementById('btnExcluir');
    if (checkbox) checkbox.checked = false;
    if (btnExcluir) {
        btnExcluir.disabled = true;
        btnExcluir.textContent = 'Excluir Produto';
    }
}

function atualizarBotaoExcluir() {
    const checkbox = document.getElementById('excluir-confirmar');
    const btnExcluir = document.getElementById('btnExcluir');
    if (checkbox && btnExcluir) {
        btnExcluir.disabled = !checkbox.checked;
    }
}

async function confirmarExclusao() {
    if (!produtoAtual) return;

    const checkbox = document.getElementById('excluir-confirmar');
    if (!checkbox?.checked) return;

    const nome = produtoAtual.nomeProduto || 'este produto';
    if (!confirm(`Tem certeza que deseja excluir "${nome}"?\n\nEsta ação não pode ser desfeita.`)) {
        return;
    }

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
        atualizarBotaoExcluir();
        btnExcluir.textContent = 'Excluir Produto';
    }
}


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

window.mudarAba                = mudarAba;
window.fecharModal             = fecharModal;
window.fecharModalMovimentacao = fecharModalMovimentacao;
window.salvarEdicao            = salvarEdicao;
window.confirmarExclusao       = confirmarExclusao;
window.atualizarBotaoExcluir   = atualizarBotaoExcluir;
window.carregarProdutos        = carregarProdutos;
window.carregarSolicitacoes    = carregarSolicitacoes;
