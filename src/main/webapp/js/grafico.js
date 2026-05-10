

'use strict';

const MESES = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

let instanciaGrafico = null;

/** Código de barras enviado à API ao carregar o gráfico (nunca enviar ID). */
let codigoBarrasFiltroAplicado = null;
let produtosGraficoCache = [];

async function buscarDadosGrafico(ano, codigoBarras) {
    const params = new URLSearchParams({ ano: String(ano) });
    if (codigoBarras) {
        params.set('codigoBarras', codigoBarras);
    }

    const response = await fetch(`../api/dadosGrafico?${params.toString()}`);

    const dados = await response.json();

    if (!response.ok || dados.erro) {
        throw new Error(dados.erro || `Servidor retornou status ${response.status}`);
    }

    return dados;
}

function tituloGrafico(dados, ano) {
    if (dados.nomeProduto) {
        return `Movimentações por mês — ${dados.nomeProduto} (${ano})`;
    }
    return `Movimentações por Mês — ${ano}`;
}

function renderizarGrafico(ctx, dados, ano) {
    if (instanciaGrafico) {
        instanciaGrafico.destroy();
        instanciaGrafico = null;
    }

    const totalEntradas = dados.entradas.reduce((a, b) => a + b, 0);
    const totalSaidas   = dados.saidas.reduce((a, b) => a + b, 0);

    instanciaGrafico = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: MESES,
            datasets: [
                {
                    label: `Entradas (Total: ${totalEntradas})`,
                    data: dados.entradas,
                    backgroundColor: 'rgba(46, 204, 113, 0.75)',
                    borderColor: '#27ae60',
                    borderWidth: 1,
                    borderRadius: 4,
                },
                {
                    label: `Saídas (Total: ${totalSaidas})`,
                    data: dados.saidas,
                    backgroundColor: 'rgba(231, 76, 60, 0.75)',
                    borderColor: '#c0392b',
                    borderWidth: 1,
                    borderRadius: 4,
                }
            ]
        },
        options: {
            responsive: true,
            animation: { duration: 600, easing: 'easeInOutQuart' },
            plugins: {
                legend: { position: 'top' },
                title: {
                    display: true,
                    text: tituloGrafico(dados, ano),
                    font: { size: 16 }
                },
                tooltip: {
                    callbacks: {
                        label: (context) => {
                            const label = context.dataset.label.split(' (')[0];
                            return ` ${label}: ${context.parsed.y} unidades`;
                        }
                    }
                }
            },
            scales: {
                x: { grid: { display: false } },
                y: {
                    beginAtZero: true,
                    ticks: {
                        precision: 0,
                        stepSize: 1
                    }
                }
            }
        }
    });
}

function mostrarCarregando(container, visivel) {
    let loader = document.getElementById('graficoLoader');

    if (!loader) {
        loader = document.createElement('p');
        loader.id = 'graficoLoader';
        loader.textContent = 'Carregando dados...';
        loader.style.cssText = 'text-align:center; color: var(--text-muted); font-size:0.95rem; margin-top:0.75rem;';
        container.insertAdjacentElement('beforebegin', loader);
    }

    loader.style.display = visivel ? 'block' : 'none';
}

function mostrarErro(container, mensagem) {
    let erroEl = document.getElementById('graficoErro');

    if (!erroEl) {
        erroEl = document.createElement('p');
        erroEl.id = 'graficoErro';
        erroEl.style.cssText = 'text-align:center; color:#e74c3c; font-size:0.95rem; margin-top:0.5rem;';
        container.insertAdjacentElement('afterend', erroEl);
    }

    erroEl.textContent = mensagem || '';
    erroEl.style.display = mensagem ? 'block' : 'none';
}

function atualizarHint(texto, classe) {
    const el = document.getElementById('graficoFiltroHint');
    if (!el) return;
    el.textContent = texto || '';
    el.classList.remove('aviso', 'erro');
    if (classe) el.classList.add(classe);
}

function popularDatalistProdutos(datalistEl, produtos) {
    datalistEl.innerHTML = '';
    produtos.forEach((p) => {
        const opt = document.createElement('option');
        opt.value = p.codigoBarras;
        opt.label = p.nomeProduto || '';
        datalistEl.appendChild(opt);
    });
}

function resolverCodigoBarras(termo) {
    const t = termo.trim();
    if (!t) return null;

    const lower = t.toLowerCase();

    const porCodigo = produtosGraficoCache.find(
        (p) => String(p.codigoBarras).toLowerCase() === lower
    );
    if (porCodigo) return porCodigo.codigoBarras;

    const porNomeExato = produtosGraficoCache.find(
        (p) => (p.nomeProduto || '').toLowerCase() === lower
    );
    if (porNomeExato) return porNomeExato.codigoBarras;

    const candidatos = produtosGraficoCache.filter((p) => {
        const nome = (p.nomeProduto || '').toLowerCase();
        const cod = String(p.codigoBarras || '').toLowerCase();
        return nome.includes(lower) || cod.includes(lower);
    });

    if (candidatos.length === 1) {
        return candidatos[0].codigoBarras;
    }

    return null;
}

