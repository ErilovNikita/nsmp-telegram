---
layout: home

hero:
  name: NSMP Telegram
  text: Интеграция Telegram с Naumen Service Desk
  tagline: Пакет для отправки сообщений, обработки входящих событий и поддержки WebHook/Long Polling сценариев.
  image:
    src: /logo.png
    alt: NSMP Telegram
  actions:
    - theme: brand
      text: Начать настройку
      link: /guide/installation
    - theme: alt
      text: Посмотреть API
      link: /api/

features:
  - title: Два режима получения событий
    details: Используйте WebHook для входящих REST-запросов от Telegram или Long Polling для планировщика NSD.
  - title: Общий обработчик обновлений
    details: WebHook и Long Polling передают события в единый TelegramUpdateProcessor, поэтому бизнес-логика остается общей.
  - title: Готовые методы Telegram API
    details: Отправка, редактирование, пересылка сообщений, реакции, фото, локации и управление участниками чата.
---

## Для чего нужен пакет

`NSMP Telegram` добавляет в Naumen Service Desk готовый слой интеграции с Telegram. Пакет закрывает низкоуровневое общение с Telegram Bot API и дает сценарии для приема входящих событий пользователей.

## Быстрые ссылки

- [Возможности пакета](/guide/features)
- [Состав модулей](/guide/modules)
- [Установка и настройка](/guide/installation)
- [Режимы получения событий](/guide/update-modes)
- [Примеры API](/api/)
