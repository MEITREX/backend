package de.unistuttgart.iste.gits.content_service.service;

import de.unistuttgart.iste.meitrex.generated.dto.*;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static java.time.OffsetDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

class SuggestionServiceTest {

    private final SectionService sectionService = mock(SectionService.class);
    private final UserProgressDataService userProgressDataService = mock(UserProgressDataService.class);
    private final SuggestionService suggestionService = new SuggestionService(sectionService, userProgressDataService);

    @Test
    void testCreateSuggestionsNoSections() {
        // Arrange
        final List<UUID> chapterIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        doReturn(List.of()).when(sectionService).getSectionsByChapterIds(chapterIds);
        final UUID userId = UUID.randomUUID();
        // Act
        final List<Content> requiredContents = suggestionService.getAvailableRequiredContentsOfChaptersForUser(chapterIds, userId);
        final List<Content> optionalContents = suggestionService.getAvailableOptionalContentsOfChaptersForUser(chapterIds, userId);
        final List<Suggestion> actual = suggestionService.createSuggestions(requiredContents, optionalContents, userId, 5, List.of());
        // Assert
        assertThat(actual, is(empty()));
        // Verify
        verifyNoInteractions(userProgressDataService);
        verify(sectionService, times(2)).getSectionsByChapterIds(chapterIds);
    }

    @Test
    void testCreateSuggestionsNoStages() {
        // Arrange
        final List<UUID> chapterIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        final UUID userId = UUID.randomUUID();

        final Section section = Section.builder()
                .setChapterId(chapterIds.get(0))
                .setStages(List.of())
                .build();

        doReturn(List.of(List.of(section))).when(sectionService).getSectionsByChapterIds(chapterIds);
        // Act
        final List<Content> requiredContents = suggestionService.getAvailableRequiredContentsOfChaptersForUser(chapterIds, userId);
        final List<Content> optionalContents = suggestionService.getAvailableOptionalContentsOfChaptersForUser(chapterIds, userId);
        final List<Suggestion> actual = suggestionService.createSuggestions(requiredContents, optionalContents, userId, 5, List.of());

        // Assert
        assertThat(actual, is(empty()));
        // Verify
        verifyNoInteractions(userProgressDataService);
        verify(sectionService, times(2)).getSectionsByChapterIds(chapterIds);
    }

    @Test
    void testCreateSuggestionsSortByDueDate() {
        // Arrange
        final List<UUID> chapterIds = List.of(UUID.randomUUID());
        final UUID userId = UUID.randomUUID();

        final Section section = Section.builder()
                .setChapterId(chapterIds.get(0))
                .setStages(List.of(
                        Stage.builder()
                                .setRequiredContents(
                                        List.of(
                                                contentWithSuggestedDate(now().plusDays(1), "plus1"),
                                                contentWithSuggestedDate(now().minusDays(2), "minus2"),
                                                contentWithSuggestedDate(now().minusDays(1), "minus1"),
                                                contentWithSuggestedDate(now().plusDays(2), "plus2"),
                                                contentWithSuggestedDate(now().minusDays(5), "minus5")))
                                .setOptionalContents(List.of())
                                .build()))
                .build();

        doReturn(List.of(List.of(section))).when(sectionService).getSectionsByChapterIds(chapterIds);

        final UserProgressData userProgressData = UserProgressData.builder().setIsLearned(false).build();

        doReturn(userProgressData).when(userProgressDataService).getUserProgressData(any(), any());

        // Act
        final List<Content> requiredContents = suggestionService.getAvailableRequiredContentsOfChaptersForUser(chapterIds, userId);
        final List<Content> optionalContents = suggestionService.getAvailableOptionalContentsOfChaptersForUser(chapterIds, userId);
        final List<Suggestion> actual = suggestionService.createSuggestions(requiredContents, optionalContents, userId, 3, List.of());

        // Assert
        assertThat(actual.get(0).getContent().getMetadata().getName(), is("minus5"));
        assertThat(actual.get(1).getContent().getMetadata().getName(), is("minus2"));
        assertThat(actual.get(2).getContent().getMetadata().getName(), is("minus1"));
        assertThat(actual, hasSize(3));
        assertThat(actual.get(0).getType(), is(SuggestionType.NEW_CONTENT));
        assertThat(actual.get(1).getType(), is(SuggestionType.NEW_CONTENT));
        assertThat(actual.get(2).getType(), is(SuggestionType.NEW_CONTENT));

        // Verify
        verify(userProgressDataService, atLeastOnce()).getUserProgressData(any(), any());
        verify(sectionService, times(2)).getSectionsByChapterIds(chapterIds);
    }

