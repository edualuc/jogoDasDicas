package jogo.server;

/**
 *
 * @author Eduardo Amador Lucas
 * @author João Gilberto Heitor Gaiewski 
 * @version 1
 */
public enum EstadoServidor {
    DESLIGADO,
    RECEBENDO_CONEXOES,
    ESPERANDO_RESPOSTA,
    TEMPO_PARA_NOVA_PALAVRA,
    JOGO_TERMINOU;
}
