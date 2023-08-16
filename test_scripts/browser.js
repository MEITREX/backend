import { browser } from 'k6/experimental/browser';
import {sleep} from 'k6';

export const options = {
    scenarios: {
        ui: {
            executor: 'shared-iterations',
            options: {
                browser: {
                    type: 'chromium',
                },
            },
        },
    },
    thresholds: {
        checks: ["rate==1.0"]
    }
}

export default async function () {
    const page = browser.newPage();

    try {
        await page.goto('https://orange.informatik.uni-stuttgart.de');

        sleep(1);

        page.locator('input[name="username"]').type('rick');
        page.locator('input[name="password"]').type('gits');

        const submitButton = page.locator('input[type="submit"]');

        await Promise.all([page.waitForNavigation(), submitButton.click()]);

        sleep(1);

        page.screenshot({ path: 'screenshot.png' });
    } finally {
        page.close();
    }
}
