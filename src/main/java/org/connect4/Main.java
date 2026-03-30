package org.connect4;

import org.connect4.gameworking.Connect4Gui;
import org.connect4.gameworking.Game;
import org.connect4.gameworking.InputReader;


/**
 * A Connect 4 játék fő osztálya, amely elindítja a játékot
 * és kezeli a játékosok interakcióját.
 */
public final class Main {
    /**
     * A játéktábla sorainak száma.
     */
    public static final int ROWS = 6;
    /**
     * A játéktábla oszlopainak száma.
     */
    public static final int COLUMNS = 7;

    /**
     * A játék belépési pontja.
     *
     * @param args A parancssori argumentumok.
     */
    public static void main(final String[] args) {
        if (isCliMode(args)) {
            InputReader inputReader = new InputReader(System.in);
            Game game = new Game(inputReader);
            game.showMenu();
            return;
        }

        Connect4Gui.launch();
    }

    private static boolean isCliMode(final String[] args) {
        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "--terminal".equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }

    // Privát konstruktor
    protected Main() {
        throw new UnsupportedOperationException(
                "Utility osztály, nem példányosítható.");
    }
}
