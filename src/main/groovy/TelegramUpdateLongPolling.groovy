/*& 900 */

/**
 * Получение новых обновлений используя механизм Long Polling
 * @author Erilov.NA
 * @since 02.06.2026
 * @contributions Erilov.NA
 * @version 1.3.0
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

// Проверка выполнения процесса
Integer offset = api.keyValue.get('telegram', 'offset') ?: 0
Long lockTtlMs = 15 * 60000 // 15 минут
Long currentTimestamp = System.currentTimeMillis()
Object longPollingStartedAtValue = api.keyValue.get('telegram', 'longPollingStartedAt')
Long longPollingStartedAt = longPollingStartedAtValue?.toString()?.isLong()
        ? longPollingStartedAtValue.toString().toLong()
        : null

if (!longPollingStartedAt || currentTimestamp - longPollingStartedAt > lockTtlMs) {
    api.keyValue.put('telegram', 'longPollingStartedAt', currentTimestamp)

    try {
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
            api.keyValue.put('telegram', 'offset', offset)
        }
    } finally {
        if (api.keyValue.get('telegram', 'longPollingStartedAt')?.toString() == currentTimestamp.toString()) api.keyValue.put('telegram', 'longPollingStartedAt', null)
    }
} else {
    // Получение данных было запущено ранее, lock еще актуален
}
