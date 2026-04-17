.PHONY: all backend frontend

# Default target
all: backend frontend

backend:
	@cd backend && mvn spring-boot:run &

frontend:
	@cd frontend && \
		[ -d node_modules ] || npm ci && \
		npm run dev
