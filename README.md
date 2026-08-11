# 🚀 CareerPilot AI

**An AI-powered resume analysis and career guidance platform — parse resumes, get an ATS score, close skill gaps, and receive a personalized AI mentor roadmap.**

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?logo=springboot&logoColor=white">
  <img alt="React" src="https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black">
  <img alt="Vite" src="https://img.shields.io/badge/Vite-7-646CFF?logo=vite&logoColor=white">
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql&logoColor=white">
  <img alt="Gemini AI" src="https://img.shields.io/badge/Gemini-AI%20Engine-8E75B2?logo=googlegemini&logoColor=white">
  <img alt="Google OAuth" src="https://img.shields.io/badge/Google-OAuth%202.0-4285F4?logo=google&logoColor=white">
  <img alt="Docker" src="https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker&logoColor=white">
</p>

<p align="center">
  <a href="https://careerpilot-ai-1-cgrw.onrender.com/"><b>🌐 Live Demo</b></a>
</p>

> ⚠️ Hosted on Render's free tier — the app may take up to a minute to wake up on first load.

---

## 📖 Overview

**CareerPilot AI** is a full-stack platform that helps students and professionals level up their careers using AI. Upload a resume and get an instant AI-driven ATS score, skill-gap breakdown, and improvement suggestions — then get a personalized learning roadmap and career guidance from the built-in AI mentor, all backed by a real job-search integration.

The project ships with Docker images for both services and is deployment-ready for Render with PostgreSQL.

---

## ✨ Key Features

### 🔐 Authentication & Security
- JWT-based authentication with protected routes
- **Google OAuth 2.0** one-click login with automatic profile creation
- Email OTP verification for registration and password reset
- Secure session management and user authorization

### 📄  AI Resume Analysis
- Upload a resume (PDF) for AI-powered parsing (via Spring AI + Apache Tika)
- Automatic **ATS score** generation
- Resume summary extraction and strength analysis
- Missing-skills detection with optimization suggestions
- Full resume analysis history, saved and retrievable per report

### 🤖 AI Mentor Dashboard
- Personalized career guidance powered by **Google Gemini**
- Skill-gap analysis and recommended technologies to learn
- Custom learning roadmaps and project recommendations
- Interview preparation tips and portfolio improvement suggestions
- Full AI mentor history, so past guidance is never lost

### 👤 User Profile System
- Editable profile with skills, career goals, bio, and experience
- Profile photo upload
- LinkedIn & GitHub integration
- Profile completion percentage tracker
- Google-linked profile support

### 🔎 Job Search Integration
- Live job recommendations pulled from the **Adzuna Jobs API**
- Matching driven by user skills, career goals, and resume analysis results
- Career role and technology-based job suggestions

### 📧 Email Delivery
- Transactional email via **Brevo** and Spring Mail (SMTP)
- Used for OTP verification, password resets, and account security notices

---

## 🏗️ Tech Stack

| Layer | Technology |
|---|---|
| **Frontend** | React 18, Vite, React Router, Framer Motion, Axios, React Toastify |
| **Backend** | Java 21, Spring Boot 3.5, Spring Security, Spring Data JPA / Hibernate |
| **Database** | PostgreSQL |
| **AI Engine** | Google Gemini (via Spring AI + `google-genai`), Apache Tika for document parsing |
| **Authentication** | JWT (jjwt) + Google OAuth 2.0 |
| **Email** | Brevo API + Spring Boot Mail (SMTP) |
| **Job Search** | Adzuna Jobs API |
| **DevOps** | Docker, Docker Compose, Spring Boot Actuator, Render |

---

## 🗂️ Project Structure

```
CareerPilot-AI/
├── Back-End/                              # Spring Boot API
│   ├── src/main/java/com/ai/Resume/analyser/
│   │   ├── configuration/                  # Security config, entry point handling
│   │   ├── controller/                     # REST controllers (App, Security, Front)
│   │   ├── jwt/                            # JWT filter & token service
│   │   ├── mail/                           # Email/OTP service
│   │   ├── model/                          # Entities & DTOs (resume, jobs, profile, history)
│   │   ├── repository/                     # Spring Data JPA repositories
│   │   └── service/                        # Business logic & OAuth success/failure handlers
│   └── Dockerfile
├── Front-End/                             # React + Vite SPA
│   ├── src/
│   │   ├── home/                           # Landing & dashboard overview
│   │   ├── login/                          # Auth pages (JWT + Google OAuth)
│   │   ├── upload/                         # Resume upload flow
│   │   ├── analyse / analysis/             # ATS analysis & history views
│   │   ├── aiMentor/                       # AI mentor dashboard & history
│   │   ├── profile/                        # Profile management
│   │   ├── resetpassword/                  # OTP-based password reset
│   │   └── components/                     # Shared UI components
│   └── Dockerfile
├── docker-compose.yml                      # Backend + Frontend + PostgreSQL stack
└── .env.example                            # Required environment variables
```

---

## 🔌 API Overview

