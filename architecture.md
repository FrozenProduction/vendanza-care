# Arquitetura

**Projeto 50+10** · ULS Barcelos/Esposende · Grupo 14 · EST/UPCA

A fonte do âmbito é o documento `APP ULSBE Requisitos.pdf`, entregue pela ULSBE a 26 de outubro de 2026.
O material de terceiros (o PDF da ULSBE, os artigos, os slides das aulas) fica fora do repositório.

Este ficheiro existe para não se refazer trabalho já decidido. Se uma alteração contrariar uma decisão
daqui, a decisão muda no mesmo commit e com uma razão escrita. Caso contrário, a próxima pessoa desfaz o
que aqui está sem saber porquê.

## 1. O que existe hoje

Uma aplicação Android. Mais nada corre ainda.

| Parte | Estado | UC |
|---|---|---|
| `app/` — Android, Kotlin + Compose | construída neste repositório, v1 offline | PDM |
| `ai/` — serviço de IA | vazia de propósito; o âmbito vem da proposta do PA | IA |
| `docs/` — documentos nossos | parcial | PA |

O documento da ULSBE pede quatro superfícies: Android, iOS, uma versão web/PWA para quem não instala
aplicações e um backoffice web com painel de indicadores anónimos. Só a primeira está aqui, e é uma
decisão de âmbito (§11, D1), não um esquecimento.

### O que a v1 faz e o que deixa de fora

| Entra na v1 | Fica de fora (e para onde vai) |
|---|---|
| 12 guias de saúde, texto e leitura em voz alta no telemóvel | ilustrações, vídeo e áudios que a ULSBE fornece (§12.1) |
| Lista de medicação no telemóvel, com fotografia da caixa | exportar a lista em imagem ou PDF (§12.2) |
| Lembretes de toma com "Já tomei" e "Daqui a 10 min" | alarme com som e luz (aviso visual para quem não ouve) (§12.2) |
| Aviso de princípios ativos sobreponíveis | verificação de interações medicamentosas: nunca, isso é conselho clínico |
| Perguntas para os profissionais de saúde, escolhidas e partilhadas | gravação da pergunta em voz (§12.3) |
| 112, SNS 24 e contactos da própria pessoa num toque | número do Gabinete do Cidadão: a ULSBE não o indicou |
| Tamanho de texto e botão "Ouvir" sempre visível | modo cuidador com várias pessoas a cargo (§12.4) |
| Sem conta, sem rede, sem dados pessoais | questionários, gamificação, área infantil e realidade aumentada (§12.5) |

## 2. Restrições que mandam no desenho

Saem do documento do cliente, não de gosto pessoal.

1. O utilizador mais frágil é o alvo do desenho: idade avançada, literacia baixa, tremor, visão reduzida,
   telemóveis antigos e internet instável. Uma funcionalidade que só serve quem já sabe usá-la não está
   feita.
2. "Alguns utentes não sabem ler". Nenhuma informação essencial pode existir só em texto, e cada ecrã de
   conteúdo tem versão falada.
3. A v1 não recolhe dados pessoais nem de saúde. A lista de medicação fica no telemóvel. Isto é o que
   permite lançar a app sem tratamento de dados e sem servidor.
4. Depois de instalada funciona sem internet: o conteúdo vai dentro do APK e a app não faz um único
   pedido de rede.
5. Telemóveis antigos e de gama baixa: `minSdk 24`, poucas dependências, APK pequeno e nenhum serviço em
   segundo plano.
6. A app não diagnostica e não acede ao processo clínico. Toda a questão clínica acaba num contacto
   (SNS 24, 112 ou o profissional), nunca numa resposta escrita por nós.
7. Acessibilidade: Decreto-Lei n.º 83/2018, com declaração de acessibilidade. O contrato da §9 é
   requisito de construção, não um extra.

## 3. Contexto do sistema

