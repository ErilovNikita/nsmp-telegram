# API

Создайте экземпляр `TelegramConnector`, чтобы работать с Telegram Bot API:

```groovy
import ru.nerilov.telegram.TelegramConnector

TelegramConnector telegram = new TelegramConnector()
```

Далее используйте вложенные API-группы:

- [`telegram.Webhook`](/api/webhook) - получение, установка и удаление WebHook.
- [`telegram.Chat`](/api/chat) - операции с чатами и участниками.
- [`telegram.Message`](/api/message) - операции с сообщениями.

## DTO

Некоторые методы принимают как простые идентификаторы, так и DTO-объекты:

```groovy
import ru.nerilov.telegram.TelegramDto
```

Например, для отправки сообщения можно передать `chatId` или `TelegramDto.Chat`.
