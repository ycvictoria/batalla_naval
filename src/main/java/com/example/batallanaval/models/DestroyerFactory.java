package com.example.batallanaval.models;

/**
 * Implementación concreta de la factoría para la creación de Destructores.
 * <p>
 * Un Destructor (Destroyer) es un tipo de barco en el juego de Batalla Naval
 * con una longitud fija de 2 unidades. Esta clase sigue el patrón Factory Method.
 */
public class DestroyerFactory extends ShipFactory {

    /**
     * Crea y devuelve una nueva instancia de Ship que representa un Destructor.
     * El barco creado siempre tendrá una longitud de 2.
     * @return Una nueva instancia de Ship con {@code length = 2}.
     */
    @Override
    public Ship createShip() {
        return new Ship(2);
    }
}