| Module | Base Path | Highlights |
|---|---|---|
| **Auth & Security** | `/resumeAnalyser/entry/v1` | Register, email OTP verify, login, password reset OTP flow |
| **Core App** | `/resumeAnalyserCore/service/v1` | Resume extract/analyze, analysis history, AI mentor save/history, career suggestions, profile CRUD, profile photo, logout, delete account |

> 📌 Protected routes require a valid JWT in the `Authorization: Bearer <token>` header. Google OAuth login issues a session that's exchanged for a JWT on success.

---

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Node.js 18+
- PostgreSQL (or Docker)
- A [Google AI (Gemini) API key](https://ai.google.dev/)
- A [Google OAuth 2.0](https://console.cloud.google.com/) client ID & secret
- A [Brevo](https://www.brevo.com/) account for transactional email
- An [Adzuna](https://developer.adzuna.com/) app ID & API key for job search

### 1. Clone the repository
```bash
git clone https://github.com/deepakdeore5650/CareerPilot-AI.git
cd CareerPilot-AI
```

### 2. Configure environment variables
```bash
cp .env.example .env
```
Fill in your database, Gemini, Google OAuth, Brevo, Adzuna, and JWT values.

### 3. Run with Docker Compose (recommended)
```bash
docker compose up --build -d
```
- Backend → `http://localhost:8086`
- Frontend → `http://localhost:5173`

### 4. Or run manually

**Backend**
```bash
cd Back-End
./mvnw spring-boot:run
```

**Frontend**
```bash
cd Front-End
npm install
npm run dev
```

---

## 🔐 Environment Variables

| Variable | Description |
|---|---|
| `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | PostgreSQL database credentials |
| `POSTGRES_PORT` | PostgreSQL port (default `5432`) |
| `APP_PORT` | Backend container port (default `8086`) |
| `DATABASE_URL` | Full PostgreSQL JDBC URL used by the backend |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origin(s) for CORS |
| `VITE_API_BASE_URL` | Backend API base URL used by the frontend |
| `API_KEY` | Brevo API key for transactional email |
| `GEN_KEY` | Google Gemini / GenAI API key |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Google OAuth 2.0 credentials |
| `APPLICATION_ID` / `APPLICATION_API_KEY` | Adzuna Jobs API credentials |
| `JWT_SECRET` | Secret used to sign JWT tokens |
| `UPLOADS_PATH` | Directory for uploaded resume files inside the container |

> ⚠️ Never commit a real `.env` file — `.env.example` is the source of truth for required keys.

---

## ☁️ Deployment (Render)

1. Create a new Render **Web Service** with build type `Docker`.
2. Point the Docker context to the repository root (uses `Back-End/Dockerfile`).
3. Add all environment variables from `.env.example` to the Render dashboard.
4. Set `PORT` to Render's assigned port value.
5. Point `DATABASE_URL` at your managed PostgreSQL instance (not `localhost`).
6. Set `CORS_ALLOWED_ORIGINS` to your deployed frontend URL.
7. Deploy the backend, then deploy the frontend with `VITE_API_BASE_URL` pointing at the backend's public URL.

**Health check:**
```bash
curl http://localhost:8086/actuator/health
```
Expected response: `UP`.

---

## 🐳 Docker Notes

- `Back-End/Dockerfile` — multi-stage, non-root, optimized production image
- `Front-End/Dockerfile` — Vite build served via preview/static container
- `docker-compose.yml` — backend + frontend + PostgreSQL with persistent volumes and internal networking
- Database schema is managed via JPA/Hibernate (`ddl-auto=update`) — no separate migration step is required
- In Docker Compose, the backend connects to PostgreSQL using the service name `postgres`, not `localhost`

---

## 🛠️ Troubleshooting

**App won't start**
- Check logs: `docker compose logs -f app`
- Confirm the PostgreSQL container is healthy and `.env` values are populated

**PostgreSQL connection fails**
- Verify the container is running: `docker compose ps postgres`
- Test connectivity: `docker compose exec postgres psql -U postgres -d resume_analayzer -c 'SELECT 1;'`

**Frontend can't reach the backend**
- Confirm `CORS_ALLOWED_ORIGINS` matches the frontend's actual origin
- Confirm `VITE_API_BASE_URL` points to the correct backend URL

---

## 📌 Future Enhancements

- [ ] AI career chatbot
- [ ] AI cover letter generator
- [ ] Mock interview system
- [ ] Real-time job matching
- [ ] Resume builder
- [ ] AI mock tests
- [ ] Multi-language support
- [ ] AI portfolio review

---

## 🤝 Contributing

Contributions are welcome! To contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m "Add your feature"`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

---

## 👨‍💻 Author

**Deepak Deore**
🔗 [GitHub](https://github.com/deepakdeore5650)

---

## ⭐ Project Goal

CareerPilot AI aims to help students and developers improve their resumes, learn in-demand technologies, build stronger projects, prepare for interviews, and grow their careers using AI-powered mentorship.

---

<p align="center">If you found this project useful, consider giving it a ⭐ on GitHub!</p>
