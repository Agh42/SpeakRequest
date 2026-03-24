(async ({ page, request }) => {
  const roomResp = await request.post('http://127.0.0.1:8080/api/rooms', {
    data: {}
  });
  if (!roomResp.ok()) {
    throw new Error(`Failed to create room for verification: ${roomResp.status()}`);
  }
  const roomPayload = await roomResp.json();
  const roomCode = roomPayload.code;
  if (!roomCode) {
    throw new Error('Verification room code missing from create-room response');
  }

  const baseUrl = `http://127.0.0.1:8080/chair.html?room=${encodeURIComponent(roomCode)}`;

  const assert = (condition, message) => {
    if (!condition) {
      throw new Error(message);
    }
  };

  const readActiveTarget = async (rootSelector) => {
    const selector = `${rootSelector} .is-active, ${rootSelector} [aria-current=\"page\"]`;
    const count = await page.locator(selector).count();
    assert(count === 1, `Expected exactly one active target in ${rootSelector}, got ${count}`);
    return page.locator(selector).first().evaluate((el) => {
      const dataTarget = el.getAttribute('data-target');
      if (dataTarget) return dataTarget;
      const dataSection = el.getAttribute('data-section');
      if (dataSection) return dataSection;
      const href = el.getAttribute('href');
      if (href && href.startsWith('#')) return href.slice(1);
      return '';
    });
  };

  await page.goto(baseUrl, { waitUntil: 'domcontentloaded' });

  await page.setViewportSize({ width: 390, height: 844 });
  await page.locator('#section-queue').scrollIntoViewIfNeeded();
  await page.waitForTimeout(150);
  const scrollBefore = await page.evaluate(() => window.scrollY);
  await page.getByRole('button', { name: /menu/i }).first().click();
  const panel = page.locator('#mobileMenuPanel');
  await panel.waitFor({ state: 'visible' });
  await page.waitForTimeout(120);
  const openBox = await panel.boundingBox();
  assert(openBox && openBox.x + openBox.width > 0, 'Panel still off-canvas after open');
  await page.keyboard.press('Escape');
  await page.waitForTimeout(220);
  const panelOpen = await panel.getAttribute('data-open');
  assert(panelOpen === 'false', 'Panel did not close after Escape');
  const scrollAfter = await page.evaluate(() => window.scrollY);
  assert(Math.abs(scrollAfter - scrollBefore) <= 8, 'Page scroll position changed across menu open/close');

  await page.setViewportSize({ width: 1366, height: 900 });
  const sectionIds = ['section-controls', 'section-queue', 'section-poll', 'section-menu'];
  for (const sectionId of sectionIds) {
    await page.locator(`#${sectionId}`).scrollIntoViewIfNeeded();
    await page.waitForTimeout(280);
    const desktopTarget = await readActiveTarget('#sidebar');
    const mobileTarget = await readActiveTarget('#mobileBottomNav');
    assert(desktopTarget === sectionId, `Desktop active target mismatch for ${sectionId}: got ${desktopTarget}`);
    assert(mobileTarget === sectionId, `Mobile active target mismatch for ${sectionId}: got ${mobileTarget}`);
  }

  console.log('PLAYWRIGHT_GAP_TESTS_OK');
})();
