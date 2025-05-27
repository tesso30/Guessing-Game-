import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.*;

public class GuessingGameForm extends JPanel {
    private JPanel mainPanel;
    private JTextField guessField;
    private JButton guessButton;
    private JButton resetButton;
    private JButton hintButton;
    private JLabel resultLabel;
    private JLabel attemptsLabel;
    private JLabel rangeLabel;
    private JProgressBar attemptsProgress;
    private JComboBox<String> difficultyCombo;
    private JPanel statsPanel;
    private JLabel highScoreLabel;
    private JLabel avgAttemptsLabel;
    private JLabel gamesPlayedLabel;
    private JLabel hintLabel;
    
    private static final int MAX_ATTEMPTS_EASY = 15;
    private static final int MAX_ATTEMPTS_MEDIUM = 10;
    private static final int MAX_ATTEMPTS_HARD = 7;
    private int maxAttempts = MAX_ATTEMPTS_MEDIUM;
    
    private List<Integer> gameScores = new ArrayList<>();
    private int highScore = Integer.MAX_VALUE;
    private int gamesPlayed = 0;
    private int totalAttempts = 0;

    // Background elements
    private static final int NUM_BUBBLES = 20;
    private List<Bubble> bubbles;
    private Timer animationTimer;
    private float hue = 0.0f;

    // Custom colors with more vibrant options
    private static final Color PRIMARY_COLOR = new Color(0x0A192F);  // Darker blue
    private static final Color SECONDARY_COLOR = new Color(0x112240); // Slightly lighter blue
    private static final Color ACCENT_COLOR = new Color(0x64FFDA);   // Bright cyan
    private static final Color SUCCESS_COLOR = new Color(0x4CAF50);  // Green
    private static final Color ERROR_COLOR = new Color(0xFF5252);    // Red
    private static final Color HINT_COLOR = new Color(0x9C27B0);     // Purple
    private static final Color TEXT_COLOR = new Color(0xE6F1FF);     // Light blue-white
    private static final Color SUBTEXT_COLOR = new Color(0x8892B0);  // Muted blue-gray

    // Minimum and preferred sizes for the window
    private static final int MIN_WIDTH = 500;   // Increased minimum width
    private static final int MIN_HEIGHT = 700;  // Increased minimum height
    private static final int PREF_WIDTH = 600;  // Increased preferred width
    private static final int PREF_HEIGHT = 800; // Increased preferred height

    private class Bubble {
        float x, y;
        float size;
        float speed;
        Color color;
        float alpha;
        float rotation;
        float rotationSpeed;

        Bubble() {
            reset();
        }

