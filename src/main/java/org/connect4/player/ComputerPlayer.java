package org.connect4.player;


import org.connect4.model.Board;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * A ComputerPlayer osztály, amely a játékosok gépi vezérlését
 * reprezentálja a Connect 4 játékban.
 */
public final class ComputerPlayer implements Player {

    /** A győzelemhez szükséges korongok száma. */
    private static final int CONNECT_WIN_COUNT = 4;

    /** A minimax keresési mélysége. */
    private static final int SEARCH_DEPTH = 5;

    /** Nagy pozitív pontszám egy nyerő állásra. */
    private static final int WIN_SCORE = 1_000_000;

    /** A játékos neve. */
    private final String playerName;

    /** A játékos színe. */
    private final String playerColor;

    /**
     * Konstruktor, amely inicializálja a játékos nevét és színét.
     *
     * @param name  A játékos neve.
     * @param color A játékos színe, 'sárga' vagy 'piros' kell legyen.
     * @throws IllegalArgumentException Ha a szín nem érvényes.
     */
    public ComputerPlayer(final String name, final String color) {
        if (!color.equals("Sárga") && !color.equals("Piros")) {
            throw new IllegalArgumentException(
                    "A színnek 'Sárga' vagy 'Piros'-nak kell lennie."
            );
        }
        this.playerName = name;
        this.playerColor = color;
    }

    /**
     * Visszaadja a játékos nevét.
     *
     * @return A játékos neve.
     */
    @Override
    public String getName() {
        return playerName;
    }

    /**
     * Visszaadja a játékos színét.
     *
     * @return A játékos színe.
     */
    @Override
    public String getColor() {
        return playerColor;
    }

    /**
     * Visszaad egy oszlop számot, ahol a gép lehelyezi a korongot.
     * Random módon választ egy oszlopot.
     *
     * @param maxColumns A maximális oszlopszám, ahova a gép
     *                   lehelyezheti a korongot.
     * @return A gép által választott oszlop száma.
     */
    @Override
    public int makeMove(final int maxColumns) {
        return maxColumns / 2;
    }

    /**
     * AI lépésválasztás minimax + alfa-béta vágás stratégiával.
     *
     * @param board A játék aktuális táblája.
     * @param opponentColor Az ellenfél színe.
     * @return A kiválasztott oszlop indexe.
     */
    public int makeMove(final Board board, final String opponentColor) {
        final char aiToken = 'A';
        final char opponentToken = 'O';
        char[][] state = toState(board, playerColor, opponentColor);

        int immediateWin = findImmediateWinningMove(state, aiToken);
        if (immediateWin != -1) {
            return immediateWin;
        }

        int immediateBlock = findImmediateWinningMove(state, opponentToken);
        if (immediateBlock != -1) {
            return immediateBlock;
        }

        int bestColumn = -1;
        int bestScore = Integer.MIN_VALUE;
        int center = board.getColumns() / 2;

        for (int column : getOrderedValidColumns(state)) {
            char[][] child = copyState(state);
            int row = getAvailableRow(child, column);
            child[row][column] = aiToken;

            int score = minimax(
                    child,
                    SEARCH_DEPTH - 1,
                    false,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE,
                    aiToken,
                    opponentToken
            );

            if (score > bestScore
                    || (score == bestScore
                    && Math.abs(column - center) < Math.abs(bestColumn - center))) {
                bestScore = score;
                bestColumn = column;
            }
        }

        return bestColumn != -1 ? bestColumn : makeMove(board.getColumns());
    }

    private int minimax(
            final char[][] state,
            final int depth,
            final boolean maximizing,
            final int alpha,
            final int beta,
            final char aiToken,
            final char opponentToken
    ) {
        int localAlpha = alpha;
        int localBeta = beta;

        boolean aiWin = checkWin(state, aiToken);
        boolean opponentWin = checkWin(state, opponentToken);
        List<Integer> validColumns = getOrderedValidColumns(state);

        if (depth == 0 || aiWin || opponentWin || validColumns.isEmpty()) {
            if (aiWin) {
                return WIN_SCORE + depth;
            }
            if (opponentWin) {
                return -WIN_SCORE - depth;
            }
            return evaluatePosition(state, aiToken, opponentToken);
        }

        if (maximizing) {
            int value = Integer.MIN_VALUE;
            for (int column : validColumns) {
                char[][] child = copyState(state);
                int row = getAvailableRow(child, column);
                child[row][column] = aiToken;

                value = Math.max(value, minimax(child, depth - 1, false,
                        localAlpha, localBeta, aiToken, opponentToken));
                localAlpha = Math.max(localAlpha, value);
                if (localAlpha >= localBeta) {
                    break;
                }
            }
            return value;
        }

        int value = Integer.MAX_VALUE;
        for (int column : validColumns) {
            char[][] child = copyState(state);
            int row = getAvailableRow(child, column);
            child[row][column] = opponentToken;

            value = Math.min(value, minimax(child, depth - 1, true,
                    localAlpha, localBeta, aiToken, opponentToken));
            localBeta = Math.min(localBeta, value);
            if (localAlpha >= localBeta) {
                break;
            }
        }
        return value;
    }

