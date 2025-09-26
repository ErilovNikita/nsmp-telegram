package ru.nerilov.telegram

/**
 * # telegramConnector - Naumen Service Desk Package
 * Пакет корректного общения с внешним сервисом Telegram. *
 * Содержит методы, которые формируют уникальные структурированные данные *
 * @author Erilov.NA*
 * @since 2025-07-03 *
 * @version 2.5.43 *
 */

/* Зависимости */
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

import ru.naumen.core.shared.dto.ISDtObject
import ru.naumen.core.server.script.api.injection.InjectApi

/**
 * Статические перемеменные
 */
class ApiConstants {
    /** Схема по умолчанию */
    protected static final String API_SCHEME = 'https'
    /** Хост сервиса */
    protected static final String API_HOST = "api.telegram.org"
    /** Базовый путь */
    protected static final String API_BASE_PATH = "bot"
    /** Ключ доступа */
    protected static final String ACCESS_KEY = "ACCESS_TOKEN"
}

/**
 * Десериализатор для преобразования Unix-времени в объект Date
 * Используется для преобразования поля date в сообщениях Telegram
 */
class UnixTimeToDateDeserializer extends JsonDeserializer<Date> {
    @Override
    Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        new Date(p.getLongValue() * 1000)
    }
}

/** Класс обработчика ошибок */
class TelegramException extends RuntimeException {
    @SuppressWarnings("unused")
    TelegramException(String message) { super(message) }

    @SuppressWarnings("unused")
    TelegramException(String message, Throwable cause) { super(message, cause) }
}

/**
 * Реализация Telegram API
 */
@InjectApi
class TelegramConnector {

    /** Клиент */
    static RestTemplate restClient

    /** Базовые конструкторы URI, при составления ссылки нужно использовать метод .cloneBuilder() на объекте */
    static UriComponentsBuilder basicUriBuilder

    /** Объект для сериализации и десериализации */
    ObjectMapper objectMapper = new ObjectMapper()

    /** Конструктор класса */
    TelegramConnector() {
        basicUriBuilder = UriComponentsBuilder.newInstance()
                .scheme(ApiConstants.API_SCHEME)
                .host(ApiConstants.API_HOST)
                .pathSegment(ApiConstants.API_BASE_PATH + ApiConstants.ACCESS_KEY)

        restClient = new RestTemplate()
    }

    /** Класс для работы с запросами */
    @SuppressWarnings("unused")
    public Request Request = new Request()

    /** Класс для работы с сообщениями */
    @SuppressWarnings("unused")
    public Message Message = new Message()

    /** Класс для интеграции с чатами */
    @SuppressWarnings("unused")
    public Chat Chat = new Chat()

    /** Класс для интеграции с веб-хуками */
    @SuppressWarnings("unused")
    public Webhook Webhook = new Webhook()

    /**
     * Ссылка на страничку документации API
     * @return Ссылка в строковом виде
     */
    @VisibilityOptions(Visibility.PUBLIC)
    @SuppressWarnings("unused")
    private static String docs() { return "https://core.telegram.org/bots/api#available-methods" }

    /** Класс взаимодействия c запросами */
    private class Request {

        /**
         * Выполнение POST-запроса к API Telegram
         * @param pathSegment Путь к методу API
         * @param body Тело запроса в виде Map
         * @return Ответ от API в виде объекта TelegramDto.BaseResponse
         */
        TelegramDto.BaseResponse post(
                String pathSegment,
                Map body = [:]
        ) {
            try {
                restClient.postForObject(
                        basicUriBuilder.cloneBuilder()
                                .pathSegment(pathSegment)
                                .toUriString(),
                        body,
                        TelegramDto.BaseResponse
                )
            } catch (RestClientException e) {
                throw new TelegramException(e.message)
            }
        }
    }

    /**
     * Получение параметров бота
     * @return Все параметры бота
     */
    @VisibilityOptions(Visibility.PUBLIC)
    @SuppressWarnings("unused")
    TelegramDto.Bot getMe() {
        TelegramDto.BaseResponse response = Request.post("getMe")
        objectMapper.readValue(
                objectMapper.writeValueAsString(response.result),
                TelegramDto.Bot
        )
    }

    /**
     * Ответ на запрос обратного вызова
     *
     * @return Все параметры бота
     */
    @VisibilityOptions(Visibility.PUBLIC)
    @SuppressWarnings("unused")
    void answerCallbackQuery(
            String callbackQueryId,
            String text = null,
            Boolean showAlert = false,
            String url = null,
            Integer cacheTime = 0
    ) {
        Request.post(
            "answerCallbackQuery",
            [
                    callback_query_id : callbackQueryId,
                    text : text,
                    show_alert : showAlert,
                    url : url,
                    cache_time : cacheTime
            ].findAll { it.value != null }
        )
    }

    /** Класс взаимодействия c веб-хуками */
    class Webhook {
        /**
         * Получение текущего статуса
         * @return Все параметры бота
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        TelegramDto.Webhook get() {
            TelegramDto.BaseResponse response = Request.post("getWebhookInfo")
            objectMapper.readValue(
                    objectMapper.writeValueAsString(response.result),
                    TelegramDto.Webhook
            )
        }

        /**
         * Устанавливает параметры веб хука
         * @param url URL для получения обновлений через веб хук.
         * @param allowedUpdates Список типов обновлений, которые будут приниматься (опционально).
         * @param certificate Файл сертификата для проверки подлинности (опционально).
         * @param maxConnections Максимальное количество одновременных HTTPS-соединений (опционально).
         * @return Информация о текущем веб хуке.
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        TelegramDto.Webhook set(
                String url,
                List<String> allowedUpdates = null,
                File certificate = null,
                Integer maxConnections = null
        ) {
            // Проверка enum'ов
            List<String> allowed = TelegramDto.Webhook.UpdateType.values()*.value
            allowedUpdates.each {String value
                if (value && !(value in allowed)) throw new TelegramException("Недопустимое значение allowed_updates: $value. Допустимые значения: ${allowed.join(', ')}")
            }

            Map requestBody = [
                url : url,
                allowed_updates : allowedUpdates,
                certificate : certificate,
                max_connections : maxConnections
            ].findAll { it.value != null }
            Request.post("setWebhook", requestBody)
            return get()
        }
    }

    /** Класс взаимодействия c сообщениями */
    class Message {

