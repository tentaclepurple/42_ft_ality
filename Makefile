# Docker rules
.PHONY: up all down exec env clean remove logs
all: up exec

load: novnc run

up:
	docker compose up -d

down:
	docker compose down

exec:
	docker exec -it scala bash

novnc:
	nohup /start-vnc.sh >/dev/null 2>&1 &

logs:
	docker logs scala

remove:
	docker stop scala
	docker rm scala

clean: down
	yes | docker system prune -a

# Scala dirs
TARGET_DIR = target
SRC_DIR = src

SCALA_FILES = $(wildcard $(SRC_DIR)/*.scala)
CLASS_FILES = $(SCALA_FILES:$(SRC_DIR)/%.scala=$(TARGET_DIR)/%.class)

# Classpath
SCALA_SWING = /usr/share/scala-swing.jar
CLASSPATH = $(TARGET_DIR):$(SCALA_SWING)

MAIN_CLASS = Main

SCALAC = scalac
SCALA = scala

# Scala rules
.PHONY: build run setup

build: setup $(CLASS_FILES)

setup:
	@mkdir -p $(TARGET_DIR) $(SRC_DIR)
	@if [ ! -f $(SRC_DIR)/$(MAIN_CLASS).scala ]; then \
		mv -f *.scala $(SRC_DIR)/ 2>/dev/null || true; \
	fi

$(TARGET_DIR)/%.class: $(SRC_DIR)/%.scala
	$(SCALAC) -d $(TARGET_DIR) -classpath $(CLASSPATH) $(SCALA_FILES)

run: build
	$(SCALA) -classpath $(CLASSPATH) $(MAIN_CLASS)

cleanscala:
	rm -rf $(TARGET_DIR)