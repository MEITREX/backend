import http from 'k6/http';
import {getToken} from './token.js';
import {check} from 'k6';

export const options = {
	scenarios: {
		ramping_arrival_rate: {
			executor: 'ramping-arrival-rate',
			preAllocatedVUs: 500,
			maxVUs: 5000,
			startRate: 0,
			stages: [
				{ duration: '30s', target: 50 },
				{ duration: '30s', target: 100 },
				{ duration: '30s', target: 200 },
				{ duration: '30s', target: 300 },
				{ duration: '30s', target: 400 },
				{ duration: '30s', target: 500 },
			],
		},
		constant_arrival_rate: {
			executor: 'constant-arrival-rate',
			startTime: "190s",
			duration: '1m',
			rate: 200,
			preAllocatedVUs: 100,
			maxVUs: 500,
		},
		
	},
	insecureSkipTLSVerify: true
};

export function setup() {
	const token = getToken();

	return {
		token: token
	};
}

export default function (data) {
	const headers = {
		//'Authorization': `Bearer ${data.token.access_token}`,
		'Authorization': "Basic VmF1bHQtZ2VuZXJpYzpQaW1tZWxiZXJnZXI="
	}

	const res = http.get(`http://dev.meitrex.de`, {
		headers: headers,
	});

	check(res, {
		"request failed": (r) => r.status !== 200,
	})

	console.log(res.body);
}