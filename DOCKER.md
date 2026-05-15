# Trouble Ticket API - Docker deployment guide

## Build Docker Image

```bash
# Build the JAR first
mvn clean package

# Build Docker image
docker build -t trouble-ticket-api:latest .
```

## Run with Docker

```bash
# Run with default H2 database
docker run -p 8080:8080 trouble-ticket-api:latest

# Run with environment variables (MySQL)
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/troubleticket \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=password \
  -e JWT_SECRET=your-secret-key \
  trouble-ticket-api:latest
```

## Docker Compose (Recommended)

Complete setup with MySQL database:

```bash
# Start services
docker-compose up -d

# Check logs
docker-compose logs -f trouble-ticket-app

# Stop services
docker-compose down
```

Services will start on:
- API: http://localhost:8080
- MySQL: localhost:3306

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:h2:mem:testdb` | Database connection URL |
| `SPRING_DATASOURCE_USERNAME` | `sa` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | (empty) | Database password |
| `JWT_SECRET` | `your-secret-key-for-dev-only` | JWT signing key |
| `JWT_EXPIRATION` | `86400000` | Token expiration in milliseconds |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `create-drop` | Schema auto-create strategy |

## Health Check

```bash
curl http://localhost:8080/actuator/health
```

## Push to Docker Registry

```bash
# Tag image
docker tag trouble-ticket-api:latest your-registry/trouble-ticket-api:latest

# Push
docker push your-registry/trouble-ticket-api:latest
```

## Kubernetes Deployment

Example deployment.yaml:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: trouble-ticket-api
spec:
  replicas: 2
  selector:
    matchLabels:
      app: trouble-ticket-api
  template:
    metadata:
      labels:
        app: trouble-ticket-api
    spec:
      containers:
      - name: api
        image: trouble-ticket-api:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:mysql://mysql-service:3306/troubleticket"
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: api-secrets
              key: jwt-secret
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
```

