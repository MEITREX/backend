package de.unistuttgart.iste.meitrex.content_service.persistence.entity;

import de.unistuttgart.iste.meitrex.generated.dto.SkillType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.List;

/**
 * @implNote fields are nullable because media content does not have them, and we use single table inheritance.
 */
@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentMetadataEmbeddable {

    @Column(nullable = true)
    private int skillPoints;

    @Column(nullable = true)
    private List<SkillType> skillTypes;

    @Column(nullable = true)
    private Integer initialLearningInterval;
}
