package de.unistuttgart.iste.gits.content_service.persistence.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Entity(name = "Stage")
@Table(indexes = {
        @Index(name = "idx_stage_section_id", columnList = "section_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, name = "section_id")
    private UUID sectionId;

    @Column(nullable = false)
    private int position;

    @ManyToMany(cascade = CascadeType.PERSIST)
    Set<ContentEntity> requiredContents;

    @ManyToMany(cascade = CascadeType.PERSIST)
    Set<ContentEntity> optionalContents;
}
