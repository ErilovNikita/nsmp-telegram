# Установка и настройка

## Требования

- Naumen Service Desk с поддержкой NSMP `>= 4.17.5`.
- Telegram Bot API token.
- Служебный сотрудник NSD, от имени которого будет работать бот.
- Атрибуты пользователя для хранения Telegram chat id и никнейма.

## Настройка токена

В модуле `telegramConnector` замените значение `ACCESS_KEY` в классе `ApiConstants`:

```groovy
protected static final String ACCESS_KEY = "ACCESS_TOKEN"
```

Значение должно содержать токен Telegram-бота.

## Настройка констант NSMP

В `telegramUpdateProcessor` проверьте значения класса `NsmpConstants`:

```groovy
class NsmpConstants {
    public static final String BOT_EMPLOYEE_LOGIN = "system"
    public static final String FUNC_MODULE = "telegramController.getWebhook"
    public static final String PAIR_MESSAGE = "Привет, давай знакомиться. Я знаю что ты скучаешь"
    public static final String USER_ID_CODE = "tlgrmChatId"
    public static final String USER_NICKNAME_CODE = "tlgrmNickname"
}
```

Настройте их под вашу инсталляцию NSD:

- `BOT_EMPLOYEE_LOGIN` - логин служебного сотрудника.
- `FUNC_MODULE` - функция модуля, принимающая WebHook.
- `USER_ID_CODE` - код атрибута с Telegram chat id.
- `USER_NICKNAME_CODE` - код атрибута с Telegram username.

## Выбор режима получения событий

Интеграция поддерживает два режима:

- [WebHook](/guide/webhook) - Telegram сам вызывает REST-функцию NSD.
- [Long Polling](/guide/long-polling) - NSD по расписанию запрашивает обновления у Telegram.

Режимы рассчитаны на работу по отдельности. Не включайте оба сценария одновременно.
