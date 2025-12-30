docker compose --file compose.yaml up

spring.application.name=demo
#spring.ai.ollama.chat.model=gemma3:4b
spring.ai.ollama.chat.model=qwen3:0.6b
#spring.ai.ollama.chat.model=deepseek-r1:1.5b
logging.level.org.zalando.logbook: TRACE
logbook.format.style=http
#spring.ai.ollama.chat.options.model=gemma3:4b
spring.ai.ollama.chat.options.model=qwen3:0.6b
#spring.ai.ollama.chat.options.model=deepseek-r1:1.5b
spring.ai.vectorstore.qdrant.initialize-schema=true
spring.ai.vectorstore.qdrant.collection-name=board-game-buddy
