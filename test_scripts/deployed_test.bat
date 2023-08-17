@echo off
k6 run -e K6_HOST=https://orange.informatik.uni-stuttgart.de/keycloak ^
       -e K6_REALM=GITS ^
       -e K6_CLIENT_ID=gits-frontend ^
       -e K6_USERNAME=test ^
       -e K6_PASSWORD=test ^
       -e k6_GATEWAY_URL=https://orange.informatik.uni-stuttgart.de/graphql ^
%*

