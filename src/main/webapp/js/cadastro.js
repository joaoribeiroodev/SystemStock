const DATA_NASCIMENTO_MIN = "1900-01-01";
const CPF_DIGITOS = 11;

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

function somenteDigitos(valor, limite) {
    var limpo = String(valor || "").replace(/\D/g, "");
    if (limite) {
        limpo = limpo.slice(0, limite);
    }
    return limpo;
}

function validarDataNascimento(dataParaValidar) {
    if (!dataParaValidar || dataParaValidar.trim() === "") {
        return { ehValida: false, mensagem: "O campo de data é obrigatório e não pode ficar vazio!" };
    }

    if (dataParaValidar < DATA_NASCIMENTO_MIN) {
        return { ehValida: false, mensagem: "A data de nascimento deve ser a partir de 01/01/1900." };
    }

    var hoje = hojeISO();
    if (dataParaValidar > hoje) {
        return { ehValida: false, mensagem: "A data de nascimento não pode ser no futuro!" };
    }

    return { ehValida: true, mensagem: "" };
}

function validarCpf(valor) {
    var digitos = somenteDigitos(valor, CPF_DIGITOS);

    if (!digitos) {
        return { ehValida: false, mensagem: "O CPF é obrigatório." };
    }

    if (digitos.length !== CPF_DIGITOS) {
        return { ehValida: false, mensagem: "O CPF deve conter exatamente 11 dígitos." };
    }

    return { ehValida: true, mensagem: "" };
}

function configurarLimitesDataNascimento() {
    var inputData = document.getElementById("dtaNascimento");
    if (!inputData) return;

    inputData.min = DATA_NASCIMENTO_MIN;
    inputData.max = hojeISO();
}

function configurarCampoCpf() {
    var inputCpf = document.getElementById("cpf");
    if (!inputCpf) return;

    inputCpf.addEventListener("input", function () {
        var limpo = somenteDigitos(inputCpf.value, CPF_DIGITOS);
        if (inputCpf.value !== limpo) {
            inputCpf.value = limpo;
        }
    });
}

function configurarCampoNumero() {
    var inputNumero = document.getElementById("numero");
    if (!inputNumero) return;

    inputNumero.addEventListener("input", function () {
        var limpo = somenteDigitos(inputNumero.value);
        if (inputNumero.value !== limpo) {
            inputNumero.value = limpo;
        }
    });
}

const inputData = document.getElementById("dtaNascimento");
const inputCpf = document.getElementById("cpf");
const avisoErro = document.getElementById("avisoErro");
const avisoErroCpf = document.getElementById("avisoErroCpf");
const formCadastro = document.querySelector('form[action="cadastro"]');

function exibirErro(elementoAviso, mensagem) {
    if (!elementoAviso) return;
    elementoAviso.textContent = mensagem;
    elementoAviso.style.display = "inline";
}

function ocultarErro(elementoAviso) {
    if (!elementoAviso) return;
    elementoAviso.style.display = "none";
}

function checarDataNascimento() {
    if (!inputData) return { ehValida: true, mensagem: "" };

    var resultado = validarDataNascimento(inputData.value);

    if (!resultado.ehValida) {
        exibirErro(avisoErro, resultado.mensagem);
        if (inputData.value && inputData.value > hojeISO()) {
            inputData.value = "";
        }
    } else {
        ocultarErro(avisoErro);
    }

    return resultado;
}

function checarCpf() {
    if (!inputCpf) return { ehValida: true, mensagem: "" };

    var resultado = validarCpf(inputCpf.value);

    if (!resultado.ehValida) {
        exibirErro(avisoErroCpf, resultado.mensagem);
    } else {
        ocultarErro(avisoErroCpf);
    }

    return resultado;
}

configurarLimitesDataNascimento();
configurarCampoCpf();
configurarCampoNumero();

if (inputData) {
    inputData.addEventListener("blur", checarDataNascimento);
    inputData.addEventListener("change", checarDataNascimento);
}

if (inputCpf) {
    inputCpf.addEventListener("blur", checarCpf);
}

if (formCadastro) {
    formCadastro.addEventListener("submit", function (e) {
        var resultadoData = checarDataNascimento();
        var resultadoCpf = checarCpf();

        if (!resultadoData.ehValida) {
            e.preventDefault();
            if (inputData) inputData.focus();
            return;
        }

        if (!resultadoCpf.ehValida) {
            e.preventDefault();
            if (inputCpf) inputCpf.focus();
        }
    });
}
