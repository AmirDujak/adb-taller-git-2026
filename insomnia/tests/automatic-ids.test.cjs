const { test } = require('node:test');
const assert = require('node:assert/strict');
const { execFileSync } = require('node:child_process');
const path = require('node:path');
const vm = require('node:vm');
const { STATUS_CODES } = require('node:http');

// Ruby aporta su parser YAML estándar; no se agregan dependencias al proyecto Java.
const collection = JSON.parse(execFileSync('ruby', ['-ryaml', '-rjson', '-e',
  'puts JSON.generate(YAML.load_file(ARGV[0]))', path.join(__dirname, '../minecraft.yaml')],
{ encoding: 'utf8' }));
const requests = collection.collection.flatMap(folder => folder.children);
const captures = {
  req_crear: 'entidad_id',
  req_creeper: 'creeper_id',
  req_player: 'player_id',
  req_zombie: 'zombie_id',
  req_cargado: 'creeper_cargado_id',
  req_player_carga: 'player_carga_id',
  req_sin_posicion: 'creeper_sin_posicion_id',
};

function fixture(request, code, body, status = STATUS_CODES[code] || '') {
  const values = { ...collection.environments.data };
  const context = vm.createContext({ insomnia: {
    environment: { set: (key, value) => { values[key] = value; } },
    // API real de Insomnia: code es el número HTTP; status es la frase textual.
    response: { code, status, json: () => JSON.parse(body) },
  } });
  return {
    values,
    before: () => vm.runInContext(request.scripts.preRequest, context, { timeout: 1000 }),
    after: () => vm.runInContext(request.scripts.afterResponse, context, { timeout: 1000 }),
  };
}

test('las siete altas capturan IDs; errores y simulaciones no los sobrescriben', () => {
  assert.equal(requests.length, 28);
  assert.deepEqual(requests.filter(r => r.scripts).map(r => r.meta.id).sort(), Object.keys(captures).sort());
});

for (const [requestId, variable] of Object.entries(captures)) {
  const request = requests.find(r => r.meta.id === requestId);
  test(`${variable}: captura el ID real y reemplaza el de una ejecución anterior`, () => {
    const run = fixture(request, 201, '{"id":12345}');
    run.values[variable] = 99;
    run.before();
    assert.equal(run.values[variable], 0);
    run.after();
    assert.equal(run.values[variable], 12345);
    assert.deepEqual(Object.keys(run.values).filter(key => run.values[key] !== collection.environments.data[key]), [variable]);
  });
  test(`${variable}: usa code=201 aunque status sea textual o esté vacío`, () => {
    for (const status of ['Created', '', 'Creado']) {
      const run = fixture(request, 201, '{"id":42}', status);
      run.before();
      run.after();
      assert.equal(run.values[variable], 42);
    }
    const failed = fixture(request, 400, '{"id":42}', 'Created');
    failed.before();
    assert.throws(failed.after);
    assert.equal(failed.values[variable], 0);
  });
  test(`${variable}: un POST fallido o ID inválido no reutiliza el ID anterior`, () => {
    for (const [status, body] of [[400, '{}'], [500, '{}'], [200, '{"id":1}'],
      [201, '{}'], [201, '{"id":0}'], [201, '{"id":-1}'], [201, '{"id":"10"}'],
      [201, '{"id":1.5}'], [201, '{"id":9007199254740992}'], [201, 'JSON roto']]) {
      const run = fixture(request, status, body);
      run.values[variable] = 99;
      run.before();
      assert.throws(run.after);
      assert.equal(run.values[variable], 0);
    }
  });
}
