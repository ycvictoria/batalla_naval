package com.example.batallanaval.models;

/**
 * Clase que almacena los datos de estado de un jugador en la partida de Batalla Naval.
 */
public class PlayerData {

    private String nickname;
    private int sunkShips;
    private boolean placementPhase;

    /**
     * Constructor para inicializar los datos del jugador.
     * @param nickname Nombre o alias del jugador.
     * @param sunkShips Cantidad inicial de barcos hundidos del oponente.
     * @param placementPhase Indica si el jugador está actualmente en la fase de colocación de barcos.
     */
    public PlayerData(String nickname, int sunkShips, boolean placementPhase) {
        this.nickname = nickname;
        this.sunkShips = sunkShips;
        this.placementPhase = placementPhase;
    }

    /**
     * Obtiene el nombre del jugador.
     * @return El nickname del jugador.
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Obtiene la cantidad de barcos del oponente que han sido hundidos por este jugador.
     * @return El contador de barcos hundidos.
     */
    public int getSunkShips() {
        return sunkShips;
    }

    /**
     * Verifica si el jugador está en la fase de colocación de barcos.
     * @return true si está en la fase de colocación; false en la fase de batalla.
     */
    public boolean isPlacementPhase() {
        return placementPhase;
    }
}
