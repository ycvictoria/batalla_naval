package com.example.batallanaval.models;

public class PlayerData {

    private String nickname;
    private int sunkShips;
    private boolean placementPhase;

    public PlayerData(String nickname, int sunkShips, boolean placementPhase) {
        this.nickname = nickname;
        this.sunkShips = sunkShips;
        this.placementPhase = placementPhase;
    }

    public String getNickname() {
        return nickname;
    }

    public int getSunkShips() {
        return sunkShips;
    }

    public boolean isPlacementPhase() {
        return placementPhase;
    }
}
