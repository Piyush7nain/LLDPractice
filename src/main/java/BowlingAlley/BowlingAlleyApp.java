package BowlingAlley;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

// --- 1. Core Data Models (SRP) ---

/**
 * Represents a player in the bowling game.
 * Adheres to SRP by holding only player-specific data.
 */
@Getter
class Player {
    private final String id;
    private final String name;
    private int score; // Total score for the game

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
        this.score = 0;
    }

    public void addScore(int points) {
        this.score += points;
    }

    // Reset score for a new game, if needed
    public void resetScore() {
        this.score = 0;
    }

    @Override
    public String toString() {
        return name + " (Score: " + score + ")";
    }
}

/**
 * Represents a single roll in a frame.
 */
class Roll {
    @Getter
    private final int pinsKnockedDown;
    private final boolean isStrike;
    private final boolean isSpare;

    public Roll(int pinsKnockedDown, boolean isStrike, boolean isSpare) {
        this.pinsKnockedDown = pinsKnockedDown;
        this.isStrike = isStrike;
        this.isSpare = isSpare;
    }

    public boolean isStrike() {
        return isStrike;
    }

    public boolean isSpare() {
        return isSpare;
    }

    @Override
    public String toString() {
        if (isStrike) return "X";
        if (isSpare) return "/";
        return String.valueOf(pinsKnockedDown);
    }
}

// --- 2. Scoring Logic (SRP, Strategy Pattern, OCP) ---

/**
 * Interface for different bonus calculation strategies.
 * Strategy Pattern: Allows for easy switching or adding new bonus rules.
 */
interface IBonusCalculationStrategy {
    /**
     * Calculates bonus points for a given frame.
     *
     * @param currentPlayerRolls The rolls of the current player in the frame.
     * @param nextTwoBalls       The next two rolls after the current frame (for strike bonus).
     * @param nextOneBall        The next one roll after the current frame (for spare bonus).
     * @return The bonus points.
     */
    int calculateBonus(List<Roll> currentPlayerRolls, List<Roll> nextTwoBalls, List<Roll> nextOneBall);
}

/**
 * Default bonus calculation strategy as per problem description.
 * - Strike: 10 bonus points.
 * - Spare: 5 bonus points.
 */
class DefaultBonusStrategy implements IBonusCalculationStrategy {
    @Override
    public int calculateBonus(List<Roll> currentPlayerRolls, List<Roll> nextTwoBalls, List<Roll> nextOneBall) {
        if (currentPlayerRolls.isEmpty()) {
            return 0;
        }

        Roll firstRoll = currentPlayerRolls.get(0);

        if (firstRoll.isStrike()) {
            return 10; // Fixed 10 bonus points for a strike
        } else if (firstRoll.isSpare()) {
            return 5; // Fixed 5 bonus points for a spare
        }
        return 0;
    }
}

/**
 * Bonus calculation strategy where bonus points depend on subsequent rolls.
 * This implements the "Bonus Point (Only Attempt If Time Permits)" section.
 */
class NextBallBonusStrategy implements IBonusCalculationStrategy {
    @Override
    public int calculateBonus(List<Roll> currentPlayerRolls, List<Roll> nextTwoBalls, List<Roll> nextOneBall) {
        if (currentPlayerRolls.isEmpty()) {
            return 0;
        }

        Roll firstRoll = currentPlayerRolls.get(0);

        if (firstRoll.isStrike()) {
            // Bonus points for strike are sum of pins in next 2 balls
            int bonus = 0;
            if (nextTwoBalls != null) {
                for (Roll roll : nextTwoBalls) {
                    bonus += roll.getPinsKnockedDown();
                }
            }
            return bonus;
        } else if (firstRoll.isSpare()) {
            // Bonus points for spare are sum of pins in next 1 ball
            int bonus = 0;
            if (nextOneBall != null && !nextOneBall.isEmpty()) {
                bonus += nextOneBall.get(0).getPinsKnockedDown();
            }
            return bonus;
        }
        return 0;
    }
}

