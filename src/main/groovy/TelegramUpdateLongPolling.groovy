/*& 900 */

/**
 * Получение новых обновлений используя механизм Long Polling
 * @author Erilov.NA
 * @since 02.06.2026
 * @contributions Erilov.NA
 * @version 1.2.0
 */

import ru.nerilov.telegram.TelegramConnector
import ru.nerilov.telegram.TelegramDto.Webhook.UpdateType
import ru.nerilov.telegram.TelegramUpdateProcessor

TelegramConnector telegram = new TelegramConnector()
TelegramUpdateProcessor processor = new TelegramUpdateProcessor(telegram)

// Отключаем планировщик по веб-хукам
String webHookSchedulerCode = "TelegramUpdateWebhook"
Object webHookSchedulerStatus = api.scheduler.getStatus('ExecuteScriptTask$' + webHookSchedulerCode) as Object
webHookSchedulerStatus?.trigger?.collect{ it.code }?.each{ api.scheduler.disableTrigger(it) }

// Удаление информации о созданном веб-хуке
telegram.Webhook.delete()

Integer offset = api.keyValue.get('telegram', 'offset') ?: 0
Boolean longPollingHasLaunched = api.keyValue.get('telegram', 'longPollingHasLaunched') as Boolean ?: false

if (!longPollingHasLaunched) {
    api.keyValue.put('telegram', 'longPollingHasLaunched', true)

    // Получение и обработка обновлений
    telegram.getUpdates(offset, 100, 30, [
            UpdateType.CALLBACK_QUERY.value,
            UpdateType.MESSAGE.value,
            UpdateType.POLL.value,
            UpdateType.POLL_ANSWER.value,
            UpdateType.MESSAGE_REACTION.value
    ]).each { update ->
        processor.processUpdate(update)
        offset = update.updateId + 1
        api.keyValue.put('telegram', 'offset', value)
    }

    api.keyValue.put('telegram', 'longPollingHasLaunched', false)
} else {
    // просто с получения данных уже был запущен ранее
}
