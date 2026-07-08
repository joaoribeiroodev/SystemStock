package model;

public class SolicitacaoCompraModel {

    private int id;
    private int produtoId;
    private String codigoBarras;
    private String nomeProduto;
    private long quantidadeAtual;
    private long quantidadeMinima;
    private long quantidadeSugerida;
    private String status;
    private String observacao;
    private String criadoEm;
    private String atualizadoEm;

    /* ── id ── */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /* ── produtoId ── */
    public int getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(int produtoId) {
        this.produtoId = produtoId;
    }

    /* ── codigoBarras ── */
    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    /* ── nomeProduto ── */
    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    /* ── quantidadeAtual ── */
    public long getQuantidadeAtual() {
        return quantidadeAtual;
    }

    public void setQuantidadeAtual(long quantidadeAtual) {
        this.quantidadeAtual = quantidadeAtual;
    }

    /* ── quantidadeMinima ── */
    public long getQuantidadeMinima() {
        return quantidadeMinima;
    }

    public void setQuantidadeMinima(long quantidadeMinima) {
        this.quantidadeMinima = quantidadeMinima;
    }

    /* ── quantidadeSugerida ── */
    public long getQuantidadeSugerida() {
        return quantidadeSugerida;
    }

    public void setQuantidadeSugerida(long quantidadeSugerida) {
        this.quantidadeSugerida = quantidadeSugerida;
    }

    /* ── status ── */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /* ── observacao ── */
    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    /* ── criadoEm ── */
    public String getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(String criadoEm) {
        this.criadoEm = criadoEm;
    }

    /* ── atualizadoEm ── */
    public String getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(String atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
