package com.example.batallanaval.persistence;

import com.example.batallanaval.models.Board;
import com.example.batallanaval.models.PlayerData;

import java.io.*;

public class SaveManager {

    private static final String SAVE_DIR = "save";

    // ==========================
    // SERIALIZACIÓN TABLEROS
    // ==========================
    public static void saveBoard(Board board, String filename) {
        try {
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

    // ==========================
    // ARCHIVO PLANO JUGADOR
    // ==========================
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

    // ==========================
    // BORRAR PARTIDA (GAME OVER)
    // ==========================
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
