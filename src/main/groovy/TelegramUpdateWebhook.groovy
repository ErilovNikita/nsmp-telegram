/*& 900 */
/*
 * Автоматическое обновление токена телеграм бота, с помощью указания новой ссылки WebHook
 * @author Erilov.NA
 * @since 03.07.2025
 * @contributions Erilov.NA
 * @version 2.1.2
 */

import ru.nerilov.telegram.TelegramConnector
import ru.nerilov.telegram.NsmpConstants
import ru.nerilov.telegram.TelegramDto.Webhook.UpdateType

// Инвалидация всех ключей бота
api.auth.removeAccessKeys(NsmpConstants.BOT_EMPLOYEE_LOGIN)

// Создание нового ключа для бота
String accessKey = api.auth.getAccessKey(NsmpConstants.BOT_EMPLOYEE_LOGIN).setDeadlineHours(25).uuid

//  Формирование URL для установки нового вебхука
String url = "${api.rest.getBaseUrl()}/services/rest/exec-post?params=requestContent&accessKey=${accessKey}&func=modules.${NsmpConstants.FUNC_MODULE}"

// Установка вебхука с новым ключем
new TelegramConnector().Webhook.set(url, [
        UpdateType.CALLBACK_QUERY.value,
        UpdateType.MESSAGE.value,
        UpdateType.POLL.value,
        UpdateType.POLL_ANSWER.value,
        UpdateType.MESSAGE_REACTION.value
])