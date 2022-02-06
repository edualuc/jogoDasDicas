
package jogo.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduardo Amador Lucas
 * @author João Gilberto Heitor Gaiewski 
 * @version 1
 */
public class Cliente implements Runnable {
    private String host = "";
    private final int port = 4956;
    private Socket socket;
    private DataOutputStream ostream;
    private DataInputStream istream;
    
    String dicaAtual = "";
    int count = 0;
    private boolean conectado = false;
    private String pontos;
    private int equipeAtual;
    private JogoImagemAcaoCliente tela;
    
    private final int segundoDeBloqueio = 5;

    public Cliente(String hostParaConectar, JogoImagemAcaoCliente tela) {
        this.tela = tela;
        host = hostParaConectar;
    }
    
    /**
     * @param args the command line arguments
     */
    public boolean conectarAoServidor( )
    {
        try
        {
            tela.setStatus("Abrindo o socket e criando o stream.");

            socket = new Socket(host, port);

            ostream = new DataOutputStream(socket.getOutputStream());
            istream = new DataInputStream(socket.getInputStream());

            tela.setStatus("Conectado ao Servidor");
        }
        catch(Exception e)
        {
            System.err.println(e);
            return false;
        }
        return true;
    }

    public void enviarMensagem(CodigoMensagem codigo) {
        try {
            ostream.writeUTF(codigo.toString());
            ostream.flush();
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
    
    public void enviarMensagem(String textSend) {
        try {
            ostream.writeUTF(textSend);
            ostream.flush();
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    public void enviarMensagem(int inteiroSend) {
        try {
            ostream.writeInt(inteiroSend);
            ostream.flush();
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    private void escutarServidor() {
        String quemAcertou, equipeAcertou, palavraCorreta, msgFimDeJogo;
        CodigoMensagem codigo;
        boolean fimDeJogo = false;

        while (!fimDeJogo) {
            try {
                //System.out.println("antes");
                String mensagemServidor = istream.readUTF();
                //codigo = CodigoMensagem.fromString(mensagemServidor);
                codigo = CodigoMensagem.fromString(mensagemServidor);
                System.out.println(mensagemServidor);

                switch (codigo) {
                    case EQUIPE:
                        equipeAtual = istream.readInt();
                        atualizarStatus(mensagemEquipe());
                        informaEquipe();
                        break;
                    case INICIAR_NOVA_PALAVRA:
                        atualizarStatus(mensagemNovaPalavra());
                        limparDicas();
                    case DICA:
                        dicaAtual = istream.readUTF();
                        addNovaDica(dicaAtual);
                        break;
                    case RESPOSTA_ERRADA: // resposta enviada anteriormente está errada
                        atualizarStatus(mensagemRespostaErrada());
                        bloquearEnvioTemporariamente();
                        break;
                    case RESPOSTA_CERTA: // resposta enviada anteriormente está certa
                    case ALGUEM_ACERTOU: // algum jogador acertou a resposta. Envia quem acertou, a palavra certa
                        quemAcertou = istream.readUTF();
                        equipeAcertou = istream.readUTF();
                        palavraCorreta = istream.readUTF();
                        pontos = istream.readUTF();
                        atualizarStatus(mensagemAlguemAcertou(quemAcertou, equipeAcertou, palavraCorreta));
                        atualizarPontos();
                        limparDicas();
                        break;
                    case DESBLOQUEIA_JOGADOR:
                        System.out.println("Desbloqueio");
                        desbloquearEnvioTemporareamente();
                        break;
                    case NINGUEM_ACERTOU:
                        palavraCorreta = istream.readUTF();
                        atualizarStatus(mensagemNinguemAcertou(palavraCorreta));
                        break;
                    case TERMINAR:
                        System.out.println("Encerrando cliente.");
                        msgFimDeJogo = istream.readUTF();
                        atualizarStatus(msgFimDeJogo);
                        fimDeJogo = true;
                        break;

                    default:
                        System.out.println("codigo do servidor inesperado.");
                }

                count++;
                Thread.sleep(500);
            } catch (Exception ex) {
                System.err.println(ex);
                Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
       
    }

    @Override
    public void run() {
        if (! conectado) {
            conectado = this.conectarAoServidor();
        }
        if (conectado) {
            this.escutarServidor();
        }
    }

    private String mensagemEquipe() {
        return "A sua equipe é " + String.valueOf(equipeAtual);
    }

    private String mensagemNovaPalavra() {
        return "Adivinhe a palavra!";
    }

    private String mensagemRespostaErrada() {
        return "Seu grupo errou. E será bloqueado por 5s.";
    }

    private String mensagemAlguemAcertou(String quemAcertou, String equipeAcertou, String palavraCorreta) {
        return "Acertou. Quem: " + quemAcertou + ". Equipe: " + equipeAcertou + ". Palavra era: " + palavraCorreta;
    }

    private String mensagemNinguemAcertou(String palavraCorreta) {
        return "Ninguem acertou a palavra. A palavra era: " + palavraCorreta;
    }
    
    private void atualizarStatus(String mensagemStatus) {
        System.out.println(mensagemStatus);
        tela.setStatus(mensagemStatus);
    }

    private void addNovaDica(String dicaAtual) {
        tela.addDica(dicaAtual);
    }

    private void bloquearEnvioTemporariamente() {
        tela.bloqueiaEnvio();
    }

    private void desbloquearEnvioTemporareamente() {
        tela.habilitaEnvio();
    }

    private void atualizarPontos() {
        tela.setPontos(pontos);
    }
    
    private void informaEquipe() {
        tela.setEquipe(equipeAtual);
    }

    private void limparDicas() {
        tela.clearDica();
    }

    void enviarPalpite(String palpite) {
        enviarMensagem(CodigoMensagem.CHUTE);
        enviarMensagem(palpite);
    }
    
}
