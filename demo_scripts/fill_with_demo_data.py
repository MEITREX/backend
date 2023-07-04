# This script fills a server with its gateway at GATEWAY_URL with some demo data
# Requires the "gql" graphql client

GATEWAY_URL = "http://127.0.0.1:8080/graphql"

from gql import gql, Client
from gql.transport.aiohttp import AIOHTTPTransport

user_token = input("Please enter a valid user token (can be found by looking at the HTTP authorization header in your browser's network inspector when logged into the GITS frontend):\n> ")

transport = AIOHTTPTransport(url=GATEWAY_URL, headers={
    "authorization": user_token
})

client = Client(transport=transport, fetch_schema_from_transport=True)

# create a course
query = gql(
    """
    mutation {
        pseCourse: createCourse(input: {
            title: "Programmierung und Softwareentwicklung",
            description: "Einführung in die Programmierung und Softwarentwicklung mit Java.",
            startDate: "2023-06-10T12:39:12.365Z",
            endDate: "2025-06-19T12:39:12.365Z",
            published: true
        }) { id }
        tiCourse: createCourse(input: {
            title: "Theoretische Informatik 1",
            description: "Formale Sprachen und Automatentheorie",
            startDate: "2023-06-10T12:39:12.365Z",
            endDate: "2025-06-10T12:39:12.365Z",
            published: true
        }) { id }
        dsaCourse: createCourse(input: {
            title: "Datenstrukturen und Algorithmen",
            description: "Grundlagen des Arbeitens mit Datenstrukturen.",
            startDate: "2023-06-10T12:39:12.365Z",
            endDate: "2025-06-10T12:39:12.365Z",
            published: true
        }) { id }
    }
    """
)
courseRes = client.execute(query)

# create chapters in the pse course
query = gql(
    """
    mutation ($courseId: UUID!) {
        chapter1: createChapter(input: {
            title: "Java Grundlagen 1: Einführung und Kontrollfluss",
            description: "Grundlagen der Java-Programmierung: Einführung, If-Conditionals, Schleifen",
            number: 1,
            startDate: "2023-06-10T12:39:12.365Z",
            endDate: "2025-06-19T12:39:12.365Z",
            courseId: $courseId
        }) { id }
        chapter2: createChapter(input: {
            title: "Java Grundlagen 2: Klassen",
            description: "Grundlagen der Java-Programmierung: Klassen und Vererbung",
            number: 2,
            startDate: "2023-06-10T12:39:12.365Z",
            endDate: "2025-06-19T12:39:12.365Z",
            courseId: $courseId
        }) { id }
    }
    """
)
params = {
    "courseId": courseRes["pseCourse"]["id"]
}
pseChapterRes = client.execute(query, variable_values=params)

# create chapters in the dsa course
query = gql(
    """
    mutation ($courseId: UUID!) {
        chapter1: createChapter(input: {
            title: "DSA 1: Listen und Arrays",
            description: "Kennenlernen der grundlegenden Datenstrukturen.",
            number: 1,
            startDate: "2023-06-10T12:39:12.365Z",
            endDate: "2025-06-19T12:39:12.365Z",
            courseId: $courseId
        }) { id }
        chapter2: createChapter(input: {
            title: "DSA 2: Mehrdimensionale Arrays",
            description: "Verwendung von mehrdimensionalen Arrays und Listen.",
            number: 2,
            startDate: "2023-06-10T12:39:12.365Z",
            endDate: "2025-06-19T12:39:12.365Z",
            courseId: $courseId
        }) { id }
        chapter3: createChapter(input: {
            title: "DSA 3: Binärbäume",
            description: "Erzeugen und Verwenden von Binärbäumen.",
            number: 3,
            startDate: "2023-06-10T12:39:12.365Z",
            endDate: "2025-06-19T12:39:12.365Z",
            courseId: $courseId
        }) { id }
    }
    """
)
params = {
    "courseId": courseRes["dsaCourse"]["id"]
}
dsaChapterRes = client.execute(query, variable_values=params)