/**
 * Handles the calculation of frame scores, including bonuses.
 * Adheres to SRP by focusing solely on score calculation.
 * Uses Strategy Pattern for bonus calculation.
 */
class ScoreCalculator {
    private final IBonusCalculationStrategy bonusStrategy;

    public ScoreCalculator(IBonusCalculationStrategy bonusStrategy) {
        this.bonusStrategy = bonusStrategy;
    }

    /**
     * Calculates the score for a single frame, including any bonuses.
     *
     * @param frame The Frame object representing the current frame.
     * @param nextTwoBalls Subsequent rolls for bonus calculation (for strikes).
     * @param nextOneBall Subsequent roll for bonus calculation (for spares).
     * @return The total score for the frame, including bonuses.
     */
    public int calculateFrameScore(Frame frame, List<Roll> nextTwoBalls, List<Roll> nextOneBall) {
        int baseScore = 0;
        for (Roll roll : frame.getRolls()) {
            baseScore += roll.getPinsKnockedDown();
        }

        int bonus = bonusStrategy.calculateBonus(frame.getRolls(), nextTwoBalls, nextOneBall);
        return baseScore + bonus;
    }
}

// --- 3. Frame/Round Management (SRP, LSP) ---

/**
 * Represents a single frame (or round) in bowling.
 * Manages the rolls within that frame and determines strike/spare status.
 */
class Frame {
    protected final int frameNumber;
    protected final List<Roll> rolls; // Rolls for the current frame
    protected int pinsDownInCurrentFrame; // Pins knocked down in this frame (up to 10)

    public Frame(int frameNumber) {
        this.frameNumber = frameNumber;
        this.rolls = new ArrayList<>();
        this.pinsDownInCurrentFrame = 0;
    }

    /**
     * Records a roll for the current frame.
     *
     * @param pins The number of pins knocked down in this roll.
     * @throws IllegalArgumentException if pins are invalid or frame is full.
     */
    public void recordRoll(int pins) {
        if (pins < 0 || pins > 10) {
            throw new IllegalArgumentException("Pins knocked down must be between 0 and 10.");
        }
        if (isComplete()) {
            throw new IllegalStateException("Frame " + frameNumber + " is already complete.");
        }

        boolean isStrike = false;
        boolean isSpare = false;

        if (rolls.isEmpty()) { // First roll
            if (pins == 10) {
                isStrike = true;
                pinsDownInCurrentFrame = 10;
            } else {
                pinsDownInCurrentFrame += pins;
            }
        } else { // Second roll
            int remainingPins = 10 - rolls.get(0).getPinsKnockedDown();
            if (pinsDownInCurrentFrame + pins > 10 && !rolls.get(0).isStrike()) { // Prevents over-10 for non-strikes
                throw new IllegalArgumentException("Cannot knock down more than 10 pins in a frame.");
            }
            if (pinsDownInCurrentFrame + pins == 10 && !rolls.get(0).isStrike()) {
                isSpare = true;
            }
            pinsDownInCurrentFrame += pins;
        }

        rolls.add(new Roll(pins, isStrike, isSpare));
    }

    /**
     * Checks if the frame is complete based on rules (2 rolls or a strike).
     *
     * @return true if the frame is complete, false otherwise.
     */
    public boolean isComplete() {
        if (rolls.isEmpty()) return false;
        if (rolls.size() == 1 && rolls.get(0).isStrike()) {
            return true; // Strike completes a regular frame immediately
        }
        return rolls.size() == 2; // Two rolls complete a frame
    }

    public List<Roll> getRolls() {
        return Collections.unmodifiableList(rolls);
    }

    public boolean isStrike() {
        return !rolls.isEmpty() && rolls.get(0).isStrike();
    }

    public boolean isSpare() {
        return !rolls.isEmpty() && rolls.get(0).isSpare();
    }

    public int getPinsKnockedDownInFrame() {
        return pinsDownInCurrentFrame;
    }

}

/**
 * Special frame for the final round (Round 5) allowing extra balls for strike/spare.
 * Adheres to LSP by extending Frame and providing specialized behavior.
 */
class BonusFrame extends Frame {
    public BonusFrame(int frameNumber) {
        super(frameNumber);
    }

