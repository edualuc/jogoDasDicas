package jogo.server;

import java.awt.Label;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduardo Amador Lucas
 * @author João Gilberto Heitor Gaiewski 
 * @version 1
 */
public class Servidor implements Runnable {
    private EstadoServidor estado = EstadoServidor.DESLIGADO;

    private ServerSocket serverSocket;
    private final int port = 4956;
    
    private Label labelStatus;
    
    private ArrayList<ClienteConectado> clientesConectados;
    
    private int qtdJogadores;
    private int qtdTimes;
    private int numeroDoJogador = 0;
        
    private final int TEMPO_NOVA_PALAVRA = 10;
    private final int TEMPO_NOVA_DICA = 15;
    private final int TEMPO_DE_BLOQUEO = 5;
   
    private ArrayList<Palavra> palavras;
    private Palavra palavraAtual;
    private Thread threadEnviarDicas;
    private ArrayList<Boolean> equipesBloqueadas;
    private int segundosNoEstadoPalavra = 0;
    private int segundosNoEstadoDicas;
    
    private final int PONTOS_PARA_VENCER = 100;
    private String pontos = "ponto aqui";
    private int[] placar;
    
    
    public Servidor(Label labelStatus_param, int times, int jogadores) {
        labelStatus = labelStatus_param;
        clientesConectados = new ArrayList<ClienteConectado>();
        qtdJogadores = jogadores;
        qtdTimes = times;
        placar = new int[times];
        equipesBloqueadas = new ArrayList<Boolean>();
        for (int indice = 0 ; indice < qtdTimes; indice++) {
            equipesBloqueadas.add(Boolean.FALSE);
            placar[indice] = 0;
        }
        initPalavras();
    }
    
    public void bloqueiaEquipe(int equipe) {
        this.setBloqueioEquipe(equipe, true);
    }
    
    public void desbloqueiaEquipe(int equipe) {
        this.setBloqueioEquipe(equipe, false);
        for (ClienteConectado clienteConectado : clientesConectados) {
            if (clienteConectado.equipe == equipe) {
                clienteConectado.enviarMensagem(CodigoMensagem.DESBLOQUEIA_JOGADOR);
            }
        }
    }
    
    private void setBloqueioEquipe(int equipe, boolean b) {
        this.equipesBloqueadas.set(equipe, b);
    }
    
    private boolean equipeBloqueada(int equipe) {
        if(equipe >= 0 && equipe < this.equipesBloqueadas.size()) {
            return this.equipesBloqueadas.get(equipe);
        } else {
            System.out.println("Equipe: Index out of bounds");
            return false;
        }
    }
    
