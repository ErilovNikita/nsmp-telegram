# Состав пакета

## telegramConnector

Файл: `src/main/groovy/ru/nerilov/telegram/telegramConnector.groovy`

Основной модуль для общения NSMP с серверами Telegram. Может использоваться самостоятельно, если проекту нужны только исходящие вызовы Telegram API.

Перед созданием модуля в системе замените значение `ACCESS_KEY` в классе `ApiConstants`.

## telegramController

Файл: `src/main/groovy/ru/nerilov/telegram/telegramController.groovy`

Контроллер для работы с WebHook-запросами Telegram. Нужен, если Telegram должен отправлять входящие события в REST-функцию NSD.

## TelegramUpdateWebhook

Файл: `src/main/groovy/TelegramUpdateWebhook.groovy`

Скрипт задачи для обновления WebHook со встроенным ключом NSD. Используйте его вместе с `telegramController`.

Скрипт рассчитан на ежедневный запуск в планировщике, чтобы регулярно обновлять access key и снижать риск компрометации.

При запуске скрипт отключает планировщик `TelegramUpdateLongPolling`, чтобы WebHook и Long Polling не обрабатывали одни и те же события параллельно.

## TelegramUpdateLongPolling

Файл: `src/main/groovy/TelegramUpdateLongPolling.groovy`

Скрипт для получения и обработки обновлений Telegram через Long Polling.

Он использует общий обработчик `telegramUpdateProcessor`, поэтому логика обработки событий совпадает с WebHook-режимом.

При запуске скрипт отключает планировщик `TelegramUpdateWebhook` и удаляет установленный WebHook в Telegram. Для защиты от параллельных запусков используется timestamp-lock в `api.keyValue` по ключу `telegram/longPollingStartedAt`. Lock считается устаревшим через 15 минут. Offset последнего обработанного update хранится в `telegram/offset`.

## telegramUpdateProcessor

Файл: `src/main/groovy/ru/nerilov/telegram/telegramUpdateProcessor.groovy`

Общий обработчик обновлений Telegram. Используется и контроллером WebHook, и Long Polling скриптом.