    /**
     * Given four contents that were already learned, with different next learn dates
     * When the user requests suggestions
     * Then the content with the earliest next learn date should be preferred
     */
    @Test
    void testCreateSuggestionsSortByNextLearnDate() {
        // Arrange
        final List<UUID> chapterIds = List.of(UUID.randomUUID());
        final UUID userId = UUID.randomUUID();

        final Section section = Section.builder()
                .setChapterId(chapterIds.get(0))
                .setStages(List.of(
                        Stage.builder()
                                .setRequiredContents(
                                        List.of(
                                                contentWithSuggestedDate(now().minusDays(10), "minus5"),
                                                contentWithSuggestedDate(now().minusDays(10), "minus2"),
                                                contentWithSuggestedDate(now().minusDays(10), "minus1"),
                                                contentWithSuggestedDate(now().minusDays(10), "plus2")))
                                .setOptionalContents(List.of())
                                .build()))
                .build();

        doReturn(List.of(List.of(section))).when(sectionService).getSectionsByChapterIds(chapterIds);

        final UserProgressData progressDataMinus5 = UserProgressData.builder()
                .setIsLearned(true)
                .setIsDueForReview(true)
                .setNextLearnDate(now().minusDays(5))
                .build();
        final UserProgressData progressDataMinus2 = UserProgressData.builder()
                .setIsLearned(true)
                .setIsDueForReview(true)
                .setNextLearnDate(now().minusDays(2))
                .build();
        final UserProgressData progressDataMinus1 = UserProgressData.builder()
                .setIsLearned(true)
                .setIsDueForReview(true)
                .setNextLearnDate(now().minusDays(1))
                .build();
        final UserProgressData progressDataPlus2 = UserProgressData.builder()
                .setIsLearned(true)
                .setIsDueForReview(true)
                .setNextLearnDate(now().plusDays(2)).
                build();

        final UUID contentIdMinus5 = section.getStages().get(0).getRequiredContents().get(0).getId();
        final UUID contentIdMinus2 = section.getStages().get(0).getRequiredContents().get(1).getId();
        final UUID contentIdMinus1 = section.getStages().get(0).getRequiredContents().get(2).getId();
        final UUID contentIdPlus2 = section.getStages().get(0).getRequiredContents().get(3).getId();

        doReturn(progressDataMinus5).when(userProgressDataService).getUserProgressData(userId, contentIdMinus5);
        doReturn(progressDataMinus2).when(userProgressDataService).getUserProgressData(userId, contentIdMinus2);
        doReturn(progressDataMinus1).when(userProgressDataService).getUserProgressData(userId, contentIdMinus1);
        doReturn(progressDataPlus2).when(userProgressDataService).getUserProgressData(userId, contentIdPlus2);

        // Act
        final List<Content> requiredContents = suggestionService.getAvailableRequiredContentsOfChaptersForUser(chapterIds, userId);
        final List<Content> optionalContents = suggestionService.getAvailableOptionalContentsOfChaptersForUser(chapterIds, userId);
        final List<Suggestion> actual = suggestionService.createSuggestions(requiredContents, optionalContents, userId, 2, List.of());

        // Assert
        assertThat(actual, hasSize(2));
        assertThat(actual.get(0).getContent().getMetadata().getName(), is("minus5"));
        assertThat(actual.get(1).getContent().getMetadata().getName(), is("minus2"));
        assertThat(actual.get(0).getType(), is(SuggestionType.REPETITION));
        assertThat(actual.get(1).getType(), is(SuggestionType.REPETITION));

        // Verify
        verify(userProgressDataService).getUserProgressData(userId, contentIdMinus5);
        verify(userProgressDataService).getUserProgressData(userId, contentIdMinus2);
        verify(userProgressDataService).getUserProgressData(userId, contentIdMinus1);
        verify(userProgressDataService).getUserProgressData(userId, contentIdPlus2);
    }