    public void bloquearEquipe(int equipeBloqueada) {
        try {
            bloqueiaEquipe(equipeBloqueada);
            Thread.sleep(TEMPO_DE_BLOQUEO * 1000);
            desbloqueiaEquipe(equipeBloqueada);
        } catch (InterruptedException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private int totalDeJogadores() {
        return qtdJogadores * qtdTimes;
    }
    
    private int equipeDoJogador(int jogador) {
        return jogador % qtdTimes;
    }

    @Override
    public void run() {
        try {
            if ( this.isEstado(EstadoServidor.DESLIGADO) ) {
                criarServidor();
            } 
            if ( this.isEstado(EstadoServidor.RECEBENDO_CONEXOES) ) {
                escutarConexoes();
            } 
            if ( this.isEstado(EstadoServidor.ESPERANDO_RESPOSTA) ) {
                controlarEnvioDeDicas();
            }
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println(ex);
            System.err.println(ex.getStackTrace());
        }
    }
        
    private void criarServidor() throws IOException {
        serverSocket = new ServerSocket(port);
        setEstado(EstadoServidor.RECEBENDO_CONEXOES);
    }

    private void escutarConexoes() throws IOException {
        while ( isEstado(EstadoServidor.RECEBENDO_CONEXOES) && numeroDoJogador < totalDeJogadores())
        { 
            System.out.println("Aguardando uma conexão cliente");
            labelStatus.setText("Aguardando conexão do cliente " + String.valueOf(numeroDoJogador+1));
            
            Socket socket = serverSocket.accept();
            if( isEstado(EstadoServidor.RECEBENDO_CONEXOES) ) {
                clientesConectados.add(new ClienteConectado(this, socket, equipeDoJogador(numeroDoJogador), numeroDoJogador));
                clientesConectados.get(numeroDoJogador).start();
                numeroDoJogador++;
            }
            labelStatus.setText("Conectado cliente " + String.valueOf(numeroDoJogador+1));
        }
        System.out.println("Sem mais conexões mais clientes!");
    }
    
    private void controlarEnvioDeDicas() {
        do {
            try {
                Thread.sleep(1000);
                if (isEstado(EstadoServidor.JOGO_TERMINOU)) {
                    break;
                } else if (isEstado(EstadoServidor.ESPERANDO_RESPOSTA)) {
                    if (segundosNoEstadoDicas >= TEMPO_NOVA_DICA) {
                        segundosNoEstadoDicas = 0;
                        if (palavraAtual.temMaisDicas()) {
                            enviarProximaDica(CodigoMensagem.DICA);
                        } else {
                            setEstado(EstadoServidor.TEMPO_PARA_NOVA_PALAVRA);
                            ninguemAcertou(palavraAtual.getPalavra());
                        }
                    } else {
                        segundosNoEstadoDicas++;
                    }
                    
                } else if (isEstado(EstadoServidor.TEMPO_PARA_NOVA_PALAVRA)) {
                    if (segundosNoEstadoPalavra >= TEMPO_NOVA_PALAVRA) {
                        segundosNoEstadoPalavra = 0;
                        segundosNoEstadoDicas = 0;
                        novaPalavra();
                        setEstado(EstadoServidor.ESPERANDO_RESPOSTA);
                    } else {
                        segundosNoEstadoPalavra++;
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            } 
        } while( ! isEstado(EstadoServidor.JOGO_TERMINOU) );
        fimDeJogo();
    }

    public void iniciarJogo() {
        int minJogadores = totalDeJogadores() - qtdJogadores + 1;
        minJogadores = (minJogadores >= qtdTimes)? minJogadores: qtdTimes;
        
        System.out.println("Minimo de jogadores: " + String.valueOf(minJogadores) + ". Quantidade Atual: " + String.valueOf(numeroDoJogador));
        if( minJogadores <= numeroDoJogador ) {
            setEstado(EstadoServidor.ESPERANDO_RESPOSTA);
            labelStatus.setText("O jogo começou!");
            novaPalavra();
            iniciarThreadDeControle();
        } else {
           labelStatus.setText("Mínimo de jogadores ("+String.valueOf(minJogadores)+") não atingido.");
        }
    }
    
    private void novaPalavra() {
        palavraAtual = sortearPalavra();
        enviarProximaDica(CodigoMensagem.INICIAR_NOVA_PALAVRA);
    }

    private Palavra sortearPalavra() {
        if (palavras.size() <= 0) {
            initPalavras();
        }
        int sort = new Random().nextInt(palavras.size());
        return palavras.remove(sort);
    }

    private void enviarProximaDica(CodigoMensagem estadoDoServidor) {
        String dicaAtual = palavraAtual.getDica();
        System.out.println(dicaAtual);
        for (ClienteConectado clienteConectado : clientesConectados) {
            clienteConectado.enviarMensagem(estadoDoServidor, dicaAtual);
        }
    }

    private void initPalavras() {
        palavras = new ArrayList<Palavra>();
        
        palavras.add(new Palavra("queijo", "comida", "rato", "laticínio"));
        palavras.add(new Palavra("celular", 30, "aparelho", "dígitos", "ligar"));
        palavras.add(new Palavra("cadeira", "sentar", "objeto", "encosto"));
        palavras.add(new Palavra("ovos", 30, "galinha", "plural", "casca"));
        palavras.add(new Palavra("girafa", 20, "animal", "áfrica", "alto"));
    }

    private synchronized void setEstado(EstadoServidor estadoServidor) {
        if (estado != EstadoServidor.JOGO_TERMINOU) {
            estado = estadoServidor;
        } else {
            if (estadoServidor == EstadoServidor.RECEBENDO_CONEXOES ||
                    estadoServidor == EstadoServidor.DESLIGADO ) {
                estado = estadoServidor;
            }
        }
    }

    private synchronized boolean isEstado(EstadoServidor estadoServidor) {
        return estado == estadoServidor;
    }

    private void iniciarThreadDeControle() {
        threadEnviarDicas = new Thread(this);
        threadEnviarDicas.start();
    }

    synchronized void respostaEnviada(String respostaEnviada, final int equipe, int jogador) {
        if (isEstado(EstadoServidor.ESPERANDO_RESPOSTA)) {
            // Controle de bloqueio da equipe
            if(!this.equipeBloqueada(equipe)) {
                if (palavraAtual.respostaEstaCerta(respostaEnviada)) {
                    setEstado(EstadoServidor.TEMPO_PARA_NOVA_PALAVRA);
                    alguemAcertou(respostaEnviada, equipe, jogador);
                    if(equipeVenceu(equipe)) {
                        setEstado(EstadoServidor.JOGO_TERMINOU);
                    }
                } else {
                    alguemErrou(respostaEnviada, equipe, jogador);
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            bloquearEquipe(equipe);
                        }
                    };
                    new Thread(r).start();
                }
            } else {
                // DESCARTA RESPOSTA, pois a equipe estava bloqueada
            }
        }
    }

    private void alguemAcertou(String respostaEnviada, int equipe, int jogador) {
        // Soma pontos
        somaPontos(equipe, palavraAtual.getPontos());
        
        setEstado(EstadoServidor.TEMPO_PARA_NOVA_PALAVRA);
        
        for (ClienteConectado clienteConectado : clientesConectados) {
            if (clienteConectado.numeroDoJogador == jogador) {
                clienteConectado.enviarMensagem(CodigoMensagem.RESPOSTA_CERTA);
            } else {
                clienteConectado.enviarMensagem(CodigoMensagem.ALGUEM_ACERTOU);
            }
            clienteConectado.enviarMensagem(String.valueOf(jogador));
            clienteConectado.enviarMensagem(String.valueOf(equipe));
            clienteConectado.enviarMensagem(respostaEnviada);
            pontos = String.valueOf(this.getPontos(clienteConectado.getEquipe()));
            clienteConectado.enviarMensagem(pontos);
        }
    }

    private void alguemErrou(String respostaEnviada, int equipe, int jogador) {
        for (ClienteConectado clienteConectado : clientesConectados) {
            if (clienteConectado.equipe == equipe) {
                clienteConectado.enviarMensagem(CodigoMensagem.RESPOSTA_ERRADA);
            }
        }
    }

    private void ninguemAcertou(String palavraCerta) {
        if(isEstado(EstadoServidor.TEMPO_PARA_NOVA_PALAVRA)) {
            for (ClienteConectado clienteConectado : clientesConectados) {
                clienteConectado.enviarMensagem(CodigoMensagem.NINGUEM_ACERTOU, palavraCerta);
            }
        }
    }

    private void somaPontos(int equipe, int pts) {
        this.placar[equipe] += pts;
    }
    
    private int getPontos(int equipe) {
        return this.placar[equipe];
    }
    
    private boolean equipeVenceu(int equipe) {
        return placar[equipe] >= PONTOS_PARA_VENCER;
    }
    
    private int getEquipeVencedora() {
        int vencedor = 0;
        for(int indice = 1; indice < (this.placar.length - 1); indice++) {
            if(placar[vencedor] < placar[indice]) {
                vencedor = indice;
            }
        }
        return vencedor;
    }    
    
    private String mensagemFimDeJogo() {
        String msg = "Fim de jogo!";
        msg += " Equipe vencedora: " + String.valueOf(getEquipeVencedora());
        return msg;
    }
    
    private void fimDeJogo() {
        String msgFim;
        if(isEstado(EstadoServidor.JOGO_TERMINOU)) {
            msgFim = this.mensagemFimDeJogo();
            labelStatus.setText(msgFim);
            for (ClienteConectado clienteConectado : clientesConectados) {
                clienteConectado.enviarMensagem(CodigoMensagem.TERMINAR);
                clienteConectado.enviarMensagem(msgFim);
            }
        }
    }
}
