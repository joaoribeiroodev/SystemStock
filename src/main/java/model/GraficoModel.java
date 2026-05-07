package model;

import java.util.ArrayList;
import java.util.List;

public class GraficoModel {
    private List<Long> entradas = new ArrayList<>();
    private List<Long> saidas = new ArrayList<>();

    public List<Long> getEntradas() { return entradas; }
    public List<Long> getSaidas() { return saidas; }
}