```
                    ┌──────────────────────────────┐
   utente /         │   VendanzaCare (Android)     │
   cuidador ───────▶│  conteúdo em assets/         │
                    │  medicação em SharedPrefs    │   nenhum pedido de rede
                    │  fala: TextToSpeech (local)  │   nenhum dado pessoal
                    └───────────┬──────────────────┘
                                │ ACTION_DIAL / ACTION_SEND (intents do sistema)
                    ┌───────────▼──────────────────┐
                    │ Telefone · SNS 24 · 112 ·    │
                    │ partilha de texto            │
                    └──────────────────────────────┘

   Fora deste repositório (a decidir, §12.6):
   backoffice web ──▶ conteúdos  ·  painel de indicadores  ·  versão web/PWA  ·  MyULSBE
```

A app fala com o sistema operativo e com mais nada. Cada uma das superfícies da última caixa exigiria um
servidor, um acordo de subcontratação (art. 28.º do RGPD) e alojamento na União Europeia.

## 4. Organização do código

O projeto Gradle está dentro de `app/`; o módulo é `app/app/`.

```
app/
├── build.gradle.kts            compileSdk/targetSdk 37, minSdk 24, namespace pt.upca.vendanzacare
└── app/src/main/
    ├── AndroidManifest.xml     uma activity, três receivers, POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED
    ├── assets/
    │   ├── conteudo.json       os 12 guias, 52 secções, ~1 250 palavras
    │   └── perguntas.json      17 perguntas em 4 temas
    ├── res/
    │   ├── mipmap-*/           ícone da app, versão clara e camadas adaptativas
    │   ├── drawable-nodpi/     marca horizontal (Início) e vertical (Sobre)
    │   ├── drawable-*dpi/      ic_stat_marca, o símbolo de uma cor na notificação
    │   ├── values-v31/         ecrã de arranque do Android 12 na cor de areia
    │   ├── xml/caminhos_fotos  FileProvider: só `files/fotos/` chega à app da câmara
    │   └── font/               Atkinson Hyperlegible Next, 4 pesos estáticos
    └── java/pt/upca/vendanzacare/
        ├── MainActivity.kt     activity única, edge-to-edge, aloja o NavHost
        ├── dominio/
        │   ├── Modelos.kt      Modulo, Seccao, Medicamento, Pergunta, Toma, Sobreposicao
        │   ├── Regras.kt       funções puras: próxima toma, horas, princípios ativos, chave da toma
        │   └── Dicionario.kt   marca → princípio ativo, tabela semente (§12.12)
        ├── dados/
        │   ├── Conteudo.kt     assets JSON → modelos, com org.json
        │   ├── MedicacaoStore.kt  lista de medicação em SharedPreferences, um JSON por chave
        │   ├── Preferencias.kt nome, data de nascimento, escala de texto, idioma, perguntas escolhidas
        │   ├── RegistoTomas.kt tomas marcadas por dia, 14 dias
        │   ├── ContactosStore.kt contactos que a pessoa acrescenta
        │   └── Fotos.kt        fotografia da caixa: câmara ou galeria → cópia direita e reduzida
        ├── lembretes/
        │   ├── Lembretes.kt    alarmes do AlarmManager, um por hora de toma
        │   ├── AlarmeReceiver.kt  alarme → notificação, e volta a armar o dia seguinte
        │   ├── TomaReceiver.kt    botões "Já tomei" e "Daqui a 10 min"
        │   └── ArranqueReceiver.kt reinício, atualização ou mudança de hora → rearma tudo
        └── ui/
            ├── Navegacao.kt    sealed class Ecra, NavHost, barra inferior com rótulos
            ├── Textos.kt       todas as frases da interface, PT e EN, numa data class
            ├── Leitor.kt       TextToSpeech em pt-PT, no dispositivo
            ├── Tomas.kt        estado das tomas para o Compose
            ├── Tempo.kt        minuto atual, atualizado a cada 30 s
            ├── Ligacoes.kt     abre o marcador com ACTION_DIAL
            ├── tema/           Cor (paleta e tons por tema), Tipo (escala Atkinson), Tema (esquema e formas)
            ├── componentes/    Comuns (cartões, botões, cabeçalhos, avisos), Icones, Foto
            └── ecras/          um ficheiro por ecrã
```

**A regra que sustenta o resto:** `dominio/` não importa nada de `android.*`. As decisões e os cálculos
vivem lá para poderem ser testados na JVM, sem emulador; os ecrãs desenham e chamam. Só `dados/` sabe
como os bytes são guardados, e `ui/` nunca lê `SharedPreferences` diretamente.

