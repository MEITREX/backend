@echo off
k6 run -e K6_HOST=http://localhost:9009 ^
       -e K6_REALM=GITS ^
       -e K6_CLIENT_ID=gits-frontend ^
       -e K6_USERNAME=test ^
       -e K6_PASSWORD=test ^
       -e k6_GATEWAY_URL=http://localhost:8080/graphql ^
%*

