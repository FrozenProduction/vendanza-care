package pt.upca.vendanzacare.ui

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Every string the interface shows, in both languages.
 *
 * A data class, not a map: a missing translation is a compile error, not a blank label at runtime.
 * Placeholders use `%d` and are filled with `String.format`, so a translator cannot silently drop a number.
 *
 * **What is translated:** the interface. **What is not:** the 12 modules' health text — that is content,
 * supplied and validated by ULSBE in European Portuguese, and inventing an English version of clinical
 * instructions nobody reviewed is not a translation, it is a new unvalidated document. In English mode the
 * interface switches and the modules stay in PT, which is what a Portuguese hospital app would ship anyway.
 */
data class Textos(
    val idioma: String,
    val locale: String,

    // navegação
    val inicio: String,
    val aprender: String,
    val medicacao: String,
    val definicoes: String,
    val voltar: String,

    // início
    val bomDia: String,
    val boaTarde: String,
    val boaNoite: String,
    val tomasDeHoje: String,
    val proxima: String,
    val nTemas: String,
    val prepararConsulta: String,
    val proximaToma: String,
    val amanha: String,
    val agora: String,
    val emMinutos: String,
    val emHorasMinutos: String,
    val verMedicacao: String,
    val semMedicacaoTitulo: String,
    val semMedicacaoTexto: String,
    val adicionarALista: String,

    // emergência
    val ajudaAgora: String,
    val ajudaAgoraTexto: String,
    val ligar112: String,
    val ligarSns24: String,
    val partilhar: String,
    val partilharLista: String,
    val partilharPerguntas: String,
    val partilharUmaPergunta: String,

    // módulos
    val aprenderIntro: String,
    val conteudoIndisponivel: String,
    val conteudoIndisponivelTexto: String,
    val tema: String,
    val temaNaoEncontrado: String,
    val temaNaoEncontradoTexto: String,
    val ouvir: String,
    val ouvirTudo: String,
    val parar: String,
    val semVoz: String,
    val escolherPerguntas: String,

    // medicação
    val aMinhaMedicacao: String,
    val listaVaziaTitulo: String,
    val listaVaziaTexto: String,
    val listaSoTelemovel: String,
    val medicamentosNaLista: String,
    val umMedicamentoNaLista: String,
    val semHoras: String,
    val adicionarMedicamento: String,
    val lembretesDesligados: String,
    val ligarNotificacoes: String,
    val duplicacaoTitulo: String,
    /** %1$s is the other medicine as the person wrote it, %2$s the shared ingredient. */
    val mesmoPrincipioQue: String,
    /** When the other entry's name already is the ingredient ("Paracetamol"), naming it twice reads oddly. */
    val igualA: String,
    val sobreponiveisConselho: String,

    // formulário do medicamento
    val novoMedicamento: String,
    val editar: String,
    val nome: String,
    val nomeAjuda: String,
    val dose: String,
    val doseAjuda: String,
    val horas: String,
    val horasAjuda: String,
    val manha: String,
    val almoco: String,
    val jantar: String,
    val deitar: String,
    val outraHora: String,
    val escolherHora: String,
    val removerHora: String,
    val ok: String,
    val fotografia: String,
    val fotografiaAjuda: String,
    val tirarFotografia: String,
    val comACamara: String,
    val daGaleria: String,
    val fotoQueJaTem: String,
    val tirarOutra: String,
    val removerFotografia: String,
    val aGuardarFoto: String,
    val fotoFalhou: String,
    val semCamara: String,
    val oQueDizACaixa: String,
    val nota: String,
    val notaAjuda: String,
    val guardar: String,
    val apagarMedicamento: String,
    val apagarTitulo: String,
    val apagarTexto: String,
    val apagar: String,
    val cancelar: String,
    val erroNome: String,

    // perguntas
    val perguntasMedico: String,
    val perguntasIntro: String,
    val limparLista: String,

    // definições
    val perfil: String,
    val oSeuNome: String,
    val oSeuNomeAjuda: String,
    val dataNascimento: String,
    val escolherData: String,
    val tamanhoTexto: String,
    val normal: String,
    val grande: String,
    val muitoGrande: String,
    val exemploTexto: String,
    val idiomaTitulo: String,
    val portugues: String,
    val ingles: String,
    val sobre: String,
    val sobreTexto: String,
    val versao: String,
) {
    fun faltamMinutos(n: Int) = String.format(emMinutos, n)
    fun faltamHorasMinutos(h: Int, m: Int) = String.format(emHorasMinutos, h, m)
    fun contagemMedicamentos(n: Int) = if (n == 1) umMedicamentoNaLista else String.format(medicamentosNaLista, n)

    fun mesmoPrincipioDe(outro: String, principio: String) = String.format(mesmoPrincipioQue, outro, principio)
    fun igualADe(outro: String) = String.format(igualA, outro)
    fun contagemTemas(n: Int) = String.format(nTemas, n)
    fun removerHoraDe(hora: String) = String.format(removerHora, hora)

    /** Greeting by time of day: 05–12 h, 12–20 h, otherwise night. */
    fun saudacao(minuto: Int) = when (minuto / 60) {
        in 5..11 -> bomDia
        in 12..19 -> boaTarde
        else -> boaNoite
    }
    fun contagemPerguntas(n: Int) = if (n == 1) partilharUmaPergunta else String.format(partilharPerguntas, n)
    fun apagarTituloDe(nome: String) = String.format(apagarTitulo, nome)
}

