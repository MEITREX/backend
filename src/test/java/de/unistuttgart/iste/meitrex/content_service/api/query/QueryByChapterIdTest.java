package de.unistuttgart.iste.meitrex.content_service.api.query;

import de.unistuttgart.iste.meitrex.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.meitrex.common.testutil.InjectCurrentUserHeader;
import de.unistuttgart.iste.meitrex.common.testutil.TablesToDelete;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser.UserRoleInCourse;
import de.unistuttgart.iste.meitrex.content_service.TestData;
import de.unistuttgart.iste.meitrex.content_service.persistence.entity.ContentEntity;
import de.unistuttgart.iste.meitrex.content_service.persistence.repository.ContentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.List;
import java.util.UUID;

import static de.unistuttgart.iste.meitrex.common.testutil.TestUsers.userWithMembershipInCourseWithId;

@GraphQlApiTest
class QueryByChapterIdTest {

    @Autowired
    private ContentRepository contentRepository;

    private final UUID courseId = UUID.randomUUID();

    @InjectCurrentUserHeader
    private final LoggedInUser loggedInUser = userWithMembershipInCourseWithId(courseId, UserRoleInCourse.STUDENT);

    /**
     * Given valid chapterIds
     * When the queryByChapterId query is called
     * Then the content is returned, correctly grouped and filtered by chapterId
     */
    @Test
    void testQueryByChapterId(final GraphQlTester graphQlTester) {
        final List<UUID> chapterIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        chapterIds.stream()
                .<ContentEntity>map(chapterId -> (
                        TestData.dummyMediaContentEntityBuilder(courseId)
                                .metadata(TestData.dummyContentMetadataEmbeddableBuilder(courseId)
                                        .chapterId(chapterId)
                                        .build()))
                        .build())
                .forEach(contentRepository::save);

        final String query = """
                query($chapterIds: [UUID!]!) {
                    contentsByChapterIds(chapterIds: $chapterIds) {
                        id
                        metadata {
                            chapterId
                        }
                    }
                }
                """;

        graphQlTester.document(query)
                .variable("chapterIds", chapterIds.subList(0, 3))
                .execute()
                .path("contentsByChapterIds[*][*].metadata.chapterId")
                .entityList(UUID.class)
                .containsExactly(chapterIds.get(0), chapterIds.get(1), chapterIds.get(2));
    }
}
