package ru.nsu.ccfit.korneshchuk.snakes.game.player;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class PlayerWithScore implements Serializable {
    @NotNull
    private final Player player;
    private final int score;

    public PlayerWithScore(@NotNull Player player, int score) {
        this.player = Objects.requireNonNull(player, "Player cant be null");
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerWithScore that = (PlayerWithScore) o;
        return score == that.score &&
                player.equals(that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, score);
    }

    @Override
    public String toString() {
        return "PlayerWithScore{" +
                "player=" + player +
                ", score=" + score +
                '}';
    }

}
