package com.example.batallanaval.models.ai;

import com.example.batallanaval.models.Board;
import com.example.batallanaval.models.Cell;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class HuntTargetStrategy implements AIStrategy {

    private final Random rand = new Random();
    private final Queue<int[]> queue = new LinkedList<>();

    @Override
    public int[] selectTarget(Board b) {

        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            int r = pos[0];
            int c = pos[1];

            if (r < 0 || c < 0 || r >= b.getSize() || c >= b.getSize()) continue;

            Cell cell = b.getCell(r, c);
            if (!cell.wasShot())
                return pos;
        }

        int r = rand.nextInt(b.getSize());
        int c = rand.nextInt(b.getSize());

        queue.offer(new int[]{r-1, c});
        queue.offer(new int[]{r+1, c});
        queue.offer(new int[]{r, c-1});
        queue.offer(new int[]{r, c+1});

        return new int[]{r, c};
    }
}
