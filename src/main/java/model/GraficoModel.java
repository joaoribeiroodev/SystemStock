package model;

import java.util.ArrayList;
import java.util.List;

public class GraficoModel {

    private List<Long> entradas = new ArrayList<>();
    private List<Long> saidas  = new ArrayList<>();
    private int ano;
    private String erro; // Mensagem de erro para retorno ao frontend, se necessário

    /** Nome para exibição quando o gráfico é filtrado por código de barras (sem expor identificadores internos). */
    private String nomeProduto;



    public List<Long> getEntradas() { return entradas; }
    public List<Long> getSaidas()   { return saidas;   }
    public int        getAno()      { return ano;       }
    public String     getErro()     { return erro;      }
    public String     getNomeProduto() { return nomeProduto; }



    public void setEntradas(List<Long> entradas) { this.entradas = entradas; }
    public void setSaidas(List<Long> saidas)     { this.saidas   = saidas;   }
    public void setAno(int ano)                  { this.ano      = ano;      }
    public void setErro(String erro)             { this.erro     = erro;     }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }




    public void garantirDozeMeses() {
        while (entradas.size() < 12) entradas.add(0L);
        while (saidas.size()   < 12) saidas.add(0L);
    }
}