.PHONY: up all down exec env clean

all: up exec

up:
	docker compose up -d

down:
	docker compose down

exec:
	docker exec -it scala bash


logs:
	docker logs backend

remove:
	docker stop scala
	docker rm scala

clean: down
	yes | docker system prune -a