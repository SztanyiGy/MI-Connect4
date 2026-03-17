package org.connect4.gameworking;

import org.connect4.Main;
import org.connect4.database.Database;
import org.connect4.gamelogic.FileManager;
import org.connect4.gamelogic.Logic;
import org.connect4.model.Board;
import org.connect4.player.ComputerPlayer;
import org.connect4.player.HumanPlayer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Egyszerű Swing alapú GUI a Connect4 játékhoz.
 */
public final class Connect4Gui {

    private final JFrame frame;
    private final JLabel statusLabel;
    private final JButton[] columnButtons;
    private final JLabel[][] cells;

    private final HumanPlayer humanPlayer;
    private final ComputerPlayer computerPlayer;
    private final Database database;
    private final FileManager fileManager;

    private Board board;
    private Logic logic;
    private boolean humanTurn;
    private boolean gameOver;

    /**
     * GUI konstruktor.
     */
    public Connect4Gui() {
        this.database = new Database();
        this.fileManager = new FileManager();

        String playerName = JOptionPane.showInputDialog(
                null,
                "Add meg a játékos nevét:",
                "Connect4",
                JOptionPane.QUESTION_MESSAGE
        );
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Játékos";
        }

        this.humanPlayer = new HumanPlayer(playerName.trim(), "Sárga");
        this.computerPlayer = new ComputerPlayer("Computer", "Piros");

        this.board = new Board(Main.ROWS, Main.COLUMNS);
        this.logic = new Logic(board);
        this.humanTurn = true;
        this.gameOver = false;

        frame = new JFrame("Connect4 GUI");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(1, Main.COLUMNS));
        columnButtons = new JButton[Main.COLUMNS];
        for (int col = 0; col < Main.COLUMNS; col++) {
            final int selectedColumn = col;
            JButton button = new JButton("↓ " + col);
            button.addActionListener(e -> handleHumanMove(selectedColumn));
            columnButtons[col] = button;
            topPanel.add(button);
        }

        JPanel boardPanel = new JPanel(new GridLayout(Main.ROWS, Main.COLUMNS, 4, 4));
        boardPanel.setBackground(Color.BLUE.darker());
        cells = new JLabel[Main.ROWS][Main.COLUMNS];
        for (int row = 0; row < Main.ROWS; row++) {
            for (int col = 0; col < Main.COLUMNS; col++) {
                JLabel label = new JLabel("●", SwingConstants.CENTER);
                label.setOpaque(true);
                label.setBackground(Color.WHITE);
                label.setForeground(Color.WHITE);
                label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
                cells[row][col] = label;
                boardPanel.add(label);
            }
        }

        statusLabel = new JLabel(
                humanPlayer.getName() + " következik.",
                SwingConstants.CENTER
        );

        JButton newGameButton = new JButton("Új játék");
        newGameButton.addActionListener(e -> resetGame());

        JButton scoresButton = new JButton("High score-ok");
        scoresButton.addActionListener(e -> database.displayHighScores());

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statusLabel, BorderLayout.CENTER);

        JPanel actionsPanel = new JPanel(new GridLayout(1, 2));
        actionsPanel.add(newGameButton);
        actionsPanel.add(scoresButton);
        bottomPanel.add(actionsPanel, BorderLayout.EAST);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setSize(760, 700);
        frame.setLocationRelativeTo(null);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(final WindowEvent e) {
                database.close();
            }
        });

        renderBoard();
    }

    /**
     * GUI indítása.
     */
    public void show() {
        frame.setVisible(true);
    }

    /**
     * Indítás az EDT-n.
     */
    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            Connect4Gui gui = new Connect4Gui();
            gui.show();
        });
    }

    private void handleHumanMove(final int column) {
        if (gameOver || !humanTurn) {
            return;
        }

        try {
            if (!board.dropDisc(humanPlayer, column)) {
                statusLabel.setText("Ez az oszlop megtelt. Válassz másikat!");
                return;
            }
        } catch (IllegalArgumentException e) {
            statusLabel.setText("Érvénytelen oszlop.");
            return;
        }

        renderBoard();
        if (finishIfTerminal(humanPlayer.getName(), humanPlayer.getColor())) {
            return;
        }

        humanTurn = false;
        statusLabel.setText("Computer lép...");
        handleComputerMove();
    }

    private void handleComputerMove() {
        if (gameOver) {
            return;
        }

        int computerColumn = computerPlayer.makeMove(board, humanPlayer.getColor());
        if (!board.dropDisc(computerPlayer, computerColumn)) {
            for (int col = 0; col < board.getColumns(); col++) {
                if (board.dropDisc(computerPlayer, col)) {
                    break;
                }
            }
        }

        renderBoard();
        if (finishIfTerminal(computerPlayer.getName(), computerPlayer.getColor())) {
            return;
        }

        humanTurn = true;
        statusLabel.setText(humanPlayer.getName() + " következik.");
    }

    private boolean finishIfTerminal(final String playerName, final String color) {
        if (logic.checkForWin(color)) {
            gameOver = true;
            statusLabel.setText(playerName + " nyert!");
            database.addWin(playerName);
            fileManager.saveGameToFile(board);
            JOptionPane.showMessageDialog(frame, playerName + " nyert!", "Játék vége",
                    JOptionPane.INFORMATION_MESSAGE);
            setButtonsEnabled(false);
            return true;
        }

        if (isBoardFull()) {
            gameOver = true;
            statusLabel.setText("Döntetlen.");
            fileManager.saveGameToFile(board);
            JOptionPane.showMessageDialog(frame, "Döntetlen.", "Játék vége",
                    JOptionPane.INFORMATION_MESSAGE);
            setButtonsEnabled(false);
            return true;
        }

        return false;
    }

    private boolean isBoardFull() {
        for (int col = 0; col < board.getColumns(); col++) {
            if (board.getDiscColor(0, col) == null) {
                return false;
            }
        }
        return true;
    }

    private void renderBoard() {
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getColumns(); col++) {
                String color = board.getDiscColor(row, col);
                if ("Piros".equals(color)) {
                    cells[row][col].setBackground(Color.RED);
                    cells[row][col].setForeground(Color.RED);
                } else if ("Sárga".equals(color)) {
                    cells[row][col].setBackground(Color.YELLOW);
                    cells[row][col].setForeground(Color.YELLOW.darker());
                } else {
                    cells[row][col].setBackground(Color.WHITE);
                    cells[row][col].setForeground(Color.WHITE);
                }
            }
        }
    }

    private void resetGame() {
        this.board = new Board(Main.ROWS, Main.COLUMNS);
        this.logic = new Logic(board);
        this.humanTurn = true;
        this.gameOver = false;
        setButtonsEnabled(true);
        renderBoard();
        statusLabel.setText(humanPlayer.getName() + " következik.");
    }

    private void setButtonsEnabled(final boolean enabled) {
        for (JButton button : columnButtons) {
            button.setEnabled(enabled);
        }
    }
}