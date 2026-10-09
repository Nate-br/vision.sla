# Contributing

## Branching

- Branch from `main`, name your branch `feat/`, `fix/`, `chore/`, `docs/` etc.
- Keep branches short-lived (≤ 3 days).
- Delete the branch after merge.

## Commits

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(payments): add Telebirr reference parser
fix(auth): handle token refresh race condition
chore(deps): bump Retrofit to 2.11.0
docs(security): tick MASVS-STORAGE-1 on SECURITY_CHECKLIST
```

## Pull Requests

- One feature / fix per PR. Keep diffs small.
- All CI checks must pass before merging.
- At least one code-review approval required.
- Update `docs/SECURITY_CHECKLIST.md` for any auth, payment, ledger or webhook change.

## Secrets

**Never commit secrets, API keys, tokens, or real receipt numbers.**  
Use `.env.example` with `PLACEHOLDER` values.  
CI runs `gitleaks` on every push.

## Money rules

- Money is stored and passed as **integer minor units (santim / cents)** or `NUMERIC(14,2)`. Never `Double`/`Float`/`float`.
- Never trust amounts sent by the app. The backend is the single source of truth.

## Definition of Done

- [ ] Compiles and lint is clean
- [ ] Unit tests pass
- [ ] `SECURITY_CHECKLIST.md` items for this feature ticked
- [ ] Accessibility checked (contrast, touch targets, TalkBack labels)
- [ ] Docs updated