## 5. Modelo de dados

Conteúdo empacotado com o APK, só de leitura:

```jsonc
// assets/conteudo.json
{ "versao": 1,
  "modulos": [{
    "id": "quedas",                       // estável: é a rota e a chave do conteúdo
    "titulo": "Evitar quedas",
    "resumo": "uma linha, para a lista",  // não pode passar de uma linha a 320 dp
    "icone": "casa",                      // chave, mapeada em ui/componentes/Icones.kt
    "seccoes": [{
      "titulo": "Em casa",
      "texto": "frases curtas. uma ideia por frase.",
      "pontos": ["opcional", "lista curta"], // 0 a 5 itens, cada um até 60 caracteres
      "destaque": false                    // true → caixa de aviso (ligar 112 ou parar e perguntar)
    }]
  }]
}
```

Dados da pessoa, criados no telemóvel e que nunca saem dele:

```kotlin
data class Medicamento(
    val id: String,          // UUID
    val nome: String,        // princípio ativo, como a pessoa escreve
    val dose: String,        // texto livre: "1 comprimido"
    val horas: List<Int>,    // minutos do dia, ordenados e sem repetições; 480 = 08:00
    val nota: String = "",   // "em jejum", por exemplo
    val foto: String? = null // content://…/fotos/foto-….jpg, cópia privada (dados/Fotos.kt)
)
```

`MedicacaoStore` grava a lista inteira como uma string JSON. Para além disso só se guardam: a escala de
texto, o idioma, o nome e a data de nascimento, os ids das perguntas escolhidas, as tomas marcadas por dia
e os contactos que a pessoa escreveu.

Não existe entidade `Utente` e é de propósito. O modo cuidador precisa de várias pessoas a cargo, o que
transforma uma lista numa lista por pessoa e mexe em todos os ecrãs. Os ids já são opacos, por isso
acrescentar `utenteId` mais tarde é uma alteração aditiva (§12.4).

## 6. Navegação

```
        ┌──────── barra inferior ────────┐
Início ─┤  Guias   │ Medicação │ Definições ├─ (4 separadores, ícone e palavra)
   │        │            │           │
   ├── contactos                              (a partir da faixa do 112)
   ├── perguntas                              (a partir do cartão do Início)
   │                     ├── medicamento/novo
   │                     └── medicamento/{id}
   └── modulo/{id} ◀─────┘
```

| Rota | Ecrã | Voltar |
|---|---|---|
| `inicio` | saudação, a toma a fazer (com "Já tomei" quando está na hora), faixa do 112, tomas de hoje com visto por medicamento, portas para Guias e Perguntas | sai da app |
| `guias` | os 12 temas em cartões coloridos (2 colunas, 1 coluna a partir de 1,3× de texto) | → inicio |
| `modulo/{id}` | cabeçalho colorido, "Ouvir o guia todo", secções numeradas com "Ouvir" | → guias |
| `contactos` | 112, SNS 24 e Centro de Informação Antivenenos, mais os contactos da própria pessoa | → inicio |
| `medicacao` | cartões com horas, aviso de sobreposição no próprio cartão, "Partilhar" e "Adicionar" | → inicio |
| `medicamento/{id}` | formulário de criação e edição (`novo` = em branco); as horas tocam-se | → medicacao |
| `perguntas` | lista por tema, com partilha presa ao fundo a partir do primeiro item escolhido | → inicio |
| `definicoes` | perfil, tamanho de texto, idioma e informação sobre a app | → inicio |

A barra inferior só aparece nas quatro rotas base. A troca de separador usa `popUpTo(inicio){saveState}`
com `launchSingleTop` e `restoreState`, para a pilha não crescer a cada toque. Todos os ecrãs de detalhe
têm um botão redondo "Voltar" no topo, com 52 dp, em vez de um gesto.

## 7. Conteúdo

1. A ULSBE fornece o texto (mais tarde imagens e áudio). A equipa adapta ao estilo da casa: uma ideia por
   frase, nenhum termo técnico sem equivalente do dia a dia, cada ponto até 60 caracteres.