    @Override
    public void recordRoll(int pins) {
        if (pins < 0 || pins > 10) {
            throw new IllegalArgumentException("Pins knocked down must be between 0 and 10.");
        }

        boolean isStrike = false;
        boolean isSpare = false;

        // Special logic for final round:
        // A strike in the first roll allows 2 more rolls
        // A spare in the first two rolls allows 1 more roll
        if (rolls.isEmpty()) { // First roll
            if (pins == 10) {
                isStrike = true;
            }
            rolls.add(new Roll(pins, isStrike, false)); // isSpare is false for first roll
            pinsDownInCurrentFrame += pins;
        } else if (rolls.size() == 1) { // Second roll
            int previousPins = rolls.get(0).getPinsKnockedDown();
            if (!rolls.get(0).isStrike() && previousPins + pins == 10) {
                isSpare = true;
                rolls.add(new Roll(pins, false, isSpare));
            } else {
                rolls.add(new Roll(pins, false, false));
            }
            pinsDownInCurrentFrame += pins;
        } else if (rolls.size() == 2 && (rolls.get(0).isStrike() || (rolls.get(0).getPinsKnockedDown() + rolls.get(1).getPinsKnockedDown() == 10))) {
            // Third roll allowed for strike or spare
            rolls.add(new Roll(pins, false, false));
            pinsDownInCurrentFrame += pins;
        } else {
            throw new IllegalStateException("Bonus Frame " + frameNumber + " is already complete or invalid roll.");
        }
    }

    @Override
    public boolean isComplete() {
        if (rolls.isEmpty()) {
            return false;
        }

        // Standard 2 rolls:
        if (rolls.size() == 2 && !rolls.get(0).isStrike() && !(rolls.get(0).getPinsKnockedDown() + rolls.get(1).getPinsKnockedDown() == 10)) {
            return true; // Two regular rolls
        }

        // If first roll was strike (rolls.get(0).isStrike()), needs 3 rolls
        // If first two rolls were spare (rolls.get(0).getPinsKnockedDown() + rolls.get(1).getPinsKnockedDown() == 10), needs 3 rolls
        return rolls.size() == 3;
    }

    @Override
    public boolean isStrike() {
        return !rolls.isEmpty() && rolls.get(0).isStrike();
    }

    @Override
    public boolean isSpare() {
        return rolls.size() >= 2 && (rolls.get(0).getPinsKnockedDown() + rolls.get(1).getPinsKnockedDown() == 10) && !rolls.get(0).isStrike();
    }
}


// --- 4. Game Orchestration (SRP, DIP) ---

/**
 * Manages the entire bowling game lifecycle.
 * Adheres to SRP by orchestrating game flow, player turns, and round progression.
 * Depends on abstractions (IBonusCalculationStrategy, IDisplayManager).
 */
class Game {
    private static final int TOTAL_ROUNDS = 5;
    private final List<Player> players;
    private final IDisplayManager displayManager;
    private final ScoreCalculator scoreCalculator;
    private final Map<Player, List<Frame>> playerFrames; // Map to store frames for each player
    private int currentRound;
    private Player currentPlayer;
    private int currentPlayerIndex;

    public Game(IDisplayManager displayManager, IBonusCalculationStrategy bonusStrategy) {
        this.displayManager = displayManager;
        this.players = new ArrayList<>();
        this.scoreCalculator = new ScoreCalculator(bonusStrategy);
        this.playerFrames = new HashMap<>();
        this.currentRound = 1;
        this.currentPlayerIndex = 0;
    }

    /**
     * Adds a player to the game.
     *
     * @param player The player to add.
     * @throws IllegalStateException if players are added after the game has started.
     */
    public void addPlayer(Player player) {
        if (currentRound > 1) {
            throw new IllegalStateException("Cannot add players after the game has started.");
        }
        this.players.add(player);
        this.playerFrames.put(player, new ArrayList<>());
        displayManager.displayMessage(player.getName() + " has joined the game.");
    }

