(function exibirErroCadastroSeHouver() {
    var params = new URLSearchParams(window.location.search);
    var codigo = params.get("erro");
    var mensagens = {
        codigo_duplicado_ativo:
            "Já existe um produto ativo com este código de barras. Use outro código ou edite o produto existente no gerenciamento.",
        codigo_duplicado_inativo:
            "Este código de barras já foi usado em um produto inativo (excluído). O sistema não permite reutilizar o mesmo código. Reative o produto antigo no banco ou cadastre com um código diferente.",
        cadastro_falhou:
            "Não foi possível salvar o produto. Verifique os dados, a conexão com o banco e tente novamente."
    };
    if (!codigo || !mensagens[codigo]) {
        return;
    }
    var banner = document.getElementById("banner-erro-cadastro");
    if (banner) {
        banner.textContent = mensagens[codigo];
        banner.classList.remove("oculto");
    }
    var path = window.location.pathname;
    window.history.replaceState(null, "", path);
})();

document.getElementById("valor").addEventListener("input", calcular);
document.getElementById("quantidade").addEventListener("input", calcular);

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

document.getElementById("formCadastro").addEventListener("submit", function (e) {
    const camposObrigatorios = [
        "codigoBarras", "nomeProduto", "fabricante", "marca",
        "dataFabricacao", "dataVencimento", "quantidade",
        "quantidadeMinima", "valor", "status"
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
});
