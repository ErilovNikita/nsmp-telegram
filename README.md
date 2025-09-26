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
- Забанить/Разбанить пользователя в чате
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

###  Работа с чатом

```groovy
import ru.nerilov.telegram.TelegramDto

// Выйти из чата
telegram.Chat.leave(chatId)
telegram.Chat.leave(chat as TelegramDto.Chat)

// Получить всех администраторов
telegram.Chat.getAdministrators(chatId)
telegram.Chat.getAdministrators(chat as TelegramDto.Chat)

// Получить участника чата
telegram.Chat.getMember(chatId, userId)
telegram.Chat.getMember(chat as TelegramDto.Chat, userId)

// Получить всех участников
telegram.Chat.getMembers(chatId, allAccessUserIds as List<Long>)
telegram.Chat.getMembers(chat as TelegramDto.Chat, allAccessUserIds as List<Long>)

// Установить новое название чата
telegram.Chat.setTitle(chatId, title)
telegram.Chat.setTitle(chat as TelegramDto.Chat, title)

// Установить аватарку чата
telegram.Chat.setPhoto(chatId, photoFile)
telegram.Chat.setPhoto(chat as TelegramDto.Chat, photoFile)
telegram.Chat.setPhoto(chatId, photoObject)
telegram.Chat.setPhoto(chat as TelegramDto.Chat, photoObject)

// Забанить участника в чате
telegram.Chat.banChatMember(chatId, userId, untilDate, revokeMessages)
telegram.Chat.banChatMember(chat as TelegramDto.Chat, user as TelegramDto.User, untilDate, revokeMessages)

// Разбанить участника в чате
telegram.Chat.unbanChatMember(chatId, userId, onlyIfBanned)
telegram.Chat.unbanChatMember(chat as TelegramDto.Chat, user as TelegramDto.User, onlyIfBanned)
```

###  Работа с сообщениями
```groovy
// Отправка сообщения в чат
telegram.Message.send(chatId, messageText)
telegram.Message.send(chat as TelegramDto.Chat, messageText)

// Удалить сообщение в чате
telegram.Message.delete(chatId, messageId)
telegram.Message.delete(chat as TelegramDto.Chat, message as TelegramDto.Message)

// Редактирование сообщения в чате
telegram.Message.edit(chatId, messageId, messageText)
telegram.Message.edit(message as TelegramDto.Message)

// Закрепить сообщение в чате
telegram.Message.pin(chatId, messageId)
telegram.Message.pin(message as TelegramDto.Message)

// Открепить сообщение в чате
telegram.Message.unpin(chatId, messageId)
telegram.Message.unpin(message as TelegramDto.Message)

// Отправка ответа на сообщение в чате
telegram.Message.reply(chatId, messageId, messageText)
telegram.Message.reply(message as TelegramDto.Message, messageText)

// Поставить реакцию на сообщение в чате
telegram.Message.setReaction(chatId, messageId, 'heart')
telegram.Message.setReaction(message as TelegramDto.Message, 'heart')

// Переслать сообщение в другой чат
telegram.Message.forward(chatId, messageId, fromChatId)
telegram.Message.forward(chat as TelegramDto.Chat, message as TelegramDto.Message)

// Отправка локации в сообщении в чат
telegram.Message.sendLocation(chatId, 53.333, 55.000, 10)
telegram.Message.sendLocation(chat as TelegramDto.Chat, 53.333, 55.000, 10)

// Отправка фотографии в сообщении в чат
telegram.Message.sendPhoto(chatId, utils.get(fileUUID), caption)
telegram.Message.sendPhoto(chat as TelegramDto.Chat, utils.get(fileUUID), caption)
telegram.Message.sendPhoto(chatId, fileObject, caption)
telegram.Message.sendPhoto(chat as TelegramDto.Chat, fileObject, caption)
```

