# Europa RPG — mod de RPG para Minecraft (Fabric 1.21.1)

RPG original para Minecraft: **12 classes, 24 subclasses, 36 poderes, árvore de talentos, patronos divinos com bênçãos, 6 profissões, missões, equipamentos lendários, minérios próprios e dungeons com chefe** — tudo numa tela de personagem própria (tecla **K**).

## Requisitos

| | Versão |
|---|---|
| Minecraft | 1.21.1 |
| Fabric Loader | ≥ 0.16 |
| Fabric API | 0.116.x+1.21.1 |
| Java | 21 |

Funciona em single-player e em servidor (o mod precisa estar no cliente **e** no servidor).

## Como jogar

1. Pressione **K** e escolha sua classe (passe o mouse para ver habilidades e subclasses).
2. **[R]** usa a habilidade da classe; **[G]** o poder supremo da subclasse (liberada no **nível 10**).
3. Cada nível dá 1 ponto de talento — gaste na aba **Talentos**.
4. Construa um **Santuário dos Patronos**, jure lealdade a um patrono na aba **Bênção** e faça oferendas para subir de tier.
5. Profissões sobem sozinhas conforme você minera, corta madeira, colhe, pesca, forja e prepara poções.
6. **Quadro de Missões** (8 tábuas + 1 livro) para missões; **Catacumbas** subterrâneas com chefe (`/locate structure europa:catacomb`).

## Classes e subclasses

| Classe | Habilidade [R] | Subclasses → Poder supremo [G] |
|---|---|---|
| **Guerreiro** | Investida | Berserker → Fúria Sangrenta · Paladino → Escudo Divino |
| **Mago** | Míssil Arcano | Piromante → Meteoro · Criomante → Nevasca |
| **Ladino** | Passo Sombrio | Assassino → Marca da Morte · Dançarino das Sombras → Bomba de Fumaça |
| **Patrulheiro** | Saraivada | Atirador → Tiro Perfurante · Mestre das Feras → Chamado da Matilha |
| **Clérigo** | Luz Restauradora | Hierofante → Santuário · Inquisidor → Julgamento |
| **Necromante** | Erguer Servo | Senhor das Legiões → Legião Profana · Hemomante → Pacto de Sangue |
| **Druida** | Laço de Espinhos | Metamorfo → Forma da Fera · Guardião do Bosque → Raízes Ancestrais |
| **Monge** | Palma Sísmica | Punho de Ferro → Cem Golpes · Andarilho do Vento → Passo do Vento |
| **Bardo** | Canção do Valor | Menestrel → Balada Restauradora · Dissonante → Acorde Dissonante |
| **Alquimista** | Frasco Volátil | Pestilento → Nuvem Tóxica · Transmutador → Elixir Perfeito |
| **Runeiro** | Runa Explosiva | Forjador de Runas → Armadura Rúnica · Arquiteto Arcano → Torre Rúnica |
| **Tecelão do Vazio** | Rasgo do Vazio | Andarilho Astral → Colapso Estelar · Devorador do Vazio → Fenda Devoradora |

Cada classe/subclasse tem atributos próprios (vida, armadura, dano, velocidade, mana e regeneração) que escalam com o nível (máx. 50). Dano das habilidades: +4% por nível + talentos. **Tomo do Renascimento** redefine classe, subclasse e talentos.

## Árvore de talentos

3 ramos × 5 tiers, com ranks. Cada tier exige o anterior; o tier 5 (capstone) exige 10 pontos no ramo. 55 ranks no total para 49 pontos — não dá para pegar tudo.

| Combate | Defesa | Arcano |
|---|---|---|
| Força Bruta (+dano) | Vigor (+vida) | Mente Profunda (+mana) |
| Fio Afiado (crítico 150%) | Pele de Ferro (+armadura) | Fluxo (+regen. de mana) |
| Poder Focado (+poder) | Inabalável (robustez, anti-repulsão) | Celeridade (−recarga) |
| Sede de Sangue (roubo de vida) | Segundo Fôlego (regeneração) | Eficiência (−custo de mana) |
| **Executor** (+30% em alvos < 30% vida) | **Última Resistência** (sobrevive a golpe fatal) | **Sobrecarga** (15% sem recarga) |

## Patronos e bênçãos

Jure lealdade num **Santuário dos Patronos** (craft: ouro, ametista, vela, tijolos de pedra). Oferendas aumentam o favor (tier 2 com 150, tier 3 com 500). Trocar de patrono zera o favor.

| Patrono | Tier 1 | Tier 2 | Tier 3 |
|---|---|---|---|
| **Solenne**, a Aurora Eterna | Regeneração ao sol | +4 vida | +30% dano em mortos-vivos |
| **Morvath**, o Abismo Silencioso | Visão noturna | Força à noite | +20% dano à noite |
| **Thalor**, o Forjador de Montanhas | Pressa | +2 armadura, +1 robustez | Resist. ao fogo, +20% mineração |
| **Ysmera**, a Senhora das Marés | Respiração aquática | Graça do golfinho + conduíte | +25% dano na água/chuva |
| **Kaelith**, a Tempestade Errante | Velocidade | Sem dano de queda | 15% raio em golpes |
| **Verdanya**, a Mãe das Raízes | +2 sorte | Recupera fome | Imune a veneno/definhamento |

