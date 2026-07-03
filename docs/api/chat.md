# Chat API

Методы группы `telegram.Chat` работают с чатами и участниками.

```groovy
import ru.nerilov.telegram.TelegramDto
```

## Управление чатом

```groovy
// Выйти из чата
telegram.Chat.leave(chatId)
telegram.Chat.leave(chat as TelegramDto.Chat)

// Установить новое название чата
telegram.Chat.setTitle(chatId, title)
telegram.Chat.setTitle(chat as TelegramDto.Chat, title)

// Установить аватарку чата
telegram.Chat.setPhoto(chatId, photoFile)
telegram.Chat.setPhoto(chat as TelegramDto.Chat, photoFile)
telegram.Chat.setPhoto(chatId, photoObject)
telegram.Chat.setPhoto(chat as TelegramDto.Chat, photoObject)
```

## Участники

```groovy
// Получить всех администраторов
telegram.Chat.getAdministrators(chatId)
telegram.Chat.getAdministrators(chat as TelegramDto.Chat)

// Получить участника чата
telegram.Chat.getMember(chatId, userId)
telegram.Chat.getMember(chat as TelegramDto.Chat, userId)

// Получить всех участников
telegram.Chat.getMembers(chatId, allAccessUserIds as List<Long>)
telegram.Chat.getMembers(chat as TelegramDto.Chat, allAccessUserIds as List<Long>)
```

## Модерация

```groovy
// Забанить участника в чате
telegram.Chat.banChatMember(chatId, userId, untilDate, revokeMessages)
telegram.Chat.banChatMember(chat as TelegramDto.Chat, user as TelegramDto.User, untilDate, revokeMessages)

// Разбанить участника в чате
telegram.Chat.unbanChatMember(chatId, userId, onlyIfBanned)
telegram.Chat.unbanChatMember(chat as TelegramDto.Chat, user as TelegramDto.User, onlyIfBanned)
```
