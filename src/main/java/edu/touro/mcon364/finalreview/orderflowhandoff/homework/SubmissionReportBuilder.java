package edu.touro.mcon364.finalreview.orderflowhandoff.homework;

import edu.touro.mcon364.finalreview.model.StudentSubmission;
import edu.touro.mcon364.finalreview.model.SubmissionReport;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Homework 3 — Building a report from a completed collection.
 *
 * A gradebook already contains a list of assignment submissions. Each submission
 * represents one student's work for one assignment. At this point, the data is
 * not changing while the report is being built. Nothing is being produced by one
 * thread and consumed by another thread. We are simply analyzing a collection
 * that already exists.
 *
 * The job of this class is to answer several reporting questions about that
 * collection and then combine those answers into one SubmissionReport.
 *
 * Before coding, think through the shape of the problem:
 * - What information is already available in each StudentSubmission?
 * - Which questions require counting?
 * - Which questions require calculating a numeric summary?
 * - Which questions require grouping submissions by one field?
 * - Which questions require selecting only some submissions?
 * - Since the input list is already complete, do we need threads here?
 *
 * Requirements:
 * - The constructor receives the submissions that will be analyzed.
 * - The builder must not expose or mutate its internal list of submissions.
 *   --> This means that the constructor should make a copy of the list, and that the methods should return unmodifiable collections or copies of the data.
 * - getLateCount() returns how many submissions were marked late.
 * - getAverageScore() returns the average score across all submissions.
 * - getSubmissionsByAssignment() returns how many submissions exist for each assignment name.
 * - getFailingSubmissions() returns submissions whose score is below 60.
 * - buildReport() returns a SubmissionReport containing all four pieces of information.
 *
 * Edge cases to consider:
 * - An empty submission list should not cause a crash.
 *   --> This means that the methods should handle the case where the list is empty, such as returning 0 for counts and averages, and returning empty collections for maps and lists.
 * - A caller should not be able to change this builder's internal state by
 *   modifying the original list after construction.
 *   - This means that the constructor should make a defensive copy of the list, such as using List.copyOf() or new ArrayList<>(submissions).
 * - Returned collections should not allow callers to mutate the builder's
 *   internal state.
 *   --> This means that methods that return collections should return unmodifiable views or copies, such as using Collections.unmodifiableList() or Map.copyOf().
 */
public class SubmissionReportBuilder {

    private final List<StudentSubmission> submissions;

    public SubmissionReportBuilder(List<StudentSubmission> submissions) {
        this.submissions = List.copyOf(Objects.requireNonNull(submissions));
    }

    /**
     * Return the number of submissions that were turned in late.
     */
    public long getLateCount() {
        return submissions.stream()
                .filter(StudentSubmission::late)
                .count();

    }

    /**
     * Return the average score across all submissions.
     *
     * If there are no submissions, return 0.0.
     */
    public double getAverageScore() {
        // TODO: answer this reporting question from the submissions collection
        return submissions.stream()
                .mapToDouble(StudentSubmission::score)
                .average()
                .orElse(0.0);
    }

    /**
     * Return a map where each assignment name is associated with the number of
     * submissions received for that assignment.
     */
    public Map<String, Long> getSubmissionsByAssignment() {
        Map <String, Long> map = submissions.stream()
                .collect(Collectors.groupingBy(StudentSubmission::assignmentName, Collectors.counting()));
        return Map.copyOf(map);
    }

    /**
     * Return the submissions whose score is below 60.
     */
    public List<StudentSubmission> getFailingSubmissions() {
        return submissions.stream()
                .filter(sub -> sub.score() < 60)
                .collect(Collectors.toUnmodifiableList());

    }

    /**
     * Build the complete report by combining the smaller reporting questions.
     */
    public SubmissionReport buildReport() {
        return new SubmissionReport(
                getLateCount(),
                getAverageScore(),
                getSubmissionsByAssignment(),
                getFailingSubmissions()
        );
    }
}
