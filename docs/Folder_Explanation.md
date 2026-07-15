# Folder Explanation

## Monorepo Root
- **`client/`**: React 19 Frontend application built with Vite.
- **`server/`**: Node.js Backend API and Socket server.
- **`android/`**: Android Studio project (Kotlin).
- **`shared/`**: Contains shared types, interfaces, or utility constants.
- **`docs/`**: Project documentation and guides.

## Client Structure (`client/src/`)
- `assets/`: Static files (images, icons).
- `components/`: Reusable UI components (buttons, inputs, cards).
- `layouts/`: Page wrappers (sidebar, navbar).
- `pages/`: Main route views.
- `hooks/`: Custom React hooks.
- `context/`: React Context providers.
- `redux/`: Redux toolkit slices and store setup.
- `routes/`: React Router definitions.
- `services/`: API calls (Axios instances).
- `socket/`: Socket.IO client instances and event listeners.
- `utils/`: Helper functions.
- `constants/`: Hardcoded config values.
- `styles/`: Global CSS/Tailwind styles.

## Server Structure (`server/src/`)
- `config/`: Configuration files (DB connection, third-party setup).
- `controllers/`: Route request handlers.
- `routes/`: Express route definitions.
- `models/`: Mongoose database schemas.
- `middleware/`: Express middleware (auth, error handling).
- `services/`: Business logic and external API integrations (Gemini AI).
- `socket/`: Socket.IO event handlers.
- `utils/`: Helper functions.
- `validators/`: Request validation schemas.
- `constants/`: Hardcoded config values.
- `logs/`: Application logs.

## Android Structure (`android/app/src/main/java/com/aimobile/`)
- `ui/`: Activities, Fragments, and Compose elements.
- `services/`: Foreground and background services.
- `accessibility/`: AccessibilityService implementations.
- `speech/`: Speech recognition implementations.
- `tts/`: Text-To-Speech implementations.
- `socket/`: Socket.IO client logic.
- `intents/`: Android intent wrappers.
- `repository/`: Data layer.
- `models/`: Data classes.
- `utils/`: Helper classes.
