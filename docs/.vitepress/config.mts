import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'NSMP Telegram',
  description: 'Интеграция Telegram с Naumen Service Desk',
  lang: 'ru-RU',
  base: process.env.BASE_PATH ?? '/nsmp-telegram/',
  cleanUrls: true,
  lastUpdated: true,
  themeConfig: {
    logo: '/logo.png',
    siteTitle: 'NSMP Telegram',
    nav: [
      { text: 'Обзор', link: '/' },
      { text: 'Установка', link: '/guide/installation' },
      { text: 'API', link: '/api/' },
      { text: 'GitHub', link: 'https://github.com/ErilovNikita/nsmp-telegram' }
    ],
    sidebar: [
      {
        text: 'Введение',
        items: [
          { text: 'Обзор', link: '/' },
          { text: 'Возможности', link: '/guide/features' },
          { text: 'Состав пакета', link: '/guide/modules' }
        ]
      },
      {
        text: 'Эксплуатация',
        items: [
          { text: 'Установка и настройка', link: '/guide/installation' },
          { text: 'Режимы получения событий', link: '/guide/update-modes' },
          { text: 'WebHook', link: '/guide/webhook' },
          { text: 'Long Polling', link: '/guide/long-polling' }
        ]
      },
      {
        text: 'API',
        items: [
          { text: 'Начало работы', link: '/api/' },
          { text: 'WebHook API', link: '/api/webhook' },
          { text: 'Chat API', link: '/api/chat' },
          { text: 'Message API', link: '/api/message' }
        ]
      },
      {
        text: 'Разработка',
        items: [
          { text: 'Документация', link: '/development/docs' }
        ]
      }
    ],
    socialLinks: [
      { icon: 'github', link: 'https://github.com/ErilovNikita/nsmp-telegram' }
    ],
    outline: {
      label: 'На странице'
    },
    docFooter: {
      prev: 'Предыдущая',
      next: 'Следующая'
    },
    lastUpdated: {
      text: 'Обновлено'
    },
    search: {
      provider: 'local'
    }
  },
  markdown: {
    theme: {
      light: 'github-light',
      dark: 'github-dark'
    }
  }
})
