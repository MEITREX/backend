@echo off
docker-compose ^
-f .\graphql_gateway\docker-compose.yml ^
-f .\media_service\docker-compose.yml ^
-f .\course_service\docker-compose.yml ^
-f .\content_service\docker-compose.yml ^
-f .\flashcard_service\docker-compose.yml ^
-f .\user_service\docker-compose.yml ^
-f .\reward_service\docker-compose.yml ^
-f .\gits-frontend\docker-compose.yml ^
--project-name gits ^
%*