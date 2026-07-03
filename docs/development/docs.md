# Документация

Документация собирается через VitePress.

## Локальный запуск

Установите зависимости:

```bash
npm install
```

Запустите dev-сервер:

```bash
npm run docs:dev
```

## Сборка

```bash
npm run docs:build
```

Результат сборки будет создан в `docs/.vitepress/dist`.

## Предпросмотр production-сборки

```bash
npm run docs:preview
```

## Публикация

Публикация выполняется GitHub Actions workflow `.github/workflows/docs.yml`.

Workflow запускается при push в ветку `main` и публикует собранный сайт в GitHub Pages.