    /**
     * Given two contents that are due for repetition
     * When the user requests suggestions
     * Then the contents are not considered
     */
    @Test
    void testContentNotDueForRepetitionIsNotConsidered() {
        // Arrange
        final List<UUID> chapterIds = List.of(UUID.randomUUID());
        final UUID userId = UUID.randomUUID();

        final Section section = Section.builder()
                .setChapterId(chapterIds.get(0))
                .setStages(List.of(
                        Stage.builder()
                                .setRequiredContents(
                                        List.of(
                                                contentWithSuggestedDate(now().minusDays(10), "minus5"),
                                                contentWithSuggestedDate(now().minusDays(10), "minus2")))
                                .setOptionalContents(List.of())
                                .build()))
                .build();

        doReturn(List.of(List.of(section))).when(sectionService).getSectionsByChapterIds(chapterIds);

        final UserProgressData progressNotDueForRepetition = UserProgressData.builder()
                .setIsLearned(true)
                .setIsDueForReview(false)
                .setNextLearnDate(now().plusDays(1))
                .build();

        doReturn(progressNotDueForRepetition).when(userProgressDataService).getUserProgressData(any(), any());

        // Act
        final List<Content> requiredContents = suggestionService.getAvailableRequiredContentsOfChaptersForUser(chapterIds, userId);
        final List<Content> optionalContents = suggestionService.getAvailableOptionalContentsOfChaptersForUser(chapterIds, userId);
        final List<Suggestion> actual = suggestionService.createSuggestions(requiredContents, optionalContents, userId, 2, List.of());

        // Assert
        assertThat(actual, is(empty()));

        // Verify
        verify(userProgressDataService, atLeastOnce()).getUserProgressData(any(), any());
    }

    /**
     * Given optional and required contents
     * When the user requests suggestions
     * Then the required contents should be preferred, even if they are due later
     */
    @Test
    void testOptionalContentLast() {
        // Arrange
        final List<UUID> chapterIds = List.of(UUID.randomUUID());
        final UUID userId = UUID.randomUUID();

        final Section section = Section.builder()
                .setChapterId(chapterIds.get(0))
                .setStages(List.of(
                        Stage.builder()
                                .setRequiredContents(
                                        List.of(
                                                contentWithSuggestedDate(now().plusDays(1), "plus1"),
                                                contentWithSuggestedDate(now().plusDays(2), "plus2")))
                                .setOptionalContents(List.of(
                                        contentWithSuggestedDate(now().minusDays(2), "minus2"),
                                        contentWithSuggestedDate(now().minusDays(1), "minus1")))
                                .build()))
                .build();

        doReturn(List.of(List.of(section))).when(sectionService).getSectionsByChapterIds(chapterIds);

        final UserProgressData userProgressData = UserProgressData.builder().setIsLearned(false).build();

        doReturn(userProgressData).when(userProgressDataService).getUserProgressData(any(), any());

        // Act
        final List<Content> requiredContents = suggestionService.getAvailableRequiredContentsOfChaptersForUser(chapterIds, userId);
        final List<Content> optionalContents = suggestionService.getAvailableOptionalContentsOfChaptersForUser(chapterIds, userId);
        final List<Suggestion> actual = suggestionService.createSuggestions(requiredContents, optionalContents, userId, 3, List.of());

        // Assert
        assertThat(actual.get(0).getContent().getMetadata().getName(), is("plus1"));
        assertThat(actual.get(1).getContent().getMetadata().getName(), is("plus2"));
        assertThat(actual.get(2).getContent().getMetadata().getName(), is("minus2"));
        assertThat(actual, hasSize(3));

        // Verify
        verify(userProgressDataService, atLeastOnce()).getUserProgressData(any(), any());
        verify(sectionService, times(2)).getSectionsByChapterIds(chapterIds);
    }