val TextosPT = Textos(
    idioma = "pt",
    locale = "pt-PT",

    inicio = "Início",
    aprender = "Aprender",
    medicacao = "Medicação",
    definicoes = "Definições",
    voltar = "Voltar",

    bomDia = "Bom dia",
    boaTarde = "Boa tarde",
    boaNoite = "Boa noite",
    tomasDeHoje = "As tomas de hoje",
    proxima = "Próxima",
    nTemas = "%d temas",
    prepararConsulta = "Prepare a próxima consulta",
    proximaToma = "Próxima toma",
    amanha = "amanhã",
    agora = "agora",
    emMinutos = "daqui a %d minutos",
    emHorasMinutos = "daqui a %d h %d min",
    verMedicacao = "Ver a minha medicação",
    semMedicacaoTitulo = "Ainda não tem medicação",
    semMedicacaoTexto = "Escreva a lista uma vez. Depois é só mostrar aos profissionais de saúde.",
    adicionarALista = "Adicionar à minha lista",

    ajudaAgora = "Precisa de ajuda agora?",
    ajudaAgoraTexto = "Em caso de perigo de vida, ligue 112.",
    ligar112 = "Ligar 112",
    ligarSns24 = "Ligar SNS 24 (808 24 24 24)",
    partilhar = "Partilhar",
    partilharLista = "Partilhar a lista",
    partilharPerguntas = "Partilhar %d perguntas",
    partilharUmaPergunta = "Partilhar 1 pergunta",

    aprenderIntro = "Toque num tema. Em todos os ecrãs pode ouvir o que está escrito.",
    conteudoIndisponivel = "Conteúdo indisponível",
    conteudoIndisponivelTexto = "O ficheiro de conteúdos não carregou. Reinicie a aplicação.",
    tema = "Tema",
    temaNaoEncontrado = "Tema não encontrado",
    temaNaoEncontradoTexto = "Este tema já não existe nesta versão da aplicação.",
    ouvir = "Ouvir",
    ouvirTudo = "Ouvir o tema todo",
    parar = "Parar",
    semVoz = "Sem voz neste telemóvel",
    escolherPerguntas = "Escolher perguntas para a consulta",

    aMinhaMedicacao = "A minha medicação",
    listaVaziaTitulo = "A lista está vazia",
    listaVaziaTexto = "Escreva o nome do medicamento, a dose e as horas a que toma. Depois mostre a lista " +
        "nas consultas, na urgência e no hospital.",
    listaSoTelemovel = "A lista fica só neste telemóvel.",
    medicamentosNaLista = "%d medicamentos nesta lista.",
    umMedicamentoNaLista = "1 medicamento nesta lista.",
    semHoras = "Sem horas definidas — não há lembretes",
    adicionarMedicamento = "Adicionar medicamento",
    lembretesDesligados = "Os lembretes estão desligados neste telemóvel. Ligue as notificações nas " +
        "definições do Android para receber os avisos das tomas.",
    ligarNotificacoes = "Ligar notificações",
    duplicacaoTitulo = "Pode estar a tomar o mesmo medicamento duas vezes.",
    mesmoPrincipioQue = "Igual a %1\$s: os dois têm %2\$s.",
    igualA = "Igual a %s: o mesmo princípio ativo.",
    sobreponiveisConselho = "Pergunte ao médico ou ao farmacêutico.",

    novoMedicamento = "Novo medicamento",
    editar = "Editar",
    nome = "Nome (princípio ativo)",
    nomeAjuda = "Ex.: Paracetamol. O nome da caixa pode ser diferente.",
    dose = "Dose",
    doseAjuda = "Ex.: 1 comprimido, 5 gotas.",
    horas = "Horas",
    horasAjuda = "Toque nas horas a que toma. Cada hora tem um lembrete.",
    manha = "Manhã",
    almoco = "Almoço",
    jantar = "Jantar",
    deitar = "Deitar",
    outraHora = "Outra hora",
    escolherHora = "Escolha a hora",
    removerHora = "Remover %s",
    ok = "OK",
    fotografia = "Fotografia da caixa",
    fotografiaAjuda = "Comece pela caixa. Depois escreva o nome e a dose que lá estão.",
    tirarFotografia = "Tirar fotografia",
    comACamara = "com a câmara",
    daGaleria = "Da galeria",
    fotoQueJaTem = "uma foto que já tem",
    tirarOutra = "Tirar outra",
    removerFotografia = "Remover fotografia",
    aGuardarFoto = "A guardar a fotografia…",
    fotoFalhou = "Não foi possível abrir essa fotografia. Tente outra vez.",
    semCamara = "Este telemóvel não tem aplicação de câmara. Escolha uma foto da galeria.",
    oQueDizACaixa = "O que diz a caixa",
    nota = "Nota (opcional)",
    notaAjuda = "Ex.: em jejum, ao deitar.",
    guardar = "Guardar",
    apagarMedicamento = "Apagar medicamento",
    apagarTitulo = "Apagar %s?",
    apagarTexto = "O medicamento e os lembretes das tomas desaparecem desta lista.",
    apagar = "Apagar",
    cancelar = "Cancelar",
    erroNome = "Escreva o nome do medicamento.",

    perguntasMedico = "Perguntas para o médico",
    perguntasIntro = "Marque o que quer perguntar. Leve a lista à consulta, ou envie-a a quem o acompanha.",
    limparLista = "Limpar a lista",

    perfil = "Perfil",
    oSeuNome = "O seu nome",
    oSeuNomeAjuda = "Aparece na saudação e na lista que mostra ao médico.",
    dataNascimento = "Data de nascimento",
    escolherData = "Escolher data",
    tamanhoTexto = "Tamanho do texto",
    normal = "Normal",
    grande = "Grande",
    muitoGrande = "Muito grande",
    exemploTexto = "Assim fica o texto.",
    idiomaTitulo = "Idioma",
    portugues = "Português",
    ingles = "English",
    sobre = "Sobre esta aplicação",
    sobreTexto = "Conteúdo educativo escrito pela equipa do projeto a partir das indicações da ULS " +
        "Barcelos/Esposende, e ainda em revisão por ela. Não substitui o conselho de um profissional de saúde." +
        "\n\nEsta aplicação não faz diagnósticos, não acede ao processo clínico e não recolhe dados pessoais " +
        "nem de saúde para nenhum servidor. O nome, a data de nascimento e a lista de medicação ficam " +
        "guardados só neste telemóvel. Não existem compras, publicidade nem ferramentas de rastreio." +
        "\n\nProjeto 50+10 · Grupo 14",
    versao = "Versão %s",
)

