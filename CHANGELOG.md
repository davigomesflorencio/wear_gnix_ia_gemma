# Changelog

Todas as mudanças relevantes do GNIX IA são registradas aqui.

O formato segue o [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e o projeto usa [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [Não lançado]

### Adicionado

- Fonte Inter Tight em todo o app, baixada pelo Google Play Services via Google Fonts (`ui-text-google-fonts`), sem arquivos de fonte no APK. Enquanto a fonte baixa, ou se o relógio estiver sem internet ou sem Play Services, o app usa a fonte padrão do sistema.

### Alterado

- O build de release agora usa R8 com minificação, ofuscação e otimização de código e recursos (`optimization { enable = true }`).
- Keep rule para a LiteRT-LM (`com.google.ai.edge.litertlm.**`): a biblioteca não traz regras próprias e o código nativo acessa as classes pelo nome via JNI.

## [1.0] - 2026-10-19

Primeira versão do app.

### Adicionado

- Chat com o modelo Gemma rodando no relógio via LiteRT-LM, com a resposta aparecendo enquanto é gerada.
- Instrução de sistema para o modelo responder sempre em português do Brasil, de forma breve.
- Entrada de texto por voz, teclado ou emoji usando o RemoteInput do Wear OS.
- Histórico de perguntas e respostas salvo com Room: lista, tela de detalhe e opção de apagar tudo com confirmação.
- Tela Home com atalhos para Apresentação, Histórico e Configurações e botão de pesquisa que envia a pergunta direto ao chat.
- Tela de Apresentação com as funcionalidades do app.
- Tela de Configurações com "Apagar histórico" e a versão do app.
- Navegação com Wear Compose Navigation 3 e gesto de deslizar para voltar.
- Splash screen com `installSplashScreen()` e ícone nas cores do tema.
- Tema próprio com a paleta `#000814`, `#001d3d`, `#003566`, `#ffc300` e `#ffd60a`.
- Previews de Wear OS (`@WearPreviewDevices` e `@WearPreviewFontScales`) em todas as telas.
- Aviso no chat quando o relógio não é compatível com o modelo (Android 32 bits).

### Alterado

- Projeto reorganizado em Clean Architecture (domain, data e presentation), com injeção de dependência manual.
- Nome do app e pacote alterados para **GNIX IA** / `com.davi.dev.gnixia`.
- Todos os textos da interface movidos para `strings.xml`.
- Balões do chat migrados para a Compose Styles API (`GnixiaStyles`).
- Dependências atualizadas: Kotlin 2.4.0, Compose BOM 2026.09.00 e Wear Compose 1.7.1.
- Uma pergunta feita enquanto o modelo ainda carrega fica guardada e é enviada quando ele termina.

### Corrigido

- O app fechava ao abrir em relógios 32 bits, como o Galaxy Watch 7 (`UnsatisfiedLinkError` da LiteRT-LM). Agora o app checa a arquitetura antes de carregar o modelo, mostra um aviso e apaga a cópia do modelo que não será usada.
- O ícone da splash screen não aparecia no Android 12+.
- A Home não ficava centralizada: o botão de pesquisa não aparecia, os cantos eram cortados na tela redonda e o título empurrava a grade para baixo.
- O build falhava por incompatibilidade entre o Kotlin 2.2 e a biblioteca padrão 2.4 exigida pela LiteRT-LM.
