package edu.touro.mcon364.finalreview.orderflowhandoff.exercises;

import edu.touro.mcon364.finalreview.model.Priority;
import edu.touro.mcon364.finalreview.model.SupportTicket;
import edu.touro.mcon364.finalreview.model.TicketReport;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Building a report from completed work.
 *
 * A support system stores tickets after they have been submitted and worked on.
 * Each ticket has information such as its category, priority, whether it was
 * resolved, and how many minutes it took to resolve.
 * --> There most be a SupportTicket class with methods like category(), priority(), resolved(), and minutesToResolve().
 *     If it's not provided you need to create it based on the requirements of this exercise.
 * The goal of this class is to turn a list of individual tickets into one
 * summary report. We are not modifying tickets. We are looking across the
 * collection and answering questions about the data.
 * --> This tells you that this is not a threading problem. This is a data processing problem.
 *     So you should consider stream operations.
 * - The job is to analyze the collection and produce answers.
 *
 * Before coding, think about the problem in two layers:
 *
 * Layer 1 — What data does this object need?
 * - Should the list of tickets be passed into every method?
 * - Or does it make sense for the builder to receive the list once and then
 *   answer several report questions about that same list?
 * - If this class stores the list, should it keep the original reference or
 *   protect itself with a copy?
 *
 * Layer 2 — What questions does the report ask?
 * - Which questions produce a single number?
 * - Which questions produce a map?
 * - Which questions produce a smaller list?
 * - Which questions require looking only at resolved tickets?
 * - Which questions require looking only at unresolved tickets?
 *
 * Requirements:
 * - The constructor receives the tickets to analyze.
 * - The original list must not be modified by this class.
 * - getResolvedCount() returns how many tickets have been resolved.
 * - getAverageResolutionMinutes() returns the average resolution time for
 *   resolved tickets only.
 * - getCountByCategory() returns how many tickets belong to each category.
 * - getHighPriorityUnresolved() returns unresolved tickets that should receive
 *   the most urgent attention.
 * - buildReport() combines the answers from the smaller methods into one
 *   TicketReport object.
 * - Use streams to express the data processing logic.
 * - Do not use loops.
 *
 * Edge cases to think about:
 * - What should the constructor do if the provided list is null?
 * - What should the average be if there are no resolved tickets?
 * - What should the category-count map look like if the ticket list is empty?
 * - Should callers be able to modify the list returned by
 *   getHighPriorityUnresolved()?
 * - Should callers be able to modify the map returned by getCountByCategory()?
 */
public class TicketReportBuilder {

    private final List<SupportTicket> tickets;

    /**
     * Store the tickets that this report builder will analyze.
     *
     * Think carefully about whether this constructor should keep the original
     * list reference or store a defensive copy.
     */
    public TicketReportBuilder(List<SupportTicket> tickets) {
        if (tickets == null) throw new IllegalArgumentException("tickets must not be null");
        this.tickets = List.copyOf(tickets);
    }

    /**
     * Return how many tickets in this report data set were resolved.
     */
    public long getResolvedCount() {
        return tickets.stream().filter(SupportTicket::resolved).count();
    }

    /**
     * Return the average resolution time for resolved tickets only.
     *
     * Tickets that are not resolved should not affect this average.
     */
    public double getAverageResolutionMinutes() {
        return tickets.stream()
                .filter(SupportTicket::resolved)
                .mapToInt(SupportTicket::minutesToResolve)
                .average()
                .orElse(0.0);
    }

    /**
     * Return how many tickets belong to each category.
     */
    public Map<String, Long> getCountByCategory() {
        return tickets.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(SupportTicket::category, Collectors.counting()),
                        Map::copyOf));
    }

    /**
     * Return unresolved tickets that should receive the most urgent attention.
     */
    public List<SupportTicket> getHighPriorityUnresolved() {
        return tickets.stream()
                .filter(t -> t.resolved() == false && t.priority() == Priority.HIGH)
                .toList();
    }

    /**
     * Build one summary report by combining the smaller report questions above.
     */
    public TicketReport buildReport() {
        return new TicketReport(
                getResolvedCount(),
                getAverageResolutionMinutes(),
                getCountByCategory(),
                getHighPriorityUnresolved()
        );
    }
}
