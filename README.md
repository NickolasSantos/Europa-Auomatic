# Europa RPG — mod de RPG para Minecraft (Fabric 1.21.1)

RPG medieval original: **32 classes em 5 categorias, 96 subclasses, 18 raças, 8 recursos de combate, árvore de talentos, 8 divindades com bênçãos, 12 profissões com 5 tiers, 7 raridades de equipamento com afixos, economia com moeda e preço dinâmico, 4 facções com reputação, missões e contratos, minérios próprios e dungeons com chefe.**

## Requisitos

| | Versão |
|---|---|
| Minecraft | 1.21.1 |
| Fabric Loader | ≥ 0.16 |
| Fabric API | 0.116.x+1.21.1 |
| Java | 21 |

Funciona em single-player e servidor (o mod precisa estar no cliente **e** no servidor).

## Modpack (CurseForge)

`build/Europa-RPG-<versão>.zip` é gerado por `python3 tools/build_modpack.py` (depois de `./gradlew build`) a partir de `modpack/modpack.json`. Importe na CurseForge em **Create Custom Profile → Import**.

| Grupo | Mods |
|---|---|
| Desempenho e shaders | Sodium, Lithium, Iris + Complementary Reimagined (ligado; **F6** liga/desliga) |
| Animações e modelos | Entity Model Features, Entity Texture Features, Fresh Animations |
| Interface | EMI, Jade, AppleSkin, Mod Menu |
| Mundo e ambiente | Terralith, YUNG's Better Dungeons/Mineshafts, Continuity, AmbientSounds, Sound Physics Remastered, Visuality, Falling Leaves |

