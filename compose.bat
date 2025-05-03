@echo off
docker network create dapr-network
docker-compose ^
-f .\graphql_gateway\docker-compose.yml ^
-f .\media_service\docker-compose.yml ^
-f .\course_service\docker-compose.yml ^
-f .\content_service\docker-compose.yml ^
-f .\flashcard_service\docker-compose.yml ^
-f .\user_service\docker-compose.yml ^
-f .\reward_service\docker-compose.yml ^
-f .\quiz_service\docker-compose.yml ^
-f .\skilllevel_service\docker-compose.yml ^
-f .\docprocai_service\docker-compose.yml ^
-f .\frontend\docker-compose.yml ^
-f .\gamification_service\docker-compose.yml ^
--project-name gits ^
%*