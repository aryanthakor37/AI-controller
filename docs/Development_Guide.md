# Development Guide

## Code Formatting Rules
- **Prettier** is used for code formatting.
- Single quotes preferred over double quotes in JS/TS.
- Trailing commas are required.

## Folder & File Naming Conventions
- Folders: `kebab-case` (e.g., `user-profile`).
- React Components: `PascalCase.jsx` (e.g., `Button.jsx`).
- Utility/Service Files: `camelCase.js` (e.g., `apiClient.js`).
- Kotlin Files: `PascalCase.kt`.

## Import Conventions
- Always group imports:
  1. Built-in modules (Node) or React packages.
  2. Third-party packages.
  3. Absolute project imports (e.g., `@/components/...`).
  4. Relative local imports (`./styles.css`).

## Commit Message Conventions
We follow Conventional Commits:
- `feat: [feature description]`
- `fix: [bug fix description]`
- `chore: [maintenance, dependencies]`
- `docs: [documentation updates]`
- `refactor: [code refactoring]`
