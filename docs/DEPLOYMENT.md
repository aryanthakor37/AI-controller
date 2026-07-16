# Production Deployment Guide for Agent.AI

This guide details the steps required to deploy the complete Agent.AI MERN stack to the cloud.

---

## 1. Database Setup: MongoDB Atlas

1. **Sign Up:** Create a free cluster on [MongoDB Atlas](https://www.mongodb.com/cloud/atlas).
2. **Access Control:** Under *Database Access*, create a user with read/write permissions.
3. **Network Access:** Whitelist all IPs (`0.0.0.0/0`) or specific production server IPs.
4. **Connection String:** Copy the SRV URI connection string:
   `mongodb+srv://<username>:<password>@cluster.mongodb.net/aimobile?retryWrites=true&w=majority`

---

## 2. Backend Deployment: Render or Railway

1. **Create Web Service:** Link your GitHub repository to [Render](https://render.com).
2. **Configuration:**
   - **Environment:** `Node`
   - **Build Command:** `cd server && npm install`
   - **Start Command:** `cd server && npm start`
3. **Environment Variables:**
   - `PORT`: `5000`
   - `NODE_ENV`: `production`
   - `MONGODB_URI`: *[Your MongoDB Atlas Connection String]*
   - `GEMINI_API_KEY`: *[Your Google Gemini API Key]*
   - `JWT_SECRET`: *[Random secure string]*
   - `CLIENT_URL`: *[Your Frontend URL]*

---

## 3. Frontend Deployment: Firebase Hosting

1. **Install CLI:** `npm install -g firebase-tools`
2. **Login & Init:**
   - Run `firebase login`
   - Run `firebase init` (Select *Hosting*, link project, choose `dist` as public build folder, configure as SPA).
3. **Build Client:**
   - Modify `client/src/api/api.js` (or env variables) to point to your Render Backend URL.
   - Run `cd client && npm install && npm run build`
4. **Deploy:** `firebase deploy`