    private int evaluatePosition(
            final char[][] state,
            final char aiToken,
            final char opponentToken
    ) {
        int score = 0;
        int center = state[0].length / 2;

        for (char[] row : state) {
            if (row[center] == aiToken) {
                score += 4;
            } else if (row[center] == opponentToken) {
                score -= 4;
            }
        }

        score += evaluateWindows(state, aiToken, opponentToken, 1, 0);
        score += evaluateWindows(state, aiToken, opponentToken, 0, 1);
        score += evaluateWindows(state, aiToken, opponentToken, 1, 1);
        score += evaluateWindows(state, aiToken, opponentToken, 1, -1);

        return score;
    }

    private int evaluateWindows(
            final char[][] state,
            final char aiToken,
            final char opponentToken,
            final int rowStep,
            final int colStep
    ) {
        int total = 0;
        int rows = state.length;
        int cols = state[0].length;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int endRow = row + (CONNECT_WIN_COUNT - 1) * rowStep;
                int endCol = col + (CONNECT_WIN_COUNT - 1) * colStep;
                if (endRow < 0 || endRow >= rows || endCol < 0 || endCol >= cols) {
                    continue;
                }
                total += evaluateWindow(state, row, col, aiToken, opponentToken,
                        rowStep, colStep);
            }
        }
        return total;
    }

    private int evaluateWindow(
            final char[][] state,
            final int row,
            final int col,
            final char aiToken,
            final char opponentToken,
            final int rowStep,
            final int colStep
    ) {
        int aiCount = 0;
        int opponentCount = 0;
        int emptyCount = 0;

        for (int i = 0; i < CONNECT_WIN_COUNT; i++) {
            char token = state[row + i * rowStep][col + i * colStep];
            if (token == aiToken) {
                aiCount++;
            } else if (token == opponentToken) {
                opponentCount++;
            } else {
                emptyCount++;
            }
        }

        if (aiCount == 4) {
            return 20_000;
        }
        if (aiCount == 3 && emptyCount == 1) {
            return 200;
        }
        if (aiCount == 2 && emptyCount == 2) {
            return 20;
        }

        if (opponentCount == 4) {
            return -20_000;
        }
        if (opponentCount == 3 && emptyCount == 1) {
            return -220;
        }
        if (opponentCount == 2 && emptyCount == 2) {
            return -18;
        }

        return 0;
    }

    private int findImmediateWinningMove(final char[][] state, final char token) {
        for (int column : getOrderedValidColumns(state)) {
            int row = getAvailableRow(state, column);
            if (row == -1) {
                continue;
            }
            char[][] child = copyState(state);
            child[row][column] = token;
            if (checkWin(child, token)) {
                return column;
            }
        }
        return -1;
    }

    private char[][] toState(
            final Board board,
            final String aiColor,
            final String opponentColor
    ) {
        char[][] state = new char[board.getRows()][board.getColumns()];

        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getColumns(); col++) {
                String color = board.getDiscColor(row, col);
                if (color == null) {
                    state[row][col] = '.';
                } else if (color.equals(aiColor)) {
                    state[row][col] = 'A';
                } else if (color.equals(opponentColor)) {
                    state[row][col] = 'O';
                } else {
                    state[row][col] = '.';
                }
            }
        }

        return state;
    }

    private boolean checkWin(final char[][] state, final char token) {
        return hasConnect(state, token, 1, 0)
                || hasConnect(state, token, 0, 1)
                || hasConnect(state, token, 1, 1)
                || hasConnect(state, token, 1, -1);
    }

    private boolean hasConnect(
            final char[][] state,
            final char token,
            final int rowStep,
            final int colStep
    ) {
        int rows = state.length;
        int cols = state[0].length;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int endRow = row + (CONNECT_WIN_COUNT - 1) * rowStep;
                int endCol = col + (CONNECT_WIN_COUNT - 1) * colStep;
                if (endRow < 0 || endRow >= rows || endCol < 0 || endCol >= cols) {
                    continue;
                }

                boolean match = true;
                for (int i = 0; i < CONNECT_WIN_COUNT; i++) {
                    if (state[row + i * rowStep][col + i * colStep] != token) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return true;
                }
            }
        }

        return false;
    }

    private List<Integer> getOrderedValidColumns(final char[][] state) {
        List<Integer> validColumns = new ArrayList<>();
        int center = state[0].length / 2;

        for (int column = 0; column < state[0].length; column++) {
            if (getAvailableRow(state, column) != -1) {
                validColumns.add(column);
            }
        }

        validColumns.sort(Comparator.comparingInt(column -> Math.abs(column - center)));
        return validColumns;
    }

    private int getAvailableRow(final char[][] state, final int column) {
        for (int row = state.length - 1; row >= 0; row--) {
            if (state[row][column] == '.') {
                return row;
            }
        }
        return -1;
    }

    private char[][] copyState(final char[][] original) {
        char[][] copy = new char[original.length][original[0].length];
        for (int row = 0; row < original.length; row++) {
            System.arraycopy(original[row], 0, copy[row], 0, original[row].length);
        }
        return copy;
    }
}
