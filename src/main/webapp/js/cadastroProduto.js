function dataLocalISO(date) {
    var d = date || new Date();
    var y = d.getFullYear();
    var m = String(d.getMonth() + 1).padStart(2, "0");
    var dia = String(d.getDate()).padStart(2, "0");
    return y + "-" + m + "-" + dia;
}

function hojeISO() {
    return dataLocalISO(new Date());
}

function amanhaISO() {
    var d = new Date();
    d.setDate(d.getDate() + 1);
    return dataLocalISO(d);
}

function configurarLimitesData() {
    var fab = document.getElementById("dataFabricacao");
    var ven = document.getElementById("dataVencimento");
    if (fab) fab.max = hojeISO();
    if (ven) ven.min = amanhaISO();
}

function preencherCodigoBarras() {
    var el = document.getElementById("codigoBarras");
    if (!el) return;

    fetch("../cadastroProdutos")
        .then(function (res) {
            if (!res.ok) throw new Error("falha");
            return res.json();
        })
        .then(function (data) {
            el.value = data.codigoBarras;
        })
        .catch(function () {
            el.value = "00000001";
        });
}

function calcular() {
    let valor = parseFloat(document.getElementById("valor").value) || 0;
    let quantidade = parseInt(document.getElementById("quantidade").value) || 0;
    document.getElementById("total").value = (valor * quantidade).toFixed(2);
}

function campoVazio(valor) {
    return valor === null || valor === undefined || String(valor).trim() === "";
}

function numeroInvalido(valor) {
    const n = Number(valor);
    return Number.isNaN(n) || n <= 0;
}

function validarDatas(dataFabricacao, dataVencimento) {
    const hoje = hojeISO();

    if (dataFabricacao > hoje) {
        return "Data de fabricação não pode ser posterior à data atual.";
    }
    if (dataVencimento <= hoje) {
        return "Data de vencimento deve ser posterior à data atual.";
    }
    if (dataVencimento < dataFabricacao) {
        return "Data de vencimento não pode ser anterior à data de fabricação.";
    }
    return null;
}

function exibirErroCadastroSeHouver() {
    var params = new URLSearchParams(window.location.search);
    var codigo = params.get("erro");
    var mensagens = {
        codigo_duplicado_ativo:
            "Já existe um produto ativo com este código de barras. Tente salvar novamente.",
        codigo_duplicado_inativo:
            "Falha ao gerar código de barras único. Tente salvar novamente.",
        cadastro_falhou:
            "Não foi possível salvar o produto. Verifique os dados e tente novamente."
    };
    if (!codigo || !mensagens[codigo]) {
        return;
    }
    var banner = document.getElementById("banner-erro-cadastro");
    if (banner) {
        banner.textContent = mensagens[codigo];
        banner.classList.remove("oculto");
    }
    preencherCodigoBarras();
    window.history.replaceState(null, "", window.location.pathname);
}

configurarLimitesData();
preencherCodigoBarras();
exibirErroCadastroSeHouver();

document.getElementById("valor").addEventListener("input", calcular);
document.getElementById("quantidade").addEventListener("input", calcular);

document.getElementById("formCadastro").addEventListener("submit", function (e) {
    const camposObrigatorios = [
        "codigoBarras", "nomeProduto", "fabricante", "marca",
        "dataFabricacao", "dataVencimento", "quantidade",
        "quantidadeMinima", "valor"
    ];

    for (const nome of camposObrigatorios) {
        const el = this.elements[nome];
        if (!el || campoVazio(el.value)) {
            e.preventDefault();
            alert("Preencha todos os campos obrigatórios.");
            return;
        }
    }

    if (numeroInvalido(this.elements.quantidade.value)) {
        e.preventDefault();
        alert("Quantidade deve ser maior que zero.");
        return;
    }

    if (numeroInvalido(this.elements.quantidadeMinima.value)) {
        e.preventDefault();
        alert("Quantidade mínima de estoque deve ser maior que zero.");
        return;
    }

    if (numeroInvalido(this.elements.valor.value)) {
        e.preventDefault();
        alert("Valor unitário deve ser maior que zero.");
        return;
    }

    const erroData = validarDatas(
        this.elements.dataFabricacao.value,
        this.elements.dataVencimento.value
    );
    if (erroData) {
        e.preventDefault();
        alert(erroData);
    }
});
