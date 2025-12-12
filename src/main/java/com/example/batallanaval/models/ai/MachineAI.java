package com.example.batallanaval.models.ai;

import com.example.batallanaval.models.Board;

public class MachineAI {

    private AIStrategy strategy;

    public MachineAI(AIStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(AIStrategy strategy) {
        this.strategy = strategy;
    }

    public int[] shoot(Board board) {
        return strategy.selectTarget(board);
    }
}
