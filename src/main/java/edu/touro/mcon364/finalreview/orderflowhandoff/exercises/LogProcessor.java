package edu.touro.mcon364.finalreview.orderflowhandoff.exercises;

import edu.touro.mcon364.finalreview.model.LogLevel;
import edu.touro.mcon364.finalreview.model.LogMessage;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
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
 * --> you should put the message on the queue
 * - start(workerCount) starts exactly workerCount background workers.
 * --> You should create a thread pool and submit workerCount tasks to it,
 *     each of which runs a loop that takes messages from the queue and processes them.
 * - workerCount must be positive.
 * --> You should validate the input and throw an exception if it's not.
 * - workers should keep processing while the processor is still accepting work
 *   or while there is still unprocessed work waiting.
 * --> Your worker loop should check both the running state and the queue state to decide when to exit.
 * - stop() tells the processor to stop accepting/expecting more work and waits
 *   until the already-submitted work has been handled.
 * --> You should set a flag to stop accepting new work, interrupt the worker threads to wake them up if they're waiting, and then wait for them to finish.
 *     After that, you should also drain any remaining messages from the queue and process them to ensure all submitted work is handled.
 * - getTotalProcessed() returns how many log messages have been processed.
 * --> You should use an AtomicInteger to keep track of the total count of processed messages, and return its value here.
 * - getCountsByLevel() returns how many processed messages there were for each
 *   LogLevel.
 * --> You should use a ConcurrentHashMap<LogLevel, AtomicInteger> to keep track of counts by level, and return a snapshot of that map here.
 * - getCountsByLevel() must not allow callers to mutate this class's internal
 *   state.
 *   --> You should return an unmodifiable copy of the counts map, so callers can't change the internal state.
 * - The class must behave correctly when multiple threads interact with it.
 *   -->
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
    private ExecutorService executor;
    private final AtomicInteger totalProcessed = new AtomicInteger(0);
    private final ConcurrentHashMap<LogLevel, Integer> countsByLevel = new ConcurrentHashMap<>();
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
        executor = Executors.newFixedThreadPool(workerCount);
        //Note that you could also submit a  Runnable that will do it.
        /**
         *         for (int i = 0; i < workerCount; i++) {
         *             executor.submit(() -> {
         *             while (running || !queue.isEmpty()) {
         *                 try {
         *                     process(queue.take());
         *                 } catch (InterruptedException e) {
         *                     Thread.currentThread().interrupt();
         *                 }
         *         });
         *         }
         */
        for (int i = 0; i < workerCount; i++) {
            executor.submit(this::workerLoop);
        }
    }

    /**
     * The work done by one background worker.
     */
    private void workerLoop() {
        try {
            while (running || !queue.isEmpty()) {
                process(queue.take());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Process one message and update statistics.
     */
    private void process(LogMessage message) {
        totalProcessed.incrementAndGet();
        // update counts by level atomically
        countsByLevel.merge(message.level(), 1, Integer::sum);
    }

    /**
     * Stop the processor and wait for worker threads to finish.
     */
    public void stop() throws InterruptedException {
        running = false;
        if (executor == null) return;
        executor.shutdownNow(); // interrupt workers blocked on take()
        //blocks main thread until all workers have finished, or the timeout occurs, or the thread is interrupted
        executor.awaitTermination(1, TimeUnit.SECONDS);
        // drain any messages that were in the queue but not yet picked up
        LogMessage msg;
        while ((msg = queue.poll()) != null) {
            process(msg);
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
        return Map.copyOf(countsByLevel);
     }
}
