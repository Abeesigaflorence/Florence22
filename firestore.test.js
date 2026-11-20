const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read flocks", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("flocks").get());
});

test("Authenticated user: can read flocks", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("flocks").get());
});

test("Authenticated user: can create a flock with required fields and createdBy == auth.uid", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("flocks").add({
      code: "Layers A",
      placementDate: "2026-11-01",
      initialCount: 500,
      currentCount: 500,
      status: "active",
      createdBy: ALICE_UID
    })
  );
});

test("Authenticated user: cannot create flock if createdBy != auth.uid", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("flocks").add({
      code: "Layers Fake",
      placementDate: "2026-11-01",
      initialCount: 500,
      currentCount: 500,
      status: "active",
      createdBy: BOB_UID
    })
  );
});

test("Authenticated user: can record production", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("productionRecords").add({
      date: "2026-11-03",
      flockId: "flock_1",
      flockCode: "Layers A",
      eggsCollected: 450,
      eggsBroken: 5,
      mortality: 1,
      feedUsedKg: 62.5,
      createdBy: ALICE_UID
    })
  );
});

test("Authenticated user: can record sale and expense", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("sales").add({
      date: "2026-11-03",
      product: "eggs",
      total: 150000,
      paymentStatus: "paid",
      createdBy: ALICE_UID
    })
  );
  await assertSucceeds(
    aliceDb.collection("expenses").add({
      date: "2026-11-03",
      category: "feed",
      amount: 85000,
      createdBy: ALICE_UID
    })
  );
});