        /**
         * Отправка сообщения в чат
         * @param chatId Идентификатор чата
         * @param message Текст сообщения
         * @param replyMarkup Клавиатура для ответа
         * @param parseMode Режим форматирования текста
         * @return Отправленное сообщение
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        TelegramDto.Message send(
                Long chatId,
                String message,
                TelegramDto.Message.InlineKeyboard.Markup replyMarkup = null,
                String parseMode = 'HTML'
        ) {
            TelegramDto.BaseResponse response = Request.post("sendMessage", [
                    chat_id     : chatId,
                    text        : message,
                    parse_mode  : parseMode,
                    reply_markup: replyMarkup ?: new TelegramDto.Message.InlineKeyboard.Markup(inlineKeyboard: [])
            ])
            objectMapper.readValue(
                    objectMapper.writeValueAsString(response.result),
                    TelegramDto.Message
            )
        }

        /**
         * Отправка сообщения в чат
         * @param chat Объект класса TelegramDto.Chat
         * @param message Текст сообщения
         * @param replyMarkup Клавиатура для ответа
         * @param parseMode Режим форматирования текста
         * @return Отправленное сообщение
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        TelegramDto.Message send(
                TelegramDto.Chat chat,
                String message,
                TelegramDto.Message.InlineKeyboard.Markup replyMarkup = null,
                String parseMode = 'HTML'
        ) {
            send(chat.id, message, replyMarkup, parseMode)
        }

        /**
         * Удаление сообщения в чате
         * @param chatId Идентификатор чата
         * @param messageId Идентификатор сообщения
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        void delete(
                Long chatId,
                Long messageId
        ) {
            Request.post("deleteMessage", [ chat_id: chatId, message_id: messageId ])
        }

        /**
         * Удаление сообщения в чате
         * @param chat Объекта класса TelegramDto.Chat
         * @param message Объекта класса TelegramDto.Message
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        void delete(
                TelegramDto.Chat chat,
                TelegramDto.Message message
        ) {
            delete(chat.id, message.messageId)
        }

        /**
         * Редактирование сообщения в чате
         * @param chatId Идентификатор чата
         * @param messageId Идентификатор сообщения
         * @param message Новый текст сообщения
         * @param replyMarkup Новая клавиатура для ответа
         * @param parseMode Режим форматирования текста
         * @return Отредактированное сообщение
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        TelegramDto.Message edit(
                Long chatId,
                Long messageId,
                String message,
                TelegramDto.Message.InlineKeyboard.Markup replyMarkup = null,
                String parseMode = 'HTML'
        ) {
            TelegramDto.BaseResponse response = Request.post("editMessageText", [
                    chat_id     : chatId,
                    message_id  : messageId,
                    text        : message,
                    parse_mode  : parseMode,
                    reply_markup: replyMarkup ?: new TelegramDto.Message.InlineKeyboard.Markup(inlineKeyboard: [])
            ])
            objectMapper.readValue(
                    objectMapper.writeValueAsString(response.result),
                    TelegramDto.Message
            )
        }

        /**
         * Редактирование сообщения в чате
         * @param message Сообщение, которое нужно отредактировать
         * @return Отредактированное сообщение
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        TelegramDto.Message edit( TelegramDto.Message message ) {
            edit(message.chat.id, message.messageId, message.text, message.replyMarkup)
        }

        /**
         * Отправка ответа на сообщение в чате
         * @param chatId Идентификатор чата
         * @param message Текст сообщения
         * @param messageId Идентификатор сообщения, на которое нужно ответить
         * @param replyMarkup Клавиатура для ответа
         * @param parseMode Режим форматирования текста
         * @return Отправленное сообщение
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        TelegramDto.Message reply(
                Long chatId,
                Long messageId,
                String message,
                TelegramDto.Message.InlineKeyboard.Markup replyMarkup = null,
                String parseMode = 'HTML'
        ) {
            TelegramDto.BaseResponse response = Request.post("sendMessage", [
                    chat_id     : chatId,
                    text        : message,
                    parse_mode  : parseMode,
                    reply_to_message_id: messageId,
                    reply_markup: replyMarkup ?: new TelegramDto.Message.InlineKeyboard.Markup(inlineKeyboard: [])
            ])
            objectMapper.readValue(
                    objectMapper.writeValueAsString(response.result),
                    TelegramDto.Message
            )
        }

        /**
         * Отправка ответа на сообщение в чате
         * @param messageFrom Сообщение на которое нужно ответить
         * @param message Текст сообщения
         * @param replyMarkup Клавиатура для ответа
         * @param parseMode Режим форматирования текста
         * @return Отправленное сообщение
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        TelegramDto.Message reply(
                TelegramDto.Message messageFrom,
                String message,
                TelegramDto.Message.InlineKeyboard.Markup replyMarkup = null,
                String parseMode = 'HTML'
        ) {
            reply(messageFrom.chat.id, messageFrom.messageId, message, replyMarkup, parseMode)
        }

        /**
         * Переслать сообщение в другой чат
         * @param chatId Идентификатор чата куда переслать
         * @param messageId Идентификатор сообщения
         * @param fromChatId Идентификатор чата откуда переслать
         * @param protectContent Флаг включающий  запрет на пересылку и сохранения (по умолчанию false)
         * @param disableNotification Флаг отключения уведомления о закреплении (по умолчанию false)
         * @return Отправленное сообщение
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        TelegramDto.Message forward(
                Long chatId,
                Long messageId,
                Long fromChatId,
                Boolean protectContent = false,
                Boolean disableNotification = false
        ) {
            TelegramDto.BaseResponse response = Request.post("forwardMessage", [
                    chat_id         : chatId,
                    message_id      : messageId,
                    from_chat_id    : fromChatId,
                    protect_content  : protectContent,
                    disable_notification  : disableNotification
            ])
            objectMapper.readValue(
                    objectMapper.writeValueAsString(response.result),
                    TelegramDto.Message
            )
        }

        /**
         * Переслать сообщение в другой чат
         * @param chat Объект класса TelegramDto.Chat куда переслать
         * @param message Объект класса TelegramDto.Message
         * @param protectContent Флаг включающий  запрет на пересылку и сохранения (по умолчанию false)
         * @param disableNotification Флаг отключения уведомления о закреплении (по умолчанию false)
         * @return Отправленное сообщение
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        TelegramDto.Message forward(
                TelegramDto.Chat chat,
                TelegramDto.Message message,
                Boolean protectContent = false,
                Boolean disableNotification = false
        ) {
            forward(chat.id, message.messageId, message.chat.id, protectContent, disableNotification)
        }

        /**
         * Отправка локации в сообщении в чат
         * @param chatId Идентификатор чата
         * @param latitude Широта местоположения
         * @param longitude Долгота местоположения
         * @param horizontalAccuracy Радиус неопределенности для местоположения, измеренный в метрах; 0-1500
         * @param protectContent Флаг включающий  запрет на пересылку и сохранения (по умолчанию false)
         * @param disableNotification Флаг отключения уведомления о закреплении (по умолчанию false)
         * @param replyMarkup Клавиатура для ответа
         * @return Отправленное сообщение
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        TelegramDto.Message sendLocation(
                Long chatId,
                Float latitude,
                Float longitude,
                Float horizontalAccuracy = 100,
                Boolean protectContent = false,
                Boolean disableNotification = false,
                TelegramDto.Message.InlineKeyboard.Markup replyMarkup = null
        ) {
            TelegramDto.BaseResponse response = Request.post("sendLocation", [
                    chat_id     : chatId,
                    latitude    : latitude,
                    longitude   : longitude,
                    horizontal_accuracy      : horizontalAccuracy,
                    protect_content          : protectContent,
                    disable_notification     : disableNotification,
                    reply_markup: replyMarkup ?: new TelegramDto.Message.InlineKeyboard.Markup(inlineKeyboard: [])
            ])
            objectMapper.readValue(
                    objectMapper.writeValueAsString(response.result),
                    TelegramDto.Message
            )
        }

        /**
         * Отправка локации в сообщении в чат
         * @param chat Объекта класса TelegramDto.Chat
         * @param latitude Широта местоположения
         * @param longitude Долгота местоположения
         * @param horizontalAccuracy Радиус неопределенности для местоположения, измеренный в метрах; 0-1500
         * @param protectContent Флаг включающий  запрет на пересылку и сохранения (по умолчанию false)
         * @param disableNotification Флаг отключения уведомления о закреплении (по умолчанию false)
         * @param replyMarkup Клавиатура для ответа
         * @return Отправленное сообщение
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        TelegramDto.Message sendLocation(
                TelegramDto.Chat chat,
                Float latitude,
                Float longitude,
                Float horizontalAccuracy = 100,
                Boolean protectContent = false,
                Boolean disableNotification = false,
                TelegramDto.Message.InlineKeyboard.Markup replyMarkup = null
        ) {
            sendLocation(chat.id, latitude, longitude, horizontalAccuracy, protectContent, disableNotification, replyMarkup)
        }

        /**
         * Отправка фотографии в сообщении в чат
         * @param chatId Идентификатор чата
         * @param photo Файл фотографии
         * @param caption Подпись к фотографии
         * @param protectContent Флаг включающий  запрет на пересылку и сохранения (по умолчанию false)
         * @param disableNotification Флаг отключения уведомления о закреплении (по умолчанию false)
         * @param replyMarkup Клавиатура для ответа
         * @return Отправленное сообщение
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        TelegramDto.Message sendPhoto(
                Long chatId,
                File photo,
                String caption,
                Boolean protectContent = false,
                Boolean disableNotification = false,
                TelegramDto.Message.InlineKeyboard.Markup replyMarkup = null
        ) {
            def body = new LinkedMultiValueMap()
            body.add("chat_id", chatId)
            body.add("photo", new FileSystemResource(photo))
            if (caption) body.add("caption", caption)
            body.add("protect_content", protectContent)
            body.add("disable_notification", disableNotification)
            if (replyMarkup) body.add("reply_markup", objectMapper.writeValueAsString(replyMarkup))

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.MULTIPART_FORM_DATA)

            TelegramDto.BaseResponse response = restClient.postForObject(
                    basicUriBuilder.cloneBuilder().pathSegment("sendPhoto").toUriString(),
                    new HttpEntity<>(body, headers),
                    TelegramDto.BaseResponse
            )

            objectMapper.readValue(
                    objectMapper.writeValueAsString(response.result),
                    TelegramDto.Message
            )
        }

        /**
         * Отправка фотографии в сообщении в чат
         * @param chat Объекта класса TelegramDto.Chat
         * @param photo Файл фотографии
         * @param caption Подпись к фотографии
         * @param protectContent Флаг включающий  запрет на пересылку и сохранения (по умолчанию false)
         * @param disableNotification Флаг отключения уведомления о закреплении (по умолчанию false)
         * @param replyMarkup Клавиатура для ответа
         * @return Отправленное сообщение
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        TelegramDto.Message sendPhoto(
                TelegramDto.Chat chat,
                File photo,
                String caption,
                Boolean protectContent = false,
                Boolean disableNotification = false,
                TelegramDto.Message.InlineKeyboard.Markup replyMarkup = null
        ) {
            sendPhoto(chat.id, photo, caption, protectContent, disableNotification, replyMarkup)
        }

        /**
         * Отправка фотографии в сообщении в чат
         * @param chatId Идентификатор чата
         * @param photoObject Объект фотографии (ISDtObject)
         * @param caption Подпись к фотографии
         * @param protectContent Флаг включающий  запрет на пересылку и сохранения (по умолчанию false)
         * @param disableNotification Флаг отключения уведомления о закреплении (по умолчанию false)
         * @param replyMarkup Клавиатура для ответа
         * @return Отправленное сообщение
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        TelegramDto.Message sendPhoto(
                Long chatId,
                ISDtObject photoObject,
                String caption,
                Boolean protectContent = false,
                Boolean disableNotification = false,
                TelegramDto.Message.InlineKeyboard.Markup replyMarkup = null
        ) {
            File file = new File("/tmp/${photoObject.title}")
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(utils.readFileContent(photoObject) as byte[])
            }
            sendPhoto(chatId, file, caption, protectContent, disableNotification, replyMarkup)
        }

        /**
         * Отправка фотографии в сообщении в чат
         * @param chat Объект класса TelegramDto.Chat
         * @param photoObject Объект фотографии (ISDtObject)
         * @param caption Подпись к фотографии
         * @param protectContent Флаг включающий  запрет на пересылку и сохранения (по умолчанию false)
         * @param disableNotification Флаг отключения уведомления о закреплении (по умолчанию false)
         * @param replyMarkup Клавиатура для ответа
         * @return Отправленное сообщение
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        TelegramDto.Message sendPhoto(
                TelegramDto.Chat chat,
                ISDtObject photoObject,
                String caption,
                Boolean protectContent = false,
                Boolean disableNotification = false,
                TelegramDto.Message.InlineKeyboard.Markup replyMarkup = null
        ) {
            sendPhoto(chat.id, photoObject, caption, protectContent, disableNotification, replyMarkup)
        }

        /**
         * Закрепить сообщение в чате
         * @param chatId Идентификатор чата
         * @param messageId Идентификатор сообщения
         * @param disableNotification Флаг отключения уведомления о закреплении (по умолчанию false)
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        void pin(Long chatId, Long messageId, Boolean disableNotification = false) {
            Request.post("pinChatMessage", [
                    chat_id     : chatId,
                    message_id  : messageId,
                    disable_notification  : disableNotification
            ])
        }

        /**
         * Закрепить сообщение в чате
         * @param message Сообщение, которое нужно закрепить
         * @param disableNotification Флаг отключения уведомления о закреплении (по умолчанию false)
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        void pin(TelegramDto.Message message, Boolean disableNotification = false) {
            pin(message.chat.id, message.messageId, disableNotification)
        }

        /**
         * Открепить сообщение в чате
         * @param chatId Идентификатор чата
         * @param messageId Идентификатор сообщения
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        void unpin(Long chatId, Long messageId) {
            Request.post("unpinChatMessage", [
                    chat_id     : chatId,
                    message_id  : messageId
            ])
        }

        /**
         * Открепить сообщение в чате
         * @param message Сообщение, которое нужно закрепить
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        void unpin(TelegramDto.Message message) {
            unpin(message.chat.id, message.messageId )
        }

        /**
         * Поставить реакцию на сообщение в чате
         * @param chatId Идентификатор чата
         * @param messageId Идентификатор сообщения
         * @param reactionCode Код реакции
         * @param isBig Флаг включения реакции с большой анимацией (по умолчанию false)
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        void setReaction(Long chatId, Long messageId, String reactionCode, isBig = false) {
            List<String> allowed = TelegramDto.Message.ReactionType.values().collect{ it?.toString() }
            if (reactionCode && reactionCode.toUpperCase() !in allowed) throw new TelegramException("Недопустимое значение \"reaction\": $reactionCode. Допустимые значения: ${allowed.join(', ')}")

            Request.post("setMessageReaction", [
                    chat_id     : chatId,
                    message_id  : messageId,
                    reaction    : [[
                                           type : "emoji",
                                           "emoji" : TelegramDto.Message.ReactionType."${reactionCode.toUpperCase()}".value
                                   ]],
                    is_big: isBig
            ])
        }

        /**
         * Поставить реакцию на сообщение в чате
         * @param message Объект класса TelegramDto.Message
         * @param reactionCode Код реакции
         * @param isBig Флаг включения реакции с большой анимацией (по умолчанию false)
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        void setReaction(TelegramDto.Message message, String reactionCode, isBig = false) {
            setReaction(message.chat.id, message.messageId, reactionCode, isBig)
        }
    }

    /** Класс взаимодействия c чатами */
    class Chat {
        /**
         * Выйти из чата
         * @param chatId Идентификатор чата
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        void leave(Long chatId) {
            Request.post("leaveChat", [chat_id: chatId])
        }

        /**
         * Выйти из чата
         * @param chat Объекта класса TelegramDto.Chat
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        void leave(TelegramDto.Chat chat) {
            leave(chat.id)
        }

        /**
         * Получить всех администраторов
         * @param chatId Идентификатор чата
         * @return Список пользователей
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        List<TelegramDto.Chat.ChatMember> getAdministrators(Long chatId) {
            TelegramDto.BaseResponse response = Request.post("getChatAdministrators", [chat_id: chatId])
            objectMapper.readValue(
                    objectMapper.writeValueAsString(response.result),
                    TelegramDto.Chat.ChatMember[]
            )
        }

        /**
         * Получить всех администраторов
         * @param chat Объекта класса TelegramDto.Chat
         * @return Список пользователей
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        List<TelegramDto.Chat.ChatMember> getAdministrators(TelegramDto.Chat chat) {
            getAdministrators(chat.id)
        }

        /**
         * Получить участника чата
         * @param chatId Идентификатор чата
         * @param userId Идентификатор участника
         * @return Пользователь или null - Если такого не существует
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        TelegramDto.User getMember(Long chatId, Long userId) {
            try {
                TelegramDto.BaseResponse response = Request.post("getChatMember", [chat_id: chatId, user_id: userId])
                objectMapper.readValue(
                        objectMapper.writeValueAsString(response.result?.user),
                        TelegramDto.User
                )
            } catch (TelegramException ignored) {
                return null
            }
        }

        /**
         * Получить участника чата
         * @param chat Объекта класса TelegramDto.Chat
         * @param userId Идентификатор участника
         * @return Пользователь или null - Если такого не существует
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        TelegramDto.User getMember(TelegramDto.Chat chat, Long userId) {
            getMember(chat.id, userId)
        }

        /**
         * Получить всех участников
         * @param chatId Идентификатор текущего чата
         * @param allAccessUserIds Список идентификаторов всех известных пользователей
         * @return Список пользователей
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        List<TelegramDto.User> getMembers(Long chatId, List<Long> allAccessUserIds = []) {
            allAccessUserIds.collect { getMember(chatId, it) }.findAll()
        }

        /**
         * Получить всех участников
         * @param chat Объекта класса TelegramDto.Chat
         * @param allAccessUserIds Список идентификаторов всех известных пользователей
         * @return Список пользователей
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        List<TelegramDto.User> getMembers(TelegramDto.Chat chat, List<Long> allAccessUserIds = []) {
            getMembers(chat.id, allAccessUserIds)
        }

        /**
         * Установить новое название чата
         * @param chatId Идентификатор чата
         * @param title Название чата
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        void setTitle(Long chatId, String title) {
            Request.post("setChatTitle", [chat_id: chatId, title: title])
        }

        /**
         * Установить новое название чата
         * @param chat Объекта класса TelegramDto.Chat
         * @param title Название чата
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        void setTitle(TelegramDto.Chat chat, String title) {
            setTitle(chat.id, title)
        }

        /**
         * Установить аватарку чата
         * @param chatId Идентификатор чата
         * @param photo Картинка
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        void setPhoto(Long chatId, File photo) {
            def body = new LinkedMultiValueMap()
            body.add("chat_id", chatId)
            body.add("photo", new FileSystemResource(photo))

            HttpHeaders headers = new HttpHeaders()
            headers.setContentType(MediaType.MULTIPART_FORM_DATA)

            restClient.postForObject(
                    basicUriBuilder.cloneBuilder().pathSegment("setChatPhoto").toUriString(),
                    new HttpEntity<>(body, headers),
                    TelegramDto.BaseResponse
            )
        }

        /**
         * Установить аватарку чата
         * @param chat Объекта класса TelegramDto.Chat
         * @param photo Картинка
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        void setPhoto(TelegramDto.Chat chat, File photo) {
            setPhoto(chat.id, photo)
        }

        /**
         * Установить аватарку чата
         * @param chatId Идентификатор чата
         * @param photoObject Объект картинки
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        void setPhoto(Long chatId, ISDtObject photoObject) {
            File photo = new File("/tmp/${photoObject.title}")
            try (FileOutputStream fos = new FileOutputStream(photo)) {
                fos.write(utils.readFileContent(photoObject) as byte[])
            }
            setPhoto(chatId, photo)
        }

        /**
         * Установить аватарку чата
         * @param chat Объекта класса TelegramDto.Chat
         * @param photoObject Объект картинки
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        void setPhoto(TelegramDto.Chat chat, ISDtObject photoObject) {
            setPhoto(chat.id, photoObject)
        }

        /**
         * Забанить участника в чате
         * @param chatId Идентификатор чата
         * @param userId Идентификатор пользователя
         * @param untilDate Дата до которой действует бан (null - навсегда)
         * @param revokeMessages Флаг удаления всех сообщений от пользователя (по умолчанию false)
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        void banChatMember(Long chatId, Long userId, Date untilDate = null, Boolean revokeMessages = false) {
            TelegramDto.BaseResponse response = Request.post("banChatMember", [
                    chat_id        : chatId,
                    user_id        : userId,
                    until_date     : untilDate ? (untilDate.time / 1000).toInteger() : null,
                    revoke_messages: revokeMessages
            ].findAll { it.value != null })
            if (!response?.ok) throw new TelegramException("Не удалось забанить пользователя $userId в чате $chatId")
        }

        /**
         * Забанить участника в чате
         * @param chat Объект класса TelegramDto.Chat
         * @param user Объект класса TelegramDto.User
         * @param untilDate Дата до которой действует бан (null - навсегда)
         * @param revokeMessages Флаг удаления всех сообщений от пользователя (по умолчанию false)
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        void banChatMember(TelegramDto.Chat chat, TelegramDto.User user, Date untilDate = null, Boolean revokeMessages = false) {
            banChatMember(chat.id, user.id, untilDate, revokeMessages)
        }

        /**
         * Разбанить участника в чате
         * @param chatId Идентификатор чата
         * @param userId Идентификатор пользователя
         * @param onlyIfBanned Флаг разбанить только если пользователь в бане (по умолчанию false)
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        void unbanChatMember(Long chatId, Long userId, Boolean onlyIfBanned = false) {
            TelegramDto.BaseResponse response = Request.post("unbanChatMember", [
                    chat_id       : chatId,
                    user_id       : userId,
                    only_if_banned: onlyIfBanned
            ].findAll { it.value != null })
            if (!response?.ok) throw new TelegramException("Не удалось разбанить пользователя $userId в чате $chatId")
        }

        /**
         * Разбанить участника в чате
         * @param chat Объект класса TelegramDto.Chat
         * @param user Объект класса TelegramDto.User
         * @param onlyIfBanned Флаг разбанить только если пользователь в бане (по умолчанию false)
         */
        @VisibilityOptions(Visibility.PUBLIC)
        @SuppressWarnings("unused")
        void unbanChatMember(TelegramDto.Chat chat, TelegramDto.User user, Boolean onlyIfBanned = false) {
            unbanChatMember(chat.id, user.id, onlyIfBanned)
        }
    }
}


/**
 * Перечень DTO, принимаемых или отправляемых
 */
class TelegramDto {

