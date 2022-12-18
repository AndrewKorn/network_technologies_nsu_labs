package ru.nsu.ccfit.korneshchuk.dns;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.BlockingQueue;

public class DNSQueueOperationsWithBlockStrategy implements DNSQueueStrategy {
    @Override
    public void appendResolveRequest(@NotNull BlockingQueue<DNSResolveRequest> queue, @NotNull DNSResolveRequest dnsResolveRequest) {
        try {
            queue.put(dnsResolveRequest);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Thread was interrupted while waiting put request to queue", e);
        }
    }

    @Override
    @NotNull
    public DNSResolveRequest takeResolveRequest(@NotNull BlockingQueue<DNSResolveRequest> queue) {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Thread was interrupted while waiting take request from queue", e);
        }
    }
}
