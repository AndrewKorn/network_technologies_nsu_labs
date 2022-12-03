package ru.nsu.ccfit.korneshchuk.snakes.net.messages;

import org.jetbrains.annotations.NotNull;
import ru.nsu.ccfit.korneshchuk.snakes.game.GameState;
import ru.nsu.ccfit.korneshchuk.snakes.game.player.Player;
import ru.nsu.ccfit.korneshchuk.snakes.net.Neighbor;

import java.util.Map;
import java.util.Objects;

public class StateMessage extends Message {
    @NotNull
    private final GameState gameState;
    @NotNull
    private final Map<Neighbor, Player> playersNode;

    public StateMessage(@NotNull GameState gameState, @NotNull Map<Neighbor, Player> nodePlayerMap) {
        super(MessageType.STATE);
        this.gameState = Objects.requireNonNull(gameState, "Game state cant be null");
        this.playersNode = Objects.requireNonNull(nodePlayerMap, "Node-Players map cant be null");
    }

    @NotNull
    public GameState getGameState() {
        return gameState;
    }

    @NotNull
    public Map<Neighbor, Player> getPlayersNode() {
        return playersNode;
    }
}
