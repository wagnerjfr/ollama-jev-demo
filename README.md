# ollama-jev-demo
Ollama now supports Jev-style decision models

## Prerequisites

- Java 21
- Maven
- Ollama running locally at `http://localhost:11434`

## Quick Start

### 1. Start Ollama

```bash
docker run -d \
    -v ollama_models:/root/.ollama \
    -p 11434:11434 \
    --name ollama-decision \
    ollama/ollama:latest
```

### 2. Pull the Model

```bash
docker exec ollama-decision ollama pull tev1:0.8b
```

### 3. Build and Run

```bash
git clone https://github.com/wagnerjfr/ollama-jev-demo.git
cd ollama-jev-demo
./mvnw spring-boot:run
```

Expected output:
```
Team: TECHNICAL (confidence 0.93)
Frustration: FRUSTRATED
Urgency: 1.00
Action: Fast lane for the TECHNICAL team
```