    /** Базовый ответ от API Telegram */
    static class BaseResponse {
        @SuppressWarnings("unused")
        Boolean ok
        Object result
    }

    /** DTO для бота */
    static class Bot {
        @JsonProperty("id")
        Long id
        @JsonProperty("is_bot")
        Boolean isBot
        @JsonProperty("first_name")
        String firstName
        @JsonProperty("username")
        String username
        @JsonProperty("can_join_groups")
        Boolean canJoinGroups
        @JsonProperty("can_read_all_group_messages")
        Boolean canReadAllGroupMessages
        @JsonProperty("supports_inline_queries")
        Boolean supportsInlineQueries
        @JsonProperty("can_connect_to_business")
        Boolean canConnectToBusiness
        @JsonProperty("has_main_web_app")
        Boolean hasMainWebApp
    }

    /** DTO для Webhook */
    static class Webhook {
        @SuppressWarnings("unused")
        String url
        @JsonProperty("has_custom_certificate")
        Boolean hasCustomCertificate
        @JsonProperty("pending_update_count")
        Integer pendingUpdateCount
        @JsonProperty("ip_address")
        String ipAddress
        @JsonProperty("last_error_date")
        Integer lastErrorDate
        @JsonProperty("last_error_message")
        String lastErrorMessage
        @JsonProperty("last_synchronization_error_date")
        Integer lastSynchronizationErrorDate
        @JsonProperty("max_connections")
        Integer maxConnections
        @JsonProperty("allowed_updates")
        List<String> allowedUpdates