    /**
     * Starts the bowling game.
     *
     * @throws IllegalStateException if fewer than 1 player is added.
     */
    public void startGame() {
        if (players.isEmpty()) {
            throw new IllegalStateException("At least one player is required to start the game.");
        }
        displayManager.displayWelcome();
        displayManager.displayPlayers(players);
        playGame();
    }

    private void playGame() {
        while (currentRound <= TOTAL_ROUNDS) {
            displayManager.displayRoundStart(currentRound);
            startRound();
            currentRound++;
        }
        endGame();
    }

    private void startRound() {
        // Iterate through players for their turns in the current round
        for (int i = 0; i < players.size(); i++) {
            currentPlayer = players.get(i);
            displayManager.displayPlayerTurn(currentPlayer, currentRound);
            playPlayerTurn();
            updateScores();
            displayManager.displayCurrentScores(players);
        }
    }

    private void playPlayerTurn() {
        Frame currentFrame;
        // Create a new frame for the current player for the current round
        if (currentRound == TOTAL_ROUNDS) {
            currentFrame = new BonusFrame(currentRound);
        } else {
            currentFrame = new Frame(currentRound);
        }
        playerFrames.get(currentPlayer).add(currentFrame); // Add the new frame to player's list

        int rollCount = 0;
        int maxRolls = (currentRound == TOTAL_ROUNDS) ? 3 : 2; // Max 3 rolls for final round for bonus

        while (!currentFrame.isComplete() && rollCount < maxRolls) {
            int pinsKnockedDown = displayManager.getRollInput(currentPlayer, rollCount + 1, 10 - currentFrame.getPinsKnockedDownInFrame());
            try {
                currentFrame.recordRoll(pinsKnockedDown);
                rollCount++;
                if (currentFrame.isStrike() && currentRound != TOTAL_ROUNDS && rollCount == 1) {
                    break; // A strike on the first roll completes the frame in regular rounds
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                displayManager.displayError(e.getMessage());
                // Ask for input again if invalid
            }
        }
    }

    private void updateScores() {
        for (Player player : players) {
            int totalScore = 0;
            List<Frame> frames = playerFrames.get(player);

            for (int i = 0; i < frames.size(); i++) {
                Frame currentFrame = frames.get(i);
                List<Roll> nextTwoBalls = new ArrayList<>();
                List<Roll> nextOneBall = new ArrayList<>();

                // Get next two balls for strike bonus
                if (i + 1 < frames.size()) {
                    Frame nextFrame = frames.get(i + 1);
                    nextTwoBalls.addAll(nextFrame.getRolls());
                    if (nextTwoBalls.size() < 2 && i + 2 < frames.size()) {
                        // If next frame is not complete (e.g., strike on 1st ball), get from next-next frame
                        nextTwoBalls.addAll(frames.get(i + 2).getRolls());
                    }
                } else if (i + 1 == frames.size() && currentRound == TOTAL_ROUNDS) {
                    // For the final frame, bonus rolls are part of the current frame's rolls
                    // The `BonusFrame` itself handles its internal rolls
                    // We need to ensure that the `scoreCalculator` has access to these "bonus" rolls
                    nextTwoBalls.addAll(currentFrame.getRolls().stream().skip(1).collect(Collectors.toList())); // Skip first roll
                    nextOneBall.addAll(currentFrame.getRolls().stream().skip(1).limit(1).collect(Collectors.toList())); // Skip first roll, take next
                }

                // Get next one ball for spare bonus
                if (i + 1 < frames.size()) {
                    Frame nextFrame = frames.get(i + 1);
                    if (!nextFrame.getRolls().isEmpty()) {
                        nextOneBall.add(nextFrame.getRolls().get(0));
                    }
                }


                int frameScore = scoreCalculator.calculateFrameScore(currentFrame, nextTwoBalls, nextOneBall);
                // Note: This model calculates bonus on the fly for display.
                // For cumulative scoring as typically done, you'd calculate the frame score
                // and add it to a running total.
                // For simplicity here, we re-calculate the player's total score.
                // In a real system, each frame would have a 'calculatedScore' field.
            }
            player.resetScore(); // Reset to re-calculate for this display update
            for(Frame frame : frames) {
                // This is a simplified way to aggregate score for display.
                // A robust system would calculate and store each frame's *final* score after subsequent frames
                // are known for bonus points, then sum those up.
                // For problem simplicity, we add the pins knocked down in the frame,
                // and the bonus calculation in ScoreCalculator factors in future rolls.
                // Re-simplifying for the output expectation: just sum the pins for now,
                // and rely on ScoreCalculator for display purposes (though it won't reflect cumulative bonus properly here).
                player.addScore(frame.getPinsKnockedDownInFrame());
            }

            // Re-calculating the full cumulative score including bonuses for all frames for current player
            // This is crucial for correctly reflecting bonuses that depend on future rolls.
            int cumulativePlayerScore = 0;
            for (int i = 0; i < frames.size(); i++) {
                Frame currentFrame = frames.get(i);
                List<Roll> subsequentRollsForStrike = new ArrayList<>();
                List<Roll> subsequentRollForSpare = new ArrayList<>();

                // Collect subsequent rolls for bonus calculation
                for (int j = i + 1; j < frames.size(); j++) {
                    subsequentRollsForStrike.addAll(frames.get(j).getRolls());
                }
                if (subsequentRollsForStrike.size() >= 1) {
                    subsequentRollForSpare.add(subsequentRollsForStrike.get(0));
                }

                // If in the final round and the current frame is a BonusFrame,
                // its own additional rolls are considered "subsequent" for its own bonus.
                if (currentRound == TOTAL_ROUNDS && currentFrame instanceof BonusFrame) {
                    List<Roll> bonusFrameOwnRolls = currentFrame.getRolls();
                    if (bonusFrameOwnRolls.size() >= 2) {
                        subsequentRollsForStrike.add(0, bonusFrameOwnRolls.get(1)); // Add 2nd roll for potential strike bonus
                        if (bonusFrameOwnRolls.size() == 3) {
                            subsequentRollsForStrike.add(0, bonusFrameOwnRolls.get(2)); // Add 3rd roll for potential strike bonus
                        }
                    }
                    if (bonusFrameOwnRolls.size() >= 2 && currentFrame.isSpare()) {
                        subsequentRollForSpare.add(0, bonusFrameOwnRolls.get(2)); // Add 3rd roll for spare bonus in last frame
                    }
                }

                // Truncate subsequent rolls to what's needed for calculations
                List<Roll> strikeBonusRolls = subsequentRollsForStrike.stream().limit(2).collect(Collectors.toList());
                List<Roll> spareBonusRolls = subsequentRollForSpare.stream().limit(1).collect(Collectors.toList());

                cumulativePlayerScore += scoreCalculator.calculateFrameScore(currentFrame, strikeBonusRolls, spareBonusRolls);
            }
            player.resetScore();
            player.addScore(cumulativePlayerScore);
        }
    }


    private void endGame() {
        displayManager.displayGameOver();
        List<Player> sortedPlayers = players.stream()
                .sorted(Comparator.comparingInt(Player::getScore).reversed())
                .collect(Collectors.toList());
        displayManager.displayFinalScores(sortedPlayers);
        displayManager.displayWinner(sortedPlayers.get(0));
    }
}

// --- 5. Display/Input Management (SRP, DIP for Platformizability) ---

/**
 * Interface for managing all display and input operations.
 * Allows for easy replacement of console I/O with GUI I/O or other forms.
 */
interface IDisplayManager {
    void displayWelcome();
    void displayMessage(String message);
    void displayError(String errorMessage);
    void displayPlayers(List<Player> players);
    void displayRoundStart(int roundNumber);
    void displayPlayerTurn(Player player, int round);
    int getRollInput(Player player, int rollNumber, int maxPins);
    void displayCurrentScores(List<Player> players);
    void displayGameOver();
    void displayFinalScores(List<Player> players);
    void displayWinner(Player winner);
    void close();
}

/**
 * Console-based implementation of IDisplayManager.
 */
class ConsoleDisplayManager implements IDisplayManager {
    final Scanner scanner;