## Profissões

Nível 1–50, independentes da classe, sobem pelo uso:

| Profissão | XP por | Bônus |
|---|---|---|
| Mineração | minérios (sem Toque Suave) | chance de minério duplo, mineração mais rápida |
| Lenhador | troncos | chance de madeira dupla |
| Agricultura | colheitas maduras | chance de colheita dupla |
| Pesca | peixes pescados | sorte de pesca |
| Ferraria | fabricar ferramentas/armas/armaduras | chance de "obra-prima" (Inquebrável) |
| Alquimia | retirar poções preparadas | chance de poção extra |

## Missões

10 missões encadeadas (pré-requisitos e nível mínimo), de matar ou entregar itens, com recompensas em XP e itens — terminando no chefe da Catacumba. Máximo de 3 ativas.

## Minérios e equipamentos

| Minério | Onde | Produz |
|---|---|---|
| Mithril (pedra/ardósia) | Overworld, Y −48 a 48 | Ferramentas e armadura de Mithril (entre ferro e diamante, encantabilidade alta) |
| Adamantita | Ardósia, abaixo de Y −20 (picareta de diamante) | Ferramentas e armadura de Adamantita (acima de diamante, robustez 3) |
| Pedra-Alma | Nether | Fragmentos de Alma (chave da catacumba, tomo, reparo de lendárias) |

**Armas lendárias** (dungeon/chefe/missões): Lâmina Flamejante (fogo), Presa Gélida (congela), Ceifadora de Almas (roubo de vida + Definhamento), Quebra-Tempestade (raios), Cajado Arcano (raio mágico que consome mana; +50% para magos).

## Dungeon: Catacumba

Estrutura subterrânea procedural: sala do chefe central (24×24) ligada por corredores a 4 salas com spawners e baús; uma delas é o **Cofre** (chave garantida + lendária) e outra tem um poço com escada até a superfície (marcado por lanternas de alma).
O **Senhor da Catacumba** (350 de vida, barra de chefe) usa Onda de Almas periodicamente e, abaixo de 50% de vida, entra em fúria e invoca esqueletos. Todos os jogadores num raio de 32 blocos recebem a recompensa.

## Comandos

| Comando | Descrição |
|---|---|
| `/europa class [id]` | Escolher classe (alternativa à tecla K) |
| `/europa subclass [id]` | Escolher subclasse (nível 10+) |
| `/europa talent <id>` · `/europa patron <id>` | Talentos e patronos (alternativa à tela) |
| `/europa stats` | Resumo do personagem |
| `/europa quest` · `accept/turnin/abandon <id>` | Missões |
| `/europa admin addxp\|setlevel\|profxp\|setfavor\|reset <jogador> ...` | Administração (op) |

## Compilar

```bash
./gradlew build          # jar em build/libs/europa-rpg-<versão>.jar
./gradlew runClient      # cliente de desenvolvimento
./gradlew runServer      # servidor de desenvolvimento
./gradlew runGametest    # testes automatizados de jogo (servidor headless)
```

Os GameTests (`src/gametest`) cobrem: escolha de classe/subclasse e restrições, atributos, níveis, Tomo do Renascimento, execução e recarga das 36 habilidades (todas as 24 subclasses), regras da árvore de talentos e seus efeitos (vida, recarga, Última Resistência), juramento/oferendas/tiers dos patronos, profissões, anti-exploit da alquimia, codec de sincronização, missões e o altar do chefe.

Texturas, modelos, receitas, loot tables, tags e worldgen são gerados por `tools/gen_assets.py`; traduções (pt_br/en_us) por `tools/gen_lang.py`. Edite os scripts e rode-os novamente em vez de editar os JSONs à mão.

## Estrutura do código

```
src/main/java/com/europa/rpg/
  rpg/        classes, subclasses (registros de dados), dados do jogador, progressão, combate
  ability/    registro das 36 habilidades e despachante
  talent/     árvore de talentos
  blessing/   patronos e bênçãos
  profession/ profissões
  mixin/      ganchos de dano, fabricação e alquimia
  quest/      definições e lógica das missões
  world/      estrutura Catacumba e injeção de minérios
  entity/     Senhor da Catacumba
  item/ block/ registry/ network/ command/ util/
src/client/java/com/europa/rpg/client/   teclas, HUD, tela de personagem, renderers
```

## Limitações conhecidas

- Missões ainda usam o chat clicável (classe, talentos, bênçãos e profissões têm tela própria).
- XP de Lenhador pode ser obtido colocando e quebrando troncos (sem rastreio de blocos colocados).
- Balanceamento ainda não foi validado em jogo real com várias pessoas.
- O chefe reutiliza o modelo do esqueleto wither (com textura própria e escala 1,6×).
- Texturas são pixel-art procedural simples — substitua os PNGs em `assets/europa/textures` por arte final.
