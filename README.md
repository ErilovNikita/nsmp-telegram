# NSMP Telegram
> Пакет для полноценной интеграции Telegram в Naumen Service Desk.

[![License](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
![nsmp support](https://img.shields.io/badge/NSMP-%3E%3D4.17.5-blue)

<p align="center">
  <img src="docs/logo.png" alt="Logo" width="256" height="256">
</p>

## Состав
### [telegramConnector](src/main/groovy/ru/nerilov/telegram/telegramConnector.groovy)
Основной модуль который служит для общения NSMP с серверами Telegram. Может успешно существовать в одиночку.

Для корректного запуска, перед созданием модуля в системе, необходимо заменить значение `ACCESS_KEY` в классе `ApiConstants`

### [telegramController](src/main/groovy/ru/nerilov/telegram/telegramController.groovy)
Модуль контроллер для работы с WebHook'ами Telegram'a.

Необходим в том случае, если есть необходимость обрабатывать внешние запросы от серверов Telegram.

### [TelegramUpdateWebhook](/src/main/groovy/TelegramUpdateWebhook.groovy)
Скрипт задачи для обновления WebHook'a, с встроенным ключем NSD.

Необходим если используете [telegramController](src/main/groovy/ru/nerilov/telegram/telegramController.groovy).

Желательно использовать в качестве скрипта планировщика, для ежедневного обновления ключа, во избежании его компрометирования.

При запуске отключает планировщик [TelegramUpdateLongPolling](/src/main/groovy/TelegramUpdateLongPolling.groovy), чтобы получение событий через WebHook и Long Polling не выполнялось одновременно.

### [TelegramUpdateLongPolling](/src/main/groovy/TelegramUpdateLongPolling.groovy)
Скрипт для получения и обработки обновлений Telegram через Long Polling.

Использует общий обработчик [telegramUpdateProcessor](src/main/groovy/ru/nerilov/telegram/telegramUpdateProcessor.groovy), поэтому логика обработки событий одинаковая для WebHook и Long Polling.

При запуске отключает планировщик [TelegramUpdateWebhook](/src/main/groovy/TelegramUpdateWebhook.groovy) и удаляет установленный WebHook в Telegram. Для защиты от параллельных запусков хранит timestamp-lock в `api.keyValue` по ключу `telegram/longPollingStartedAt`. Lock считается устаревшим через 15 минут. Offset последнего обработанного update хранится в `telegram/offset`.

### [telegramUpdateProcessor](src/main/groovy/ru/nerilov/telegram/telegramUpdateProcessor.groovy)
Общий обработчик обновлений Telegram.

Используется и контроллером WebHook'ов, и скриптом Long Polling.

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
- Получение событий пользователей через Long Polling (Требуется [TelegramUpdateLongPolling](/src/main/groovy/TelegramUpdateLongPolling.groovy))


## Использование
###  Режимы получения событий
Интеграция поддерживает два режима получения входящих событий Telegram:

- WebHook: Telegram сам вызывает REST-функцию NSD, которую обслуживает [telegramController](src/main/groovy/ru/nerilov/telegram/telegramController.groovy).
- Long Polling: NSD по расписанию вызывает [TelegramUpdateLongPolling](/src/main/groovy/TelegramUpdateLongPolling.groovy), получает обновления через `getUpdates` и передает их в общий обработчик.

Режимы рассчитаны на работу по отдельности. При включении WebHook-режима скрипт [TelegramUpdateWebhook](/src/main/groovy/TelegramUpdateWebhook.groovy) отключает планировщик Long Polling. При включении Long Polling-режима скрипт [TelegramUpdateLongPolling](/src/main/groovy/TelegramUpdateLongPolling.groovy) отключает планировщик WebHook-обновления и удаляет WebHook в Telegram.

Для WebHook-режима:
- Создайте модуль [telegramController](src/main/groovy/ru/nerilov/telegram/telegramController.groovy);
- Настройте [TelegramUpdateWebhook](/src/main/groovy/TelegramUpdateWebhook.groovy) как ежедневную задачу планировщика для обновления access key;
- Убедитесь, что планировщик [TelegramUpdateLongPolling](/src/main/groovy/TelegramUpdateLongPolling.groovy) выключен.

Для Long Polling-режима:
- Создайте планировщик для [TelegramUpdateLongPolling](/src/main/groovy/TelegramUpdateLongPolling.groovy);
- Убедитесь, что планировщик [TelegramUpdateWebhook](/src/main/groovy/TelegramUpdateWebhook.groovy) выключен;
- При первом запуске WebHook будет удален автоматически;
- `offset` обновлений хранится в `api.keyValue` как `telegram/offset`;
- `timestamp` текущего запуска хранится в `api.keyValue` как `telegram/longPollingStartedAt` и защищает от повторного запуска в течение 15 минут.

### Создание экземпляра класса для взаимодействия с API Telegram
```groovy
import ru.nerilov.telegram.TelegramConnector

TelegramConnector telegram = new TelegramConnector()
```

###  Работа с WebHook'ами
```groovy
// Получение текущего статуса
telegram.Webhook.get()

//Устанавливает новые параметры
telegram.Webhook.set("https://ya.ru")

// Удаляет WebHook, если используется Long Polling
telegram.Webhook.delete()
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
