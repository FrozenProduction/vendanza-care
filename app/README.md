# app — a aplicação Android (PDM)

Kotlin com Jetpack Compose, um módulo (`:app`), sem rede. O projeto Gradle está dentro de `app/` porque a
raiz do repositório é um monorepo de três UC.

A arquitetura e as decisões estão em `../architecture.md`. Vale a pena ler antes de mexer.

## Abrir no Android Studio

*File → Open* → a pasta `vendanza-care/app`, a que tem o `settings.gradle.kts`. Não abrir a raiz.

| Definição | Valor |
|---|---|
| `namespace` / `applicationId` | `pt.upca.vendanzacare` |
| `minSdk` | 24 (Android 7.0) |
| `compileSdk` / `targetSdk` | 37 |
| Versões | AGP 9.4.1, Kotlin 2.2.10, Gradle 9.6.0, Compose BOM 2026.02.01 |

Para correr num telemóvel, escolher a variante release em Build Variants. O build de debug não tem R8 e é
a causa habitual de a app parecer lenta; o release fica em 2 MB e instala-se por cima do debug, porque usa
a mesma chave.

## Construir sem o Android Studio

```bash
cd app
./gradlew assembleDebug testDebugUnitTest
```

O APK fica em `app/build/outputs/apk/debug/`. O `local.properties`, que guarda o caminho do SDK desta
máquina, é local e não entra no repositório; o Android Studio cria o seu ao abrir o projeto.

## Onde está o quê

| Caminho | O que é |
|---|---|
| `app/src/main/assets/conteudo.json` | os 12 guias. Editar aqui, não no código |
| `app/src/main/assets/perguntas.json` | as perguntas para levar à consulta |
| `res/font/` | Atkinson Hyperlegible Next, quatro pesos estáticos, licença OFL |
| `res/mipmap-*` | o ícone da app, versão clara |
| `res/drawable-nodpi/marca_*.png`, `res/drawable-*/ic_stat_marca.png` | a marca dentro da app: logótipo horizontal no Início, vertical em Sobre e o símbolo de uma cor na notificação. São os únicos ficheiros tirados do pacote de marca, que continua fora do repositório |
| `ui/Textos.kt` | todas as frases da interface, em português e inglês. O conteúdo dos guias não está aqui |
| `dominio/Regras.kt` | as decisões: próxima toma, horas válidas, princípios ativos, chave de uma toma. Sem Android, testado |
| `dados/` | leitura do JSON, a medicação, as tomas marcadas, os contactos e as fotografias das caixas |
| `lembretes/` | alarmes, notificação da toma e os botões "Já tomei" e "Daqui a 10 min" |
| `ui/ecras/` | um ficheiro por ecrã |
| `ui/componentes/` | as peças visuais partilhadas: botões, cartões, faixa do 112, botão Ouvir, ícones |

## Regras

- Sem rede. A app não faz um único pedido HTTP e não recolhe dados pessoais. Se um requisito parecer pedir
  rede, a decisão toma-se primeiro em `architecture.md`.
- Texto de saúde é conteúdo, não código. Só entra em `conteudo.json`, com o revisor da ULSBE nomeado no
  commit.
- Nada de `sp` literal nos ecrãs. Usar `MaterialTheme.typography`, porque é disso que depende o controlo
  de tamanho de texto nas Definições.
- Alvos de toque com 48 dp no mínimo; a ação principal de um ecrã com 56 dp.
- Não commitar `local.properties`, `build/`, `.gradle/` nem keystores. O repositório é público: sem
  números de aluno, sem contactos do hospital, sem dados de doentes.

## Dados e privacidade

A lista de medicação fica em `SharedPreferences` e não sai do telemóvel: `allowBackup=false` e as regras de
extração em `res/xml/regras_extracao.xml` impedem a cópia para a nuvem. As fotografias das caixas ficam em
`files/fotos/`, dentro do espaço privado da app, e nunca vão para a galeria. A câmara usada é a do próprio
telemóvel, por isso a app não pede permissão de câmara, e nada analisa a fotografia.

As tomas marcadas guardam-se 14 dias, na mesma lógica: são uma ajuda para responder a "já tomei isto?",
não um registo clínico.

## Testes

```bash
./gradlew testDebugUnitTest
```

Correm no mesmo build do APK e cobrem `Regras.kt` (as três decisões que podem magoar alguém e a chave de
uma toma), a estrutura dos dois ficheiros de conteúdo e o registo de tomas.