val TextosEN = Textos(
    idioma = "en",
    locale = "en-GB",

    inicio = "Home",
    aprender = "Learn",
    medicacao = "Medication",
    definicoes = "Settings",
    voltar = "Back",

    bomDia = "Good morning",
    boaTarde = "Good afternoon",
    boaNoite = "Good evening",
    tomasDeHoje = "Today's doses",
    proxima = "Next",
    nTemas = "%d topics",
    prepararConsulta = "Get ready for your appointment",
    proximaToma = "Next dose",
    amanha = "tomorrow",
    agora = "now",
    emMinutos = "in %d minutes",
    emHorasMinutos = "in %d h %d min",
    verMedicacao = "Open my medication",
    semMedicacaoTitulo = "No medication yet",
    semMedicacaoTexto = "Write the list once. Then just show it to the health professionals.",
    adicionarALista = "Add to my list",

    ajudaAgora = "Need help now?",
    ajudaAgoraTexto = "If life is in danger, call 112.",
    ligar112 = "Call 112",
    ligarSns24 = "Call SNS 24 (808 24 24 24)",
    partilhar = "Share",
    partilharLista = "Share the list",
    partilharPerguntas = "Share %d questions",
    partilharUmaPergunta = "Share 1 question",

    aprenderIntro = "Tap a topic. Every screen can be read aloud.",
    conteudoIndisponivel = "Content unavailable",
    conteudoIndisponivelTexto = "The content file did not load. Restart the app.",
    tema = "Topic",
    temaNaoEncontrado = "Topic not found",
    temaNaoEncontradoTexto = "This topic no longer exists in this version of the app.",
    ouvir = "Listen",
    ouvirTudo = "Listen to the whole topic",
    parar = "Stop",
    semVoz = "No voice on this phone",
    escolherPerguntas = "Choose questions for the appointment",

    aMinhaMedicacao = "My medication",
    listaVaziaTitulo = "The list is empty",
    listaVaziaTexto = "Write the medicine name, the dose and the times you take it. Then show the list at " +
        "appointments, at the emergency department and in hospital.",
    listaSoTelemovel = "The list stays on this phone only.",
    medicamentosNaLista = "%d medicines in this list.",
    umMedicamentoNaLista = "1 medicine in this list.",
    semHoras = "No times set — no reminders",
    adicionarMedicamento = "Add medicine",
    lembretesDesligados = "Reminders are turned off on this phone. Allow notifications in Android settings " +
        "to get dose alerts.",
    ligarNotificacoes = "Turn on notifications",
    duplicacaoTitulo = "You may be taking the same medicine twice.",
    mesmoPrincipioQue = "Same as %1\$s: both contain %2\$s.",
    igualA = "Same as %s: the same active ingredient.",
    sobreponiveisConselho = "Ask your doctor or pharmacist.",

    novoMedicamento = "New medicine",
    editar = "Edit",
    nome = "Name (active ingredient)",
    nomeAjuda = "E.g. Paracetamol. The box may carry a different name.",
    dose = "Dose",
    doseAjuda = "E.g. 1 tablet, 5 drops.",
    horas = "Times",
    horasAjuda = "Tap the times you take it. Each time gets a reminder.",
    manha = "Morning",
    almoco = "Lunch",
    jantar = "Dinner",
    deitar = "Bedtime",
    outraHora = "Other time",
    escolherHora = "Choose the time",
    removerHora = "Remove %s",
    ok = "OK",
    fotografia = "Photo of the box",
    fotografiaAjuda = "Start with the box. Then type the name and dose printed on it.",
    tirarFotografia = "Take a photo",
    comACamara = "with the camera",
    daGaleria = "From gallery",
    fotoQueJaTem = "a photo you already have",
    tirarOutra = "Take another",
    removerFotografia = "Remove photo",
    aGuardarFoto = "Saving the photo…",
    fotoFalhou = "That photo could not be opened. Please try again.",
    semCamara = "This phone has no camera app. Choose a photo from the gallery.",
    oQueDizACaixa = "What the box says",
    nota = "Note (optional)",
    notaAjuda = "E.g. on an empty stomach, at bedtime.",
    guardar = "Save",
    apagarMedicamento = "Delete medicine",
    apagarTitulo = "Delete %s?",
    apagarTexto = "The medicine and its dose reminders disappear from this list.",
    apagar = "Delete",
    cancelar = "Cancel",
    erroNome = "Write the medicine name.",

    perguntasMedico = "Questions for the doctor",
    perguntasIntro = "Tick what you want to ask. Take the list to the appointment, or send it to whoever goes with you.",
    limparLista = "Clear the list",

    perfil = "Profile",
    oSeuNome = "Your name",
    oSeuNomeAjuda = "Shown in the greeting and on the list you show the doctor.",
    dataNascimento = "Date of birth",
    escolherData = "Choose date",
    tamanhoTexto = "Text size",
    normal = "Normal",
    grande = "Large",
    muitoGrande = "Very large",
    exemploTexto = "This is how text will look.",
    idiomaTitulo = "Language",
    portugues = "Português",
    ingles = "English",
    sobre = "About this app",
    sobreTexto = "Educational content written by the project team from the guidance of ULS " +
        "Barcelos/Esposende, and still under review by them. It does not replace advice from a health " +
        "professional.\n\nThis app does not diagnose, has no access to the clinical record and sends no " +
        "personal or health data to any server. Your name, date of birth and medication list stay on this " +
        "phone only. There are no purchases, no advertising and no tracking tools." +
        "\n\nProjeto 50+10 · Group 14",
    versao = "Version %s",
)

/** Swapped by [AppVendanzaCare] when the language changes in Definições. */
val LocalTextos = staticCompositionLocalOf { TextosPT }

fun textosDe(idioma: String): Textos = if (idioma == "en") TextosEN else TextosPT