        /** DTO для типов обновлений */
        @SuppressWarnings("unused")
        static enum UpdateType {
            /** Новое входящие сообщения любого рода - текст, фотография, наклейка и т. д. */
            MESSAGE("message"),

            /** Новая версия сообщения, которое известно боту и было отредактировано.
             * Иногда это обновление может быть вызвано изменениями в полях сообщений,
             * которые либо недоступны, либо активно не используются вашим ботом. */
            EDITED_MESSAGE("edited_message"),

            /** Реакция на сообщение была изменена пользователем.
             * Бот должен быть администратором в чате и должен явно указать "message_reaction" в списке allowed_updates,
             * чтобы получать эти обновления. Обновление не получено для реакций, установленных ботами. */
            MESSAGE_REACTION("message_reaction"),

            /** Новый запрос на обратный вызов */
            CALLBACK_QUERY("callback_query"),

            /** Новое состояние опроса.
             * Боты получают только обновления о остановленных вручную опросах и опросах, которые отправляются ботом */
            POLL("poll"),

            /** Пользователь изменил свой ответ в не анонимном опросе.
             * Боты получают новые голоса только в опросах, которые были отправлены самим ботом.*/
            POLL_ANSWER("poll_answer"),

            /** Статус участника чата бота был обновлен в чате.
             * Для частных чатов это обновление получается только тогда,
             * когда бот заблокирован или разблокирован пользователем. */
            MY_CHAT_MEMBER("my_chat_member")

