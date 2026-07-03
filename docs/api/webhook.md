# WebHook API

Методы группы `telegram.Webhook` управляют WebHook Telegram.

```groovy
// Получение текущего статуса
telegram.Webhook.get()

// Установка нового WebHook
telegram.Webhook.set("https://example.com/webhook")

// Удаление WebHook, если используется Long Polling
telegram.Webhook.delete()
```

## Когда использовать

- `get()` - для диагностики текущего состояния WebHook.
- `set(url)` - при настройке WebHook-режима.
- `delete()` - при переходе на Long Polling.
