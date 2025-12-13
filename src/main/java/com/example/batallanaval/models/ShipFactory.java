
package com.example.batallanaval.models;


/**
 * Esta clase es responsable de definir el método abstracto para crear
 * objetos Ship. Las subclases concretas implementarán este método
 * para crear barcos de diferentes longitudes o tipos.
 */
public abstract class ShipFactory {

    /**
     * Método Factory abstracto para crear un objeto Ship.
     * @return Una nueva instancia de Ship.
     */
    public abstract Ship createShip();
}

