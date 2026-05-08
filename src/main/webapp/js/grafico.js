const MESES = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];
let instanciaGrafico = null;


async function buscarDadosGrafico(ano) {
    const response = await fetch(`../api/dadosGrafico?ano=${ano}`);

    if (!response.ok) {
        throw new Error(`Servidor retornou status ${response.status}`);
    }

    const dados = await response.json();

    if (dados.erro) {
        throw new Error(dados.erro);
    }

    return dados;
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
                    text: `Movimentações por Mês — ${ano}`,
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
        loader.style.cssText = 'text-align:center; color:#555; font-size:1rem; margin-top:1rem;';
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

    select.addEventListener('change', () => onMudanca(parseInt(select.value)));

    wrapper.appendChild(label);
    wrapper.appendChild(select);
    return wrapper;
}


async function inicializarGrafico() {
    const canvas = document.getElementById('graficoGerenciamento');
    if (!canvas) return;

    const container = canvas.parentElement;
    const anoAtual  = new Date().getFullYear();

    const seletor = criarSeletorAno(anoAtual, (anoSelecionado) => carregarEDesenhar(anoSelecionado));
    container.insertBefore(seletor, canvas);

    await carregarEDesenhar(anoAtual);
}


async function carregarEDesenhar(ano) {
    const canvas = document.getElementById('graficoGerenciamento');
    const ctx    = canvas.getContext('2d');

    mostrarCarregando(canvas, true);
    mostrarErro(canvas, '');

    try {
        const dados = await buscarDadosGrafico(ano);
        renderizarGrafico(ctx, dados, ano);
    } catch (error) {
        console.error('[grafico.js] Falha ao carregar gráfico:', error);
        mostrarErro(canvas, `Não foi possível carregar os dados: ${error.message}`);
    } finally {
        mostrarCarregando(canvas, false);
    }
}

document.addEventListener('DOMContentLoaded', inicializarGrafico);