            public final String value
            UpdateType(String value) { this.value = value }
        }

        /** DTO для обновления */
        static class Update {
            @JsonProperty("update_id")
            Integer updateId
            Message message
            @JsonProperty("edited_message")
            Message editedMessage
            @JsonProperty("channel_post")
            Message channelPost
            @JsonProperty("edited_channel_post")
            Message editedChannelPost
            @JsonProperty("my_chat_member")
            ChatMemberUpdated myChatMember
            @JsonProperty("chat_member")
            ChatMemberUpdated chatMember
            @JsonProperty("poll_answer")
            Poll.Answer pollAnswer
            @JsonProperty("callback_query")
            CallbackQuery callbackQuery

            static class CallbackQuery {
                @SuppressWarnings("unused")
                String id
                @SuppressWarnings("unused")
                User from
                @SuppressWarnings("unused")
                Message message
                @JsonProperty("inline_message_id")
                @SuppressWarnings("unused")
                String inlineMessageId
                @JsonProperty("chat_instance")
                @SuppressWarnings("unused")
                String chatInstance
                @SuppressWarnings("unused")
                String data
                @JsonProperty("game_short_name")
                @SuppressWarnings("unused")
                String gameShortName
            }

            static class ChatMember {
                @SuppressWarnings("unused")
                String status
                @SuppressWarnings("unused")
                User user
                @JsonProperty("is_anonymous")
                Boolean isAnonymous
                @JsonProperty("custom_title")
                String customTitle
            }