2. O texto entra em `assets/conteudo.json` sob o `id` do módulo. Nada mais na app muda, e é esse o ponto
   do ficheiro.
3. O campo `versao` sobe quando o conteúdo muda. Um backoffice futuro produz exatamente este JSON.
4. A ordem dos módulos no ficheiro é a ordem da lista. Primeiro o risco maior, não a ordem alfabética.

Um texto que seja instrução clínica (dose, interação, "pare de tomar X") só entra com um revisor da ULSBE
nomeado no commit.

## 8. Camadas

| Camada | Pode importar | Não pode |
|---|---|---|
| `dominio/` | biblioteca padrão do Kotlin | `android.*`, `androidx.*` |
| `dados/` | `dominio/`, `android.content`, `org.json` | `androidx.compose.*` |
| `lembretes/` | `dominio/`, framework Android | Compose |
| `ui/` | tudo o que está acima, Compose e Material 3 | `SharedPreferences` diretamente |

Na prática, é isto que está em jogo: `Regras.kt` decide qual é a próxima toma, se uma hora escrita é
válida e se dois medicamentos partilham princípio ativo. São os três sítios onde um erro chega ao doente,
e os três estão cobertos por testes de JVM que correm no mesmo build do APK.

A regra dos princípios ativos (novembro de 2026): o nome é formado por todas as palavras até à dose
("ácido fólico 5 mg" fica "acido folico"), tirando as palavras de sal ("cloridrato de"); as marcas passam
pelo `Dicionario`; dois nomes são o mesmo princípio quando um é o início do outro, palavra a palavra
("paracetamol" e "paracetamol generis"). A regra anterior olhava só para a primeira palavra: avisava de
ácido fólico com ácido acetilsalicílico e ficava calada com Paracetamol e Ben-u-ron. Os dois casos estão
hoje em testes.

## 9. Contrato de acessibilidade

Medido no aparelho de referência: 1080×2400 a 420 dpi, ou seja 411×914 dp.

| Regra | Valor | Porquê |
|---|---|---|
| Alvos de toque | 48 dp no mínimo, ação principal 56 dp | tremor (Sr. António) |
| Texto corrido | 18 sp no mínimo, sempre pela escala do tema, nunca um `sp` literal | a D. Maria lê com esforço |
| Escala de texto | 1,0 / 1,3 / 1,6× sobre a escala do sistema, nas Definições, com o produto limitado a 2,0× | DL 83/2018, WCAG 2.1 1.4.4 (200%). Sem limite, sistema a 1,3× com app a 1,6× dava 2,08× e uma caixa de aviso ocupava o ecrã todo |
| Contraste | 4,5:1 no texto corrido, 3:1 em texto grande e contornos | os valores estão calculados ao lado de cada cor em `tema/Cor.kt`: texto 14,6:1 e secundário 6,3:1 sobre areia, tintas dos temas ≥ 5,5:1 sobre o próprio fundo, branco ≥ 6,5:1 nas duas pontas do gradiente da toma |
| Áudio | todos os ecrãs de conteúdo têm "Ouvir", à vista | "alguns utentes não sabem ler" |
| Ícones | cada ícone que significa algo sozinho leva rótulo visível, incluindo os quatro separadores. Os rótulos da barra são o único texto que não segue a escala toda: param em 1,15× para os quatro ficarem iguais e numa linha | ícones sem rótulo são a primeira coisa em que os utilizadores mais velhos falham |
| Texto grande | a partir de 1,3× (`ESCALA_UMA_COLUNA`) tudo o que está lado a lado passa a uma coluna: a grelha de temas, as portas do Início, os momentos do dia | a duas colunas e 1,6×, "Comunicação" partia-se em três linhas |
| Ações destrutivas | dois toques; apagar pergunta | evita o toque acidental |
| Sem tempo limite | nada expira, nada avança sozinho | idem |

A cor nunca é o único sinal: a gravidade leva forma, rótulo e cor.

## 10. Lembretes

Cada hora de toma tem o seu alarme do `AlarmManager` (`setAndAllowWhileIdle`, `RTC_WAKEUP`), com um
`PendingIntent` identificado por `"$id-$hora"`. Um alarme dispara uma vez, por isso
`Lembretes.reagendar()`, que cancela tudo e volta a armar a lista guardada, é chamado em três sítios:

| Onde | Porquê |
|---|---|
| arranque da app e depois de cada alteração à medicação | a lista mudou ou pode ter mudado |
| `AlarmeReceiver`, logo a seguir à notificação | é isto que faz tocar amanhã. Sem esta chamada, cada lembrete tocava uma vez e a cadeia só recomeçava quando a pessoa abria a app |
| `ArranqueReceiver` (`BOOT_COMPLETED`, `MY_PACKAGE_REPLACED`, `TIME_SET`, `TIMEZONE_CHANGED`) | o sistema perde os alarmes no reinício, e uma mudança de hora ou de fuso desloca-os |

Rearmar lê sempre a lista guardada, nunca o intent que disparou, para um medicamento entretanto apagado
não voltar.

Limite assumido: `setAndAllowWhileIdle` é inexato. O `dumpsys alarm` mostra uma janela de entrega até
mais uma hora (75% do tempo que falta, com teto de uma hora), por isso uma toma das 20:00 pode tocar às
20:59. Alarmes exatos exigem `SCHEDULE_EXACT_ALARM`, que o utilizador tem de conceder e que vem desligado
por omissão a partir do Android 14. Fica para decidir com a ULSBE; até lá, em testes com utentes diz-se
"pode chegar até uma hora depois", não se promete a hora certa.

"Já tomei" e "Daqui a 10 min" são os dois botões da notificação, tratados pelo `TomaReceiver` sem abrir a
app. "Já tomei" escreve `"<id>@<minutos>"` no conjunto do dia em `RegistoTomas` (14 dias guardados) e
fecha a notificação; "Daqui a 10 min" volta a tocar a mesma toma com um código próprio, sem mexer no
alarme diário. O dia viaja com a notificação, por isso uma toma das 23:30 respondida à 00:10 fica no dia
certo. Uma toma já marcada não notifica. O Início ouve o mesmo ficheiro de preferências e atualiza-se com
a app aberta.

Não há serviço em segundo plano, nem WorkManager, nem wake locks. É o que mantém a app instalável num
telemóvel com 500 MB livres.

`POST_NOTIFICATIONS` é pedido no primeiro arranque (Android 13+). Se for recusado, a lista funciona, os
lembretes não, e o ecrã da medicação di-lo em vez de falhar em silêncio.

## 11. Decisões

