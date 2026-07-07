//impede ano data superior À corrrente

function validarDataNascimento(dataParaValidar) {
  // Regra 1: Vazio
  if (!dataParaValidar || dataParaValidar.trim() === '') {
    return { ehValida: false, mensagem: 'O campo de data é obrigatório e não pode ficar vazio!' };
  }

  // Regra 2: Futuro
  const dataDeHoje = new Date().toISOString().split('T')[0];
  if (dataParaValidar > dataDeHoje) {
    return { ehValida: false, mensagem: 'A data de nascimento não pode ser no futuro!' };
  }

  return { ehValida: true, mensagem: '' };
}

const inputData = document.getElementById('dtaNascimento');
const avisoErro = document.getElementById('avisoErro');

// Função que será chamada para checar e mostrar o aviso
function checarEExibirErro() {
  const dataEscolhida = inputData.value;
  const resultado = validarDataNascimento(dataEscolhida);

  if (!resultado.ehValida) {
    avisoErro.textContent = resultado.mensagem;
    avisoErro.style.display = 'inline';
    inputData.value = ''; // Limpa caso seja uma data futura
  } else {
    avisoErro.style.display = 'none';
  }
}

// O 'blur' garante que se o usuário clicar no campo e sair sem digitar, o aviso de vazio aparece
inputData.addEventListener('blur', checarEExibirErro);

// O 'change' garante que se ele digitar uma data inválida, avisa na mesma hora
inputData.addEventListener('change', checarEExibirErro);