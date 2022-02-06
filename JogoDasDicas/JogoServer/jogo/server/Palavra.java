package jogo.server;

import java.util.ArrayList;

/**
 *
 * @author Eduardo Amador Lucas
 * @author João Gilberto Heitor Gaiewski 
 * @version 1
 */
public class Palavra {
    private String palavra;
    private ArrayList<String> dicas = new ArrayList<String>();
    private int dicaAtual = 0;
    private int pontos = 10;

    public Palavra(String texto) {
        palavra = texto;
    }

    public Palavra(String texto, String dica1, String dica2, String dica3) {
        palavra = texto;
        dicas.add(dica1);
        dicas.add(dica2);
        dicas.add(dica3);
        //this.pontos = 10;
    }
    
    public Palavra(String texto, int pontos, String dica1, String dica2, String dica3) {
        palavra = texto;
        dicas.add(dica1);
        dicas.add(dica2);
        dicas.add(dica3);
        this.pontos = pontos;
    }
    
    public void addDica(String novaDica) {
        dicas.add(novaDica);
    }
    
    public void setPontos(int pts) {
        pontos = pts;
    }

    public boolean respostaEstaCerta(String resposta) {
        return (palavra.equals(resposta));
    }

    public String getDica() {
        String dica = null;
        if (dicaAtual < dicas.size()) {
            dica = dicas.get(dicaAtual);
            dicaAtual++;
        }
        return dica;
    }

    public String getDica(int dica) {
        if (dica < dicas.size()) {
            return dicas.get(dica);
        } else {
            return null;
        }
    }

    public int getDicaAtual() {
        return dicaAtual;
    }

    public boolean temMaisDicas() {
        return getDicaAtual() < dicas.size();
    }
    public int getNumDicas() {
        return dicas.size();
    }

    public String getPalavra() {
        return palavra;
    }
    
    public int getPontos() {
        return pontos;
    }
}
