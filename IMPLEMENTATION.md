# Trouble Ticket API - Spring Boot Implementation

## Przegląd

Ten projekt to implementacja RESTful API do zarządzania zgłoszeniami (Trouble Tickets) zgodnie ze specyfikacją OpenAPI 3.1.0. Aplikacja jest zbudowana na Spring Boot 4.0.6 z Java 21.

## Struktura projektu

```
src/main/java/service/troubleticket/
├── TroubleticketApplication.java          # Główna klasa aplikacji
├── controller/
│   ├── TroubleTicketController.java        # REST Controller z endpointami API
│   └── GlobalExceptionHandler.java         # Globalna obsługa wyjątków
├── service/
│   └── TroubleTicketService.java           # Warstwa biznesowa
├── entity/
│   ├── TroubleTicketEntity.java            # Encja JPA dla zgłoszenia
│   └── NoteEntity.java                     # Encja JPA dla notatki
├── dto/
│   ├── TroubleTicketCreateRequest.java
│   ├── TroubleTicketResponse.java
│   ├── TroubleTicketSummary.java
│   ├── TroubleTicketCloseStatusRequest.java
│   ├── NoteCreateRequest.java
│   ├── NoteResponse.java
│   └── ErrorResponse.java
├── repository/
│   ├── TroubleTicketRepository.java        # JPA Repository dla tickets
│   └── NoteRepository.java                 # JPA Repository dla notatek
├── security/
│   ├── JwtTokenFilter.java                 # JWT Authentication Filter
│   └── JwtTokenProvider.java               # Walidacja i parsowanie JWT
├── exception/
│   ├── TroubleTicketException.java
│   └── TroubleTicketNotFoundException.java
└── config/
    ├── SecurityConfig.java                 # Konfiguracja Spring Security
    └── JacksonConfig.java                  # Konfiguracja JSON serialization
```

## API Endpoints

### Trouble Tickets

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

### Notes

- **POST /api/v1/troubleTicket/{id}/note** - Dodaj notatkę
  - Tworzy subresource notatki do zgłoszenia

## Uwierzytelnianie

Wszystkie endpointy wymagają Bearer token w nagłówku `Authorization`:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Tenant scope wynika wyłącznie z claim 'sub' (subject) w JWT tokenie.

## Konfiguracja

### application.properties

```properties
# JWT
jwt.secret=your-super-secret-key-for-jwt-signing-do-not-use-in-production
jwt.expiration=86400000  # 24 godziny

# Baza danych (H2 in-memory)
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop  # Auto-create schema
```

### Zmienne środowiskowe (production)

```bash
JWT_SECRET=your-production-secret-key
JWT_EXPIRATION=86400000
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/troubleticket
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=password
```

## Koncepty implementacji

### Idempotencja (Create)

Operacja `POST /api/v1/troubleTicket` jest idempotentna:
- Klucz unikatowości: `(tenantId, externalId)`
- Jeśli zgłoszenie już istnieje → zwraca 200 z istniejącej reprezentacją
- Jeśli nowe → zwraca 201 z nową reprezentacją

### Status Mapping

- Request status "new" → Entity status "acknowledged" (simulacja przetwarzania po stronie SOZ)
- Dostępne statusy: `new`, `acknowledged`, `inProgress`, `resolved`, `closed`, `rejected`

### Bezpieczeństwo

- Spring Security + JWT Token Filter
- STATELESS session management
- CSRF protection disabled (API-only, no forms)
- CORS disabled (customize as needed)

## Budowanie i uruchamianie

### Build

```bash
cd trouble-ticket
mvn clean package
```

### Run

```bash
java -jar target/troubleticket-0.0.1-SNAPSHOT.jar
```

Aplikacja uruchomi się na `http://localhost:8080`

### H2 Console (Development)

```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (empty)
```

## Testowanie API

### Przykład: Utwórz ticket

```bash
curl -X POST http://localhost:8080/api/v1/troubleTicket \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "OK-123456",
    "serviceId": 987654321,
    "description": "Brak transmisji danych dla usługi klienta.",
    "status": "new",
    "note": "Zgłoszenie utworzone przez konto API partnera."
  }'
```

### Przykład: Listuj tickets

```bash
curl -X GET http://localhost:8080/api/v1/troubleTicket \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Przykład: Pobierz szczegóły

```bash
curl -X GET http://localhost:8080/api/v1/troubleTicket/TT-1715767620000 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Przykład: Zamknij ticket

```bash
curl -X PATCH http://localhost:8080/api/v1/troubleTicket/TT-1715767620000 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "closed"}'
```

### Przykład: Dodaj notatkę

```bash
curl -X POST http://localhost:8080/api/v1/troubleTicket/TT-1715767620000/note \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"text": "Klient prosi o potwierdzenie planowanego terminu zamknięcia."}'
```

## Generowanie JWT tokenu (Development)

W celach testowania możesz wygenerować token za pomocą np. https://jwt.io z payloadem:

```json
{
  "sub": "tenant-id-here",
  "iat": 1715767620,
  "exp": 1715854020
}
```

Secret: `your-super-secret-key-for-jwt-signing-do-not-use-in-production`

Algorytm: HS256

## Uwagi

- Baza H2 in-memory resetuje się przy każdym uruchomieniu (development)
- W produkcji zmień na MySQL/PostgreSQL
- Zaktualizuj `jwt.secret` na silny, bezpieczny klucz
- Dodaj logowanie (Spring Boot Actuator) dla monitoring
- Rozważ dodanie OpenAPI/Swagger documentation (springdoc-openapi)

## Zależności

- Spring Boot 4.0.6
- Spring Security 7.0.5
- Spring Data JPA
- H2 Database (development)
- JJWT 0.12.3 (JWT handling)
- Lombok (Code generation)
- Jackson (JSON processing)
- Java 21

## Licencja

TMF621 Trouble Ticket API Profile v1.0

