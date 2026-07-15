# Environment Variables Guide

## Client Environment (`client/.env`)
| Variable | Description | Placeholder / Default |
|----------|-------------|------------------------|
| `VITE_API_URL` | The HTTP endpoint for the backend API | `http://localhost:5000/api` |
| `VITE_SOCKET_URL` | The Socket.IO endpoint for the backend | `http://localhost:5000` |

## Server Environment (`server/.env`)
| Variable | Description | Placeholder / Default |
|----------|-------------|------------------------|
| `PORT` | The port the server runs on | `5000` |
| `MONGODB_URI` | MongoDB Connection String | `mongodb://localhost:27017/ai-mobile-control` |
| `JWT_SECRET` | Secret key for signing JSON Web Tokens | `your_jwt_secret_placeholder` |
| `GEMINI_API_KEY` | Google Gemini API Key | `your_gemini_api_key_placeholder` |
| `CLIENT_URL` | The URL of the React frontend | `http://localhost:3000` |
