import http from 'k6/http';
import { check } from 'k6';
import { sleep } from 'k6';
import {getToken} from "./token.js";

export const options = {
  stages: [
	{ duration: '20s', target: 0 },
	{ duration: '30s', target: 50 },
	{ duration: '30s', target: 100 },
	{ duration: '30s', target: 200 },
	{ duration: '30s', target: 300 },
	{ duration: '30s', target: 400 },
	{ duration: '30s', target: 500 },
  ],
  insecureSkipTLSVerify: true
};

export function setup() {
    return getToken();
}

export default function (data) {
    const headers = {
        'Authorization': `Bearer ${data.access_token}`,
        'Content-Type': 'application/json',
    }

  const res = http.get('https://orange.informatik.uni-stuttgart.de',  {
      headers: headers});
    console.log(res.status)

  check(res, {
	  'is status 200': (r) => r.status === 200,
  });
}
