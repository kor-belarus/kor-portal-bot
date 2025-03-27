
clean:
	./gradlew clean
build:
	./gradlew build
build-no-tests:
	./gradlew build -x test
jar:
	./gradlew jar
tests:
	./gradlew test
run:
	./gradlew bootRun
release:
	./gradlew release

SERVER ?= valery@laps.roborace.org
scp-jar:
	ssh ${SERVER} sudo chown valery:valery /app/kor-portal/
	scp build/libs/kor-portal-0.0.1-SNAPSHOT.jar ${SERVER}:/app/kor-portal/kor-portal.jar

#remote-update:
#	ssh ${SERVER} sudo service roborace stop
#	ssh ${SERVER} sudo service jr-roborace stop
#	make scp-jar
#	ssh ${SERVER} sudo service roborace start
#	ssh ${SERVER} sudo service jr-roborace start
