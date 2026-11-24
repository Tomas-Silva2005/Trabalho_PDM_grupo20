const express = require('express');
const cors = require('cors');
const crypto = require('crypto');

const app = express();
app.use(express.json({ limit: '1mb' }));
app.use(cors());

const rpName = 'SecureNote';
const rpID = 'note-g20-2026.web.app';
// Origin que o Android coloca no clientDataJSON: hash do certificado da app
const ANDROID_CERT_SHA256 =
  'FC:EA:7F:D5:4F:25:22:06:24:6C:98:6A:F4:A4:B9:A8:E6:FF:F6:04:3C:E4:C5:B3:37:74:30:C8:A7:8B:41:99';
const expectedOrigin =
  'android:apk-key-hash:' +
  Buffer.from(ANDROID_CERT_SHA256.replace(/:/g, ''), 'hex').toString('base64url');

const CHALLENGE_TTL_MS = 5 * 60 * 1000;

const users = new Map();      // username -> { id, username, credentials: [{ id, raw }] }
const challenges = new Map(); // challenge -> { type, username, expires }

app.use((req, res, next) => {
  console.log(`[Server] ${req.method} ${req.url}`);
  next();
});

function newChallenge(type, username = null) {
  const challenge = crypto.randomBytes(32).toString('base64url');
  challenges.set(challenge, { type, username, expires: Date.now() + CHALLENGE_TTL_MS });
  return challenge;
}

function parseClientData(credential) {
  const raw = credential && credential.response && credential.response.clientDataJSON;
  if (!raw) throw new Error('clientDataJSON em falta');
  return JSON.parse(Buffer.from(raw, 'base64url').toString('utf8'));
}

// Valida type, origin e challenge (uso único + expiração)
function consumeChallenge(clientData, expectedType) {
  if (clientData.type !== expectedType) throw new Error('Tipo de operação inválido');

  if (clientData.origin !== expectedOrigin) {
    console.warn('[Server] Origin recebido:', clientData.origin, '| esperado:', expectedOrigin);
    throw new Error('Origin inválido');
  }

  const entry = challenges.get(clientData.challenge);
  challenges.delete(clientData.challenge); // uso único
  if (!entry || entry.type !== expectedType) throw new Error('Challenge desconhecido');
  if (entry.expires < Date.now()) throw new Error('Challenge expirado');
  return entry;
}

app.get('/api/users', (req, res) => {
  const list = Array.from(users.values()).map(u => ({
    username: u.username,
    credentials: u.credentials.map(c => c.id)
  }));
  res.json({ registeredUsers: list, totalUsers: list.length });
});

// Login por palavra-passe (continua a ser demonstração: aceita qualquer valor não vazio)
app.post('/api/auth/login', (req, res) => {
  const { username, password } = req.body || {};
  if (username && password && username.trim() !== '' && password.trim() !== '') {
    return res.status(200).json({ success: true });
  }
  return res.status(401).json({ error: 'Credenciais inválidas' });
});

app.post('/api/auth/register/options', (req, res) => {
  try {
    const username = String((req.body && req.body.username) || '').trim();
    if (!username) return res.status(400).json({ error: 'username em falta' });

    let user = users.get(username);
    if (!user) {
      user = { id: Buffer.from(username).toString('base64url'), username, credentials: [] };
      users.set(username, user);
    }

    res.json({
      rp: { id: rpID, name: rpName },
      user: { id: user.id, name: username, displayName: username },
      challenge: newChallenge('webauthn.create', username),
      pubKeyCredParams: [
        { type: 'public-key', alg: -7 },
        { type: 'public-key', alg: -257 }
      ],
      timeout: 60000,
      attestation: 'none',
      authenticatorSelection: {
        authenticatorAttachment: 'platform',
        userVerification: 'required',
        residentKey: 'required',
        requireResidentKey: true
      }
    });
  } catch (err) {
    console.error('register/options:', err);
    res.status(500).json({ error: err.message });
  }
});

app.post('/api/auth/register/verify', (req, res) => {
  try {
    const credential = req.body;
    const entry = consumeChallenge(parseClientData(credential), 'webauthn.create');

    const user = users.get(entry.username);
    if (!user) throw new Error('Utilizador desconhecido');
    if (!credential.id) throw new Error('ID da credencial em falta');

    if (!user.credentials.some(c => c.id === credential.id)) {
      user.credentials.push({ id: credential.id, raw: credential });
    }
    console.log(`[Server] Passkey registada para ${user.username}: ${credential.id}`);
    res.status(200).json({ verified: true });
  } catch (err) {
    console.error('register/verify:', err.message);
    res.status(400).json({ error: err.message });
  }
});

app.post('/api/auth/passkey/options', (req, res) => {
  res.json({
    challenge: newChallenge('webauthn.get'),
    rpId: rpID,
    timeout: 60000,
    userVerification: 'required'
    // sem allowCredentials: o dispositivo mostra as passkeys deste rpId
  });
});

app.post('/api/auth/passkey/verify', (req, res) => {
  try {
    const credential = req.body;
    consumeChallenge(parseClientData(credential), 'webauthn.get');

    let owner = null;
    for (const user of users.values()) {
      if (user.credentials.some(c => c.id === credential.id)) owner = user;
    }
    if (!owner) throw new Error('Credencial não registada');

    console.log(`[Server] Login com passkey: ${owner.username}`);
    res.status(200).json({ verified: true, username: owner.username });
  } catch (err) {
    console.error('passkey/verify:', err.message);
    res.status(401).json({ error: err.message });
  }
});

const PORT = 3000;
app.listen(PORT, () => {
  console.log(`SecureNote Mock Server em http://192.168.1.70:${PORT}`);
  console.log(`Origin esperado: ${expectedOrigin}`);
});