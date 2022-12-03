package ru.nsu.ccfit.korneshchuk.snakes.gui.utils;

import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import ru.nsu.ccfit.korneshchuk.snakes.game.player.Player;

import java.util.*;

public class PlayerColorMapper {

    @NotNull
    private final Map<Player, Color> playerColors;

    private int prevColorIndex;

    @NotNull
    private static final List<Color> snakeColors = List.of(
            Color.RED,
            Color.BLUE,
            Color.ORANGE,
            Color.PURPLE,
            Color.PINK
    );

    @NotNull
    private static final Color ZOMBIE_SNAKE_COLOR = Color.BLACK;

    public PlayerColorMapper() {
        this.playerColors = new HashMap<>();
        this.prevColorIndex = 0;
    }

    @NotNull
    public Optional<Color> getColor(@NotNull Player player) {
        return Optional.ofNullable(playerColors.get(player));
    }

    public void addPlayer(@NotNull Player player) {
        int currentColorIndex = (prevColorIndex + 1) % snakeColors.size();
        playerColors.put(Objects.requireNonNull(player), snakeColors.get(currentColorIndex));
        prevColorIndex = currentColorIndex;
    }

    public void removePlayer(@NotNull Player player) {
        Objects.requireNonNull(player, "Player for remove cant be null");
        playerColors.remove(player);
    }

    public boolean isPlayerRegistered(@NotNull Player player) {
        Objects.requireNonNull(player, "Player cant be null");
        return playerColors.containsKey(player);
    }

    @NotNull
    public Set<Player> getRegisteredPlayers() {
        return Collections.unmodifiableSet(playerColors.keySet());
    }

    @NotNull
    public Color getZombieSnakeColor() {
        return ZOMBIE_SNAKE_COLOR;
    }
}
