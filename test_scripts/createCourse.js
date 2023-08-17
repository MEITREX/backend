import http from 'k6/http';
import {getToken} from './token.js';

export const options = {
    stages: [
        {duration: '10s', target: 0},
        {duration: '1m', target: 50},
        // { duration: '30s', target: 100 },
        // { duration: '30s', target: 200 },
        // { duration: '30s', target: 300 },
        // { duration: '30s', target: 400 },
        // { duration: '30s', target: 500 },
    ],
    insecureSkipTLSVerify: true
};

const createCourse = `
mutation {
                    createCourse(
                        input: {
                            title: "New Course"
                            description: "This is a new course"
                            startDate: "2020-01-01T00:00:00.000Z"
                            endDate: "2021-01-01T00:00:00.000Z"
                            published: false
                        }
                    ) {
                        id
                        title
                        description
                        startDate
                        endDate
                        published
                        chapters {
                            elements {
                                id
                            }
                        }
                    }
                }
`;


export function setup() {
    return getToken();
}

export default function (data) {
    const headers = {
        'Authorization': `Bearer ${data.access_token}`,
        'Content-Type': 'application/json',
    }

    const res = http.post(`${__ENV.k6_GATEWAY_URL}`, JSON.stringify({query: createCourse}), {
        headers: headers,
    });

    const id = JSON.parse(res.body).data.createCourse.id;

    const deleteCourse = `
        mutation {
            deleteCourse(id: "${id}")
        }
    `;

    const resDelete = http.post(`${__ENV.k6_GATEWAY_URL}`, JSON.stringify({query: deleteCourse}), {
        headers: headers,
    });
};
