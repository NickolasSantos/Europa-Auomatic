# Europa RPG — mod de RPG para Minecraft (Fabric 1.21.1)

Transforma o Minecraft em um RPG: **classes, subclasses, poderes únicos, níveis, missões, equipamentos lendários, minérios próprios e dungeons com chefe**.

## Requisitos

| | Versão |
|---|---|
| Minecraft | 1.21.1 |
| Fabric Loader | ≥ 0.16 |
| Fabric API | 0.116.x+1.21.1 |
| Java | 21 |

Funciona em single-player e em servidor (o mod precisa estar no cliente **e** no servidor).

## Como jogar

1. Ao entrar no mundo aparece no chat a escolha de classe (clicável) — ou use `/europa class`.
2. **[R]** usa a habilidade da classe; **[G]** usa o poder supremo da subclasse (as teclas podem ser trocadas em *Opções → Controles*).
3. Mate criaturas e conclua missões para ganhar XP de RPG. No **nível 10** você escolhe uma subclasse.
4. Construa um **Quadro de Missões** (8 tábuas + 1 livro) e clique com o direito para ver/aceitar/entregar missões.
5. Ache uma **Catacumba** (`/locate structure europa:catacomb`), pegue a chave no cofre, use-a no **Altar** e derrote o **Senhor da Catacumba**.

O HUD (canto superior esquerdo) mostra classe, nível, barra de XP, mana e recarga das habilidades.

## Classes e subclasses

| Classe | Habilidade [R] | Subclasse → Poder supremo [G] |
|---|---|---|
| **Guerreiro** — +vida, +armadura, +dano | Investida: avanço com golpe em área | **Berserker** → Fúria Sangrenta (Força II, Velocidade II, Pressa II, Regeneração) · **Paladino** → Escudo Divino (cura/protege aliados, queima mortos-vivos) |
| **Mago** — mana alta, regeneração 3/s | Míssil Arcano: raio mágico instantâneo | **Piromante** → Meteoro (explosão de fogo no ponto mirado, sem destruir blocos) · **Criomante** → Nevasca (congela/enfraquece em 8 blocos) |
| **Ladino** — velocidade, ataque rápido | Passo Sombrio: teleporta às costas do alvo + invisibilidade | **Assassino** → Marca da Morte (golpe massivo + Definhamento) · **Dançarino das Sombras** → Bomba de Fumaça (cega inimigos, você some) |
| **Patrulheiro** — vida, mobilidade | Saraivada: leque de 5 flechas | **Atirador** → Tiro Perfurante (atravessa todos em linha, 64 blocos) · **Mestre das Feras** → Chamado da Matilha (3 lobos espirituais por 60 s) |

- Nível máximo 50; XP por nível = `100 + 25·nível²`.
- Dano das habilidades escala +4% por nível. Atributos (vida, armadura, dano, velocidade) crescem com o nível conforme a classe.
- **Tomo do Renascimento** redefine classe/subclasse mantendo nível e missões.

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
| `/europa class [id]` | Escolher classe (`warrior`, `mage`, `rogue`, `ranger`) |
| `/europa subclass [id]` | Escolher subclasse (nível 10+) |
| `/europa stats` | Ficha do personagem |
| `/europa quest` · `accept/turnin/abandon <id>` | Missões |
| `/europa admin addxp\|setlevel\|reset <jogador> ...` | Administração (op) |

## Compilar

```bash
./gradlew build          # jar em build/libs/europa-rpg-<versão>.jar
./gradlew runClient      # cliente de desenvolvimento
./gradlew runServer      # servidor de desenvolvimento
./gradlew runGametest    # testes automatizados de jogo (servidor headless)
```

Os GameTests (`src/gametest`) cobrem: escolha de classe/subclasse e suas restrições, atributos, subida de nível, Tomo do Renascimento, execução e cooldown de todas as 16 habilidades, missões de abate e de entrega (inclusive entrega parcial) e invocação do chefe pelo altar.

Texturas, modelos, receitas, loot tables, tags e worldgen são gerados por `tools/gen_assets.py`; traduções (pt_br/en_us) por `tools/gen_lang.py`. Edite os scripts e rode-os novamente em vez de editar os JSONs à mão.

## Estrutura do código

```
src/main/java/com/europa/rpg/
  rpg/        classes, subclasses, dados do jogador (Data Attachment), progressão, UI de chat
  ability/    execução das habilidades
  quest/      definições e lógica das missões
  world/      estrutura Catacumba e injeção de minérios
  entity/     Senhor da Catacumba
  item/ block/ registry/ network/ command/ util/
src/client/java/com/europa/rpg/client/   teclas, HUD, renderer do chefe
```

## Limitações conhecidas

- Interface de missões e escolha de classe é por chat clicável (não há tela GUI dedicada).
- O chefe reutiliza o modelo do esqueleto wither (com textura própria e escala 1,6×).
- Texturas são pixel-art procedural simples — substitua os PNGs em `assets/europa/textures` por arte final.
