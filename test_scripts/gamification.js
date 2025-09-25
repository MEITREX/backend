import { browser } from 'k6/browser';
import { sleep } from 'k6';
import { check } from 'https://jslib.k6.io/k6-utils/1.5.0/index.js';
import { vu } from 'k6/execution';

export const options = {
    scenarios: {
        ui: {
            executor: 'shared-iterations',
            vus: 2,
            iterations: 2,
            options: {
                browser: {
                    type: 'chromium',
                },
            },
        },
    },
    thresholds: {
        checks: ['rate==1.0'],
    },
};

export default async function () {
    const id = vu.idInTest-1;
    const username = `test-user-${id}`;
    const page = await browser.newPage();

    try {
       await page.goto('https://dev.meitrex.de');

        const registerLink = page.locator('//div[@class="register-link"]//a');
        await registerLink.click();

        check(page.locator('//p[@class="register-title"]'), {
            'Login-Seite geladen': async (rT) => (await rT.textContent()) === 'Register',
        });
        
        await page.locator('input[name="firstName"]').type('Max');
        await page.locator('input[name="lastName"]').type('Mustermann');
        await page.locator('input[name="email"]').type(`${username}@example.com`);
        await page.locator('input[name="username"]').type(username);
        await page.locator('input[name="password"]').type('password123');
        await page.locator('input[name="password-confirm"]').type('password123');
        const registerSubmit = await page.locator('input[type="submit"]')

        await Promise.all([page.waitForNavigation(), registerSubmit.click()]);

        check(page, {
            'Registrierung erfolgreich & Namensauswahl wird angezeigt': async (r) => (await r.locator('h6').textContent()) === 'Pick your nickname',
        });

        const continueButton = await   page.locator('//button[text()="Continue"]')
        await Promise.all([page.waitForNavigation(), continueButton.click()]);

        check(page, {
            'Gamification Umfrage wird  angezeigt': async (r) => (await r.locator('h5').textContent()) === 'Welcome to the Player Type survey',
        });

        const startButton = await page.locator('//button[text()="Start Survey"]');
        await startButton.click();
        
        await page.waitForSelector('h2');

        check(page, {
            'Gamification Umfrage gestartet': async (r) => (await r.locator('h2').textContent()) === 'Question 1',
        });

        const teamActivities = await page.locator('//h6[contains(text(), "assist teammates")] | //div[contains(text(), "assist teammates")] | //span[contains(text(), "assist teammates")]');
        await teamActivities.click();

        const nextButtonQ1 = await page.locator('//button[text()="Next"]');
        await nextButtonQ1.click();
        
        await page.waitForSelector('h2');

        check(page, {
            'Frage 2 geladen': async (r) => (await r.locator('h2').textContent()) === 'Question 2',
        });


        const featureExploring = await page.locator('//h6[contains(text(), "experiment freely")] | //div[contains(text(), "experiment freely")] | //span[contains(text(), "experiment freely")]');
        await featureExploring.click();

        const nextButtonQ2 = await page.locator('//button[text()="Next"]');
        await nextButtonQ2.click();


        await page.waitForSelector('h2');

        check(page, {
            'Frage 3 geladen': async (r) => (await r.locator('h2').textContent()) === 'Question 3',
        });


        const competitionView = await page.locator('//h6[contains(text(), "improve team rankings")] | //div[contains(text(), "improve team rankings")] | //span[contains(text(), "improve team rankings")]');
        await competitionView.click();

        const nextButtonQ3 = await page.locator('//button[text()="Next"]');
        await nextButtonQ3.click();


        await page.waitForSelector('h2');

        check(page, {
            'Frage 4 geladen': async (r) => (await r.locator('h2').textContent()) === 'Question 4',
        });


        const completionTask = await page.locator('//h6[contains(text(), "earning unique")] | //div[contains(text(), "earning unique")] | //span[contains(text(), "earning unique")]');
        await completionTask.click();

        const nextButtonQ4 = await page.locator('//button[text()="Next"]');
        await nextButtonQ4.click();


        await page.waitForSelector('h2');

        check(page, {
            'Frage 5 geladen': async (r) => (await r.locator('h2').textContent()) === 'Question 5',
        });

        const systemBug = await page.locator('//h6[contains(text(), "exploit it")] | //div[contains(text(), "exploit it")] | //span[contains(text(), "exploit it")]');
        await systemBug.click();

        const nextButtonQ5 = await page.locator('//button[text()="Next"]');
        await nextButtonQ5.click();


        await page.waitForSelector('h2');

        check(page, {
            'Frage 6 geladen': async (r) => (await r.locator('h2').textContent()) === 'Question 6',
        });


        const learningStyle = await page.locator('//h6[contains(text(), "collaborating")] | //div[contains(text(), "collaborating")] | //span[contains(text(), "collaborating")]');
        await learningStyle.click();

        const nextButtonQ6 = await page.locator('//button[text()="Next"]');
        await nextButtonQ6.click();


        await page.waitForSelector('h2');

        check(page, {
            'Frage 7 geladen': async (r) => (await r.locator('h2').textContent()) === 'Question 7',
        });


        const newFeature = await page.locator('//h6[contains(text(), "clear progress")] | //div[contains(text(), "clear progress")] | //span[contains(text(), "clear progress")]');
        await newFeature.click();

        const nextButtonQ7 = await page.locator('//button[text()="Next"]');
        await nextButtonQ7.click();


        await page.waitForSelector('h2');

        check(page, {
            'Frage 8 geladen': async (r) => (await r.locator('h2').textContent()) === 'Question 8',
        });


        const taskChoosing = await page.locator('//h6[contains(text(), "open-ended")] | //div[contains(text(), "open-ended")] | //span[contains(text(), "open-ended")]');
        await taskChoosing.click();

        const nextButtonQ8 = await page.locator('//button[text()="Next"]');
        await nextButtonQ8.click();


        await page.waitForSelector('h2');

        check(page, {
            'Frage 9 geladen': async (r) => (await r.locator('h2').textContent()) === 'Question 9',
        });


        const rewardEarning = await page.locator('//h6[contains(text(), "their practical")] | //div[contains(text(), "their practical")] | //span[contains(text(), "their practical")]');
        await rewardEarning.click();

        const nextButtonQ9 = await page.locator('//button[text()="Next"]');
        await nextButtonQ9.click();


        await page.waitForSelector('h2');

        check(page, {
            'Frage 10 geladen': async (r) => (await r.locator('h2').textContent()) === 'Question 10',
        });


        const otherMistakes = await page.locator('//h6[contains(text(), "offer guidance")] | //div[contains(text(), "offer guidance")] | //span[contains(text(), "offer guidance")]');
        await otherMistakes.click();

        const nextButtonQ10 = await page.locator('//button[text()="Next"]');
        await nextButtonQ10.click();


        await page.waitForSelector('h2');

        check(page, {
            'Frage 11 geladen': async (r) => (await r.locator('h2').textContent()) === 'Question 11',
        });


        const ruleAttitude = await page.locator('//h6[contains(text(), "follow them")] | //div[contains(text(), "follow them")] | //span[contains(text(), "follow them")]');
        await ruleAttitude.click();

        const nextButtonQ11 = await page.locator('//button[text()="Next"]');
        await nextButtonQ11.click();


        await page.waitForSelector('h2');

        check(page, {
            'Frage 12 geladen': async (r) => (await r.locator('h2').textContent()) === 'Question 12',
        });


        const participationPreferences = await page.locator('//h6[contains(text(), "community-building")] | //div[contains(text(), "community-building")] | //span[contains(text(), "community-building")]');
        await participationPreferences.click();

        const finishButton = await page.locator('//button[text()="Finish"]');
        await finishButton.click();


        await page.waitForSelector('h5');

        check(page, {
            'Umfrage abgeschlossen': async (r) => (await r.locator('h5').textContent()) === 'Survey completed',
        });

    } finally {
        await page.close();
    }
}
