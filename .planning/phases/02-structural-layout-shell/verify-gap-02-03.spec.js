const { test, expect } = require('@playwright/test');

test('phase 02 gap closure verification', async ({ page, request }) => {
  const roomResp = await request.post('http://127.0.0.1:8080/api/rooms', {
    data: {}
  });
  expect(roomResp.ok()).toBeTruthy();
  const roomPayload = await roomResp.json();
  const roomCode = roomPayload.code;
  expect(roomCode).toBeTruthy();

  await page.goto(`http://127.0.0.1:8080/chair.html?room=${encodeURIComponent(roomCode)}`);

  await page.setViewportSize({ width: 390, height: 844 });
  await page.locator('#section-queue').scrollIntoViewIfNeeded();
  await page.waitForTimeout(150);
  const scrollBefore = await page.evaluate(() => window.scrollY);
  await page.getByRole('button', { name: /menu/i }).first().click();
  const panel = page.locator('#mobileMenuPanel');
  await expect(panel).toBeVisible();
  const openBox = await panel.boundingBox();
  expect(openBox).toBeTruthy();
  expect(openBox.x + openBox.width).toBeGreaterThan(0);

  await page.keyboard.press('Escape');
  await page.waitForTimeout(220);
  await expect(panel).toHaveAttribute('data-open', 'false');
  const scrollAfter = await page.evaluate(() => window.scrollY);
  expect(Math.abs(scrollAfter - scrollBefore)).toBeLessThanOrEqual(8);

  const readActiveTarget = async (rootSelector) => {
    const selector = `${rootSelector} .is-active, ${rootSelector} [aria-current="page"]`;
    const active = page.locator(selector);
    await expect(active).toHaveCount(1);
    const first = active.first();
    const dataTarget = await first.getAttribute('data-target');
    if (dataTarget) return dataTarget;
    const dataSection = await first.getAttribute('data-section');
    if (dataSection) return dataSection;
    const href = await first.getAttribute('href');
    return href && href.startsWith('#') ? href.slice(1) : '';
  };

  await page.setViewportSize({ width: 1366, height: 900 });
  const sectionIds = ['section-controls', 'section-queue', 'section-poll', 'section-menu'];
  for (const sectionId of sectionIds) {
    await page.locator(`#${sectionId}`).scrollIntoViewIfNeeded();
    await page.waitForTimeout(280);
    const desktopTarget = await readActiveTarget('#sidebar');
    const mobileTarget = await readActiveTarget('#mobileBottomNav');
    expect(desktopTarget).toBe(sectionId);
    expect(mobileTarget).toBe(sectionId);
  }
});
