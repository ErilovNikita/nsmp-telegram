# WebHook

WebHook-режим принимает входящие события через REST-функцию NSD.

## Компоненты

- `telegramController` принимает запрос от Telegram.
- `TelegramUpdateWebhook` обновляет access key и устанавливает WebHook.
- `TelegramUpdateProcessor` обрабатывает update.

## Настройка

1. Создайте модуль `telegramController`.
2. Настройте `TelegramUpdateWebhook` как ежедневную задачу планировщика.
3. Убедитесь, что планировщик `TelegramUpdateLongPolling` выключен.
4. Проверьте `NsmpConstants.FUNC_MODULE`, чтобы он указывал на функцию `telegramController.getWebhook`.

## Как работает обновление ключа

`TelegramUpdateWebhook` выполняет следующие действия:

1. Отключает планировщик Long Polling.
2. Инвалидирует старые access keys служебного сотрудника.
3. Создает новый access key на 25 часов.
4. Собирает URL REST-функции NSD.
5. Устанавливает WebHook в Telegram.

## Поддерживаемые типы событий

Скрипт устанавливает WebHook для следующих update type:

- `callback_query`
- `message`
- `poll`
- `poll_answer`
- `message_reaction`
