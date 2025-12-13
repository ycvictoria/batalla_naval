package com.example.batallanaval.models;

import com.example.batallanaval.models.Ship;

/**
 * Implementación concreta de la factoría para la creación de Fragatas.
 * <p>
 * Una Fragata (Frigate) es un tipo de barco pequeño en el juego de Batalla Naval
 * con una longitud fija de 1 unidad. Esta clase sigue el patrón Factory Method.
 */
public class FrigateFactory extends ShipFactory {


    /**
     * Crea y devuelve una nueva instancia de Ship que representa una Fragata.
     * El barco creado siempre tendrá una longitud de 1.
     * @return Una nueva instancia de Ship con {@code length = 1}.
     */
    @Override
    public Ship createShip() {
        return new Ship(1);
    }
}