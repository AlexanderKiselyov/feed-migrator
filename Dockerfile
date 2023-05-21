FROM gradle:jdk17

WORKDIR /feed-migrator-app
COPY . .

RUN gradle build

CMD gradle :tg-bot:run
