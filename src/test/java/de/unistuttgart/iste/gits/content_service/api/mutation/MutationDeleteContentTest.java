package de.unistuttgart.iste.meitrex.content_service.api.mutation;

import de.unistuttgart.iste.meitrex.common.dapr.TopicPublisher;
import de.unistuttgart.iste.meitrex.common.event.CrudOperation;
import de.unistuttgart.iste.meitrex.common.testutil.*;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser.UserRoleInCourse;
import de.unistuttgart.iste.meitrex.content_service.TestData;
import de.unistuttgart.iste.meitrex.content_service.persistence.entity.*;
import de.unistuttgart.iste.meitrex.content_service.persistence.repository.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;

import java.util.*;

import static de.unistuttgart.iste.meitrex.common.testutil.TestUsers.userWithMembershipInCourseWithId;
import static graphql.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;


@ContextConfiguration(classes = MockTestPublisherConfiguration.class)
@GraphQlApiTest
@TablesToDelete({"content_tags", "user_progress_data", "section", "stage", "content"})
class MutationDeleteContentTest {

    @Autowired
    private ContentRepository contentRepository;
    @Autowired
    private UserProgressDataRepository userProgressRepository;
    @Autowired
    private StageRepository stageRepository;
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private SkillRepository skillRepository;

    private final UUID courseId = UUID.randomUUID();

    @InjectCurrentUserHeader
    private final LoggedInUser loggedInUser = userWithMembershipInCourseWithId(courseId, UserRoleInCourse.ADMINISTRATOR);

    @Autowired
    private TopicPublisher topicPublisher;

    @BeforeEach
    void beforeEach() {
        reset(topicPublisher);
    }

    /**
     * Given a UUID of an existing content
     * When the deleteContent mutation is executed
     * Then the content is deleted
     */
    @Test
    @Transactional
    @Commit
    void testDeleteExistingContent(final GraphQlTester graphQlTester) {
        ContentEntity contentEntity = contentRepository.save(TestData.dummyAssessmentEntityBuilder(courseId)
                .metadata(TestData.dummyContentMetadataEmbeddableBuilder(courseId)
                        .tags(new HashSet<>(Set.of("Tag", "Tag2")))
                        .build())
                        .items(List.of(TestData.dummyItemEntityBuilder(UUID.randomUUID()).build()))
                .build());
        contentEntity = contentRepository.save(contentEntity);

        final UserProgressDataEntity progress1 = UserProgressDataEntity.builder()
                .contentId(contentEntity.getId())
                .userId(UUID.randomUUID())
                .learningInterval(2)
                .build();
        userProgressRepository.save(progress1);

        final UserProgressDataEntity progress2 = UserProgressDataEntity.builder()
                .contentId(contentEntity.getId())
                .userId(UUID.randomUUID())
                .learningInterval(1)
                .build();
        userProgressRepository.save(progress2);
        final String query = """
                mutation($id: UUID!) {
                    mutateContent(contentId: $id){
                        deleteContent
                    }
                }
                """;

        graphQlTester.document(query)
                .variable("id", contentEntity.getId())
                .execute()
                .path("mutateContent.deleteContent").entity(UUID.class).isEqualTo(contentEntity.getId());

        // test that content is deleted
        assertThat(contentRepository.count(), is(0L));

        // test that user progress is deleted
        assertThat(userProgressRepository.count(), is(0L));

        assertThat(skillRepository.count(),is(0L));
        assertThat(itemRepository.count(),is(0L));

        verify(topicPublisher).notifyContentChanges(List.of(contentEntity.getId()), CrudOperation.DELETE);

    }


    /**
     * Given a UUID of an existing content
     * When the deleteContent mutation is executed
     * Then the content is deleted and all links to stages are removed
     */
    @Test
    @Transactional
    @Commit
    void testDeleteExistingContentLinkedToStage(final GraphQlTester graphQlTester) {

        final ContentEntity contentEntity = contentRepository.save(TestData.dummyAssessmentEntityBuilder(courseId)
                .metadata(TestData.dummyContentMetadataEmbeddableBuilder(courseId)
                        .tags(Set.of("Tag3", "Tag4"))
                        .build())
                .build());

        userProgressRepository.save(UserProgressDataEntity.builder()
                .contentId(contentEntity.getId())
                .userId(UUID.randomUUID())
                .learningInterval(1)
                .build());

        userProgressRepository.save(UserProgressDataEntity.builder()
                .contentId(contentEntity.getId())
                .userId(UUID.randomUUID())
                .learningInterval(2)
                .build());

        // add Section and Stage to db and link content to a stage
        final SectionEntity sectionEntity = sectionRepository.save(SectionEntity.builder()
                .stages(new HashSet<>())
                .name("TestSection")
                .chapterId(UUID.randomUUID())
                .courseId(courseId)
                .build());
        StageEntity stageEntity = StageEntity.builder()
                .sectionId(sectionEntity.getId())
                .position(0)
                .requiredContents(new HashSet<>())
                .optionalContents(new HashSet<>())
                .build();
        stageEntity.getRequiredContents().add(contentEntity);
        stageEntity = stageRepository.save(stageEntity);

        final String query = """
                mutation($id: UUID!) {
                    mutateContent(contentId: $id){
                        deleteContent
                    }
                }
                """;

        graphQlTester.document(query)
                .variable("id", contentEntity.getId())
                .execute()
                .path("mutateContent.deleteContent").entity(UUID.class).isEqualTo(contentEntity.getId());

        assertThat(contentRepository.findById(contentEntity.getId()).isEmpty(), is(true));
        assertThat(contentRepository.count(), is(0L));

        // assert content has been unlinked from Stages
        stageEntity = stageRepository.getReferenceById(stageEntity.getId());
        assertFalse(stageEntity.getRequiredContents().contains(contentEntity));

        // assert user progress has been deleted
        assertThat(userProgressRepository.count(), is(0L));
    }

    /**
     * Given a UUID of a non-existing content
     * When the deleteContent mutation is executed
     * Then an error is returned
     */
    @Test
    void testDeleteNonExistingContent(final GraphQlTester graphQlTester) {
        final UUID id = UUID.randomUUID();
        final String query = """
                mutation($id: UUID!) {
                    mutateContent(contentId: $id) {
                        deleteContent
                    }
                }
                """;

        graphQlTester.document(query)
                .variable("id", id)
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors.size(), is(1));
                    assertThat(responseErrors.get(0).getMessage(),
                            containsString("with id(s) " + id + " not found"));
                });

    }
}
