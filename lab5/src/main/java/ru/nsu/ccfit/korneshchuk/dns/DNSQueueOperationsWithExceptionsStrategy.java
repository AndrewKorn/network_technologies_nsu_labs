package ru.nsu.ccfit.korneshchuk.dns;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.BlockingQueue;

public class DNSQueueOperationsWithExceptionsStrategy implements DNSQueueStrategy {
    @Override
    public void appendResolveRequest(@NotNull BlockingQueue<DNSResolveRequest> queue, @NotNull DNSResolveRequest dnsResolveRequest) {
        queue.add(dnsResolveRequest);
    }

    @Override
    @NotNull
    public DNSResolveRequest takeResolveRequest(@NotNull BlockingQueue<DNSResolveRequest> queue) {
        if (queue.isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Thread was interrupted while waiting take request from queue", e);
        }
    }
}