| # | Decisão | Razão | Alternativa rejeitada |
|---|---|---|---|
| D1 | Só a app Android é construída agora | A PDM avalia a app; backoffice, PWA e painel de indicadores são um segundo produto, com servidor, art. 28.º do RGPD e alojamento na UE | Um backend fino "para depois": peso morto e uma superfície de ataque sem dono |
| D2 | Offline, sem rede e sem conta | Regra da v1 no documento do cliente: não recolher dados pessoais nem de saúde | Firebase ou sincronização na nuvem: exigiria avaliação de privacidade antes do primeiro ecrã |
| D3 | Conteúdo num JSON empacotado, lido com `org.json` | Zero dependências, um ficheiro para editar, e um backoffice pode gerar a mesma estrutura | Room com base de dados semeada (mais pesado, precisa de KSP), configuração remota (rede) |
| D4 | `SharedPreferences` para a lista de medicação | Uma lista local, sem consultas. Trocar o armazenamento não mexe na interface | Room agora: duas dependências e um esquema para cinco campos |
| D5 | `android.speech.tts.TextToSpeech` para o áudio | No dispositivo, offline, gratuito, com voz pt-PT em qualquer Android desde a API 24 | Ficheiros de áudio gravados, que a ULSBE ainda não forneceu; uma chamada a um serviço de voz na nuvem quebra a D2 |
| D6 | Pacote `pt.upca.vendanzacare` | O código é entregue à ULSBE; `example` num `applicationId` entregue está mal, e `upca` acompanha a mudança de nome da instituição em 2026 | `pt.ipca.vendanzacare` (nome antigo), `upca.example.*` (convenção de exercício de aula) |
| D7 | Conjunto de ícones próprio, sem biblioteca | Cerca de 35 ícones desenhados na mesma grelha de 24 dp com o mesmo traço de 2 dp, como `ImageVector`. Cada um representa o seu assunto e custam poucos KB de código. Substituiu o `material-icons-core`, cujos 49 glifos obrigavam a substituições erradas ("Falar" era um envelope, "Evitar infeções" uma seta circular) | `material-icons-extended`: cerca de 1 MB, e os glifos cheios da Material continuam a parecer um modelo |
| D8 | Um só módulo, `:app` | Um produto, um build | Módulos `:core` e `:feature`: cerimónia sem segundo consumidor |
| D9 | Material 3 com cor dinâmica desligada | As cores da marca têm significado (azul é informação, turquesa é cuidado); uma paleta tirada do papel de parede estragaria o contraste da faixa do 112 | Cor dinâmica ligada |
| D10 | Só tema claro | O contraste foi medido numa paleta. Uma paleta escura que ninguém testou é pior do que não ter tema escuro, e o conteúdo médico lê-se em corredores de hospital com muita luz | Um esquema escuro gerado automaticamente; só com uma paleta medida |
| D11 | Uma só arte, o ícone claro | Decisão do grupo (26/10/2026): sem variante azul-escura, sem glifo turquesa. O cabeçalho e o ícone mostram o mesmo ficheiro, por isso a app não se afasta da identidade | Guardar um glifo para o cabeçalho: dois logótipos na mesma app é o que faz parecer inconsistente |
| D12 | Atkinson Hyperlegible Next na interface | Escolhida pelo grupo. Foi desenhada pelo Braille Institute para baixa visão: `1`, `l` e `I` e `0` e `O` têm formas distintas, o que importa num ecrã de doses ("1 comprimido", "100 mg"). Quatro pesos estáticos, 46 KB cada, porque os eixos variáveis são ignorados abaixo da API 26 | Nunito em todo o lado (quente, mas `l`, `I` e `1` quase iguais); Figtree (bonita, mesmo problema); Lexend (larga, custa uma linha por parágrafo a 1,6×) |
| D13 | "Ajuda" passou a "Definições", e os contactos de urgência não foram atrás | O nome e a data de nascimento fazem a lista dizer a quem pertence, e tamanho de texto e idioma são definições. O 112 e o SNS 24 ficam no Início, a um toque de onde a pessoa aterra | Manter Ajuda como quinto separador: cinco separadores a 1,6× não cabem |
| D14 | O idioma muda a interface, não o conteúdo | Os 12 guias são texto de saúde fornecido e validado em português europeu; inventar uma versão inglesa é um documento clínico novo, não uma tradução. Quem fala inglês navega a app e recebe o conteúdo quando a ULSBE assinar uma tradução | Traduzir tudo já: ninguém consegue validar 1 250 palavras de conselhos de saúde numa segunda língua antes da defesa |
| D15 | Uma só linguagem visual (revisão de novembro de 2026) | Fundo areia, cartões brancos com uma linha fina em vez de sombra, botões em pílula de 56 dp, um gradiente (a próxima toma), um vermelho (o 112) e uma cor por tema. As horas tocam-se em vez de se escreverem (Manhã 08:00, Almoço 13:00, Jantar 20:00, Deitar 22:00, mais um relógio para o resto). O aviso de sobreposição passou para o cartão a que diz respeito. "Ouvir" passa a "Parar" enquanto lê | Manter o campo de hora em texto livre: aceitava cinco grafias e ainda produzia erros que a pessoa não sabia corrigir |
| D16 | Fotografia da caixa com a app da câmara do telemóvel, sem reconhecimento | Decisão do grupo: o passo de "ler o medicamento" é tirar a fotografia, nada a analisa. `TakePicture` com um ficheiro do FileProvider evita a permissão de câmara e o CameraX, e usa a câmara que a pessoa já conhece. As escolhas da galeria são copiadas, não referenciadas, para a fotografia sobreviver se o original for apagado. Cada foto é rodada pelo EXIF e reduzida a 1280 px (12 MP dá cerca de 150 KB) e aparece no cartão, no Início e na notificação | Um visor dentro da app com CameraX: só vale a pena quando o reconhecimento precisar de imagens ao vivo |
| D17 | Do pacote de marca entram quatro ficheiros derivados, o pacote não | A regra do próprio pacote mantém-no fora do repositório público. A app leva o logótipo horizontal (reconstruído com transparência a partir dos originais), o vertical (sobre cartão branco, onde o fundo branco não se vê) e o símbolo de uma cor na notificação, em cinco densidades. O nome passou a ser imagem, por isso o Nunito deixou de ir dentro da app (menos 120 KB) | Recompôr "VendanzaCare" em texto: afasta-se do logótipo (era todo azul, e a marca tem "Care" em turquesa) |
| D18 | Uma família de botões com base saliente | `BaseBotao` serve quatro tipos: principal, secundário, contorno e grande. O principal assenta numa base 3 dp mais escura e desce até ela quando é premido; tudo encolhe 3% sob o dedo; desativado mantém 5,7:1 em vez dos 38% de opacidade da Material; os botões que abrem um ecrã terminam em ›; as ações destrutivas são contornos vermelhos. Os botões planos são os que os utilizadores mais velhos menos reconhecem como tocáveis | Os botões por omissão da Material: corretos, mas planos, e o rótulo desativado falha o contraste |
| D19 | "Aprender" passou a "Guias" e "médico" a "profissionais de saúde" | Feedback da apresentação. "Aprender" dizia o que a pessoa faz, não o que ali está; o separador é Guias e o ecrã Guias de saúde. "Médico" deixava de fora o enfermeiro e o farmacêutico, que respondem a maior parte destas perguntas | "Saúde" (vago ao lado de "Medicação"), "Conselhos" (não cabe em quatro separadores a 1,15×) |
| D20 | Contactos de urgência: os nacionais fixos, os locais acrescentados pela pessoa | Pedido na apresentação ("bombeiros, etc."). O 112 já chega aos bombeiros, ao INEM e à polícia; o SNS 24 e o Centro de Informação Antivenenos (800 250 250, a chamada depois de tomar demasiado de um medicamento) são nacionais. A linha dos bombeiros muda de terra para terra, por isso a app não inventa um número: a pessoa acrescenta-o, escrito ou escolhido nos contactos do telemóvel, sem permissão de leitura da agenda | Uma lista de números locais dentro da app: precisa de uma fonte que a ULSBE garanta e de alguém que a mantenha atualizada |
| D21 | A app só regista uma toma quando a pessoa o diz | O visto significa que alguém tocou em "Já tomei", na notificação, no cartão do Início ou na lista, e tocar outra vez desmarca. Um conjunto de chaves por dia, 14 dias guardados, nunca partilhado. É uma ajuda de memória, não um registo de adesão | Histórico e gráficos de adesão: um registo clínico com outro nome, fora do âmbito da v1 |