    /**
     * Given two contents with the same amount of days overdue, one of which is learned and one of which is not
     * When the user requests suggestions
     * Then the content that was not learned yet should be preferred
     */
    @Test
    void testNewContentsPreferred() {
        // Arrange
        final List<UUID> chapterIds = List.of(UUID.randomUUID());
        final UUID userId = UUID.randomUUID();

        final Section section = Section.builder()
                .setChapterId(chapterIds.get(0))
                .setStages(List.of(
                        Stage.builder()
                                .setRequiredContents(
                                        List.of(
                                                contentWithSuggestedDate(now().minusDays(10), "new"),
                                                contentWithSuggestedDate(now().minusDays(10), "repetition")))
                                .setOptionalContents(List.of())
                                .build()))
                .build();

        doReturn(List.of(List.of(section))).when(sectionService).getSectionsByChapterIds(chapterIds);

        final UserProgressData progressDataNew = UserProgressData.builder().setIsLearned(false).setNextLearnDate(now().minusDays(5)).build();
        final UserProgressData progressDataRepetition = UserProgressData.builder().setIsLearned(true).setNextLearnDate(now().minusDays(10)).build();

        final UUID contentIdNew = section.getStages().get(0).getRequiredContents().get(0).getId();
        final UUID contentIdRepetition = section.getStages().get(0).getRequiredContents().get(1).getId();

        doReturn(progressDataNew).when(userProgressDataService).getUserProgressData(userId, contentIdNew);
        doReturn(progressDataRepetition).when(userProgressDataService).getUserProgressData(userId, contentIdRepetition);

        // Act
        final List<Content> requiredContents = suggestionService.getAvailableRequiredContentsOfChaptersForUser(chapterIds, userId);
        final List<Content> optionalContents = suggestionService.getAvailableOptionalContentsOfChaptersForUser(chapterIds, userId);
        final List<Suggestion> actual = suggestionService.createSuggestions(requiredContents, optionalContents, userId, 1, List.of());

        // Assert
        assertThat(actual.get(0).getContent().getMetadata().getName(), is("new"));
        assertThat(actual, hasSize(1));
        assertThat(actual.get(0).getType(), is(SuggestionType.NEW_CONTENT));

        // Verify
        verify(userProgressDataService).getUserProgressData(userId, contentIdNew);
        verify(userProgressDataService).getUserProgressData(userId, contentIdRepetition);
        verify(sectionService, times(2)).getSectionsByChapterIds(chapterIds);
    }

    /**
     * Given content with different reward points with the same amount of days overdue
     * When the user requests suggestions
     * Then the content with more reward points should be preferred
     */
    @Test
    void testContentsWithMoreRewardPointsArePreferred() {
        // Arrange
        final List<UUID> chapterIds = List.of(UUID.randomUUID());
        final UUID userId = UUID.randomUUID();

        final Section section = Section.builder()
                .setChapterId(chapterIds.get(0))
                .setStages(List.of(
                        Stage.builder()
                                .setRequiredContents(
                                        List.of(
                                                contentWithSuggestedDateAndRewardPoints(now().minusDays(10), "lessPoints", 5),
                                                contentWithSuggestedDateAndRewardPoints(now().minusDays(10), "morePoints", 10)))
                                .setOptionalContents(List.of())
                                .build()))
                .build();

        doReturn(List.of(List.of(section))).when(sectionService).getSectionsByChapterIds(chapterIds);

        final UserProgressData progressDataMorePoints = UserProgressData.builder().setIsLearned(false).build();
        final UserProgressData progressDataLessPoints = UserProgressData.builder().setIsLearned(false).build();

        final UUID contentIdMorePoints = section.getStages().get(0).getRequiredContents().get(0).getId();
        final UUID contentIdLessPoints = section.getStages().get(0).getRequiredContents().get(1).getId();

        doReturn(progressDataMorePoints).when(userProgressDataService).getUserProgressData(userId, contentIdMorePoints);
        doReturn(progressDataLessPoints).when(userProgressDataService).getUserProgressData(userId, contentIdLessPoints);

        // Act
        final List<Content> requiredContents = suggestionService.getAvailableRequiredContentsOfChaptersForUser(chapterIds, userId);
        final List<Content> optionalContents = suggestionService.getAvailableOptionalContentsOfChaptersForUser(chapterIds, userId);
        final List<Suggestion> actual = suggestionService.createSuggestions(requiredContents, optionalContents, userId, 1, List.of());

        // Assert
        assertThat(actual.get(0).getContent().getMetadata().getName(), is("morePoints"));
        assertThat(actual, hasSize(1));
        assertThat(actual.get(0).getType(), is(SuggestionType.NEW_CONTENT));

        // Verify
        verify(userProgressDataService).getUserProgressData(userId, contentIdMorePoints);
        verify(userProgressDataService).getUserProgressData(userId, contentIdLessPoints);
        verify(sectionService, times(2)).getSectionsByChapterIds(chapterIds);
    }

