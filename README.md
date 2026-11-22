# Notes API

Small HTTP notes service in pure Java (no frameworks, JDK 11+).

## What it does
- `GET /` - greeting
- `GET /healthz` - health check, returns `ok`
- `POST /notes` - body is the note text, returns the created note as JSON
- `GET /notes` - list of all notes (in memory)

## How to run
```bash
scripts/run.sh
```
## Port
Taken from the `PORT` environment variable, default **8080**:
`PORT=9000 scripts/run.sh`

## How to test
```bash
scripts/test.sh
```
Prints `TESTS: n/n` and exits 0 on success.
Tested with JDK 11+.

## Rollback

Argo CD deploys what is on the `main` branch, from the `k8s` folder.
To roll back, undo the commit that deployed the bad version, with a new commit:

    git log --oneline -- k8s/deployment.yaml
    git revert <commit>
    git push

Argo CD deploys the previous version within three minutes. Do not use
`kubectl rollout undo`: self-heal would put the bad version back.