## 12. O que ainda não existe

Cada ponto diz o que o destranca, para não ser feito duas vezes nem esquecido.

1. Ilustrações, vídeo e áudios gravados. A ULSBE fornece. Entram em `res/drawable` com uma chave `imagem` no `conteudo.json`; o leitor já tolera a chave em falta.
2. Partilha da lista de medicação em imagem ou PDF (pedida pela Ana). Precisa de um caminho de impressão; `PrintManager` com uma página WebView é o mais curto. Começa no primeiro teste de usabilidade com um cuidador.
3. Gravação da pergunta em voz. `MediaRecorder` e ficheiro local, sem permissão além de `RECORD_AUDIO`. Fica para depois de a v1 estar validada; é o único pedido da Ana que a v1 não responde.
4. Modo cuidador com várias pessoas. Entidade `Utente`, um seletor no ecrã da medicação e alarmes por pessoa. O desenho do seletor depende do que os testadores fizerem, não do que imaginarmos.
5. Questionários, gamificação e área infantil com realidade aumentada. Exige produção de conteúdo (banco de perguntas, personagem, narração) e ARCore. Só depois de os guias de texto estarem validados.
6. Backoffice, painel de indicadores, PWA e iOS. É outro produto (D1), quando houver uma UC com essa entrega e a ULSBE confirmar o alojamento.
7. Leitura de código QR ou de barras da embalagem. Precisa do ML Kit (`com.google.mlkit:barcode-scanning`, cerca de 2 MB) ou do CameraX, e acrescenta a permissão de câmara. Quando a ULSBE confirmar que os códigos das suas embalagens são legíveis.
8. Conversa com o profissional de serviço. Bloqueada à espera da resposta da ULSBE sobre o canal e quem responde. Um botão que não leva a lado nenhum é pior do que não ter botão.
9. Capturas de ecrã e ficha para as lojas, antes de qualquer publicação. As contas são da ULSBE.
10. Fotografia da caixa a preencher a ficha do medicamento. É o passo seguinte do grupo: apontar a câmara a uma caixa e obter nome, dose e horário já escritos. A metade da captura existe desde D16; o reconhecimento leria o mesmo ficheiro, no dispositivo, e preencheria o formulário com sugestões editáveis. É entrega da IA (`ai/`), não da PDM, e esbarra em duas regras: a fotografia mostra um medicamento, logo é dado de saúde e tem de ser tratada no telemóvel, nunca enviada; e a extração tem de aparecer como sugestão a confirmar, nunca como facto, porque uma dose mal lida é exatamente o erro que a app existe para evitar.
11. Declaração de acessibilidade (DL 83/2018) e uma passagem por um telemóvel real com o TalkBack ligado a 1,6×. A declaração é um documento que a ULSBE publica, e a passagem é a única forma de saber se a barra com rótulos e o texto aumentado sobrevivem a um ecrã real. O resto da §9 está garantido no código; isto é o que um compilador não vê.
12. Tabela de marcas para princípios ativos a partir do Infarmed. O `Dicionario.kt` tem uma semente de cerca de 55 marcas comuns (Ben-u-ron → paracetamol, Janumet → sitagliptina e metformina), escrita à mão e por validar. Deve ser substituída pelos dados do Infarmed (a mesma tabela de que os pontos 7 e 10 precisam) e assinada pela ULSBE, antes de qualquer teste com doentes.

