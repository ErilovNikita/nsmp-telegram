package ru.nerilov.telegram

/*! UTF8 */

/**
 * # telegramController - Naumen Service Desk Package
 * Пакет для взаимодействия с инстансом Telegram. *
 * Содержит методы, которые формируют уникальные структурированные данные *
 * @author Erilov.NA
 * @since 03.07.2025
 * @version 2.0.0
 */

import ru.nerilov.telegram.TelegramDto.Webhook.Update

/**
 * Получает запросы от Telegram
 * @param requestContent системная переменная запроса
 */
@SuppressWarnings(["GrMethodMayBeStatic", "unused"])
String getWebhook(Map requestContent) {
    TelegramConnector telegram = new TelegramConnector()
    TelegramUpdateProcessor processor = new TelegramUpdateProcessor()

    Update update = telegram.objectMapper.readValue(
            telegram.objectMapper.writeValueAsString(requestContent),
            Update
    )

    if (update?.callbackQuery) processor.processCallback(update.callbackQuery)
    if (update?.message) processor.processMessage(telegram, update.message)

    return 'ok'
}