    public ConsoleDisplayManager() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void displayWelcome() {
        System.out.println("=====================================");
        System.out.println("  Welcome to the Bowling Alley Game! ");
        System.out.println("=====================================");
        System.out.println("Game consists of 5 rounds.");
        System.out.println("Strike ('X') bonus: 10 points.");
        System.out.println("Spare ('/') bonus: 5 points.");
        System.out.println("Final round allows extra balls for strike/spare.");
        System.out.println("-------------------------------------");
    }

    @Override
    public void displayMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void displayError(String errorMessage) {
        System.err.println("ERROR: " + errorMessage);
    }

    @Override
    public void displayPlayers(List<Player> players) {
        System.out.println("\nPlayers in the game:");
        players.forEach(player -> System.out.println("- " + player.getName()));
        System.out.println("-------------------------------------");
    }

    @Override
    public void displayRoundStart(int roundNumber) {
        System.out.println("\n===== ROUND " + roundNumber + " START =====");
    }

    @Override
    public void displayPlayerTurn(Player player, int round) {
        System.out.println("\n--- " + player.getName() + "'s turn (Round " + round + ") ---");
    }

    @Override
    public int getRollInput(Player player, int rollNumber, int maxPins) {
        int pins = -1;
        while (true) {
            System.out.print(player.getName() + ", Roll " + rollNumber + ": Enter pins knocked down (0-" + maxPins + "): ");
            try {
                pins = Integer.parseInt(scanner.nextLine());
                if (pins >= 0 && pins <= maxPins) {
                    return pins;
                } else {
                    System.out.println("Invalid input. Pins must be between 0 and " + maxPins + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    @Override
    public void displayCurrentScores(List<Player> players) {
        System.out.println("\n--- Current Scores ---");
        players.forEach(player -> System.out.println(player.getName() + ": " + player.getScore()));
        System.out.println("----------------------");
    }

    @Override
    public void displayGameOver() {
        System.out.println("\n=====================================");
        System.out.println("          GAME OVER!                 ");
        System.out.println("=====================================");
    }

    @Override
    public void displayFinalScores(List<Player> players) {
        System.out.println("\n--- Final Rankings ---");
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            System.out.println((i + 1) + ". " + p.getName() + " - " + p.getScore() + " points");
        }
        System.out.println("----------------------");
    }

    @Override
    public void displayWinner(Player winner) {
        System.out.println("\nCongratulations! The winner is: " + winner.getName() + " with " + winner.getScore() + " points!");
        System.out.println("=====================================");
    }

    @Override
    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }
}

// --- 6. Main Application Entry Point ---

/**
 * Main class to run the Bowling Alley Game.
 * Demonstrates how to assemble the components.
 */
public class BowlingAlleyApp {
    public static void main(String[] args) {
        IDisplayManager displayManager = new ConsoleDisplayManager();

        // Choose which bonus strategy to use:
        IBonusCalculationStrategy bonusStrategy = new DefaultBonusStrategy(); // Standard bonus rules
        // IBonusCalculationStrategy bonusStrategy = new NextBallBonusStrategy(); // Bonus depends on next balls

        Game bowlingGame = new Game(displayManager, bonusStrategy);

        // Add players
        displayManager.displayMessage("Enter number of players:");
        int numPlayers = 0;
        while(numPlayers < 1) {
            try {
                numPlayers = Integer.parseInt(((ConsoleDisplayManager)displayManager).scanner.nextLine());
                if (numPlayers < 1) {
                    displayManager.displayError("Please enter at least 1 player.");
                }
            } catch (NumberFormatException e) {
                displayManager.displayError("Invalid input. Please enter a number.");
            }
        }

        for (int i = 0; i < numPlayers; i++) {
            displayManager.displayMessage("Enter name for Player " + (i + 1) + ":");
            String playerName = ((ConsoleDisplayManager)displayManager).scanner.nextLine();
            bowlingGame.addPlayer(new Player("P" + (i + 1), playerName));
        }

        try {
            bowlingGame.startGame();
        } finally {
            displayManager.close(); // Ensure scanner is closed
        }
    }
}