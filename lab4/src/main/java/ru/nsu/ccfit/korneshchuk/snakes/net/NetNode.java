package ru.nsu.ccfit.korneshchuk.snakes.net;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Objects;

public class NetNode implements Serializable {
    @NotNull
    private final InetAddress address;
    private final int port;

    public NetNode(@NotNull InetAddress address, int port) {
        this.address = Objects.requireNonNull(address, "Node address cant be null");
        if (port <= 0) {
            throw new IllegalArgumentException("Port must be positive");
        }
        this.port = port;
    }

    public NetNode(@NotNull InetSocketAddress multicastInfo) {
        this(multicastInfo.getAddress(), multicastInfo.getPort());
    }

    @NotNull
    InetAddress getAddress() {
        return address;
    }

    int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetNode)) return false;
        NetNode netNode = (NetNode) o;
        return port == netNode.port &&
                address.equals(netNode.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port);
    }

    @Override
    public String toString() {
        return "NetNode{" +
                "address=" + address +
                ", port=" + port +
                '}';
    }
}