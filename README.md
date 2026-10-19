# GNIX IA

Assistente de IA para **Wear OS** que roda um modelo **Gemma** direto no relógio, sem internet. Você pergunta por voz ou teclado, recebe a resposta em português do Brasil, e as conversas ficam salvas no histórico.

## Funcionalidades

- **Chat com IA no relógio**: o modelo Gemma roda localmente com [LiteRT-LM](https://ai.google.dev/edge/litert); a resposta aparece enquanto é gerada.
- **Funciona offline**: nenhuma pergunta sai do relógio.
- **Respostas em português do Brasil**, curtas, pensadas para tela pequena.
- **Entrada por voz, teclado ou emoji** pelo teclado do sistema do Wear OS.
- **Histórico** de perguntas e respostas salvo localmente (Room), com tela de detalhe e opção de apagar tudo.
- **Home** com atalhos para Apresentação, Histórico e Configurações, e botão de pesquisa que leva a pergunta direto ao chat.
- **Splash screen** e tema próprio (azul-marinho e amarelo).

## Requisitos

| Item | Versão |
|---|---|
| Android Studio | com suporte a AGP 9.5 (alpha) |
| Wear OS | API 33+ (`minSdk 33`) |
| Arquitetura do relógio | **64 bits** (`arm64-v8a`) ou emulador `x86_64` |

> **Compatibilidade:** a LiteRT-LM só tem bibliotecas nativas para 64 bits. Relógios com Android 32 bits (`armeabi-v7a`), como o **Galaxy Watch 7**, abrem o app normalmente, mas o chat mostra que o aparelho não é compatível com o modelo. Para testar o chat, use um relógio 64 bits ou o emulador Wear OS `x86_64`.

## Como rodar

1. Coloque o modelo em `app/src/main/assets/model.litertlm` (formato `.litertlm` da LiteRT-LM).
2. Abra o projeto no Android Studio.
3. Rode no relógio ou no emulador Wear OS:

   ```bash
   ./gradlew installDebug
   ```

Na primeira abertura, o app copia o modelo dos assets para o armazenamento interno e o carrega. Isso pode levar alguns segundos, e o chat mostra o progresso.

> O modelo tem centenas de MB e passa do limite de 100 MB por arquivo do GitHub, por isso `model.litertlm` está no `.gitignore`. Depois de clonar o projeto, baixe o modelo e coloque-o na pasta `assets`.

## Arquitetura

O app segue **Clean Architecture** em um único módulo, com injeção de dependência manual (`AppContainer`):

```
com.davi.dev.gnixia
├── GnixiaApplication.kt        cria o AppContainer
├── di/                         AppContainer (injeção manual)
├── domain/                     regras do app, sem Android
│   ├── model/                  HistoryEntry, ModelStatus, UnsupportedDeviceException
│   ├── repository/             ChatRepository, HistoryRepository (interfaces)
│   └── usecase/                InitializeModel, SendMessage, ObserveHistory,
│                               ObserveHistoryEntry, ClearHistory
├── data/                       implementações
│   ├── llm/                    GemmaEngine (LiteRT-LM), ModelDownloader
│   ├── local/                  Room: AppDatabase, ChatHistoryDao, ChatHistoryEntity
│   └── repository/             ChatRepositoryImpl, HistoryRepositoryImpl
└── presentation/               UI em Jetpack Compose para Wear OS
    ├── MainActivity.kt
    ├── navigation/             Routes, AppNavigation (Navigation 3)
    ├── home/                   HomeScreen
    ├── chat/                   ChatScreen, ChatViewModel
    ├── history/                HistoryScreen, HistoryDetailScreen, HistoryViewModel
    ├── about/                  AboutScreen
    ├── settings/               SettingsScreen
    ├── common/                 rememberTextInputLauncher
    └── theme/                  GnixiaTheme, GnixiaStyles (Styles API)
```

- O `SendMessageUseCase` envia a resposta do modelo em partes para a tela e salva a pergunta e a resposta no histórico quando a geração termina.
- O modelo fica no `ChatRepositoryImpl`, que dura enquanto o app estiver aberto, e é carregado uma única vez.
- O comportamento do modelo (responder sempre em pt-BR e de forma breve) é definido em `GemmaEngine.SYSTEM_INSTRUCTION`.

## Navegação

| Rota | Tela |
|---|---|
| `HomeRoute` | Home (tela inicial) |
| `ChatRoute` | Chat |
| `HistoryRoute` | Lista do histórico |
| `HistoryDetailRoute(id)` | Pergunta e resposta completas |
| `AboutRoute` | Apresentação do app |
| `SettingsRoute` | Configurações |

A navegação usa **Navigation 3** com `rememberSwipeDismissableSceneStrategy()`: deslize para a direita para voltar.

## Tecnologias

- Kotlin 2.4 · Jetpack Compose (BOM 2026.09.00)
- Wear Compose Material 3 1.7 · Wear Compose Navigation 3
- LiteRT-LM 0.17 (Gemma no dispositivo)
- Room 2.8 + KSP
- Lifecycle ViewModel · Kotlin Coroutines/Flow
- Core SplashScreen · Wear Input (RemoteInput)
- Compose Styles API (experimental) nos balões do chat

## Textos e tradução

Todos os textos da interface ficam em `app/src/main/res/values/strings.xml`. Para traduzir, crie `values-<idioma>/strings.xml` com as mesmas chaves.

## Changelog

Veja o [CHANGELOG.md](CHANGELOG.md).