# create chapters in the ti course
query = gql(
    """
    mutation ($courseId: UUID!) {
        chapter1: createChapter(input: {
            title: "Deterministische Endliche Automaten",
            description: "",
            number: 1,
            startDate: "2023-06-10T12:39:12.365Z",
            endDate: "2025-06-19T12:39:12.365Z",
            courseId: $courseId
        }) { id }
        chapter2: createChapter(input: {
            title: "Nichtdeterministische Endliche Automaten",
            description: "",
            number: 2,
            startDate: "2023-06-10T12:39:12.365Z",
            endDate: "2025-06-19T12:39:12.365Z",
            courseId: $courseId
        }) { id }
        chapter3: createChapter(input: {
            title: "Kellerautomaten",
            description: "",
            number: 3,
            startDate: "2023-06-10T12:39:12.365Z",
            endDate: "2025-06-19T12:39:12.365Z",
            courseId: $courseId
        }) { id }
    }
    """
)
params = {
    "courseId": courseRes["tiCourse"]["id"]
}
tiChapterRes = client.execute(query, variable_values=params)

# create content in the pse chapters
query = gql(
    """
    mutation ($chapter1Id: UUID!, $chapter2Id: UUID!) {
        chapter1Content: createMediaContent(input: {
            metadata: {
                name: "PSE Vorlesung 1 Materialien",
                type: MEDIA,
                rewardPoints: 1,
                suggestedDate: "2023-06-10T12:39:12.365Z",
                chapterId: $chapter1Id 
            }
        }) { id }
        cheatSheetContent: createMediaContent(input: {
            metadata: {
                name: "Java Cheat Sheet",
                type: MEDIA,
                rewardPoints: 0,
                suggestedDate: "2023-06-10T12:39:12.365Z",
                chapterId: $chapter1Id
            }
        }) { id }
        chapter1Flashcards: createAssessment(input: {
            metadata: {
                name: "Chapter 1 Flashcards",
                type: FLASHCARDS,
                rewardPoints: 2,
                suggestedDate: "2023-06-10T12:39:12.365Z",
                chapterId: $chapter1Id
            },
            assessmentMetadata: {
                skillPoints: 3,
                skillType: REMEMBER,
                initialLearningInterval: 3
            }
        }) { id }
        chapter2Content: createMediaContent(input: {
            metadata: {
                name: "PSE Vorlesung 2 Materialien",
                type: MEDIA,
                rewardPoints: 1,
                suggestedDate: "2023-06-10T12:39:12.365Z",
                chapterId: $chapter2Id 
            }
        }) { id }
    }
    """
)
params = {
    "chapter1Id": pseChapterRes["chapter1"]["id"],
    "chapter2Id": pseChapterRes["chapter2"]["id"]
}
contentRes = client.execute(query, variable_values=params)

# create media records
query = gql(
    """
    mutation ($chapter1Content: UUID!, $cheatSheetContent: UUID!) {
        mediaVideo: createMediaRecord(input: {
            name: "PSE VL-Video 1",
            type: VIDEO,
            contentIds: [$chapter1Content]
        }) { id }
        mediaPresentation: createMediaRecord(input: {
            name: "Folien VL 1",
            type: PRESENTATION,
            contentIds: [$chapter1Content]
        }) { id }
        javaCheatSheet: createMediaRecord(input: {
            name: "Java Cheat Sheet",
            type: DOCUMENT,
            contentIds: [$cheatSheetContent]
        }) { id }
    }
    """
)
params = {
    "chapter1Content": contentRes["chapter1Content"]["id"],
    "cheatSheetContent": contentRes["cheatSheetContent"]["id"]
}
mediaRes = client.execute(query, variable_values=params)

# create flashcard set
query = gql(
    """
    mutation ($assessmentId: UUID!) {
        flashcardSet: createFlashcardSet(input: {
            assessmentId: $assessmentId,
            flashcards: [
                { sides: [
                    { label: "Question", text: "What is a *string*?", isQuestion: true },
                    { label: "Answer", text: "A sequence of text characters.", isQuestion: false }
                ] },
                { sides: [
                    { label: "Question", text: "What is a *char*?", isQuestion: true },
                    { label: "Answer", text: "A single text character.", isQuestion: false }
                ] },
                { sides: [
                    { label: "Question", text: "In Java and C#, the *static* keyword has different meanings when used on classes. What are they?", isQuestion: true },
                    { label: "Static Classes in C#", text: "In C#, a static class is a class whose members are also all defined as static.", isQuestion: false },
                    { label: "Static Classes in Java", text: "In Java, only nested classes can be declared static. A static nested class can be instantiated without an instance of the outer class.", isQuestion: false }
                ] }
            ]
        }) { assessmentId }
    }
    """
)
params = {
    "assessmentId": contentRes["chapter1Flashcards"]["id"]
}
flashcardsRes = client.execute(query, variable_values=params)