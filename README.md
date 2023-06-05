# Детали реализации

Реализовано с использованием библиотеки [TelegramBots](https://github.com/rubenlagus/TelegramBots)

Библиотека в фоновом процессе занимается поллингом всех обновлений адрессованых боту и прокидывает список updates в
метод onUpdatesReceived(). Пришедшие команды делегируются к методам execute() у классов-наследников Command.



# Запуск

Настроить проперти в файле [application.properties](application.properties)

Запуск при помощи docker: 
```
docker-compose up
```

# Использование

# Особенности и ограничения




