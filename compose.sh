# comment out services that you don't need
# new services need to be added as they get worked on
# make sure that all projects are in the same folder e.g. gits
docker network create dapr-network
docker compose \
-f ./graphql_gateway/docker-compose.yml \
-f ./media_service/docker-compose.yml \
-f ./course_service/docker-compose.yml \
-f ./content_service/docker-compose.yml \
-f ./flashcard_service/docker-compose.yml \
-f ./user_service/docker-compose.yml \
-f ./reward_service/docker-compose.yml \
-f ./quiz_service/docker-compose.yml \
-f ./assignment_service/docker-compose.yml \
-f ./skilllevel_service/docker-compose.yml \
-f ./docprocai_service/docker-compose.yml \
-f ./frontend/docker-compose.yml \
-f ./gamification_service/docker-compose.yml \
-f ./tutor_service/docker-compose.yml \
--project-name gits \
$@
