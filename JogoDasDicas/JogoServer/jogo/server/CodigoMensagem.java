package jogo.server;

/**
 *
 * @author Eduardo Amador Lucas
 * @author João Gilberto Heitor Gaiewski 
 * @version 1
 */
public enum CodigoMensagem {
    EQUIPE, // informa a equipe do cliente
    INICIAR_NOVA_PALAVRA, // informa inicio de ums nova palavra para adivinhar
    DICA, // dica para adivinhar palavra atual
    RESPOSTA_CERTA, // resposta enviada anteriormente está certa
    RESPOSTA_ERRADA, // resposta enviada anteriormente está errada
    ALGUEM_ACERTOU, // algum jogador acertou a resposta. Envia quem acertou, a palavra certa
    DESBLOQUEIA_JOGADOR, // desbloqueia jogador após tempo de bloquaio por errar resposta
    NINGUEM_ACERTOU, // ninguem acertou a palavra

    PONTOS, // atualiza pontos das equipes
    TERMINAR, // jogo terminado

    CHUTE, // CLIENTE - chutar resposta
    ERRO; // Erro de conversão
    
    public static String toString(CodigoMensagem codigo) {
        switch (codigo) {
        case EQUIPE:
                return "EQUIPE";
        case INICIAR_NOVA_PALAVRA:
                return "INICIAR_NOVA_PALAVRA";
        case DICA:
                return "DICA";
        case RESPOSTA_CERTA:
                return "RESPOSTA_CERTA";
        case RESPOSTA_ERRADA:
                return "RESPOSTA_ERRADA";
        case ALGUEM_ACERTOU:
                return "ALGUEM_ACERTOU";
        case DESBLOQUEIA_JOGADOR:
                return "DESBLOQUEIA_JOGADOR";
        case NINGUEM_ACERTOU:
                return "NINGUEM_ACERTOU";
        case PONTOS:
                return "PONTOS";
        case TERMINAR:
                return "TERMINAR";
        case CHUTE:
                return "CHUTE";
        default:
                return "ERRO";
        }
    }

    public static CodigoMensagem fromString(String nomeDoCodigo) {
        if (nomeDoCodigo.equals("EQUIPE")) 
                return EQUIPE;
        if (nomeDoCodigo.equals("INICIAR_NOVA_PALAVRA")) 
                return INICIAR_NOVA_PALAVRA;
        if (nomeDoCodigo.equals("DICA")) 
                return DICA;
        if (nomeDoCodigo.equals("RESPOSTA_CERTA")) 
                return RESPOSTA_CERTA;
        if (nomeDoCodigo.equals("RESPOSTA_ERRADA")) 
                return RESPOSTA_ERRADA;
        if (nomeDoCodigo.equals("ALGUEM_ACERTOU")) 
                return ALGUEM_ACERTOU;
        if (nomeDoCodigo.equals("DESBLOQUEIA_JOGADOR")) 
                return DESBLOQUEIA_JOGADOR;
        if (nomeDoCodigo.equals("NINGUEM_ACERTOU")) 
                return NINGUEM_ACERTOU;
        if (nomeDoCodigo.equals("PONTOS")) 
                return PONTOS;
        if (nomeDoCodigo.equals("TERMINAR")) 
                return TERMINAR;
        if (nomeDoCodigo.equals("CHUTE")) 
                return CHUTE;
        
        return ERRO;/*
        switch (nomeDoCodigo) {
        case "EQUIPE":
                return EQUIPE;
        case "INICIAR_NOVA_PALAVRA":
                return INICIAR_NOVA_PALAVRA;
        case "DICA":
                return DICA;
        case "RESPOSTA_CERTA":
                return RESPOSTA_CERTA;
        case "RESPOSTA_ERRADA":
                return RESPOSTA_ERRADA;
        case "ALGUEM_ACERTOU":
                return ALGUEM_ACERTOU;
        case DESBLOQUEIA_JOGADOR:
                return "DESBLOQUEIA_JOGADOR";
        case "NINGUEM_ACERTOU":
                return NINGUEM_ACERTOU;
        case "PONTOS":
                return PONTOS;
        case "TERMINAR":
                return TERMINAR;
        case "CHUTE":
                return CHUTE;
        default:
                return ERRO;
        }*/
    }
}