    /**
     * Given content with skill type ANALYSE and APPLY
     * When the user requests suggestions for skill type APPLY
     * Then only content with skill type APPLY should be considered
     */
    @Test
    void testSkillLevelFilter() {
        // Arrange
        final List<UUID> chapterIds = List.of(UUID.randomUUID());
        final UUID userId = UUID.randomUUID();

        final Section section = Section.builder()
                .setChapterId(chapterIds.get(0))
                .setStages(List.of(
                        Stage.builder()
                                .setRequiredContents(
                                        List.of(
                                                assessmentWithSuggestedDateAndSkillType(now().minusDays(10), "skill1", List.of(SkillType.ANALYSE)),
                                                assessmentWithSuggestedDateAndSkillType(now().minusDays(10), "skill2", List.of(SkillType.APPLY)),
                                                contentWithSuggestedDate(now().minusDays(10), "noSkill")))
                                .setOptionalContents(List.of())
                                .build()))
                .build();

        doReturn(List.of(List.of(section))).when(sectionService).getSectionsByChapterIds(chapterIds);

        final UserProgressData progressDataAll = UserProgressData.builder().setIsLearned(false).build();

        doReturn(progressDataAll).when(userProgressDataService).getUserProgressData(eq(userId), any());

        // Act
        final List<Content> requiredContents = suggestionService.getAvailableRequiredContentsOfChaptersForUser(chapterIds, userId);
        final List<Content> optionalContents = suggestionService.getAvailableOptionalContentsOfChaptersForUser(chapterIds, userId);
        final List<Suggestion> actual = suggestionService.createSuggestions(requiredContents, optionalContents, userId, 3, List.of(SkillType.APPLY));

        // Assert
        assertThat(actual.get(0).getContent().getMetadata().getName(), is("skill2"));
        assertThat(actual, hasSize(1));

        // Verify
        verify(userProgressDataService, atLeastOnce()).getUserProgressData(eq(userId), any());
        verify(sectionService, times(2)).getSectionsByChapterIds(chapterIds);
    }

