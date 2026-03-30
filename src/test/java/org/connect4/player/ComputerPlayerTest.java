package org.connect4.player;

import org.connect4.model.Board;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComputerPlayerAITest {

    @Test
    void shouldPlayWinningMoveWhenAvailable() {
        Board board = new Board(6, 7);
        ComputerPlayer ai = new ComputerPlayer("AI", "Piros");
        HumanPlayer opponent = new HumanPlayer("Játékos", "Sárga");

        board.dropDisc(ai, 0);
        board.dropDisc(ai, 1);
        board.dropDisc(ai, 2);

        int column = ai.makeMove(board, opponent.getColor());

        assertEquals(3, column);
    }

    @Test
    void shouldBlockOpponentWinningMove() {
        Board board = new Board(6, 7);
        ComputerPlayer ai = new ComputerPlayer("AI", "Piros");
        HumanPlayer opponent = new HumanPlayer("Játékos", "Sárga");

        board.dropDisc(opponent, 0);
        board.dropDisc(opponent, 1);
        board.dropDisc(opponent, 2);

        int column = ai.makeMove(board, opponent.getColor());

        assertEquals(3, column);
    }

    @Test
    void shouldPreferCenterColumnWhenNoThreatOrWin() {
        Board board = new Board(6, 7);
        ComputerPlayer ai = new ComputerPlayer("AI", "Piros");

        int column = ai.makeMove(board, "Sárga");

        assertEquals(3, column);
    }


}
