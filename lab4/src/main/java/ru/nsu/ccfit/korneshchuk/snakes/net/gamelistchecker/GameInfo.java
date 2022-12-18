package ru.nsu.ccfit.korneshchuk.snakes.net.gamelistchecker;

import org.jetbrains.annotations.NotNull;
import ru.nsu.ccfit.korneshchuk.snakes.config.GameConfig;
import ru.nsu.ccfit.korneshchuk.snakes.net.NetNode;

import java.util.Objects;

public class GameInfo {
    @NotNull
    private final GameConfig config;
    private final boolean canJoin;
    private final int playersNumber;
    @NotNull
    private final NetNode masterNode;

    public GameInfo(@NotNull GameConfig config, @NotNull NetNode masterNode, int playersNumber, boolean canJoin) {
        this.config = Objects.requireNonNull(config);
        this.masterNode = Objects.requireNonNull(masterNode);
        this.canJoin = canJoin;
        this.playersNumber = playersNumber;
    }

    @NotNull
    public GameConfig getConfig() {
        return config;
    }

    public boolean isCanJoin() {
        return canJoin;
    }

    public int getPlayersNumber() {
        return playersNumber;
    }

    @NotNull
    public NetNode getMasterNode() {
        return masterNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameInfo gameInfo = (GameInfo) o;
        return canJoin == gameInfo.canJoin &&
                playersNumber == gameInfo.playersNumber &&
                Objects.equals(config, gameInfo.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(config, canJoin, playersNumber);
    }

    @Override
    public String toString() {
        return "GameInfo{" +
                "config=" + config +
                ", canJoin=" + canJoin +
                ", playersNumber=" + playersNumber +
                ", masterNode=" + masterNode +
                '}';
    }
}
