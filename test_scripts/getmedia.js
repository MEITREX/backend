import http from 'k6/http';
import {getToken} from './token.js';

export const options = {
	scenarios: {
		five_hundred_users: {
			executor: 'shared-iterations',
			startTime: "0s",
			vus: 500,
			iterations: 500,
			maxDuration: '5m'
		},
		ramping_users: {
			executor: 'ramping-vus',
			// startTime: "10s",
			startVUs: 0,
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
			// startTime: "210s",
			duration: '1m',
			rate: 200,
			preAllocatedVUs: 100,
			maxVUs: 500,
		},
		ramping_arrival_rate: {
			executor: 'ramping-arrival-rate',
			// startTime: '300s',
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
		}
	},
	insecureSkipTLSVerify: true
};

const query = `
	query MediaRecordsById {
		mediaRecordsByIds(ids: ["064e6b7e-f3b9-44c5-86c3-2441e0de598c", "985f51bf-a526-432a-9156-199f5b1676b3"]) {
			id
			name
			creatorId
			type
			contentIds
			uploadUrl
			downloadUrl
		}
	}`;

export function setup() {
	return getToken();
}

export default function (data) {


    const headers = {
        'Authorization': `Bearer ${data.access_token}`,
        'Content-Type': 'application/json',
    }

  const res = http.post(`${__ENV.k6_GATEWAY_URL}`, JSON.stringify({query: query}), {
	headers: headers,
  });

	console.log(res.body)

}
