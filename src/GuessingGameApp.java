import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class GuessingGameApp {
    private int numberToGuess;
    private int numberOfAttempts;
    private int minRange;
    private int maxRange;
    private Random rand;
    private GuessingGameForm form;
    private boolean gameOver;
    private JFrame frame;

    public GuessingGameApp() {
        rand = new Random();
        form = new GuessingGameForm();
        startNewGame();
        createGUI();
    }

    private void startNewGame() {
        minRange = 1;
        maxRange = 100;
        numberToGuess = rand.nextInt(maxRange - minRange + 1) + minRange;
        numberOfAttempts = 0;
        gameOver = false;
        form.updateRange(minRange, maxRange);
        form.updateAttempts(numberOfAttempts);
        form.setResultText("", false);
        form.clearInput();
    }

    private void createGUI() {
        frame = new JFrame("Guessing Game");
        frame.setContentPane(form);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Set window properties
        frame.setMinimumSize(form.getMinimumSize());
        frame.setPreferredSize(form.getPreferredSize());
        frame.setResizable(true);
        
        // Center the window
        frame.setLocationRelativeTo(null);
        
        // Add window listener for proper resizing
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Ensure the window doesn't get too small
                Dimension size = frame.getSize();
                if (size.width < form.getMinimumSize().width) {
                    size.width = form.getMinimumSize().width;
                }
                if (size.height < form.getMinimumSize().height) {
                    size.height = form.getMinimumSize().height;
                }
                frame.setSize(size);
            }
        });

        // Add guess button listener
        form.getGuessButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processGuess();
            }
        });

        // Add reset button listener
        form.getResetButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startNewGame();
            }
        });

        // Add hint button listener
        form.getHintButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                provideHint();
            }
        });

        // Show the window
        frame.pack();
        frame.setVisible(true);
    }

    private void processGuess() {
        if (gameOver) {
            form.setResultText("Game Over! Start a new game.", true);
            return;
        }

        if (numberOfAttempts >= form.getMaxAttempts()) {
            gameOver = true;
            form.setResultText("Game Over! The number was " + numberToGuess, true);
            return;
        }

        try {
            int guess = Integer.parseInt(form.getGuessField().getText());
            
            if (guess < minRange || guess > maxRange) {
                form.setResultText("Please enter a number between " + minRange + " and " + maxRange, true);
                return;
            }

            numberOfAttempts++;
            form.updateAttempts(numberOfAttempts);

            if (guess < numberToGuess) {
                minRange = guess + 1;
                form.updateRange(minRange, maxRange);
                if (numberOfAttempts >= form.getMaxAttempts()) {
                    gameOver = true;
                    form.setResultText("Game Over! The number was " + numberToGuess, true);
                } else {
                    form.setResultText("Too low! Try a higher number.", false);
                }
            } else if (guess > numberToGuess) {
                maxRange = guess - 1;
                form.updateRange(minRange, maxRange);
                if (numberOfAttempts >= form.getMaxAttempts()) {
                    gameOver = true;
                    form.setResultText("Game Over! The number was " + numberToGuess, true);
                } else {
                    form.setResultText("Too high! Try a lower number.", false);
                }
            } else {
                gameOver = true;
                form.setResultText("Congratulations! You guessed the number in " + numberOfAttempts + " attempts!", false);
                form.updateStats(numberOfAttempts);
            }
            
            form.clearInput();
        } catch (NumberFormatException ex) {
            form.setResultText("Please enter a valid number", true);
        }
    }

    private void provideHint() {
        if (gameOver) {
            form.setHint("Start a new game to get hints!");
            return;
        }

        if (numberOfAttempts == 0) {
            form.setHint("Try guessing a number in the middle of the range!");
            return;
        }

        int range = maxRange - minRange;
        if (range <= 10) {
            form.setHint("You're getting close! The number is between " + minRange + " and " + maxRange);
        } else if (range <= 20) {
            form.setHint("The number is " + (numberToGuess % 2 == 0 ? "even" : "odd"));
        } else {
            int midPoint = (minRange + maxRange) / 2;
            if (numberToGuess < midPoint) {
                form.setHint("The number is in the lower half of the range");
            } else {
                form.setHint("The number is in the upper half of the range");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GuessingGameApp();
            }
        });
    }
}