            static class ChatMemberUpdated {
                @SuppressWarnings("unused")
                Chat chat
                @SuppressWarnings("unused")
                User from
                @JsonDeserialize(using = UnixTimeToDateDeserializer)
                Date date
                @JsonProperty("via_join_request")
                Boolean viaJoinRequest
                @JsonProperty("via_chat_folder_invite_link")
                Boolean viaChatFolderInviteLink
                @JsonProperty("old_chat_member")
                ChatMember oldChatMember
                @JsonProperty("new_chat_member")
                ChatMember newChatMember
            }
        }
    }

    /** DTO для пользователя */
    static class User {
        @SuppressWarnings("unused")
        Long id
        @JsonProperty("is_bot")
        Boolean isBot
        @JsonProperty("language_code")
        String languageCode
        @JsonProperty("is_premium")
        Boolean isPremium
        @JsonProperty("first_name")
        String firstName
        @JsonProperty("last_name")
        String lastName
        String username
        @SuppressWarnings("unused")
        String type
    }

    /** DTO для чата */
    static class Chat {
        Long id
        @SuppressWarnings("unused")
        String title
        @JsonProperty("first_name")
        String firstName
        @JsonProperty("last_name")
        String lastName
        @SuppressWarnings("unused")
        String username
        @SuppressWarnings("unused")
        String type
        @JsonProperty("is_forum")
        Boolean isForum
        @JsonProperty("all_members_are_administrators")
        Boolean allMembersAreAdministrators
        @JsonProperty("accepted_gift_types")
        AcceptedGiftTypes acceptedGiftTypes

        /** DTO для типов подарков */
        static class AcceptedGiftTypes{
            @JsonProperty("unlimited_gifts")
            Boolean unlimitedGifts
            @JsonProperty("limited_gifts")
            Boolean limitedGifts
            @JsonProperty("unique_gifts")
            Boolean uniqueGifts
            @JsonProperty("premium_subscription")
            Boolean premiumSubscription
        }

