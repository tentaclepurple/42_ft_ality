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
TARGET_DIR = target/classes
SRC_DIR = src

# Encontrar archivos fuente de manera más segura
SCALA_FILES = $(shell find $(SRC_DIR) -name "*.scala" -type f | tr '\n' ' ')

# Classpath
SCALA_SWING = /usr/share/scala-swing.jar
CLASSPATH = $(TARGET_DIR):$(SCALA_SWING)

MAIN_CLASS = Main

SCALAC = scalac
SCALA = scala

# Scala rules
.PHONY: build run setup cleanscala

build: setup
	@echo "Compiling: $(SCALA_FILES)"
	@$(SCALAC) -d $(TARGET_DIR) -encoding UTF-8 -classpath $(SCALA_SWING) $(SCALA_FILES)

setup:
	@mkdir -p $(TARGET_DIR)
	@mkdir -p $(SRC_DIR)/grammar
	@mkdir -p $(SRC_DIR)/automaton
	@mkdir -p $(SRC_DIR)/game

run: build
	$(SCALA) -classpath $(CLASSPATH) $(MAIN_CLASS) blueprints/estrificher.gmr

#run: build
#	$(SCALA) -classpath $(CLASSPATH) $(MAIN_CLASS) $(ARGS)


cleanscala:
	rm -rf $(TARGET_DIR)

# scalac -d target/classes -classpath /usr/share/scala-swing.jar src/grammar/Parser.scala src/grammar/Types.scala src/MainApp.scala src/Main.scala

# scala -classpath target/classes:/usr/share/scala-swing.jar Main blueprints/strificher.gmr