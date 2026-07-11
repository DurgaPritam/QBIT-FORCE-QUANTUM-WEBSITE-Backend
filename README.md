# Qbit Force Backend (Spring Boot)

Secure REST API for contact submissions, gallery, videos, publications, and press media — with JWT-protected admin CMS.

## Prerequisites

- Java 21+
- Maven 3.9+ (or use IDE Maven integration)
- MySQL 8+ running locally

## 1. Configure environment

Create `backend/.env` with your secrets:


| Variable | Description |
|----------|-------------|
| `DB_PASSWORD` | MySQL password (e.g. root user) |
| `JWT_SECRET` | **Required** — see below |
| `ADMIN_EMAIL` | Optional — default `info@qbitforcequantum.com` (in `application.yml`) |
| `ADMIN_PASSWORD` | Optional after first admin exists — only needed once to seed a new admin |

### Generate JWT secret

Use OpenSSL (recommended — 512-bit key):

```bash
openssl rand -base64 64
```

PowerShell alternative:

```powershell
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))
```

Paste the output into `JWT_SECRET` in `backend/.env`. **Never commit this value.**

### How JWT works in this project

1. Admin logs in at `POST /api/auth/login` with username + password.
2. Server verifies credentials against MySQL (`admin_users.password_hash` — BCrypt).
3. Server signs a JWT using `JWT_SECRET` (HMAC-SHA256).
4. Frontend stores the token in `sessionStorage` and sends `Authorization: Bearer <token>` on admin requests.
5. `JwtAuthFilter` validates signature + expiry on every `/api/admin/**` request.

### Admin password storage

- The plain `ADMIN_PASSWORD` in `.env` is used **only once** to create the initial admin row.
- MySQL stores `password_hash` (BCrypt, strength 12) — **not** the plain password.
- To change the password later, update the hash in MySQL or delete the admin row and restart with a new `ADMIN_PASSWORD`.

## 2. Create database

MySQL creates `qbitforce_db` automatically on first run (`createDatabaseIfNotExist=true`).

Or manually:

```sql
CREATE DATABASE qbitforce_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 3. Run the API

```bash
cd backend
mvn spring-boot:run
```

API base URL: `http://localhost:8080/api`

## 4. Frontend

Root `.env`:

```
VITE_API_BASE_URL=http://localhost:8080/api
```

```bash
npm run dev
```

Admin panel (no public link): **`http://localhost:5173/qbitadmin-2026-login`**

## API overview

### Public (no auth)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/public/contact` | Submit contact form (saved to DB) |
| GET | `/api/public/gallery` | Active gallery items |
| GET | `/api/public/videos` | Active videos |
| GET | `/api/public/publications` | Active publications / blog posts |
| GET | `/api/public/press` | Active press & media items |

### Auth

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/login` | Returns JWT `{ accessToken, tokenType, expiresIn, username }` |

### Admin (Bearer JWT required)

| Method | Path | Description |
|--------|------|-------------|
| GET/POST | `/api/admin/gallery` | List / create gallery items |
| PUT/DELETE | `/api/admin/gallery/{id}` | Update / delete |
| GET/POST | `/api/admin/videos` | List / create videos |
| PUT/DELETE | `/api/admin/videos/{id}` | Update / delete |
| GET/POST | `/api/admin/publications` | List / create publications |
| PUT/DELETE | `/api/admin/publications/{id}` | Update / delete |
| GET/POST | `/api/admin/press` | List / create press items |
| PUT/DELETE | `/api/admin/press/{id}` | Update / delete |
| GET | `/api/admin/contacts` | View contact form submissions |

## Security features

- BCrypt password hashing (admin)
- JWT stateless authentication
- CORS restricted to configured origins
- Rate limiting on login and contact submissions
- Security headers (HSTS, frame deny, referrer policy)
- Secrets only in `.env` (gitignored)
- Generic error messages (no stack traces to clients)
- Admin panel hidden URL — not linked from public site

## Contact submissions

Contact form posts are saved to MySQL and shown in the admin panel. No SMTP / outbound email is used.

Admin account email is set via `ADMIN_EMAIL` (default: `info@qbitforcequantum.com`).
Forgot-password creates a reset link and writes it to the server logs (no email send).

## Production checklist

- [ ] Generate a new strong `JWT_SECRET`
- [ ] Use a dedicated MySQL user (not `root`)
- [ ] Set `CORS_ALLOWED_ORIGINS` to your production domain only
- [ ] Enable HTTPS (reverse proxy / load balancer)
- [ ] Rotate admin password after first login
- [ ] Set `ADMIN_EMAIL=info@qbitforcequantum.com`
