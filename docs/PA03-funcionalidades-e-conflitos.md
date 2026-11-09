# Proposta de sistema: o que entra, o que fica de fora e porquê

Escrito a partir do enunciado do PA03 (10 slides) e revisto depois da primeira apresentação. Serve para
responder à pergunta que o júri faz sempre: porque é que a app não faz isto ou aquilo.

## O que o enunciado pede

A proposta de sistema segue seis blocos: problema, utilizadores e contexto, duas ou três funcionalidades
principais, viabilidade e dados, componente de IA e perguntas para a ULSBE.

O enunciado traz dois exemplos de propostas de outras equipas, para mostrar o formato: uma app de adesão à
medicação na alta e uma app de preparação para consultas e exames. São dois problemas diferentes do nosso.
Servem de inspiração, não são uma lista de funcionalidades a copiar.

## As três funcionalidades da v1

| Funcionalidade | O que existe hoje | O que falta |
|---|---|---|
| Literacia: explicar em linguagem simples | 12 guias, 52 secções, cerca de 1 250 palavras, com leitura em voz alta | ilustrações e áudios da ULSBE (§12.1 da arquitetura) |
| Medicação: a lista que a pessoa leva a tudo | lista no telemóvel, fotografia da caixa, horas de toma, aviso de princípio ativo repetido, lembretes com "Já tomei" | partilha em imagem ou PDF, alarme com som e luz |
| Segurança: saber o que fazer primeiro | sinais de alarme nos guias, faixa do 112 em todos os ecrãs, contactos de urgência e os da própria pessoa, perguntas para a consulta | número do Gabinete do Cidadão, que a ULSBE não indicou |

Estas três respondem ao ponto 3 do enunciado. As restantes funcionalidades que o documento da ULSBE pede
aparecem como evolução, com o que as destranca escrito em `architecture.md` §12.

## O que fica de fora, e a razão

1. **Servidor.** Alerta ao cuidador, chat com o profissional de serviço, painel de indicadores e backoffice
   exigem um servidor, um acordo de subcontratação (art. 28.º do RGPD) e alojamento na União Europeia. A v1
   existe sem nada disso, e é isso que a torna lançável.
2. **Conteúdo que não temos.** Vídeo por medicamento, ilustrações e áudios têm de ser produzidos e
   validados pela ULSBE. Código para conteúdo que não existe é trabalho perdido.
3. **Âmbito.** O enunciado pede duas ou três funcionalidades principais. Acrescentar um calendário semanal,
   uma checklist por exame e uma contagem decrescente diluía as três que resolvem o problema.
4. **Conselho clínico.** Verificação de interações medicamentosas, ajuste de doses e respostas a sintomas
   ficam fora, sempre. A app encaminha para o SNS 24, o 112 ou o profissional.

## Função a função, contra os exemplos

| Do enunciado | Entra? | Onde está ou porque não |
|---|---|---|
| Calendário visual da medicação | não | A lista com horas e o cartão da próxima toma cobrem o uso real. Uma vista semanal acrescenta densidade a quem lê com esforço |
| Lembretes de toma | sim | AlarmManager, com notificação e os botões "Já tomei" e "Daqui a 10 min" |
| Vídeo curto por medicamento | não | É conteúdo, não código. Depende da ULSBE |
| Alerta ao cuidador quando falha uma toma | não | Exige servidor e o contacto de outra pessoa. Contra a regra de não recolher dados |
| Checklist por consulta ou exame | não | As instruções-padrão teriam de vir da ULSBE e mudam por serviço. Sem fonte, seria conteúdo inventado |
| Linguagem simples com áudio | sim | TextToSpeech em pt-PT, no dispositivo, offline |
| Contagem decrescente até uma data | não | O lembrete já cobre a hora da toma; a contagem serve exames, que não estão na v1 |
| Assistente de dúvidas | não | A app não tem chatbot. A camada de IA é a UC de IA, e as suas regras estão abaixo |

## A componente de IA

A app atual não tem assistente e não faz perguntas de texto livre. A camada de IA vive em `ai/` e tem o
âmbito da proposta do PA. Duas coisas já estão decididas, porque condicionam tudo o resto:

- **No dispositivo.** Uma fotografia de uma caixa de medicamentos é um dado de saúde. Tem de ser tratada no
  telemóvel e nunca enviada para fora, pela mesma razão que a lista de medicação não sai de lá.
- **Sugestão, não facto.** Se a IA ler o nome, a dose e o horário de uma embalagem, o resultado entra no
  formulário como proposta editável. Uma dose mal lida é exatamente o erro que a app existe para evitar.

E uma que continua em aberto: respostas "inteligentes" a perguntas que ninguém escreveu exigem um modelo de
linguagem. Ou corre no telemóvel, com o custo de memória e velocidade que isso tem em gama baixa, ou corre
fora, o que quebra a regra de não haver rede. Enquanto não houver uma resposta que satisfaça as duas, a app
responde com o conteúdo que a equipa escreveu e valida.

## Perguntas para a ULSBE

Respondidas pelo documento de requisitos de 26/10/2026:

- Plataformas: Android e iOS, com alternativa web/PWA. Confirmado.
- Conteúdos: fornecidos e validados pela ULSBE, adaptados pela equipa. Confirmado.
- Dados pessoais: a v1 não recolhe dados pessoais nem de saúde. Confirmado.
- Acessibilidade: DL 83/2018, com declaração. Confirmado.

Em aberto:

1. Canal e responsável do chat com o profissional de serviço. Sem isso, é um botão que não leva a lado
   nenhum.
2. Número do Gabinete do Cidadão, para o contacto num toque.
3. Versões mínimas de Android e iOS. A app assume Android 7.0 até a ULSBE dizer outra coisa.
4. Quem entrega a versão web, o backoffice e o painel de indicadores, e em que trimestre.
5. Quem assina a tabela de marcas para princípios ativos antes de qualquer teste com doentes.