        /** DTO для типов участника чата */
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonTypeInfo(
                use = JsonTypeInfo.Id.NAME,
                include = JsonTypeInfo.As.EXISTING_PROPERTY,
                property = "status",
                visible = true
        )
        @JsonSubTypes([
                @JsonSubTypes.Type(value = ChatMemberLeft.class, name = "left"),
                @JsonSubTypes.Type(value = ChatMemberOwner.class, name = "creator"),
                @JsonSubTypes.Type(value = ChatMemberAdministrator.class, name = "administrator"),
                @JsonSubTypes.Type(value = ChatMemberMember.class, name = "member"),
                @JsonSubTypes.Type(value = ChatMemberBanned.class, name = "kicked"),
                @JsonSubTypes.Type(value = ChatMemberRestricted.class, name = "restricted")
        ])
        static abstract class ChatMember {
            @SuppressWarnings('unused')
            String status
            @SuppressWarnings('unused')
            User user

            /** DTO для нового участника чата */
            static class ChatMemberLeft extends ChatMember{}

            /** DTO для владельца чата */
            static class ChatMemberOwner extends ChatMember {
                @JsonProperty("is_anonymous")
                Boolean isAnonymous
                @JsonProperty("custom_title")
                String customTitle
            }

            /** DTO для участника чата */
            static class ChatMemberMember extends ChatMember {
                @JsonProperty("until_date")
                Integer untilDate
            }

            /** DTO для забаненного участника чата */
            static class ChatMemberBanned extends ChatMemberMember {}

            /** DTO для участника который находится под определенными ограничениями чата (Только для супер чатов) */
            static class ChatMemberRestricted extends ChatMemberMember {
                @JsonProperty("is_member")
                Boolean isMember
                @JsonProperty("can_send_messages")
                Boolean canSendMessages
                @JsonProperty("can_send_audios")
                Boolean canSendAudios
                @JsonProperty("can_send_documents")
                Boolean canSendDocuments
                @JsonProperty("can_send_photos")
                Boolean canSendPhotos
                @JsonProperty("can_send_videos")
                Boolean canSendVideos
                @JsonProperty("can_send_video_notes")
                Boolean canSendVideoNotes
                @JsonProperty("can_send_voice_notes")
                Boolean canSendVoiceNotes
                @JsonProperty("can_send_polls")
                Boolean canSendPolls
                @JsonProperty("can_send_other_messages")
                Boolean canSendOtherMessages
                @JsonProperty("can_add_web_page_previews")
                Boolean canAddWebPagePreviews
                @JsonProperty("can_change_info")
                Boolean canChangeInfo
                @JsonProperty("can_invite_users")
                Boolean canInviteUsers
                @JsonProperty("can_pin_messages")
                Boolean canPinMessages
                @JsonProperty("can_manage_topics")
                Boolean canManageTopics
            }

            /** DTO для администратора чата */
            static class ChatMemberAdministrator extends ChatMemberOwner {
                @JsonProperty("can_be_edited")
                Boolean canBeEdited
                @JsonProperty("can_manage_chat")
                Boolean canManageChat
                @JsonProperty("can_delete_messages")
                Boolean canDeleteMessages
                @JsonProperty("can_manage_video_chats")
                Boolean canManageVideoChats
                @JsonProperty("can_restrict_members")
                Boolean canRestrictMembers
                @JsonProperty("can_promote_members")
                Boolean canPromoteMembers
                @JsonProperty("can_change_info")
                Boolean canChangeInfo
                @JsonProperty("can_invite_users")
                Boolean canInviteUsers
                @JsonProperty("can_post_stories")
                Boolean canPostStories
                @JsonProperty("can_edit_stories")
                Boolean canEditStories
                @JsonProperty("can_delete_stories")
                Boolean canDeleteStories
                @JsonProperty("can_post_messages")
                Boolean canPostMessages
                @JsonProperty("can_edit_messages")
                Boolean canEditMessages
                @JsonProperty("can_pin_messages")
                Boolean canPinMessages
                @JsonProperty("can_manage_topics")
                Boolean canManageTopics
                @JsonProperty("can_manage_direct_messages")
                Boolean canManageDirectMessages
            }
        }
    }

    /** DTO для сообщения */
    static class Message {
        @JsonProperty("message_id")
        Long messageId
        @JsonProperty("reply_to_message")
        Message replyToMessage
        @SuppressWarnings("unused")
        User from
        Chat chat
        @JsonProperty("new_chat_participant")
        User newChatParticipant
        @JsonProperty("new_chat_members")
        List<User> newChatMembers
        @JsonProperty("new_chat_title")
        String newChatTitle
        @JsonProperty("new_chat_member")
        User newChatMember
        @JsonProperty("new_chat_photo")
        newChatPhoto
        @JsonDeserialize(using = UnixTimeToDateDeserializer)
        Date date
        @JsonProperty("edit_date")
        @JsonDeserialize(using = UnixTimeToDateDeserializer)
        Date editDate
        @JsonProperty("forward_date")
        @JsonDeserialize(using = UnixTimeToDateDeserializer)
        Date forwardDate
        String text
        @SuppressWarnings("unused")
        String type
        Poll poll
        @SuppressWarnings("unused")
        List<Photo> photo
        @SuppressWarnings("unused")
        String caption
        @SuppressWarnings("unused")
        List<MessageEntity> entities
        @JsonProperty("forward_origin")
        Message forwardOrigin
        @JsonProperty("forward_from")
        User forwardFrom
        @JsonProperty("sender_user")
        User senderUser
        @JsonProperty("left_chat_participant")
        User leftChatParticipant
        @JsonProperty("left_chat_member")
        User leftChatMember
        @JsonProperty("group_chat_created")
        Boolean groupChatCreated
        @SuppressWarnings("unused")
        Location location
        @JsonProperty("reply_markup")
        InlineKeyboard.Markup replyMarkup

