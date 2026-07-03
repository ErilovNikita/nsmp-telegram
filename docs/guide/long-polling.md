# Long Polling

Long Polling-режим получает события через периодический вызов Telegram Bot API `getUpdates`.

## Компоненты

- `TelegramUpdateLongPolling` получает updates.
- `TelegramUpdateProcessor` обрабатывает каждый update.
- `api.keyValue` хранит offset и lock запуска.

## Настройка

1. Создайте планировщик для `TelegramUpdateLongPolling`.
2. Убедитесь, что планировщик `TelegramUpdateWebhook` выключен.
3. Запустите задачу.

При первом запуске установленный WebHook будет удален автоматически.

## Хранилище состояния

Скрипт использует `api.keyValue`:

| Ключ | Назначение |
| --- | --- |
| `telegram/offset` | Offset последнего обработанного update. |
| `telegram/longPollingStartedAt` | Timestamp текущего запуска. |

Lock защищает от параллельного запуска. Он считается устаревшим через 15 минут.

## Поддерживаемые типы событий

Long Polling запрашивает следующие update type:

- `callback_query`
- `message`
- `poll`
- `poll_answer`
- `message_reaction`
