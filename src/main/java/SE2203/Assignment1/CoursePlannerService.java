package SE2203.Assignment1;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer: Manages the in-memory list of assessments[cite: 119].
 */
@Service
public class CoursePlannerService {
    private final List<Assessment> assessments = new ArrayList<>();

    public List<Assessment> findAll() {
        return new ArrayList<>(assessments);
    }

    public void save(Assessment assessment) {
        if (!assessments.contains(assessment)) {
            assessments.add(assessment);
        }
    }

    public void delete(Assessment assessment) {
        assessments.remove(assessment);
    }

    public boolean isNameUnique(String name, Assessment current) {
        return assessments.stream()
                .noneMatch(a -> a.getName().equalsIgnoreCase(name) && a != current);
    }
}