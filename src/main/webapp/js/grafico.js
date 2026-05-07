/**
 * grafico.js
 */

async function inicializarGrafico() {
    const elementoCanvas = document.getElementById('graficoGerenciamento');
    
    if (!elementoCanvas) return;

    try {
        // O fetch "bate" na URL definida no @WebServlet do Java
        const response = await fetch('../api/dadosGrafico');
        
        if (!response.ok) {
            throw new Error('Erro ao procurar dados da API');
        }

        const dadosJson = await response.json();

        const ctx = elementoCanvas.getContext('2d');
        
        new Chart(ctx, {
            type: 'bar', // Tipo de gráfico (Barras)
            data: {
                labels: ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'],
                datasets: [
                    {
                        label: 'Entradas',
                        data: dadosJson.entradas, // Lista vinda do Java
                        backgroundColor: '#2ecc71',
                        borderWidth: 1
                    },
                    {
                        label: 'Saídas',
                        data: dadosJson.saidas, // Lista vinda do Java
                        backgroundColor: '#e74c3c',
                        borderWidth: 1
                    }
                ]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    } catch (error) {
        console.error("Erro ao carregar o gráfico:", error);
    }
}

// Inicia a lógica quando a página carregar
document.addEventListener('DOMContentLoaded', inicializarGrafico);