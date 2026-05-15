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

## Szybki start

### Wymagania

- Java 21+
- Maven 3.8+

### Build

```bash
cd trouble-ticket
mvn clean package
```

### Run

```bash
java -jar target/troubleticket-0.0.1-SNAPSHOT.jar
```

Aplikacja będzie dostępna na `http://localhost:8080`

## API Documentation

Pełna dokumentacja API znajduje się w pliku: **[IMPLEMENTATION.md](./IMPLEMENTATION.md)**

### Główne endpointy

```
POST   /api/v1/troubleTicket              # Utwórz zgłoszenie
GET    /api/v1/troubleTicket              # Listuj zgłoszenia
GET    /api/v1/troubleTicket/{id}         # Pobierz szczegóły
PATCH  /api/v1/troubleTicket/{id}         # Zamknij zgłoszenie
POST   /api/v1/troubleTicket/{id}/note    # Dodaj notatkę
```

## Struktura projektu

```
src/main/java/service/troubleticket/
├── TroubleticketApplication.java    # Entry point
├── controller/                       # REST Controllers
├── service/                         # Business logic
├── entity/                          # JPA Entities
├── dto/                            # Data Transfer Objects
├── repository/                      # Data access layer
├── security/                        # JWT authentication
├── exception/                       # Exception handling
└── config/                         # Spring configuration
```

## Konfiguracja

Edytuj `src/main/resources/application.properties`:

```properties
# Server
server.port=8080

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000

# Database
spring.datasource.url=jdbc:h2:mem:testdb
```

## Technologia

- **Framework**: Spring Boot 4.0.6
- **Security**: Spring Security + JWT (JJWT)
- **Database**: H2 (development) / MySQL/PostgreSQL (production)
- **ORM**: Spring Data JPA
- **Java**: 21
- **Build**: Maven

## Functional Features

✅ Create Trouble Ticket (POST)
- Idempotent operation based on (tenantId, externalId)
- Only "new" status accepted in request
- Auto-transitions to "acknowledged" status
- Creates initial note

✅ List Trouble Tickets (GET)
- Returns summary for authenticated tenant scope
- Ordered by creation date (descending)

✅ Get Trouble Ticket Details (GET)
- Full representation with all notes
- 404 if not found or not in tenant scope

✅ Close Trouble Ticket (PATCH)
- Only "closed" status transition allowed
- Returns 400 for invalid status changes

✅ Add Note (POST)
- Creates sub-resource for ticket
- Associates with authenticated tenant scope

## Security

- JWT Bearer token authentication required
- Tenant scope from token's 'sub' claim
- Stateless session management
- CSRF disabled (API-only)

## Testing

See **[IMPLEMENTATION.md](./IMPLEMENTATION.md)** for curl examples and JWT token generation.

## Development

H2 Console available at: `http://localhost:8080/h2-console`

Default credentials:
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

## Production Deployment

1. Update `jwt.secret` to a strong random key
2. Switch database to MySQL/PostgreSQL
3. Set appropriate logging levels
4. Enable HTTPS/SSL
5. Configure environment variables for secrets
6. Add monitoring (Spring Boot Actuator)
7. Consider API documentation (springdoc-openapi)

## Files

- `pom.xml` - Maven dependencies
- `IMPLEMENTATION.md` - Full implementation details
- `src/main/devtask/trouble-ticket-api.yaml` - OpenAPI specification

## License

Implementacja profilu TMF621 Trouble Ticket API

