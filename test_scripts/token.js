import http from 'k6/http';
import { check } from 'k6';

export let options = {
    vus: 1,
    iterations: 1,
    insecureSkipTLSVerify: true,
};

export  function getToken () {

    const data = {
        grant_type: 'password',
        client_id: __ENV.K6_CLIENT_ID,
        username: __ENV.K6_USERNAME,
        password: __ENV.K6_PASSWORD,
    };

    const res = http.post(`${__ENV.K6_HOST}/realms/${__ENV.K6_REALM}/protocol/openid-connect/token`, data);

    console.log(res.json().access_token)

    check(res, {
        'has JWT access token': (r) => r.json().access_token.length > 0,
        'has JWT refresh token': (r) => r.json().refresh_token.length > 0,
    });

    return res.json();
}