async function carregarListaProdutosGrafico() {
    try {
        const res = await fetch('../api/estoque');
        if (!res.ok) return;
        produtosGraficoCache = await res.json();
        const dl = document.getElementById('listaProdutosGrafico');
        if (dl && Array.isArray(produtosGraficoCache)) {
            popularDatalistProdutos(dl, produtosGraficoCache);
        }
    } catch (e) {
        console.warn('[grafico.js] Lista de produtos indisponível para sugestões:', e);
    }
}

function criarSeletorAno(anoAtual, onMudanca) {
    const wrapper = document.createElement('div');
    wrapper.style.cssText = 'display:flex; align-items:center; gap:8px; margin-bottom:12px;';

    const label = document.createElement('label');
    label.htmlFor = 'seletorAno';
    label.textContent = 'Filtrar por ano:';
    label.style.fontWeight = '600';

    const select = document.createElement('select');
    select.id = 'seletorAno';
    select.style.cssText = 'padding:4px 10px; border-radius:6px; border:1px solid #ccc; font-size:0.95rem;';

    for (let a = anoAtual; a >= anoAtual - 4; a--) {
        const opt = document.createElement('option');
        opt.value = a;
        opt.textContent = a;
        if (a === anoAtual) opt.selected = true;
        select.appendChild(opt);
    }

    select.addEventListener('change', () => onMudanca(parseInt(select.value, 10)));

    wrapper.appendChild(label);
    wrapper.appendChild(select);
    return wrapper;
}

async function carregarEDesenhar(ano) {
    const canvas = document.getElementById('graficoGerenciamento');
    const ctx    = canvas.getContext('2d');

    mostrarCarregando(canvas, true);
    mostrarErro(canvas, '');
    atualizarHint('');

    try {
        const dados = await buscarDadosGrafico(ano, codigoBarrasFiltroAplicado);
        renderizarGrafico(ctx, dados, ano);

        if (dados.nomeProduto) {
            atualizarHint(`Gráfico filtrado pelo produto «${dados.nomeProduto}».`);
        }
    } catch (error) {
        console.error('[grafico.js] Falha ao carregar gráfico:', error);
        mostrarErro(canvas, `Não foi possível carregar os dados: ${error.message}`);
        atualizarHint(error.message, 'erro');
    } finally {
        mostrarCarregando(canvas, false);
    }
}

function aplicarFiltroProdutoGrafico() {
    const input = document.getElementById('buscaProdutoGrafico');
    if (!input) return;

    const raw = input.value.trim();
    if (!raw) {
        codigoBarrasFiltroAplicado = null;
        const sel = document.getElementById('seletorAno');
        const ano = sel ? parseInt(sel.value, 10) : new Date().getFullYear();
        carregarEDesenhar(ano);
        return;
    }

    const codigo = resolverCodigoBarras(raw);
    if (!codigo) {
        atualizarHint(
            'Use o código de barras completo, o nome exato do produto ou escolha uma sugestão única na lista.',
            'aviso'
        );
        return;
    }

    codigoBarrasFiltroAplicado = codigo;
    const sel = document.getElementById('seletorAno');
    const ano = sel ? parseInt(sel.value, 10) : new Date().getFullYear();
    carregarEDesenhar(ano);
}

function limparFiltroProdutoGrafico() {
    const input = document.getElementById('buscaProdutoGrafico');
    if (input) input.value = '';
    codigoBarrasFiltroAplicado = null;
    atualizarHint('');
    const sel = document.getElementById('seletorAno');
    const ano = sel ? parseInt(sel.value, 10) : new Date().getFullYear();
    carregarEDesenhar(ano);
}

function inicializarFiltrosGrafico() {
    const btnAplicar = document.getElementById('btnGraficoAplicarProduto');
    const btnLimpar = document.getElementById('btnGraficoLimparProduto');
    const input = document.getElementById('buscaProdutoGrafico');

    if (btnAplicar) btnAplicar.addEventListener('click', aplicarFiltroProdutoGrafico);
    if (btnLimpar) btnLimpar.addEventListener('click', limparFiltroProdutoGrafico);

    if (input) {
        input.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                aplicarFiltroProdutoGrafico();
            }
        });
    }
}

async function inicializarGrafico() {
    const canvas = document.getElementById('graficoGerenciamento');
    if (!canvas) return;

    const container = canvas.parentElement;
    const anoAtual  = new Date().getFullYear();

    const seletor = criarSeletorAno(anoAtual, (anoSelecionado) => {
        carregarEDesenhar(anoSelecionado);
    });
    container.insertBefore(seletor, canvas);

    inicializarFiltrosGrafico();
    await carregarListaProdutosGrafico();
    await carregarEDesenhar(anoAtual);
}

document.addEventListener('DOMContentLoaded', inicializarGrafico);
