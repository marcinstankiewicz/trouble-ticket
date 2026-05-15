# Trouble Ticket API

Spring Boot implementation of TMF621 Trouble Ticket API specification (OpenAPI 3.1.0)

## Opis

Minimalny, design-first profil TMF621 Trouble Ticket API z dodatkowymi funkcjonalnościami:
- Tworzenie zgłoszeń (Trouble Tickets)
- Listowanie zgłoszeń
- Pobieranie szczegółów zgłoszenia
- Zamykanie zgłoszenia
- Dodawanie notatek do zgłoszeń
- Obsługa tenant scope poprzez JWT Bearer tokens
- Idempotencja operacji tworzenia

## Główne endpointy

- **POST /api/v1/troubleTicket** - Utwórz nowe zgłoszenie
    - Status request musi być "new"
    - Zwraca 201 dla nowego, 200 dla istniejącego (idempotencja)
    - Nowy ticket automatycznie zmienia status na "acknowledged"

- **GET /api/v1/troubleTicket** - Listuj zgłoszenia
    - Zwraca listę podsumowań dla tenant scope użytkownika
    - Bez paginacji w v1

- **GET /api/v1/troubleTicket/{id}** - Pobierz szczegóły zgłoszenia
    - Zwraca pełną reprezentację z notatkami

- **PATCH /api/v1/troubleTicket/{id}** - Zamknij zgłoszenie
    - Zmienia status wyłącznie na "closed"
    - Inne statusy są odrzucane z 400

- **POST /api/v1/troubleTicket/{id}/note** - Dodaj notatkę
    - Tworzy subresource notatki do zgłoszenia


## 🔒 Bezpieczeństwo

### Implementacja JWT
- **Filter**: `JwtTokenFilter` - extracts Bearer token from Authorization header
- **Provider**: `JwtTokenProvider` - validates JWT and extracts tenant ID
- **Algorytm**: HS256 (HMAC with SHA-256)
- **Secret**: Konfigurowalny via `jwt.secret` property

### Spring Security Configuration
```
- Stateless session management (JWT-based)
- CSRF disabled (API-only)
- CORS disabled (customize as needed)
- JWT filter added before Username/Password filter
- All /api/** endpoints require authentication
```

## 📋 Specyfikacja API

### Request/Response Format
- **Content-Type**: application/json
- **Encoding**: UTF-8
- **Date Format**: ISO 8601 (YYYY-MM-DDTHH:mm:ssZ)

### Status Codes
| Status | Endpoint | Scenariusz |
|--------|----------|-----------|
| 201 | POST Create | Nowe zgłoszenie |
| 200 | POST Create / GET / PATCH | Istniejące lub operacja ok |
| 400 | Any | Błąd walidacji |
| 401 | Any | Brak/nieprawidłowy token |
| 403 | Any | Użytkownik bez uprawnień |
| 404 | GET / PATCH | Zasób nie znaleziony |
| 500 | Any | Wewnętrzny błąd serwera |

### Specjalne koncepty

#### 1. Idempotencja (Create)
```
Klucz unikatowości: (tenantId, externalId)
- Jeśli istnieje: zwraca 200 z istniejącą reprezentacją
- Jeśli nowe: zwraca 201 z nową reprezentacją
```

#### 2. Tenant Scope
```
Wynika wyłącznie z JWT token claim 'sub'
- Użytkownik widzi tylko resources w jego tenant scope
- Bezpieczna izolacja multi-tenant
```

#### 3. Status Mapping
```
Request: "new" → Database: "acknowledged"
(Symuluje auto-przetwarzanie po stronie systemu)
```

#### 4. Notatki
```
- Pierwsza notatka tworzona wraz z ticketem
- Dodatkowe notatki dodawane przez oddzielny endpoint
- Wszystkie notatki zwracane w response ticketu
```

## 🚀 Run

```bash
mvn spring-boot:run
```

Aplikacja będzie dostępna na `http://localhost:8080`

## 🐳 Docker

```bash
# Build image
docker build -t trouble-ticket-api:latest .

# Run with compose
docker-compose up -d

# View logs
docker-compose logs -f trouble-ticket-app
```

## 🧪 Testing

### Postman Collection
Gotowa kolekcja: `Trouble_Ticket_API.postman_collection.json`
Zmienne:
- `baseUrl`: http://localhost:8080
- `jwt_token`: Wygeneruj via jwt.io
- `ticket_id`: Otrzymaj z response create

### Przykład JWT Token (Development)
```
Secret: your-super-secret-key-for-jwt-signing-do-not-use-in-production
Payload:
{
  "sub": "tenant-id-here",
  "iat": 1715767620,
  "exp": 1715854020
}
Algorytm: HS256
```

TOP SECRET!
```
Docker 
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.6NvwUYZqfAm2e1TAY94WwLG2DodkLKKuW55ynPrzL_o
Maven 
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.gRZXVk0MRb1S-2ESxLefw-3LsPiaXffg6Cr9VLebh-M
```
