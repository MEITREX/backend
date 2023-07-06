# comment out services that you don't need
# new services need to be added as they get worked on
# make sure that all projects are in the same folder e.g. gits
docker compose \
-f ./graphql_gateway/docker-compose.yml \
-f ./media_service/docker-compose.yml \
-f ./course_service/docker-compose.yml \
-f ./content_service/docker-compose.yml \
-f ./flashcard_service/docker-compose.yml \
-f ./user_service/docker-compose.yml \
-f ./reward_service/docker-compose.yml \
-f ./gits-frontend/docker-compose.yml \
--project-name gits \
$@
