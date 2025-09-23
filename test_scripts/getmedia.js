import http from 'k6/http';
import {getToken} from './token.js';
import {check} from 'k6';

export const options = {
	scenarios: {
		/*ramping_users: {
			executor: 'ramping-vus',
			startTime: "0s",
			startVUs: 0,
			stages: [
				{ duration: '30s', target: 50 },
				{ duration: '30s', target: 100 },
				{ duration: '30s', target: 200 },
				{ duration: '30s', target: 300 },
				{ duration: '30s', target: 400 },
				{ duration: '30s', target: 500 },
			],
		},*/
		ramping_arrival_rate: {
			executor: 'ramping-arrival-rate',
			startTime: '0s',
			preAllocatedVUs: 1000,
			maxVUs: 5000,
			startRate: 0,
			stages: [
				{ duration: '100s', target: 90 },
				{ duration: '1000s', target: 300 },
				{ duration: '800s', target: 500 }
			],
		},
		/*constant_arrival_rate: {
			executor: 'constant-arrival-rate',
			startTime: "190s",
			duration: '1m',
			rate: 200,
			preAllocatedVUs: 1000,
			maxVUs: 5000,
		},*/
		/*five_hundred_users: {
			executor: 'shared-iterations',
			startTime: "260s",
			vus: 500,
			iterations: 500,
			maxDuration: '5m'
		},*/
	},
	insecureSkipTLSVerify: true
};

const queryByIds = `
	query MediaRecordsById($ids: [UUID!]!) {
		mediaRecordsByIds(ids: $ids) {
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
	const token = getToken();

	const recordIds = sendQuery(token,
		`
		query {
			mediaRecords {
				id
			}
		}`).json().data.mediaRecords.map(record => record.id);

	return {
		token: token,
		recordIds: recordIds
	};
}

export default function (data) {
	const randomId = data.recordIds[Math.floor(Math.random() * data.recordIds.length)];

	const variables = {
		ids: [randomId]
	};

	let res = null;

	res = sendQuery(data.token, queryByIds, variables);

	// simple check to see if json has the right structure (i.e. the response is actual data and not some error)
	check(res, {
		"error response": (r) => {
			let failed = isFailedResponse(r);
			if(failed) {
				console.log("Unsuccessful response: ");
				console.log(r);
			}
			return failed;
		}
	});

	//console.log(res.body);
}

function sendQuery(token, query, variables = {}) {
	const headers = {
        'Authorization': `Bearer ${token.access_token}`,
        "currentUser": "{\"id\": \"0504b344-a5e5-4962-85fa-52baadd82dc3\",\"userName\": \"test\",\"firstName\": \"test\",\"lastName\": \"test\"}",
		'Content-Type': 'application/json',
    }

	return http.post(`${__ENV.k6_GATEWAY_URL}`, JSON.stringify({query: query, variables: variables}), {
		headers: headers,
	  });
}

function isFailedResponse(res) {
	if(res.status === 200)
		return false;

	try { 
		return res.json().data.mediaRecordsByIds[0].id === null;
	} catch(e) {
		return true;
	}
}