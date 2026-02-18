package SE2203.Assignment1;

import java.util.List;

/**
 * Service layer: Pure functions for grade math[cite: 120].
 * All numeric values rounded to 1 decimal place in the UI[cite: 83].
 */
public class GradeCalculator {

    public static double calculateTotalMarkedWeight(List<Assessment> assessments) {
        return assessments.stream()
                .filter(Assessment::isMarked)
                .mapToDouble(Assessment::getWeight)
                .sum();
    }

    public static double calculateWeightedGradeSoFar(List<Assessment> assessments) {
        double totalWeight = calculateTotalMarkedWeight(assessments);
        if (totalWeight == 0) return 0.0;

        double weightedSum = assessments.stream()
                .filter(Assessment::isMarked)
                .mapToDouble(a -> (a.getMark() / 100.0) * a.getWeight())
                .sum();

        return (weightedSum / totalWeight) * 100.0;
    }

    public static double calculateRequiredAverage(List<Assessment> assessments, double targetOverall) {
        double currentWeightedSum = assessments.stream()
                .filter(Assessment::isMarked)
                .mapToDouble(a -> (a.getMark() / 100.0) * a.getWeight())
                .sum();

        double remainingWeight = 100.0 - calculateTotalMarkedWeight(assessments);
        if (remainingWeight <= 0) return 0.0;

        // Formula: (targetOverall - weightedSoFar) / (remainingWeight / 100) [cite: 81, 82]
        return ((targetOverall - currentWeightedSum) / (remainingWeight / 100.0));
    }
}