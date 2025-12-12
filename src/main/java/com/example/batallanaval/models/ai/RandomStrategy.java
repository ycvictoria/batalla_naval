package com.example.batallanaval.models.ai;

import com.example.batallanaval.models.Board;

import java.util.Random;

public class RandomStrategy implements AIStrategy {

    private final Random r = new Random();

    @Override
    public int[] selectTarget(Board board) {
        int size = board.getSize();
        return new int[]{ r.nextInt(size), r.nextInt(size) };
    }
}