        /** DTO для месторасположения */
        static class Location {
            @SuppressWarnings("unused")
            Float latitude
            @SuppressWarnings("unused")
            Float longitude
            @JsonProperty("horizontal_accuracy")
            Float horizontalAccuracy
        }

        /** DTO для фотографии */
        static class Photo {
            @JsonProperty("file_id")
            String fileId
            @JsonProperty("file_unique_id")
            String fileUniqueId
            @JsonProperty("file_size")
            Integer fileSize
            @SuppressWarnings("unused")
            Integer width
            @SuppressWarnings("unused")
            Integer height
        }

        /** DTO для сущности сообщения */
        static class MessageEntity {
            @SuppressWarnings("unused")
            String type
            @SuppressWarnings("unused")
            Integer offset
            @SuppressWarnings("unused")
            Integer length
            @SuppressWarnings("unused")
            String url
            @SuppressWarnings("unused")
            User user
            @SuppressWarnings("unused")
            String language
            @JsonProperty("custom_emoji_id")
            String customEmojiId
        }

        /** Enum для реакций */
        static enum ReactionType {
            HEART("❤"),
            THUMBS_UP("👍"),
            THUMBS_DOWN("👎"),
            FIRE("🔥"),
            IN_LOVE("🥰"),
            CLAP("👏"),
            GRINNING("😁"),
            THINKING("🤔"),
            EXPLODING_HEAD("🤯"),
            SCREAM("😱"),
            SWEARING("🤬"),
            CRYING("😢"),
            PARTY("🎉"),
            STAR_STRUCK("🤩"),
            VOMIT("🤮"),
            POOP("💩"),
            PRAY("🙏"),
            OK("👌"),
            DOVE("🕊"),
            CLOWN("🤡"),
            YAWN("🥱"),
            WOOZY("🥴"),
            HEART_EYES("😍"),
            WHALE("🐳"),
            HEART_ON_FIRE("❤‍🔥"),
            MOON("🌚"),
            HOT_DOG("🌭"),
            HUNDRED("💯"),
            LAUGH("🤣"),
            ZAP("⚡"),
            BANANA("🍌"),
            TROPHY("🏆"),
            BROKEN_HEART("💔"),
            RAISED_EYEBROW("🤨"),
            NEUTRAL("😐"),
            STRAWBERRY("🍓"),
            CHAMPAGNE("🍾"),
            KISS("💋"),
            MIDDLE_FINGER("🖕"),
            SMILING_DEVIL("😈"),
            SLEEPING("😴"),
            SOBBING("😭"),
            NERD("🤓"),
            GHOST("👻"),
            MALE_TECH("👨‍💻"),
            EYES("👀"),
            PUMPKIN("🎃"),
            SEE_NO_EVIL("🙈"),
            ANGEL("😇"),
            FEAR("😨"),
            HANDSHAKE("🤝"),
            WRITING("✍"),
            HUG("🤗"),
            SALUTE("🫡"),
            SANTA("🎅"),
            CHRISTMAS_TREE("🎄"),
            SNOWMAN("☃"),
            NAIL_POLISH("💅"),
            ZANY("🤪"),
            MOAI("🗿"),
            COOL("🆒"),
            HEART_ARROW("💘"),
            HEAR_NO_EVIL("🙉"),
            UNICORN("🦄"),
            KISSING("😘"),
            PILL("💊"),
            SPEAK_NO_EVIL("🙊"),
            SUNGLASSES("😎"),
            ALIEN("👾"),
            SHRUG_MAN("🤷‍♂"),
            SHRUG("🤷"),
            SHRUG_WOMAN("🤷‍♀"),
            ANGRY("😡")

            public final String value
            ReactionType(String value) { this.value = value }
        }

        /** DTO для Inline-клавиатуры */
        static class InlineKeyboard {

            /** Кнопка в Inline-клавиатуре */
            static class Button {
                @SuppressWarnings("unused")
                String text
                @SuppressWarnings("unused")
                String url
                @SuppressWarnings("unused")
                @JsonProperty("callback_data")
                String callbackData
            }

            /** Разметка Inline-клавиатуры */
            static class Markup {
                @JsonProperty("inline_keyboard")
                List<List<Button>> inlineKeyboard
            }
        }
    }

    /** DTO для опроса */
    static class Poll {
        @SuppressWarnings("unused")
        Long id
        String question
        @JsonProperty("question_entities")
        List<Message.MessageEntity> questionEntities
        List<Option> options
        @JsonProperty("total_voter_count")
        Integer totalVoterCount
        @JsonProperty("is_closed")
        Boolean isClosed
        @JsonProperty("is_anonymous")
        Boolean isAnonymous
        @SuppressWarnings("unused")
        String type
        @JsonProperty("allows_multiple_answers")
        Boolean allowsMultipleAnswers
        @JsonProperty("correct_option_id")
        Integer correctOptionId
        @SuppressWarnings("unused")
        String explanation
        @JsonProperty("explanation_entities")
        List<Message.MessageEntity> explanationEntities
        @JsonProperty("open_period")
        Integer openPeriod
        @JsonDeserialize(using = UnixTimeToDateDeserializer)
        @JsonProperty("close_date")
        Integer closeDate

        /** DTO для ответа на опрос */
        static class Answer {
            @JsonProperty("poll_id")
            String pollId
            @JsonProperty("voter_chat")
            Chat voterChat
            @SuppressWarnings("unused")
            User user
            @JsonProperty("option_ids")
            List<Integer> optionIds
        }

        /** DTO для опции в опросе */
        static class Option {
            String text
            @JsonProperty("text_entities")
            List<Message.MessageEntity> textEntities
            @JsonProperty("voter_count")
            Integer voterCount
        }
    }

    /** DTO для логирования сообщения */
    @SuppressWarnings("unused")
    static class MessageLog {
        @JsonProperty("message_id")
        @SuppressWarnings("unused")
        Long messageId
        @JsonProperty("chat_id")
        @SuppressWarnings("unused")
        Long chatId
        @SuppressWarnings("unused")
        Date date
    }
}