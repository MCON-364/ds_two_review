package edu.touro.mcon364.finalreview.treesandthreads.homework;

import java.util.*;
import java.util.stream.*;

/**
 * Homework 2 - Student GradeBook (TreeMap + Streams + DoubleSummaryStatistics)
 *
 * Scenario: a course has many students. Each student is identified by name and
 * has a numeric grade (0.0 to 100.0). The gradebook must support sorted lookup
 * and statistical analysis.
 *
 * Before coding, think about:
 * - Should the map key be the student name or the grade? Why does it matter?
 * - What does TreeMap.firstEntry() return? What does lastEntry() return?
 * - How do we turn a numeric score into a letter grade inside a stream?
 *
 * Requirements:
 * - The constructor receives a Map of student name to grade.
 * - buildSortedGradeBook() returns a TreeMap so students are iterated alphabetically.
 * - getStatistics() returns DoubleSummaryStatistics over all grades.
 * - getLetterGradeDistribution() returns a TreeMap counting how many students
 *   received each letter grade: A (90+), B (80-89), C (70-79), D (60-69), F (below 60).
 * - getTopStudents(n) returns the names of the n highest-scoring students, highest first.
 * - getStudentsInScoreRange(low, high) returns a sorted list of student names
 *   whose grade is in [low, high] inclusive.
 *
 * Do not use explicit loops. Use streams and collectors.
 */
public class StudentGradeBook {

    private final Map<String, Double> grades;
    private final TreeMap<Double, String> gradeToStudent;

    public StudentGradeBook(Map<String, Double> grades) {
        // TODO: validate non-null; store a defensive copy
        this.grades = Map.copyOf(Objects.requireNonNull(grades, "Employee list cannot be null"));
        this.gradeToStudent = grades.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getValue, // key: value
                        Map.Entry::getKey,   // value: grade
                        (existing, replacement) -> existing, // if two students have the same grade, keep the first one
                        () -> new TreeMap<Double, String>(Comparator.reverseOrder()) // descending order by grade
                ));
    }

    /**
     * Returns a TreeMap so iteration visits students alphabetically.
     *
     */
    public TreeMap<String, Double> buildSortedGradeBook() {
        // TODO
        //we can just create a new TreeMap from the existing map, which will automatically sort the entries by key (student name)
        return new TreeMap<>(grades);
    }

    /**
     * Returns summary statistics (count, min, max, average, sum) over all grades.
     *
     */
    public DoubleSummaryStatistics getStatistics() {
        // TODO
        return grades.values().stream()
                .mapToDouble(Double::doubleValue)
                .summaryStatistics();
    }

    /**
     * Returns a TreeMap counting students per letter grade.
     *
     */
    public TreeMap<String, Long> getLetterGradeDistribution() {
        // TODO
        return grades.entrySet().stream()
                .collect(Collectors.groupingBy(
                        entry -> {
                            double grade = entry.getValue();
                            if (grade >= 90) return "A";
                            else if (grade >= 80) return "B";
                            else if (grade >= 70) return "C";
                            else if (grade >= 60) return "D";
                            else return "F";
                        },
                        TreeMap::new,
                        Collectors.counting()
                ));
    }


    /**
     * Returns the names of the n highest-scoring students, highest first.
     */
    public List<String> getTopStudents(int n) {
        return gradeToStudent.values().stream().limit(n).toList();
    }

    /**
     * Returns a sorted list of names whose grade falls in [low, high] inclusive.
     *
     */
    public List<String> getStudentsInScoreRange(double low, double high) {
        return this.gradeToStudent.subMap(high, true, low, true).values().stream()
                .sorted() // sort by student name
                .toList();
    }
}