Fabric Loader **0.20.1** (o Lithostitched, exigido pelo Terralith, usa um formato que o 0.18 não lê). Mods com ID exato na CurseForge são referenciados; os de licença livre sem ID exato (Iris, Cloth Config, YUNG's, Falling Leaves) vão embutidos na versão testada. Entity Culling ficou de fora (licença não permite redistribuir). O pacote remapeia as teclas do Iris que colidiam com as do mod (R recarregava shaders, K desligava shaders).

O conjunto foi testado num lançamento de produção (jar oficial do Minecraft + Fabric Loader + jars reais), com shaders ligados.

## Controles

| Tecla | Ação |
|---|---|
| **K** | Tela de personagem (Ficha, Raça, Talentos, Bênção, Profissões, Facções); sem classe, abre o seletor de classes |
| **R** | Habilidade da classe |
| **G** | Poder supremo da subclasse (nível 10) |
| **V** | Habilidade racial |

Os atalhos Q/E/F/X do documento de design não foram usados porque colidem com controles do Minecraft (soltar item, inventário, trocar mão, barras salvas). Todas as teclas são reconfiguráveis.

## Camadas do personagem

1. **Raça** (18): escala corporal, bônus e limitações, passiva e habilidade racial. Nenhuma é obrigatória para classe alguma.
2. **Classe** (32) com **recurso próprio**: Mana, Fúria (construída em combate, decai fora dele), Fé, Energia Interior, Energia Espiritual (cresce ao matar), Oportunidade (5 pontos, ganha 1 por acerto), Vínculo (cresce com companheiros ativos) e Cargas (5, recarregam devagar).
3. **Subclasse** (3 por classe) no nível 10, com poder supremo e bônus.
4. **Talentos**: 3 ramos × 5 tiers, 1 ponto por nível.
5. **Bênção** de uma divindade, com favor por oferendas num Santuário.
6. **Profissões** independentes, evoluídas pelo uso.
7. **Equipamento** com raridade e afixos; bônus de conjunto (Mithril e Adamantita completos).

### Classes

| Categoria | Classes |
|---|---|
| Marciais | Guerreiro, Bárbaro, Paladino, Samurai, Monge, Gladiador |
| Destreza e distância | Ladino, Patrulheiro, Besteiro, Atirador Arcano, Espadachim, Domador |
| Arcanas | Mago, Feiticeiro, Piromante, Criomante, Eletromante, Geomante, Ilusionista, Graviturgo |
| Espirituais | Clérigo, Druida, Necromante, Bruxo, Xamã, Bardo |
| Híbridas | Cavaleiro Dracônico, Arcano de Guerra, Artífice, Alquimista, Lâmina Arcana, Invocador |

Cada classe tem 3 subclasses (lista completa na tela K e em `content/ClassCatalog.java`). As habilidades são definidas por uma **DSL** (`ability/Spell.java`): alvo (próprio, mirado, área, linha, corrente), dano, efeitos, pulsos, movimento, invocações, marcas. **A descrição exibida ao jogador é gerada a partir dos mesmos números**, então nunca fica desatualizada.

Invocações (servos, lobos, golens) têm limite de 6 por dono e não sobrevivem a reinícios do servidor.

### Raças

Humano, Elfo, Anão, Orc, Tiefling, Draconato, Halfling, Feérico, Renascido, Vidrante, Miceliano, Umbral, Porcelânico, Abissal, Verdano, Horólogo, Tinteiro, Astrário. Exemplos: o Anão é menor (85%), ganha vida e armadura e tem Pressa abaixo de Y 40; o Umbral tem visão noturna e +15% de dano no escuro, mas fica fraco sob o sol; o Horólogo reduz recargas e pode **Retroceder** à posição de 5 segundos atrás.

### Divindades

Aureon (sol), Tharion (tempestades), Sylvara (natureza), Nymira (lua e percepção), Thalor (forja), Ysmera (marés), Morvath (abismo) e Vaelor (guerra), cada uma com 3 tiers de bênção.

### Profissões

Engenheiro de Autômatos, Tecelão de Ecos, Cozinheiro de Metamorfoses, Restaurador de Artefatos, Geólogo de Anomalias, Coletor de Essências, Jardineiro de Biomas, Domador de Fenômenos, Arqueólogo de Ruínas, Cartomante de Relíquias, Destilador de Memórias e Navegador Dimensional. Tiers: Aprendiz (1–9), Praticante (10–19), Especialista (20–29), Mestre (30–39), Lendário (40–50). Cada uma está ligada a uma atividade concreta do jogo (fabricar mecanismos, extrair sculk, cozinhar, forjar e reparar, minerar, abater, colher, pescar e criar, saquear baús de estruturas, encantar, preparar poções, viajar entre dimensões).

## Equipamentos

Raridades: Comum, Incomum, Raro, Épico, Lendário, Mítico, Divino. Cada tier acima de Comum adiciona um afixo — atributos (dano, vida, armadura, velocidade…) ou especiais (crítico, roubo de vida, poder de habilidade). A raridade é sorteada nos baús de todas as estruturas, na dungeon (com raridade mínima) e no chefe; a profissão Restaurador de Artefatos pode forjar obras-primas com raridade.

### Evolução de equipamentos

Armas ganham XP ao matar; armaduras, ao receber dano. Nível 1–5: cada nível adiciona um afixo, e os níveis 3 e 5 sobem um tier de raridade (até Mítico). Os equipamentos do mod mudam de textura em três fases — base, **Desperto** (runas douradas) e **Ascendido** (brilho e aura) — conforme o nível ou a raridade. Itens Épicos ou melhores brilham; o nome aparece na cor da raridade.

## Economia e facções

- Moeda: **Fragmento de Aster** (9 = 1 Prisma de Aster).
- **Balcão do Mercador**: vende materiais e equipamentos com raridade; mão vazia abre a loja. O preço de venda cai conforme a quantidade vendida recentemente e se recupera com o tempo (estado salvo no mundo). Compras retiram moeda de circulação.
- **Facções**: Observatório de Aster, Companhia dos Errantes, Conclave das Engrenagens e Círculo do Inominado. Reputação vem de contratos (no Quadro de Missões), chefes e XP de profissões afins. Ranks: Neutro → Amigável → Honrado → Reverenciado → Exaltado, que liberam itens e descontos na loja.

## Comandos

| Comando | Descrição |
|---|---|
| `/europa class\|subclass\|race\|talent\|patron <id>` | Alternativas em texto à tela K |
| `/europa quest` | Missões e contratos |
| `/europa shop` · `buy <oferta>` · `price` | Loja e cotação do item em mãos |
| `/europa stats` | Resumo do personagem |
| `/europa admin addxp\|setlevel\|profxp\|setfavor\|addrep\|rarity\|reset ...` | Administração (op) |

## Compilar e testar

```bash
./gradlew build          # jar em build/libs/
./gradlew runClient      # cliente de desenvolvimento
./gradlew runGametest    # 11 testes automatizados em servidor headless
```

Os GameTests cobrem: classes/subclasses e restrições, as habilidades de todas as 96 subclasses e das 18 raças, talentos, bênçãos, profissões, recursos, raridade, mercado com preço dinâmico e troco, facções e loja por rank, missões, altar do chefe e migração de saves antigos.

Texturas são pixel art procedural original (`tools/art.py`, sombreamento e contorno automáticos), retratos das classes por `tools/portraits.py`; modelos, receitas, loot e worldgen por `tools/gen_assets.py`; traduções pt_br/en_us por `tools/gen_lang.py` + `tools/lang_content.py`.

## Arquitetura

```
src/main/java/com/europa/rpg/
  content/     catálogo das 32 classes / 96 subclasses e habilidades (DSL)
  ability/     DSL de habilidades, habilidades artesanais, invocações, despachante
  rpg/         dados do jogador (Data Attachment), progressão, recursos, combate, ações
  race/        18 raças, passivas e raciais
  talent/      árvore de talentos
  blessing/    divindades e bênçãos
  profession/  12 profissões e ganchos de atividade
  economy/     moeda, mercado dinâmico, facções
  item/        raridade, afixos, função de loot, itens únicos
  quest/ world/ entity/ block/ registry/ network/ command/ mixin/ util/
src/client/java/com/europa/rpg/client/   teclas, HUD, tela de personagem, tooltips, renderers
```

O estado do jogador é um único registro persistente, que também é sincronizado inteiro com o cliente quando muda. O HUD recebe um pacote leve a cada 5 ticks. Ids antigos (profissões e divindades da 0.2) são migrados ao carregar.

## Ainda fora do escopo (planejado no documento, não implementado)

- Atributos distribuíveis (Potência, Precisão, Intelecto, Vigor, Percepção): conflitam com talentos e afixos como terceira camada de stats; a recomendação é fundi-los na árvore de talentos.
- Caminhos secundários (Runas, Alquimia, Magia Sombria…), missões de domínio por classe, títulos além do nível 50.
- Anéis, amuletos e relíquias exigem slots extras (Trinkets/Accessories); relíquias conscientes.
- Regiões e biomas próprios, novas masmorras e chefes, níveis de dificuldade, NPCs de facção, leilão entre jogadores, bestiário, atlas e diário.
- Balanceamento validado em jogo; PvP sem regras próprias.
- A interface de loja e missões ainda é pelo chat.
