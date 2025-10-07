@echo off
for %%f in (*.js) do (
    echo Running %%f ...
    k6 run -e K6_HOST=https://dev.meitrex.de/keycloak ^
           -e K6_REALM=GITS ^
           -e K6_CLIENT_ID=frontend ^
           -e K6_USERNAME=test ^
           -e K6_PASSWORD=test ^
           -e k6_GATEWAY_URL=https://dev.meitrex.de/graphql ^
           %%f
    if errorlevel 1 (
        echo Test %%f failed!
        exit /b 1
    )
)
echo All tests completed.