## 13. Construir e verificar

```bash
cd app && ./gradlew assembleDebug testDebugUnitTest
```

Um cache do Gradle frio descarrega o Gradle 9.6, o AGP e o Compose (20 a 30 minutos numa ligação lenta);
com cache quente são 2 a 4 minutos.

No Android Studio abre-se a pasta `app/`, a que tem o `settings.gradle.kts`, e não a raiz do repositório.
A raiz é um monorepo de três UC.

Para instalar num telemóvel, usar a variante release (Build Variants → app → release). O build de debug
não está otimizado e é a razão habitual de a app parecer lenta; o release tem R8 ligado, fica em 2 MB e é
assinado com a chave de debug, por isso instala-se por cima do debug e mantém a lista de medicação. Uma
versão com keystore própria obriga a desinstalar primeiro.

Uma alteração só está fechada quando o build passa, os testes estão verdes e, se for visual, há uma
captura de ecrã. O compilador não vê um botão de 40 dp.

Estado da última verificação:

| Verificação | Resultado |
|---|---|
| Build | `assembleDebug` e `assembleRelease` concluídos, sem avisos do Kotlin |
| Testes | 23 de 23 (17 em `RegrasTest`, 2 em `ConteudoAssetsTest` e 4 sobre tomas e contactos) |
| APK | debug com 12,5 MB; release com 2,0 MB e um único dex |
| Lint | `lintVitalRelease` sem erros; `lintDebug` sem erros |
| Por verificar | o comportamento da câmara num telemóvel real e a ordem de leitura do TalkBack. Um emulador não os mostra |

## 14. Convenções

- Em português tudo o que uma pessoa lê. Em inglês os identificadores do código. As mensagens de commit
  são em português, como o resto do trabalho do grupo.
- Um módulo novo é um bloco de JSON, não um ecrã novo.
- Um ecrã novo é um ficheiro em `ui/ecras/` e uma rota em `Navegacao.kt`.
- Uma peça visual partilhada vai para `ui/componentes/Comuns.kt`; não se copia um cartão para um terceiro
  ecrã.
- Tudo o que for clínico leva comentário com a fonte da ULSBE e o revisor.
