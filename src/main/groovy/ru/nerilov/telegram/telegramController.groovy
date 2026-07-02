package ru.nerilov.telegram

/*! UTF8 */

/**
 * # telegramController - Naumen Service Desk Package
 * Пакет для взаимодействия с инстансом Telegram. *
 * Содержит методы, которые формируют уникальные структурированные данные *
 * @author Erilov.NA
 * @since 03.07.2025
 * @version 2.0.1
 */

import ru.nerilov.telegram.TelegramDto.Webhook.Update

/**
 * Получает запросы от Telegram
 * @param requestContent системная переменная запроса
 */
@SuppressWarnings(["GrMethodMayBeStatic", "unused"])
String getWebhook(Map requestContent) {
    TelegramConnector telegram = new TelegramConnector()
    TelegramUpdateProcessor processor = new TelegramUpdateProcessor(telegram)

    Update update = telegram.objectMapper.readValue(telegram.objectMapper.writeValueAsString(requestContent), Update)

    processor.processUpdate(update)
    return 'ok'
}