import http from 'k6/http';
import {getToken} from './token.js';

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

const query = `
	query MyQuery {
  findQuizzesByAssessmentIds(
    assessmentIds: "12f55e91-6df7-4b1d-b99c-914e9a8e92d6"
  ) {
    assessmentId
    numberOfRandomlySelectedQuestions
    questionPool {
      hint
      id
      number
      type
    }
    questionPoolingMode
    requiredCorrectAnswers
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
};
