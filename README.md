# Детали реализации

Реализовано с использованием библиотеки [TelegramBots](https://github.com/rubenlagus/TelegramBots)

Библиотека в фоновом процессе занимается поллингом всех обновлений адрессованых боту и прокидывает список updates в
метод `onUpdatesReceived()` у класса `Bot`. Здесь имеет место логика, которая обрабатывает отдельно посты из каналов, и отдельно
сообщения из чата.

### Обработка постов

* Адпейты, являющиеся постами, мы прокинули в метод `processPostsInChannel()` у класса `PostsProcessor`. Здесь мы их
  сгруппируем по id канала, и отдельно займёмся обработкой опубликованного контента для каждого телеграмм канала
* Скачиваем контент постов
* Понимаем, что за группы связанны с этим каналом, извлекаем accessToken аккаунта владельца этих групп
* Выполняем логику (возможно специфичную) по загрузке контента в социальную сеть и публикации поста.
  Метод `processPostInChannel()` у `IPostProcessor`. Через удобную обёртку обращаться к модулям, отвечающим за
  взаимодействие с апи конкретной социальной сети

Имеем модули:
* `ok-api` - наша реализация взаимодействия с апи социальной сети `Одноклассники`
* `vk-api` - обёртка над `vk-api-sdk`, взаимодействие с социальной сетью `ВКонтакте`

### Обработка пришедших боту сообщений

Прийти могут:

- Команды, зарегистрированные путём использования метода `register()` у класса `Bot`. Их обработчика,
  класса-наследника `BotCommand` с логикой обработки команды в методе `execute()`, можно достать
  методом `getRegisteredCommand()` - просто маппинг по идентификатору команды.
- Текстовое сообщение. Может содержать `код авторизации, ссылку на группу, ссылку на канал`. Нужно понимать на каком
  этапе общения с ботом находится пользователь, и правильно интерпретировать его сообщение.
- Всё?

# Запуск

Настроить проперти в файле application.properties. [Пример](application.properties.demo)

Запуск при помощи docker:

```
docker-compose up
```

# Использование

# Особенности и ограничения

1. Один пользователь может работать с несколькими Телеграм-каналами;
2. Один пользователь может работать с несколькими социальными сетями;
3. Один пользователь может работать с несколькими аккаунтами в пределах одной социальной сети;
4. Один пользователь может работать с несколькими группами в пределах одного аккаунта (пользователь является администратором данных групп);
5. Публикация постов для каждого Телеграм-канала может быть связана не более, чем с одной группой в каждой из социальных сетей;
6. Публикация контента, являющейся чужой собственностью (пересылаемые сообщения или посты других пользователей) не осуществляется по причине соблюдения авторских прав;
7. Публикация из закрытых Телеграм-каналов, а также публикация в закрытые группы социальных сетей не осуществляется по причине ограничений посещаемости таких групп и каналов.
8. Поддерживаемый тип медиа-файлов в социальных сетях:
   8.1. Oдноклассники:
   8.1.1. Фотографии
   8.1.2. Видео
   8.1.3. Опросы
   8.2. ВКонтакте:
   8.2.1. Фотографии
   8.2.2. Видео
   8.2.3. Опросы
   8.2.4. Документы




