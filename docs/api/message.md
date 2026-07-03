# Message API

Методы группы `telegram.Message` работают с сообщениями.

```groovy
import ru.nerilov.telegram.TelegramDto
```

## Отправка и удаление

```groovy
// Отправка сообщения в чат
telegram.Message.send(chatId, messageText)
telegram.Message.send(chat as TelegramDto.Chat, messageText)

// Удалить сообщение в чате
telegram.Message.delete(chatId, messageId)
telegram.Message.delete(chat as TelegramDto.Chat, message as TelegramDto.Message)
```

## Редактирование и закрепление

```groovy
// Редактирование сообщения в чате
telegram.Message.edit(chatId, messageId, messageText)
telegram.Message.edit(message as TelegramDto.Message)

// Закрепить сообщение в чате
telegram.Message.pin(chatId, messageId)
telegram.Message.pin(message as TelegramDto.Message)

// Открепить сообщение в чате
telegram.Message.unpin(chatId, messageId)
telegram.Message.unpin(message as TelegramDto.Message)
```

## Ответы, реакции и пересылка

```groovy
// Отправка ответа на сообщение в чате
telegram.Message.reply(chatId, messageId, messageText)
telegram.Message.reply(message as TelegramDto.Message, messageText)

// Поставить реакцию на сообщение в чате
telegram.Message.setReaction(chatId, messageId, 'heart')
telegram.Message.setReaction(message as TelegramDto.Message, 'heart')

// Переслать сообщение в другой чат
telegram.Message.forward(chatId, messageId, fromChatId)
telegram.Message.forward(chat as TelegramDto.Chat, message as TelegramDto.Message)
```

## Медиа и геоданные

```groovy
// Отправка локации в сообщении в чат
telegram.Message.sendLocation(chatId, 53.333, 55.000, 10)
telegram.Message.sendLocation(chat as TelegramDto.Chat, 53.333, 55.000, 10)

// Отправка фотографии в сообщении в чат
telegram.Message.sendPhoto(chatId, utils.get(fileUUID), caption)
telegram.Message.sendPhoto(chat as TelegramDto.Chat, utils.get(fileUUID), caption)
telegram.Message.sendPhoto(chatId, fileObject, caption)
telegram.Message.sendPhoto(chat as TelegramDto.Chat, fileObject, caption)
```
