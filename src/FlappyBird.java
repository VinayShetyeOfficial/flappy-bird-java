import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    // Game board dimensions
    int boardWidth = 360;
    int boardHeight = 640;

    // Images for game assets
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // Bird dimensions and position
    int birdX = boardWidth / 8;
    int birdY = boardWidth / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Pipe dimensions and initial position
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;  // Scaled by 1/6
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Game logic variables
    Bird bird;
    int velocityX = -4; // Speed of pipes moving to the left
    int velocityY = 0;  // Speed of bird moving up/down
    int gravity = 1;    // Gravity effect on the bird

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    Timer blinkTimer;
    boolean gameOver = false;
    double score = 0;

    JButton playAgainButton;
    JLabel instructionLabel;

    // Constructor
    FlappyBird() {
        // Setting up the game board
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setLayout(null);  // Use null layout for absolute positioning
        setFocusable(true);
        addKeyListener(this);

        // Load images for game assets
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.gif")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // Initialize bird and pipes
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        // Set up the "Play Again" button
        playAgainButton = new JButton("Play Again");
        playAgainButton.setBounds((boardWidth - 150) / 2, (boardHeight - 50) / 2, 150, 50);
        playAgainButton.setFont(new Font("Arial", Font.BOLD, 16));
        playAgainButton.setVisible(false);
        playAgainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });
        add(playAgainButton);

        // Set up the instruction label
        instructionLabel = new JLabel("Press SPACE to Jump", SwingConstants.CENTER);
        instructionLabel.setBounds(0, boardHeight - 40, boardWidth, 30); // Adjust the Y position
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        instructionLabel.setForeground(Color.WHITE);
        add(instructionLabel);

        // Timer for blinking the instruction label
        blinkTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                instructionLabel.setVisible(!instructionLabel.isVisible());
            }
        });
        blinkTimer.start();

        // Timer for placing pipes
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipeTimer.start();

        // Game loop timer
        gameLoop = new Timer(1000 / 60, this); // 60 FPS game loop
        gameLoop.start();
    }

    // Method to place new pipes at random positions
    void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    // Custom paint method to draw game elements
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    // Method to draw game elements on the screen
    public void draw(Graphics g) {
        // Draw background
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);

        // Draw bird
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        // Draw pipes
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Draw score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + (int) score, 10, 35);
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    // Method to update game state
    public void move() {
        // Update bird position with gravity
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0); // Prevent bird from moving above the screen

        // Update pipes position
        for (Pipe pipe : pipes) {
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5; // Increment score when bird passes a pipe
                pipe.passed = true;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        // Check if bird hits the ground
        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    // Collision detection between bird and pipe
    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&   // Bird's top left corner doesn't reach pipe's top right corner
               a.x + a.width > b.x &&   // Bird's top right corner passes pipe's top left corner
               a.y < b.y + b.height &&  // Bird's top left corner doesn't reach pipe's bottom left corner
               a.y + a.height > b.y;    // Bird's bottom left corner passes pipe's top left corner
    }

    // Action performed every game loop tick
    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
            blinkTimer.stop();  // Stop blinking when the game is over
            playAgainButton.setVisible(true);
            instructionLabel.setVisible(false);
        }
    }

    // Key pressed event handler
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;

            if (gameOver) {
                resetGame();
            }
        }
    }

    // Not needed but must be implemented due to KeyListener interface
    @Override
    public void keyTyped(KeyEvent e) {}

    // Not needed but must be implemented due to KeyListener interface
    @Override
    public void keyReleased(KeyEvent e) {}

    // Method to reset the game to initial state
    private void resetGame() {
        bird.y = birdY;
        velocityY = 0;
        pipes.clear();
        gameOver = false;
        score = 0;
        playAgainButton.setVisible(false);
        instructionLabel.setVisible(true);
        blinkTimer.start();  // Restart blinking
        gameLoop.start();
        placePipeTimer.start();
    }
}
