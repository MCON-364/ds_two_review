package edu.touro.mcon364.finalreview.orderflowhandoff.exercises;

import edu.touro.mcon364.finalreview.model.Priority;
import edu.touro.mcon364.finalreview.model.SupportTicket;
import edu.touro.mcon364.finalreview.model.TicketReport;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TicketReportBuilder {

    private final List<SupportTicket> tickets;

    public TicketReportBuilder(List<SupportTicket> tickets) {
        if (tickets == null) throw new IllegalArgumentException("tickets must not be null");
        this.tickets = List.copyOf(tickets);
    }

    public long getResolvedCount() {
        return tickets.stream().filter(SupportTicket::resolved).count();
    }

    public double getAverageResolutionMinutes() {
        return tickets.stream()
                .filter(SupportTicket::resolved)
                .mapToInt(SupportTicket::minutesToResolve)
                .average()
                .orElse(0.0);
    }

    public Map<String, Long> getCountByCategory() {
        return tickets.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(SupportTicket::category, Collectors.counting()),
                        Map::copyOf));
    }

    public List<SupportTicket> getHighPriorityUnresolved() {
        return tickets.stream()
                .filter(t -> t.resolved() == false && t.priority() == Priority.HIGH)
                .toList();
    }

    public TicketReport buildReport() {
        return new TicketReport(
                getResolvedCount(),
                getAverageResolutionMinutes(),
                getCountByCategory(),
                getHighPriorityUnresolved()
        );
    }
}
