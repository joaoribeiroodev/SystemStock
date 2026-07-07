const DATA_NASCIMENTO_MIN = "1900-01-01";

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

function configurarLimitesDataNascimento() {
    var inputData = document.getElementById("dtaNascimento");
    if (!inputData) return;

    inputData.min = DATA_NASCIMENTO_MIN;
    inputData.max = hojeISO();
}

const inputData = document.getElementById("dtaNascimento");
const avisoErro = document.getElementById("avisoErro");
const formCadastro = document.querySelector('form[action="cadastro"]');

function checarEExibirErro() {
    if (!inputData || !avisoErro) return;

    var resultado = validarDataNascimento(inputData.value);

    if (!resultado.ehValida) {
        avisoErro.textContent = resultado.mensagem;
        avisoErro.style.display = "inline";
        if (inputData.value && inputData.value > hojeISO()) {
            inputData.value = "";
        }
    } else {
        avisoErro.style.display = "none";
    }
}

configurarLimitesDataNascimento();

if (inputData) {
    inputData.addEventListener("blur", checarEExibirErro);
    inputData.addEventListener("change", checarEExibirErro);
}

if (formCadastro) {
    formCadastro.addEventListener("submit", function (e) {
        var resultado = validarDataNascimento(inputData ? inputData.value : "");
        if (!resultado.ehValida) {
            e.preventDefault();
            if (avisoErro) {
                avisoErro.textContent = resultado.mensagem;
                avisoErro.style.display = "inline";
            }
            if (inputData) {
                inputData.focus();
            }
        }
    });
}
