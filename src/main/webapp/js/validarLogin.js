async function aplicarPermissoesMenu() {
    try {
        const res = await fetch("../api/perfil");
        if (!res.ok) return;

        const dados = await res.json();

        if (!dados.podeCadastrarUsuario) {
            document.querySelectorAll('[data-menu="cadastro-usuario"]').forEach(function (el) {
                el.style.display = "none";
                if (el.parentElement && el.parentElement.tagName === "LI") {
                    el.parentElement.style.display = "none";
                }
            });
        }
    } catch (e) {
        console.error("Erro ao verificar permissões do menu:", e);
    }
}

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", aplicarPermissoesMenu);
} else {
    aplicarPermissoesMenu();
}
