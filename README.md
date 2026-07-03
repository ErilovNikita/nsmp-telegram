# NSMP Telegram

> Пакет для полноценной интеграции Telegram в Naumen Service Desk.

[![License](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
![nsmp support](https://img.shields.io/badge/NSMP-%3E%3D4.17.5-blue)

<p align="center">
  <img src="docs/logo.png" alt="Logo" width="256" height="256">
</p>

## Что внутри

`NSMP Telegram` добавляет в Naumen Service Desk интеграцию с Telegram Bot API:

- отправка, редактирование, удаление и пересылка сообщений;
- отправка фото, локаций и реакций;
- управление чатами и участниками;
- прием входящих событий через WebHook;
- прием входящих событий через Long Polling;
- общий обработчик update для обоих режимов.

## Документация

Документация вынесена в VitePress и разложена по разделам в [`docs`](docs/index.md):

- [Состав пакета](docs/guide/modules.md)
- [Установка и настройка](docs/guide/installation.md)
- [Режимы получения событий](docs/guide/update-modes.md)
- [Примеры API](docs/api/index.md)

## Локальный запуск документации

```bash
npm install
npm run docs:dev
```

## Сборка документации

```bash
npm run docs:build
```

## Публикация

Автоматическая публикация настроена через GitHub Actions workflow:

```text
.github/workflows/docs.yml
```

Workflow запускается при push в `main` и публикует сайт в GitHub Pages.