    /**
     * Given a section with 3 stages, where the first stage is completed and the second stage is not completed
     * When the user requests 2 suggestions
     * Then the third stage should not be considered
     */
    @Test
    void testOnlyUnlockedContentsAreConsidered() {
        // Arrange
        final List<UUID> chapterIds = List.of(UUID.randomUUID());
        final UUID userId = UUID.randomUUID();

        final Section section = Section.builder()
                .setChapterId(chapterIds.get(0))
                .setStages(List.of(
                        Stage.builder()
                                .setRequiredContents(
                                        List.of(
                                                contentWithSuggestedDate(now().minusDays(1), "unlocked1"),
                                                contentWithSuggestedDate(now().minusDays(2), "unlocked2")))
                                .setOptionalContents(List.of())
                                .build(),
                        Stage.builder()
                                .setRequiredContents(
                                        List.of(
                                                contentWithSuggestedDate(now().minusDays(3), "unlocked3"),
                                                contentWithSuggestedDate(now().minusDays(4), "unlocked4")))
                                .setOptionalContents(List.of())
                                .build(),
                        Stage.builder()
                                .setRequiredContents(
                                        List.of(
                                                contentWithSuggestedDate(now().minusDays(10), "locked"),
                                                contentWithSuggestedDate(now().minusDays(10), "locked")))
                                .setOptionalContents(List.of())
                                .build()))
                .build();

        doReturn(List.of(List.of(section))).when(sectionService).getSectionsByChapterIds(chapterIds);

        final UserProgressData progressDataLearned = UserProgressData.builder().setIsLearned(true).setNextLearnDate(now().minusDays(1)).build();
        final UserProgressData progressDataNotLearned = UserProgressData.builder().setIsLearned(false).build();

        final UUID contentIdUnlocked1 = section.getStages().get(0).getRequiredContents().get(0).getId();
        final UUID contentIdUnlocked2 = section.getStages().get(0).getRequiredContents().get(1).getId();
        final UUID contentIdUnlocked3 = section.getStages().get(1).getRequiredContents().get(0).getId();
        final UUID contentIdUnlocked4 = section.getStages().get(1).getRequiredContents().get(1).getId();

        doReturn(progressDataLearned).when(userProgressDataService).getUserProgressData(userId, contentIdUnlocked1);
        doReturn(progressDataLearned).when(userProgressDataService).getUserProgressData(userId, contentIdUnlocked2);
        doReturn(progressDataNotLearned).when(userProgressDataService).getUserProgressData(userId, contentIdUnlocked3);
        doReturn(progressDataNotLearned).when(userProgressDataService).getUserProgressData(userId, contentIdUnlocked4);

        // Act
        final List<Content> requiredContents = suggestionService.getAvailableRequiredContentsOfChaptersForUser(chapterIds, userId);
        final List<Content> optionalContents = suggestionService.getAvailableOptionalContentsOfChaptersForUser(chapterIds, userId);
        final List<Suggestion> actual = suggestionService.createSuggestions(requiredContents, optionalContents, userId, 2, List.of());

        // Assert
        assertThat(actual.get(0).getContent().getMetadata().getName(), is("unlocked4"));
        assertThat(actual.get(1).getContent().getMetadata().getName(), is("unlocked3"));
        assertThat(actual, hasSize(2));

        // Verify
        verify(userProgressDataService, atLeastOnce()).getUserProgressData(userId, contentIdUnlocked1);
        verify(userProgressDataService, atLeastOnce()).getUserProgressData(userId, contentIdUnlocked2);
        verify(userProgressDataService, atLeastOnce()).getUserProgressData(userId, contentIdUnlocked3);
        verify(userProgressDataService, atLeastOnce()).getUserProgressData(userId, contentIdUnlocked4);
        verify(sectionService, atLeastOnce()).getSectionsByChapterIds(chapterIds);
    }

    private Content contentWithSuggestedDate(final OffsetDateTime suggestedDate, final String name) {
        return contentWithMetadata(ContentMetadata.builder()
                .setSuggestedDate(suggestedDate)
                .setName(name)
                .build());
    }

    private Assessment assessmentWithSuggestedDateAndSkillType(final OffsetDateTime suggestedDate, final String name, final List<SkillType> skillTypes) {
        return FlashcardSetAssessment.builder()
                .setMetadata(ContentMetadata.builder()
                        .setSuggestedDate(suggestedDate)
                        .setName(name)
                        .build())
                .setAssessmentMetadata(AssessmentMetadata.builder()
                        .setSkillTypes(skillTypes)
                        .build())
                .build();
    }

    private Content contentWithSuggestedDateAndRewardPoints(final OffsetDateTime suggestedDate, final String name, final int rewardPoints) {
        return contentWithMetadata(ContentMetadata.builder()
                .setSuggestedDate(suggestedDate)
                .setName(name)
                .setRewardPoints(rewardPoints)
                .build());
    }

    private Content contentWithMetadata(final ContentMetadata metadata) {
        return MediaContent.builder()
                .setId(UUID.randomUUID())
                .setMetadata(metadata)
                .build();
    }
}
