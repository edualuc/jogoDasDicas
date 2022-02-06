package jogo.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author Eduardo Amador Lucas
 * @author João Gilberto Heitor Gaiewski 
 * @version 1
 */
public class ClienteConectado implements Runnable {
    private Thread cliente = new Thread(this);
    
    private Servidor servidor;
    public Socket socket;
    private DataOutputStream ostream;
    private DataInputStream istream;

    public String respostaEnviada;
    
    public final int equipe; // equipe não pode trocar
    public final int numeroDoJogador; // equipe não pode trocar

    public ClienteConectado(Servidor servidor, Socket socketAceito, int equipeAtual, int numeroDoJogador) throws IOException {
        this.servidor = servidor;
        this.socket = socketAceito;
        ostream = new DataOutputStream(socket.getOutputStream());
        istream = new DataInputStream(socket.getInputStream());
        
        this.equipe = equipeAtual;
        this.numeroDoJogador = numeroDoJogador;
    }

    @Override
    public void run() {
        CodigoMensagem codigo;
        boolean continuar = true;
        
        informaEquipe();
        
        while(continuar) {
            try {
                codigo = CodigoMensagem.fromString(istream.readUTF());
                
                switch (codigo) {
                    case CHUTE:
                        respostaEnviada = istream.readUTF();
                        System.out.println("Resposta enviada: " + respostaEnviada + ". Jogador: " + String.valueOf(numeroDoJogador));
                        servidor.respostaEnviada(respostaEnviada, equipe, numeroDoJogador);
                        break;
                    default:
                        System.out.println("codigo do cliente inesperado.");
						System.out.println(codigo);
                }
                
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
                continuar = false;

                try {
                    socket.close();
                } 
                catch (IOException exc) { System.err.println(exc); }
            }
        }
    }

    public void enviarMensagem(String stringParaEnviar) {
        try {
            ostream.writeUTF(stringParaEnviar);
            ostream.flush();
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
    
    public void enviarMensagem(CodigoMensagem codigoParaEnviar) {
        enviarMensagem(CodigoMensagem.toString(codigoParaEnviar));
    }

    public void enviarMensagem(int inteiroParaEnviar) {
        try {
            ostream.writeInt(inteiroParaEnviar);
            ostream.flush();
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
    
    public void enviarMensagem(CodigoMensagem code, String text) {
        enviarMensagem(code);
        enviarMensagem(text);
    }

    // Start Thread cliente
    public void start(){
        cliente.start();
    }

    // Fecha a conexao com o socket
    void close() throws IOException {
        socket.close();
    }

    private void informaEquipe() {
        enviarMensagem(CodigoMensagem.EQUIPE);
        enviarMensagem(equipe);
    }

    public int getEquipe() {
        return this.equipe;
    }
}
