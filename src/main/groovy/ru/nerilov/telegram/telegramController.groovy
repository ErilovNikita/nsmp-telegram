package ru.nerilov.telegram

/*! UTF8 */

/**
 * # telegramController - Naumen Service Desk Package
 * Пакет для взаимодейсвтия с инстансом Telegram. *
 * Содержит методы, которые формируют уникальные структурированные данные *
 * @author Erilov.NA
 * @since 03.07.2025
 * @version 1.0.0
 */

import ru.naumen.core.server.script.api.injection.InjectApi

/**
 * Статические перемеменные
 */
class NsmpConstants {
    /** Логин служебного сотрудника в NSD */
    public static final String BOT_EMPLOYEE_LOGIN = "system"
    /** Функционал модуля, который будет использоваться для установки вебхука */
    public static final String FUNC_MODULE = "telegramController.getWebhook"
    /** Сообщение для "знакомства" с ботом */
    public static final String PAIR_MESSAGE = "Привет, давай знакомиться. Я знаю что ты скучаешь"
    /** Код атрибута с идентификатором чата */
    public static final String USER_ID_CODE = "tlgrmChatId"
    /** Код атрибута с никнеймом */
    public static final String USER_NICKNAME_CODE = "tlgrmNickname"
}

/**
 * Обертка Для работы с данными NSMP. Позволяет создавать
 * и обновлять объекты. Содержит бизнес-логику
 */
@InjectApi
class ObjectWrapper {

    /**
     * Метод получения chatId из объекта пользователя
     * @param userObject Объект пользователя
     * @return Возвращает chatId пользователя, если он есть, иначе null
     */
    static Long getChatId(Object userObject) {
        if (!userObject) return null
        return userObject[(NsmpConstants.USER_ID_CODE)] as Long
    }

    /**
     * Метод получения никнейма из объекта пользователя
     * @param userObject Объект пользователя
     * @return Возвращает никнейм пользователя, если он есть, иначе null
     */
    static String getNickNameId(Object userObject) {
        if (!userObject) return null
        return userObject[(NsmpConstants.USER_NICKNAME_CODE)]?.replaceAll('@', '')
    }

    /**
     * Метод получения объекта пользователя по никнейму в Telegram
     * @param nickname Никнейм пользователя в Telegram
     * @return Возвращает объект пользователя, если он есть, иначе null
     */
    Object getEmployeeByNickname(String nickname) {
        if (!nickname) return null
        nickname = nickname?.replaceAll('@', '')
        utils.get('employee', [(NsmpConstants.USER_NICKNAME_CODE): nickname]) ?: null
    }

    /**
     * Получает список всех известных пользователей
     * @return Возвращает список идентификаторов пользователей, которые имеют доступ к боту
     */
    List<Long> getAllAccessUser() {
        utils.find('employee', [(NsmpConstants.USER_ID_CODE): op.isNotNull()])
                .collect { it[(NsmpConstants.USER_ID_CODE)]?.toLong() ?: null }
                .findAll { it != null }
    }
}

/**
 * Знакомство пользователей
 * @param message Сообщение
 * @param telegram Объект Telegram для взаимодействия с API
 */
void pairUser(
        TelegramDto.Message message,
        TelegramConnector telegram = new TelegramConnector()
) {
    ObjectWrapper objectWrapper = new ObjectWrapper()
    // Проверка на наличие пользователя в базе
    Object employeeObject = objectWrapper.getEmployeeByNickname(message.from.username)

    if ( employeeObject ) {
        // Обновляем информацию о пользователе
        utils.editWithoutEventActions(employeeObject, [(NsmpConstants.USER_ID_CODE) : message.chat.id?.toString()])
        telegram.Message.reply(message, "Привет, ${employeeObject?.title}, рад тебя видеть, теперь будем знакомы")
    } else {
        telegram.Message.reply(message, """Привет, не видел тебя раньше, мне не разрешают знакомиться с незнакомыми людьми.
Если ты хочешь, чтобы я тебя запомнил, то напиши в личку администратору бота, чтобы он добавил тебя в список пользователей.""")
    }
}

/**
 * Получает запросы от Telegram
 * @param requestContent системная переменная запроса
 */
@SuppressWarnings('unused')
String getWebhook(Map requestContent) {
    TelegramConnector telegram = new TelegramConnector()
    TelegramDto.Webhook.Update update = telegram.objectMapper.readValue(
            telegram.objectMapper.writeValueAsString(requestContent),
            TelegramDto.Webhook.Update
    )

    // Обработка входящего сообщения
    if (update?.message) processMessage(telegram, update.message)

    return 'ok'
}

/**
 * Метод для обрабатки сообщений
 * @param messageData Данные о сообщении
 */
void processMessage(TelegramConnector telegram, TelegramDto.Message message) {

    /** Текущий идетификатор чата */
    Long chatId = message.chat.id
    /** Текст обрабатываемого сообщения (сразу в нижнем регистре) */
    String messageText = message.text?.toLowerCase() ?: null
    /** Информация о текущем боте */
    TelegramDto.Bot me = telegram.getMe()
    ObjectWrapper objectWrapper = new ObjectWrapper()

    // Бота позвали в новую беседу или создали чат с ботом
    if (message?.newChatMembers?.any {it?.username == me.username} || message.groupChatCreated){
        String responseText = """Оп-па, а вот и новый чатик.
Если вдруг кому-то нужно, вот текущий ChatID: <pre>${chatId as String}</pre>"""

        telegram.Message.send(chatId, responseText)
    }

    // Обработка опроса
    else if (message.poll) {
        TelegramDto.Poll poll = message.poll
        telegram.Message.send(chatId, "Новый опрос это здорово, давайте тыкайте реще! ${poll.question}")
        telegram.Message.send(chatId, "ХЗ кто-как, лично я - ${poll.options[0].text}!")
    }

    // Обработка текста сообщения
    else if (messageText) {

        // Обращение к боту
        if (messageText?.contains("@${me.username.toLowerCase()}")) {

            // Обработка обращения к боту с вопросом выбрать случайного пользователя
            if (messageText?.contains("кого следующим") || messageText?.contains("кто следующий")) {
                List<TelegramDto.User> allMembers = telegram.Chat.getMembers(chatId, objectWrapper.getAllAccessUser())
                List<String> usernames = allMembers.collect { '@' + it.username }
                usernames += 'хз'

                telegram.Message.reply(message, usernames[new Random().nextInt(usernames.size())])
            }

            // Не обработанное обращение
            else {
                telegram.Message.send(chatId, 'Что звал то? Ничего не могу понять')
            }
        }

        // Обработка тега #Кричу
        else if (messageText?.contains("#кричу")) telegram.Message.reply(message, 'Не кричи')

        // Обработка точного совпадения текста сообщения
        else if (messageText == 'привет') telegram.Message.send(chatId, 'Ооо, здарова, брат')

        // Обработка сообщения "Знакомство"
        else if (messageText == NsmpConstants.PAIR_MESSAGE.toLowerCase()) pairUser(message, telegram)
    }

    // Не обработанное сообщение
    else {
        logger.info(message?.dump()?.toString())
    }

}