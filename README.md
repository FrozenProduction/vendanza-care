# VendanzaCare

Aplicação Android de literacia em saúde e segurança do doente, feita para os utentes e cuidadores da
ULS de Barcelos/Esposende (ULSBE), no âmbito do Projeto 50+10 do 3.º ano de Engenharia de Sistemas
Informáticos (EST/UPCA).

> "Tecnologia que traduz a saúde em clareza, capacitando o utente para um cuidado mais seguro."

O mesmo produto responde a três unidades curriculares, com a mesma equipa de quatro pessoas:

| UC | O que fica neste repositório |
|---|---|
| Projeto Aplicado (PA) | requisitos com a ULSBE, arquitetura, coordenação, relatório e pitch |
| Programação de Dispositivos Móveis (PDM) | a aplicação Android, em `app/` |
| Inteligência Artificial (IA) | o serviço de IA, em `ai/` |

## O problema

Em Portugal, 61% das pessoas inquiridas no estudo de validação do HLS-EU-PT têm um nível de literacia em
saúde problemático ou inadequado, contra 49,2% na média de nove países europeus. Os grupos mais afetados
são os idosos, quem tem menos escolaridade e quem está desempregado. No hospital, isso aparece de três
formas: diagnósticos mal compreendidos, sinais de alarme ignorados e medicação mal tomada.

A app trata a parte que depende do utente: explicar em linguagem simples, com áudio, e dar-lhe ferramentas
para levar à consulta. Não diagnostica, não dá conselhos clínicos e não acede ao processo clínico. Também
não substitui o MyHSMM, a app institucional da ULS para marcações, pagamentos e documentos: esta camada é
a de literacia, e é complementar.

## O que a app faz

- 12 guias de saúde adaptados dos conteúdos da ULSBE, com leitura em voz alta no próprio telemóvel.
- Lista de medicação guardada só no aparelho, com fotografia da caixa, horas de toma e aviso quando dois
  medicamentos partilham o mesmo princípio ativo.
- Lembretes de toma, com "Já tomei" e "Daqui a 10 min" na própria notificação.
- Perguntas para levar à consulta, escolhidas de uma lista e partilhadas como texto.
- Contactos de urgência num toque: 112, SNS 24, Centro de Informação Antivenenos e os números da própria
  pessoa.
- Texto até ao dobro do tamanho, interface em português e inglês, e nenhum pedido de rede.

## Estrutura

```
app/               aplicação Android (PDM), Kotlin e Compose, offline
ai/                serviço de IA (IA), âmbito definido pela proposta do PA
docs/              os documentos que escrevemos
architecture.md    como a app está montada e porquê
```

O projeto Gradle está dentro de `app/`. No Android Studio abre-se essa pasta, a que tem o
`settings.gradle.kts`, e não a raiz do repositório, porque a raiz junta as três UC.

## Equipa

- Diogo Pereira, coordenação, requisitos com a ULSBE e arquitetura
- Bruno Paiva, aplicação móvel
- Tiago Fontes, inteligência artificial
- Guilherme Nogueira, qualidade, documentação, cartaz e pitch

Os números de aluno e os contactos do docente ficam fora do repositório.

## Estado

- 2026-09-22: equipa fechada e nome registado com o docente. Repositório criado.
- 2026-10-26: a ULSBE entregou os requisitos escritos e a app foi construída a partir deles.
- 2026-11-09: primeira apresentação à turma. A app passou a registar as tomas ("Já tomei"), ganhou o ecrã
  de contactos de urgência e o separador Aprender passou a Guias.
- Blocos 50+10: 14 a 19 de dezembro de 2026 e 5 a 9 de janeiro de 2027. Defesa na semana pedagógica
  complementar.

## Fora do repositório

Material de terceiros (os PDFs das aulas, o Plano de Ação Global da OMS, o artigo do HLS-EU-PT) e dados
pessoais não são publicados aqui. O repositório é público e só tem trabalho nosso.
