# AGENTS.md — Taller SOLID

## Project

Java workshop where students refactor intentionally bad code to fix all 5 SOLID violations.

## Key structure

- **No build tool** — plain Java with VS Code settings (`.vscode/settings.json`).
- Source: `src/ean/solid/`, entry point: `src/Main.java` (default package).
- Output: `bin/`. Referenced libraries (none exist now): `lib/**/*.jar`.
- No tests. No CI. No formatter/linter config.

## How to compile & run

```powershell
# compile
javac -d bin -sourcepath src src/Main.java

# run
java -cp bin Main
```

Or use VS Code's Java extension which picks up `.vscode/settings.json`.

## Package

All classes under `package ean.solid`. 6 files: `OrderProcessor`, `Product`, `DigitalProduct`, `IWorker`, `MySQLDatabase`, `EmailService`. All intentionally violate SOLID — this is the starting point for students.

## Reference solution

`lib/solucion.md` contains the step-by-step answer. The suggested final architecture splits into layers (domain, infrastructure, application) and uses dependency injection.

## What to preserve

- All existing `.java` files in `src/ean/solid/` are intentional violations — do not delete them.
- `src/Main.java` is the student's integration point that should compile and run after refactoring.
