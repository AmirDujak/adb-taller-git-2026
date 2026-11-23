const { test } = require('node:test');
const assert = require('node:assert/strict');
const { execFileSync } = require('node:child_process');
const path = require('node:path');
const vm = require('node:vm');

const collection = JSON.parse(execFileSync('ruby', ['-ryaml', '-rjson', '-e',
  'puts JSON.generate(YAML.load_file(ARGV[0]))', path.join(__dirname, '../poo09.yaml')], { encoding: 'utf8' }));
const requests = collection.collection.flatMap(f => f.children);
const uuid = '11111111-1111-4111-8111-111111111111';

function run(request, code, body, values = {}) {
  const context = vm.createContext({ insomnia: {
    environment: { set: (k, v) => { values[k] = v; }, get: k => values[k] },
    response: { code, status: code === 201 ? 'Created' : 'Conflict', json: () => body },
  } });
  return {
    before: () => vm.runInContext(request.scripts.preRequest, context),
    after: () => vm.runInContext(request.scripts.afterResponse, context),
    values,
  };
}

test('colección completa, IDs únicos y tres bases configuradas', () => {
  assert.equal(requests.length, 19);
  assert.equal(new Set(requests.map(r => r.meta.id)).size, requests.length);
  for (const role of ['a1', 'a2', 'a3']) assert.ok(collection.environments.data[`${role}_base_url`]);
});

for (const request of requests.filter(r => r.scripts?.preRequest && r.url.includes('a2_base_url') ||
  r.scripts?.preRequest && r.url.includes('a3_base_url'))) {
  const key = request.scripts.preRequest.match(/set\("([^"]+)"/)[1];
  test(`${key}: captura UUID y limpia un valor antiguo ante errores`, () => {
    const good = run(request, 201, { id: uuid });
    good.before(); good.after();
    assert.equal(good.values[key], uuid);
    for (const [code, body] of [[400, { id: uuid }], [201, {}], [201, { id: 10 }]]) {
      const bad = run(request, code, body, { [key]: uuid });
      bad.before();
      assert.throws(bad.after);
      assert.equal(bad.values[key], '');
    }
  });
}

test('éxito exige 201 y ambas aprobaciones', () => {
  const request = requests.find(r => r.meta.id === 'req_p09_feliz');
  const good = run(request, 201, { id: 9, aceptadaSofia: true, puedeExplotarNicolas: true });
  good.before(); good.after();
  assert.equal(good.values.jugada_id, 9);
  const bad = run(request, 201, { id: 9, aceptadaSofia: true, puedeExplotarNicolas: false });
  bad.before(); assert.throws(bad.after);
  assert.equal(bad.values.jugada_id, 0);
});

for (const [role, provider] of [['a2', 'A2_SOFIA'], ['a3', 'A3_NICOLAS']]) {
  test(`rechazo ${role}: atribución y snapshot exacto`, () => {
    const rejection = requests.find(r => r.meta.id === `req_p09_rechazo_${role}`);
    run(rejection, 409, { proveedor: provider }).after();
    assert.throws(run(rejection, 409, { proveedor: 'OTRO' }).after);
    const before = requests.find(r => r.meta.id === `req_p09_antes_${role}`);
    const after = requests.find(r => r.meta.id === `req_p09_despues_${role}`);
    const state = {};
    run(before, 200, [{ id: 9 }], state).after();
    run(after, 200, [{ id: 9 }], state).after();
    assert.throws(run(after, 200, [{ id: 9 }, { id: 10 }], state).after);
  });
}
