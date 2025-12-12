package com.example.batallanaval.models.ai;

import com.example.batallanaval.models.Board;

public interface AIStrategy {
    int[] selectTarget(Board playerBoard);
}
