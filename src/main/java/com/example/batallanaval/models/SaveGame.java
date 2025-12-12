package com.example.batallanaval.models;

import com.example.batallanaval.models.Board;
import com.example.batallanaval.models.MachineAI;

import java.io.*;

public class SaveGame {

    private static final String OBJ_NAME = "Batalla_naval_save.dat";
    private static final String META_FILE = "Batalla_naval_meta.txt";

    public static void saveGame(Board playerBoard, Board machineBoard, MachineAI ai, String nickName, int shipsSunk) {
        try (
            FileOutputStream fileOut = new FileOutputStream(OBJ_NAME);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            ){
            //para escribir el estado del juego en orden
            objectOut.writeObject(playerBoard);
            objectOut.writeObject(machineBoard);
            objectOut.writeObject(ai);
        } catch (IOException e){
          System.err.println("ERROR el juego no se pudo guardar"+ e.getMessage());
        }

        try (PrintWriter writer = new PrintWriter(META_FILE)){
            writer.println("nickName: " + nickName);
            writer.println("shipsSunk: " + shipsSunk);
        }catch (IOException e){
            System.err.println("ERROR al guardar los datos");
        }
        System.out.println("El juego fue guardado exitosamente");
    }

    public static Object[] restartGame(){
        try(
            FileInputStream fileInp = new FileInputStream(OBJ_NAME);
            ObjectInputStream objectInp = new ObjectInputStream(fileInp)
        ) {
            //Se cargan los archivo en el orden que fueron escritos
            Board playerBoard = (Board) objectInp.readObject();
            Board machineBoard = (Board) objectInp.readObject();
            MachineAI ai = (MachineAI) objectInp.readObject();

            System.out.println("El juego fue cargado exitosamente");
            return new Object[]{playerBoard, machineBoard, ai};
        } catch (FileNotFoundException e) {
            System.err.println("No se encontró el archivo archivo guardado, se inicia nuevo juego");
            return null;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("ERROR el juego no se pudo reiniciar"+ e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void deleteGame(){
        File objFile = new File(OBJ_NAME);
        File metaFile = new File(META_FILE);

        if (objFile.exists()) {
            if (objFile.delete()) {
                System.out.println("Partida guardada eliminada.");
            } else {
                System.err.println("Error al eliminar el archivo de objetos.");
            }
        }
        if (metaFile.exists()) {
            metaFile.delete();
        }
    }
}
