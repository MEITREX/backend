import http from 'k6/http';
import { check } from 'k6';
import { sleep } from 'k6';

export const options = {
  stages: [
	{ duration: '20s', target: 0 },
	{ duration: '30s', target: 50 },
	{ duration: '30s', target: 100 },
	{ duration: '30s', target: 200 },
	{ duration: '30s', target: 300 },
	{ duration: '30s', target: 400 },
	{ duration: '30s', target: 500 },
	{ duration: '30s', target: 1000 }, 
  ],
  insecureSkipTLSVerify: true
};

export default function () {
  const res = http.get('https://orange.informatik.uni-stuttgart.de');
  sleep(1);
  check(res, {
	  'is status 200': (r) => r.status === 200,
  });
}
