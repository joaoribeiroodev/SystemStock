async function validarLogin() {
    try {
        const res = await fetch("http://localhost:8080/api/perfil");
        const dado = await res.json();

        console.log("PERFIL FRONT: ", dado.perfil);

        if(!dado.perfil || dado.perfil.toLowerCase() !== "admin") {
            document.getElementsByClassName(".btn-menu").style.display = "none";
        }
    } catch (e) {
        console.log("Erro ao verificar o perfil.", e);
    }
}

validarLogin();