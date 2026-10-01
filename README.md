# Ollama now supports Jev-style decision models

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
./mvnw spring-boot:run
```
# ollama-jev-demo
