# nsmp-telegram
Пакет для работы с Telegram из Naumen Service Desk.

## Состав
### [telegramConnector](src/main/groovy/ru/nerilov/telegram/telegramConnector.groovy)
Основной модуль который служит для общения NSD с серверами Telegram. Может успешно существовать в одиночку.

Для корректного запуска, перед созданием модуля в системе, необходимо заменить значение `ACCESS_KEY` в классе `ApiConstants`

### [telegramController](src/main/groovy/ru/nerilov/telegram/telegramController.groovy)
Модуль контроллер для работы с WebHook'ами Telegram'a.

Необходим в том случае, если есть необходимость реагировать на какие-либо события пользователей в Telegram, и обрабатывать их силами NSD.

### [TelegramUpdateWebhook](/src/main/groovy/TelegramUpdateWebhook.groovy)
Скрипт для обновления ссылки WebHook'a, с встроенным ключем NSD.

Необходим если используете [telegramController](src/main/groovy/ru/nerilov/telegram/telegramController.groovy).

Желательно использовать в качестве скрипта планировщика, для ежедневного обновления ключа, во избежании его компрометирования.

## Возможности
- Отправка сообщения в чат
- Редактирование сообщения в чате
- Закрепить/Открепить сообщение в чате
- Отправка ответа на сообщение в чате
- Поставить реакцию на сообщение в чате
- Переслать сообщение в другой чат
- Отправка локации в сообщении в чат
- Отправка фотографии в сообщении в чат
- Работа с WebHook'ами Telegram'a
- Работа с событиями пользователей в Telegram (Требуется [telegramController](src/main/groovy/ru/nerilov/telegram/telegramController.groovy))
- Автоматическое обновление ключа WebHook'а (Требуется [TelegramUpdateWebhook](/src/main/groovy/TelegramUpdateWebhook.groovy))


## Использование

### Создание экземпляра класса для взаимодействия с API Telegram
```groovy
Telegram telegram = new Telegram()
```

###  Работа с WebHook'ами
```groovy
// Получение текущего статуса
telegram.Webhook.get()

//Устанавливает новые параметры
telegram.Webhook.set("https://ya.ru")
```

###  Работа с сообщениями
```groovy
// Отправка сообщения в чат
telegram.Message.send(userId, messageText)

// Редактирование сообщения в чате
telegram.Message.edit(userId, messageId, messageText)

// Закрепить сообщение в чате
telegram.Message.pin(userId, messageId)
telegram.Message.pin(message as TelegramDto.Message)

// Открепить сообщение в чате
telegram.Message.unpin(userId, messageId)
telegram.Message.unpin(message as TelegramDto.Message)

// Отправка ответа на сообщение в чате
telegram.Message.reply(userId, messageId, messageText)
telegram.Message.reply(message as TelegramDto.Message, messageText)

// Поставить реакцию на сообщение в чате
telegram.Message.setReaction(myID, messageId, 'heart')

// Переслать сообщение в другой чат
telegram.Message.forward(userId, messageId, newUserId)

// Отправка локации в сообщении в чат
telegram.Message.sendLocation(userId, 53.333, 55.000, 10)

// Отправка фотографии в сообщении в чат
telegram.Message.sendPhoto(myID, utils.get(fileUUID), caption)
telegram.Message.sendPhoto(myID, fileUUID as java.io.File, caption)
```

