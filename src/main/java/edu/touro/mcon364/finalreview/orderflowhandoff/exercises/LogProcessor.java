package edu.touro.mcon364.finalreview.orderflowhandoff.exercises;

import edu.touro.mcon364.finalreview.model.LogLevel;
import edu.touro.mcon364.finalreview.model.LogMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LogProcessor.
 *
 * A server receives log messages from different parts of an application:
 * authentication, payments, reporting, background jobs, and so on. Messages may
 * arrive while earlier messages are still being processed. We want one part of
 * the program to submit log messages, and a small group of worker threads to
 * process those messages in the background.
 *
 * This class represents that log-processing service.
 *
 * The main problem you are solving:
 * - incoming messages need to wait somewhere until a worker is ready for them;
 * - more than one worker may be running at the same time;
 * - every submitted message should be processed once;
 * - while messages are processed, the class must keep accurate summary counts.
 *
 * Requirements:
 * - submit(message) accepts one log message for later processing.
 * - start(workerCount) starts exactly workerCount background workers.
 * - workerCount must be positive.
 * - workers should keep processing while the processor is still accepting work
 *   or while there is still unprocessed work waiting.
 * - stop() tells the processor to stop accepting/expecting more work and waits
 *   until the already-submitted work has been handled.
 * - getTotalProcessed() returns how many log messages have been processed.
 * - getCountsByLevel() returns how many processed messages there were for each
 *   LogLevel.
 * - getCountsByLevel() must not allow callers to mutate this class's internal
 *   state.
 * - The class must behave correctly when multiple threads interact with it.
 *
 * Questions to think about before coding:
 * - Where should submitted messages wait before a worker processes them?
 * - What behavior do we need from that structure: newest first, oldest first,
 *   priority order, or something else?
 * - Which state is shared by multiple threads?
 * - Which operations must be protected so the statistics stay correct?
 * - How will worker threads know when to continue waiting for work and when to
 *   finish?
 * - What should happen if stop() is called while messages are still waiting?
 * - What should the public getter methods return so outside code cannot damage
 *   the processor's internal state?
 */
public class LogProcessor {

    private final BlockingQueue<LogMessage> queue = new LinkedBlockingQueue<>();
    private final List<Thread> workers = new ArrayList<>();
    private final AtomicInteger totalProcessed = new AtomicInteger(0);
    private final ConcurrentHashMap<LogLevel, AtomicInteger> countsByLevel = new ConcurrentHashMap<>();
    private volatile boolean running = false;

    /**
     * Accept one message for processing.
     */
    public void submit(LogMessage message) {
        if (running) {
            queue.offer(message);
        }
    }

    /**
     * Start the requested number of background workers.
     */
    public void start(int workerCount) {
        if (workerCount <= 0) throw new IllegalArgumentException("workerCount must be positive");
        running = true;
        for (int i = 0; i < workerCount; i++) {
            Thread t = new Thread(this::workerLoop);
            workers.add(t);
            t.start();
        }
    }

    /**
     * The work done by one background worker.
     *
     * You may keep this helper method, rename it, or replace it with another
     * private helper if your design is clearer that way.
     */
    private void workerLoop() {
        while (running || !queue.isEmpty()) {
            try {
                // poll with timeout so we re-check running flag regularly
                LogMessage msg = queue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (msg != null) {
                    process(msg);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Process one message and update whatever statistics this class tracks.
     */
    private void process(LogMessage message) {
        totalProcessed.incrementAndGet();
        countsByLevel.computeIfAbsent(message.level(), level -> new AtomicInteger(0))
                     .incrementAndGet();
    }

    /**
     * Stop the processor and wait for worker threads to finish.
     */
    public void stop() throws InterruptedException {
        running = false;
        for (Thread t : workers) {
            t.join();
        }
    }

    /**
     * Return the number of messages processed so far.
     */
    public int getTotalProcessed() {
        return totalProcessed.get();
    }

    /**
     * Return a safe snapshot of the counts by level.
     */
    public Map<LogLevel, Integer> getCountsByLevel() {
        Map<LogLevel, Integer> snapshot = new HashMap<>();
        countsByLevel.forEach((level, count) -> snapshot.put(level, count.get()));
        return Map.copyOf(snapshot);
    }
}