        void reset() {
            x = (float) (Math.random() * getWidth());
            y = getHeight() + size;
            size = (float) (Math.random() * 40 + 20);  // Larger bubbles
            speed = (float) (Math.random() * 3 + 1);   // Faster movement
            alpha = (float) (Math.random() * 0.4 + 0.2);
            rotation = (float) (Math.random() * 360);
            rotationSpeed = (float) (Math.random() * 2 - 1);
            color = new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), ACCENT_COLOR.getBlue(), (int)(alpha * 255));
        }

        void update() {
            y -= speed;
            rotation += rotationSpeed;
            if (y < -size) {
                reset();
            }
        }

        void draw(Graphics2D g2d) {
            g2d.setColor(color);
            AffineTransform oldTransform = g2d.getTransform();
            g2d.rotate(Math.toRadians(rotation), x + size/2, y + size/2);
            g2d.fill(new Ellipse2D.Float(x, y, size, size));
            g2d.setTransform(oldTransform);
        }
    }

    public GuessingGameForm() {
        setLayout(new BorderLayout());
        setBackground(PRIMARY_COLOR);
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));
        
        // Initialize bubbles
        bubbles = new ArrayList<>();
        for (int i = 0; i < NUM_BUBBLES; i++) {
            bubbles.add(new Bubble());
        }

        // Create animation timer
        animationTimer = new Timer(16, e -> {
            for (Bubble bubble : bubbles) {
                bubble.update();
            }
            hue = (hue + 0.001f) % 1.0f;
            repaint();
        });
        animationTimer.start();

        // Create main content panel with padding
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Draw semi-transparent background
                g2d.setColor(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 200));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create scroll pane for the main panel
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(PRIMARY_COLOR);
        scrollPane.getViewport().setBackground(PRIMARY_COLOR);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Create title panel with gradient
        JPanel titlePanel = createTitlePanel();
        
        // Create difficulty panel with custom styling
        JPanel difficultyPanel = createDifficultyPanel();
        
        // Create input panel with modern styling
        JPanel inputPanel = createInputPanel();
        
        // Create feedback panel with animations
        JPanel feedbackPanel = createFeedbackPanel();
        
        // Create stats panel with glass effect
        statsPanel = createStatsPanel();
        
        // Create hint panel with custom styling
        JPanel hintPanel = createHintPanel();

        // Add all panels to main panel with proper spacing
        mainPanel.add(titlePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(difficultyPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(inputPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(feedbackPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(statsPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(hintPanel);

        // Add scroll pane to this panel
        add(scrollPane, BorderLayout.CENTER);

        // Add input validation with visual feedback
        guessField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    guessButton.doClick();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Draw gradient background with more vibrant colors
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(0x0A192F),
            getWidth(), getHeight(), new Color(0x112240)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw decorative elements with enhanced effects
        drawDecorativeElements(g2d);

        // Draw bubbles with glow effect
        for (Bubble bubble : bubbles) {
            // Draw glow
            g2d.setColor(new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), ACCENT_COLOR.getBlue(), 50));
            g2d.fill(new Ellipse2D.Float(bubble.x - 5, bubble.y - 5, bubble.size + 10, bubble.size + 10));
            bubble.draw(g2d);
        }
    }

    private void drawDecorativeElements(Graphics2D g2d) {
        // Draw floating numbers with glow effect
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        for (int i = 0; i < 10; i++) {
            float x = (float) (Math.random() * getWidth());
            float y = (float) (Math.random() * getHeight());
            float size = (float) (Math.random() * 20 + 10);
            float alpha = (float) (Math.random() * 0.15 + 0.05);
            
            // Draw glow
            g2d.setColor(new Color(255, 255, 255, (int)(alpha * 255)));
            g2d.drawString(String.valueOf((int)(Math.random() * 100)), x + 1, y + 1);
            g2d.drawString(String.valueOf((int)(Math.random() * 100)), x - 1, y - 1);
            
            // Draw main text
            g2d.setColor(new Color(255, 255, 255, (int)(alpha * 255)));
            g2d.drawString(String.valueOf((int)(Math.random() * 100)), x, y);
        }

        // Draw grid lines with gradient
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i < getWidth(); i += 30) {
            float alpha = (float) (Math.sin(i * 0.1) * 0.1 + 0.1);
            g2d.setColor(new Color(255, 255, 255, (int)(alpha * 255)));
            g2d.drawLine(i, 0, i, getHeight());
        }
        for (int i = 0; i < getHeight(); i += 30) {
            float alpha = (float) (Math.cos(i * 0.1) * 0.1 + 0.1);
            g2d.setColor(new Color(255, 255, 255, (int)(alpha * 255)));
            g2d.drawLine(0, i, getWidth(), i);
        }
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw glowing effect
                for (int i = 5; i > 0; i--) {
                    float alpha = 0.1f - (i * 0.02f);
                    g2d.setColor(new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), ACCENT_COLOR.getBlue(), (int)(alpha * 255)));
                    g2d.fillRoundRect(i, i, getWidth() - 2*i, getHeight() - 2*i, 20, 20);
                }
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        JLabel titleLabel = new JLabel("Number Guessing Game");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Guess a number between 1 and 100");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        subtitleLabel.setForeground(SUBTEXT_COLOR);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(subtitleLabel);
        
        return panel;
    }

    private JPanel createDifficultyPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel.setOpaque(false);
        panel.setBackground(PRIMARY_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 2, true),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        String[] difficulties = {"Easy", "Medium", "Hard"};
        difficultyCombo = new JComboBox<>(difficulties);
        difficultyCombo.setSelectedIndex(1);
        difficultyCombo.setFont(new Font("Arial", Font.BOLD, 14));
        difficultyCombo.setBackground(SECONDARY_COLOR);
        difficultyCombo.setForeground(TEXT_COLOR);
        difficultyCombo.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        difficultyCombo.setPreferredSize(new Dimension(150, 35));
        
        // Custom renderer for combo box
        difficultyCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? ACCENT_COLOR : SECONDARY_COLOR);
                setForeground(TEXT_COLOR);
                setFont(new Font("Arial", Font.BOLD, 14));
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return this;
            }
        });
        
        difficultyCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                switch (difficultyCombo.getSelectedIndex()) {
                    case 0: maxAttempts = MAX_ATTEMPTS_EASY; break;
                    case 1: maxAttempts = MAX_ATTEMPTS_MEDIUM; break;
                    case 2: maxAttempts = MAX_ATTEMPTS_HARD; break;
                }
                updateAttempts(0);
            }
        });
        
        panel.add(difficultyCombo);
        return panel;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setOpaque(false);
        panel.setBackground(PRIMARY_COLOR);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        // Custom styled text field
        guessField = new JTextField(10) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(SUBTEXT_COLOR);
                    g2d.setFont(new Font("Arial", Font.ITALIC, 14));
                    g2d.drawString("Enter number...", 10, getHeight() - 10);
                }
            }
        };
        guessField.setFont(new Font("Arial", Font.PLAIN, 16));
        guessField.setPreferredSize(new Dimension(150, 35));
        guessField.setMinimumSize(new Dimension(150, 35));
        guessField.setMaximumSize(new Dimension(200, 35));
        guessField.setBackground(SECONDARY_COLOR);
        guessField.setForeground(TEXT_COLOR);
        guessField.setCaretColor(TEXT_COLOR);
        guessField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        // Custom styled buttons
        guessButton = createStyledButton("Guess", SUCCESS_COLOR);
        resetButton = createStyledButton("New Game", ERROR_COLOR);
        
        panel.add(guessField);
        panel.add(guessButton);
        panel.add(resetButton);
        
        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw shadow
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 10, 10);
                
                // Draw button with gradient
                if (getModel().isPressed()) {
                    g2d.setColor(color.darker());
                } else if (getModel().isRollover()) {
                    GradientPaint gradient = new GradientPaint(
                        0, 0, color,
                        0, getHeight(), color.brighter()
                    );
                    g2d.setPaint(gradient);
                } else {
                    GradientPaint gradient = new GradientPaint(
                        0, 0, color.darker(),
                        0, getHeight(), color
                    );
                    g2d.setPaint(gradient);
                }
                g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 10, 10);
                
                // Draw border
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 10, 10);
                
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(120, 35));
        button.setMinimumSize(new Dimension(120, 35));
        button.setMaximumSize(new Dimension(150, 35));
        return button;
    }

    private JPanel createFeedbackPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBackground(PRIMARY_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 2, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        rangeLabel = new JLabel("Range: 1 - 100");
        rangeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        rangeLabel.setForeground(TEXT_COLOR);
        rangeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        resultLabel = new JLabel("");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 18));
        resultLabel.setForeground(TEXT_COLOR);
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        attemptsLabel = new JLabel("Attempts: 0/" + maxAttempts);
        attemptsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        attemptsLabel.setForeground(SUBTEXT_COLOR);
        attemptsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Custom styled progress bar
        attemptsProgress = new JProgressBar(0, maxAttempts) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(SECONDARY_COLOR);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.setColor(ACCENT_COLOR);
                int width = (int) ((getValue() / (double) getMaximum()) * getWidth());
                g2d.fillRoundRect(0, 0, width, getHeight(), 10, 10);
            }
        };
        attemptsProgress.setStringPainted(true);
        attemptsProgress.setForeground(TEXT_COLOR);
        attemptsProgress.setBackground(SECONDARY_COLOR);
        attemptsProgress.setMaximumSize(new Dimension(250, 25));
        attemptsProgress.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(rangeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(resultLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(attemptsLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(attemptsProgress);
        
        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 2, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        JLabel statsTitle = new JLabel("Statistics");
        statsTitle.setFont(new Font("Arial", Font.BOLD, 18));
        statsTitle.setForeground(TEXT_COLOR);
        statsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        highScoreLabel = new JLabel("Best Score: -");
        highScoreLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        highScoreLabel.setForeground(SUBTEXT_COLOR);
        highScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        avgAttemptsLabel = new JLabel("Average Attempts: -");
        avgAttemptsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        avgAttemptsLabel.setForeground(SUBTEXT_COLOR);
        avgAttemptsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        gamesPlayedLabel = new JLabel("Games Played: 0");
        gamesPlayedLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gamesPlayedLabel.setForeground(SUBTEXT_COLOR);
        gamesPlayedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(statsTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(highScoreLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(avgAttemptsLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(gamesPlayedLabel);
        
        return panel;
    }

    private JPanel createHintPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBackground(PRIMARY_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(HINT_COLOR, 2, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        hintButton = createStyledButton("Get Hint", HINT_COLOR);
        hintButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        hintLabel = new JLabel("");
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        hintLabel.setForeground(SUBTEXT_COLOR);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(hintButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(hintLabel);
        
        return panel;
    }

    public void updateRange(int min, int max) {
        rangeLabel.setText("Range: " + min + " - " + max);
    }

    public void updateAttempts(int attempts) {
        attemptsLabel.setText("Attempts: " + attempts + "/" + maxAttempts);
        attemptsProgress.setValue(attempts);
        attemptsProgress.setMaximum(maxAttempts);
    }

    public void setResultText(String text, boolean isError) {
        resultLabel.setText(text);
        resultLabel.setForeground(isError ? ERROR_COLOR : SUCCESS_COLOR);
        playSound(isError);
    }

    public void updateStats(int attempts) {
        gamesPlayed++;
        totalAttempts += attempts;
        if (attempts < highScore) {
            highScore = attempts;
        }
        
        highScoreLabel.setText("Best Score: " + (highScore == Integer.MAX_VALUE ? "-" : highScore));
        avgAttemptsLabel.setText(String.format("Average Attempts: %.1f", 
            (double) totalAttempts / gamesPlayed));
        gamesPlayedLabel.setText("Games Played: " + gamesPlayed);
    }

    public void setHint(String hint) {
        hintLabel.setText(hint);
    }

    public void clearInput() {
        guessField.setText("");
        hintLabel.setText("");
        guessField.requestFocus();
    }

    private void playSound(boolean isError) {
        try {
            byte[] soundData = new byte[1];
            AudioFormat format = new AudioFormat(44100, 8, 1, true, true);
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(info);
            
            if (isError) {
                // Error sound (lower pitch)
                for (int i = 0; i < soundData.length; i++) {
                    soundData[i] = (byte) (Math.sin(i / 10.0) * 127);
                }
            } else {
                // Success sound (higher pitch)
                for (int i = 0; i < soundData.length; i++) {
                    soundData[i] = (byte) (Math.sin(i / 5.0) * 127);
                }
            }
            
            ByteArrayInputStream bais = new ByteArrayInputStream(soundData);
            AudioInputStream ais = new AudioInputStream(bais, format, soundData.length);
            clip.open(ais);
            clip.start();
        } catch (Exception e) {
            // Ignore sound errors
        }
    }

    // Getters
    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JTextField getGuessField() {
        return guessField;
    }

    public JButton getGuessButton() {
        return guessButton;
    }

    public JButton getResetButton() {
        return resetButton;
    }

    public JButton getHintButton() {
        return hintButton;
    }

    public JLabel getResultLabel() {
        return resultLabel;
    }

    public JLabel getAttemptsLabel() {
        return attemptsLabel;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }
}
