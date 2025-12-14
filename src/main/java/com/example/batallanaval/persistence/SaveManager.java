package com.example.batallanaval.persistence;

import com.example.batallanaval.models.Board;
import com.example.batallanaval.models.PlayerData;

import java.io.*;

/**
 * Clase estática para gestionar la persistencia de datos del juego.
 * Maneja la serialización de los objetos Board y el guardado/carga
 * de información del jugador en archivos planos.
 */
public class SaveManager {

    private static final String SAVE_DIR = "save";

    /**
     * Serializa y guarda un objeto Board en un archivo.
     * @param board El objeto Board a guardar.
     * @param filename El nombre del archivo para la serialización (ej: "player_board.ser").
     */
    public static void saveBoard(Board board, String filename) {
        try {
            // Asegura que el directorio save exista.
            File dir = new File(SAVE_DIR);
            if (!dir.exists()) dir.mkdirs();
            ObjectOutputStream out =
                    new ObjectOutputStream(new FileOutputStream(SAVE_DIR + "/" + filename));
            System.out.println("📁 Guardando en: " + new File(filename).getAbsolutePath());
            out.writeObject(board);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Carga un objeto Board serializado desde un archivo.
     * @param filename El nombre del archivo serializado.
     * @return El objeto Board cargado o null si hay un error o el archivo no existe.
     */
    public static Board loadBoard(String filename) {
        try {
            ObjectInputStream in =
                    new ObjectInputStream(new FileInputStream(SAVE_DIR + "/" + filename));
            System.out.println("📁 Leyendo en: " + new File(filename).getAbsolutePath());
            Board board = (Board) in.readObject();
            System.out.println("board: "+board );
            in.close();
            return board;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Guarda la información esencial del jugador en un archivo de texto plano.
     * @param nickname El apodo del jugador.
     * @param sunkShips El número de barcos hundidos.
     * @param placementPhase Si el juego está en fase de colocación.
     */
    public static void savePlayerInfo(String nickname,
                                      int sunkShips,
                                      boolean placementPhase) {

        File file = new File(SAVE_DIR, "player.txt");

        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println(nickname);
            pw.println(sunkShips);
            pw.println(placementPhase);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Carga la información del jugador desde un archivo de texto plano.
     * @return Un objeto PlayerData con la información cargada o null sí falla.
     */
    public static PlayerData loadPlayerInfo() {

        File file = new File(SAVE_DIR, "player.txt");
        if (!file.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String nickname = br.readLine();
            int sunkShips = Integer.parseInt(br.readLine());
            boolean placementPhase = Boolean.parseBoolean(br.readLine());

            return new PlayerData(nickname, sunkShips, placementPhase);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Elimina todos los archivos de guardado al finalizar una partida.
     */
    public static void deleteSaves() {
        try {
            File pBoard = new File(SAVE_DIR + "/player_board.ser");
            File mBoard = new File(SAVE_DIR + "/machine_board.ser");
            File pInfo = new File(SAVE_DIR + "/player.txt");

            if (pBoard.exists()) pBoard.delete();
            if (mBoard.exists()) mBoard.delete();
            if (pInfo.exists()) pInfo.delete();

            System.out.println("🗑️ Archivos de guardado eliminados tras finalizar la partida.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
