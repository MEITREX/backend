# This script fills a server with its gateway at GATEWAY_URL with some demo data
# Requires the "gql" graphql client
import urllib
import json

GATEWAY_URL = "http://127.0.0.1:8080/graphql"
KEYCLOAK_URL = "http://localhost:9009/realms/GITS/protocol/openid-connect/token"
#GATEWAY_URL = "http://orange.informatik.uni-stuttgart.de/graphql"
#KEYCLOAK_URL = "https://orange.informatik.uni-stuttgart.de/keycloak/"

from gql import gql, Client
from gql.transport.aiohttp import AIOHTTPTransport

user_name = input("Please enter your user name: ")
user_password = input("Please enter your password: ")

data = {
    "grant_type": "password",
    "client_id": "frontend",
    "username": user_name,
    "password": user_password
}

data = urllib.parse.urlencode(data).encode("utf-8")
req = urllib.request.Request(KEYCLOAK_URL, data=data)
with urllib.request.urlopen(req) as response:
    user_token = json.loads(response.read())["access_token"]

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
    mutation ($chapter1Content: UUID!, $chapter2Content: UUID! $cheatSheetContent: UUID!) {
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
        c2Video: createMediaRecord(input: {
            name: "PSE VL-Video 2",
            type: VIDEO,
            contentIds: [$chapter2Content]
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
    "cheatSheetContent": contentRes["cheatSheetContent"]["id"],
    "chapter2Content": contentRes["chapter2Content"]["id"]
}
mediaRes = client.execute(query, variable_values=params)

# create flashcard set assessment
query = gql(
    """
    mutation ($chapter1Id: UUID!) {
        flashcardSetAssessment: createFlashcardSetAssessment(
            assessmentInput: {
                metadata: {
                    name: "Chapter 1 Flashcards",
                    type: FLASHCARDS,
                    rewardPoints: 2,
                    suggestedDate: "2023-06-10T12:39:12.365Z",
                    chapterId: $chapter1Id
                },
                assessmentMetadata: {
                    skillPoints: 3,
                    skillTypes: [REMEMBER],
                    initialLearningInterval: 3
                }
            }
            flashcardSetInput: {
                flashcards: [
                    { sides: [
                        {
                            label: "Question",
                            text: "What is a *string*?",
                            isQuestion: true,
                            isAnswer: false
                        },
                        {
                            label: "Answer",
                            text: "A sequence of text characters.",
                            isQuestion: false,
                            isAnswer: true
                        }
                    ] },
                    { sides: [
                        {
                            label: "Question",
                            text: "What is a *char*?",
                            isQuestion: true,
                            isAnswer: false
                        },
                        {
                            label: "Answer",
                            text: "A single text character.",
                            isQuestion: false,
                            isAnswer: true
                        }
                    ] },
                    { sides: [
                        {
                            label: "Question",
                            text: "In Java and C#, the *static* keyword has different meanings when used on classes. What are they?",
                            isQuestion: true,
                            isAnswer: false
                        },
                        {
                            label: "Static Classes in C#",
                            text: "In C#, a static class is a class whose members are also all defined as static.",
                            isQuestion: false,
                            isAnswer: true
                        },
                        {
                            label: "Static Classes in Java",
                            text: "In Java, only nested classes can be declared static. A static nested class can be instantiated without an instance of the outer class.",
                            isQuestion: false,
                            isAnswer: true
                        }
                    ] }
                ]
            }
        ) { id }
    }
    """
)
params = {
    "chapter1Id": pseChapterRes["chapter1"]["id"]
}
flashcardsRes = client.execute(query, variable_values=params)

# create quiz
query = gql(
    """
    mutation ($chapterId: UUID!) {
        createQuizAssessment(
          assessmentInput: {
            metadata: {
              name: "TestQuiz",
              type: QUIZ,
              suggestedDate: "2023-06-10T12:39:12.365Z",
              rewardPoints: 1,
              tagNames: []
          		chapterId: $chapterId
            }
            assessmentMetadata: {
              skillPoints: 1,
              skillTypes: [REMEMBER],
              initialLearningInterval: 1
            }
          },
          quizInput: {
            requiredCorrectAnswers: 1,
            questionPoolingMode: RANDOM,
            numberOfRandomlySelectedQuestions: 1
          }
        ) {
          id
        }
    }
    """
)
params = {
    "chapterId": pseChapterRes["chapter1"]["id"]
}
quizRes = client.execute(query, variable_values=params)

# add questions to quiz
query = gql(
    """
    mutation ($quizId: UUID!) {
        mutateQuiz(assessmentId: $quizId) {
            addMultipleChoiceQuestion(input: {
                number: 1
                text: "What is German food"
              	hint: "Are you stupid?"
                answers: [
                  {
                    answerText: "Brot",
                    correct: true,
                    feedback: "Good"
                  },
                  {
                    answerText: "Curry"
                    correct: false
                    feedback: "Aber ganz sicher nicht, außer du meinst damit Currywurst."
                  }
                ]
              }
            ) { assessmentId }
        }
    }
    """
)
params = {
    "quizId": quizRes["createQuizAssessment"]["id"]
}
quizQuestionRes = client.execute(query, variable_values=params)


# create section
query = gql(
    """
    mutation($chapter1Id: UUID!, $chapter2Id: UUID!) {
        c1Section: createSection(input: {
            chapterId: $chapter1Id,
            name: "Test Section"
        }) { id }
        c2Section: createSection(input: {
            chapterId: $chapter2Id,
            name: "Test Section2"
        }) { id }
    }
    """
)
params = {
    "chapter1Id": pseChapterRes["chapter1"]["id"],
    "chapter2Id": pseChapterRes["chapter2"]["id"]
}
sectionRes = client.execute(query, variable_values=params)

# create stages
query = gql(
    """
    mutation ($sectionId: UUID!, $c2sectionId: UUID!, $s1RequiredContents: [UUID!]!, $s2RequiredContents: [UUID!]!, $c2s1RequiredContents: [UUID!]!) {
        c1s1: mutateSection(sectionId: $sectionId) {
            s1: createStage(input: {
                requiredContents: $s1RequiredContents
                optionalContents: []
            }) { id }
            s2: createStage(input: {
                requiredContents: $s2RequiredContents
                optionalContents: []
            }) { id }
        }
        c2s1: mutateSection(sectionId: $c2sectionId) {
            createStage(input: {
                requiredContents: $c2s1RequiredContents
                optionalContents: []
            }) { id }
        }
    }
    """
)
params = {
    "sectionId": sectionRes["c1Section"]["id"],
    "c2sectionId": sectionRes["c2Section"]["id"],
    "s1RequiredContents": [contentRes["chapter1Content"]["id"]],
    "s2RequiredContents": [
        flashcardsRes["flashcardSetAssessment"]["id"],
        quizRes["createQuizAssessment"]["id"]
    ],
    "c2s1RequiredContents": [contentRes["chapter2Content"]["id"]]
}
stageRes = client.execute(query, variable_values=params)