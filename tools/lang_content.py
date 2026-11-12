# -*- coding: utf-8 -*-
"""Conteúdo de idioma da expansão do documento mestre: (pt_br, en_us)."""

CATEGORIES = {
    "martial": ("Marciais", "Martial"), "dexterity": ("Destreza", "Dexterity"), "arcane": ("Arcanas", "Arcane"),
    "spiritual": ("Espirituais", "Spiritual"), "hybrid": ("Híbridas", "Hybrid"),
}

RESOURCES = {
    "mana": ("Mana", "Mana"), "fury": ("Fúria", "Fury"), "faith": ("Fé", "Faith"), "energy": ("Energia Interior", "Inner Energy"),
    "spirit": ("Energia Espiritual", "Spirit Energy"), "opportunity": ("Oportunidade", "Opportunity"), "bond": ("Vínculo", "Bond"),
    "charges": ("Cargas", "Charges"),
}

# id: (pt, en, desc_pt, desc_en)
CLASSES = {
    "warrior": ("Guerreiro", "Warrior", "Especialista em armas, técnicas e estilos de batalha.", "Master of weapons, techniques and battle styles."),
    "barbarian": ("Bárbaro", "Barbarian", "Força bruta e fúria: fica mais forte quanto mais luta.", "Brute force and fury: grows stronger the longer it fights."),
    "paladin": ("Paladino", "Paladin", "Guerreiro sagrado que une defesa, combate e poderes divinos.", "Holy warrior combining defense, combat and divine power."),
    "samurai": ("Samurai", "Samurai", "Espadas, posturas e golpes precisos.", "Swords, stances and precise strikes."),
    "monk": ("Monge", "Monk", "Técnicas corporais, disciplina e energia interior.", "Body techniques, discipline and inner energy."),
    "gladiator": ("Gladiador", "Gladiator", "Combate agressivo de arena e armas de impacto.", "Aggressive arena combat and impact weapons."),
    "rogue": ("Ladino", "Rogue", "Furtividade e ataques oportunos; acumula Oportunidade ao acertar.", "Stealth and opportune strikes; builds Opportunity on hits."),
    "ranger": ("Patrulheiro", "Ranger", "Arqueiro e explorador especializado em rastreamento.", "Archer and explorer specialized in tracking."),
    "crossbowman": ("Besteiro", "Crossbowman", "Bestas e disparos técnicos; usa Cargas que recarregam devagar.", "Crossbows and technical shots; uses slowly recharging Charges."),
    "arcane_gunner": ("Atirador Arcano", "Arcane Gunner", "Armas de projéteis encantadas e munições mágicas.", "Enchanted projectile weapons and magic ammunition."),
    "swordsman": ("Espadachim", "Swordsman", "Esgrima, velocidade e precisão.", "Fencing, speed and precision."),
    "tamer": ("Domador", "Tamer", "Vínculos com feras de combate; o Vínculo cresce com companheiros ativos.", "Bonds with combat beasts; Bond grows with active companions."),
    "mage": ("Mago", "Mage", "Estudioso das escolas de magia.", "Scholar of the schools of magic."),
    "sorcerer": ("Feiticeiro", "Sorcerer", "Magia inata e caótica que evolui com a experiência.", "Innate, chaotic magic that grows with experience."),
    "pyromancer": ("Piromante", "Pyromancer", "Fogo, explosões e combustão mágica.", "Fire, explosions and magical combustion."),
    "cryomancer": ("Criomante", "Cryomancer", "Gelo, congelamento e temperaturas extremas.", "Ice, freezing and extreme cold."),
    "electromancer": ("Eletromante", "Electromancer", "Eletricidade, raios e energia.", "Electricity, lightning and energy."),
    "geomancer": ("Geomante", "Geomancer", "Terra, pedras, minerais e estruturas.", "Earth, stone, minerals and structures."),
    "illusionist": ("Ilusionista", "Illusionist", "Ilusões, disfarces e distorções da percepção.", "Illusions, disguises and distorted perception."),
    "graviturge": ("Graviturgo", "Graviturge", "Peso, gravidade e atração.", "Weight, gravity and attraction."),
    "cleric": ("Clérigo", "Cleric", "Canaliza poderes divinos para curar e proteger.", "Channels divine power to heal and protect."),
    "druid": ("Druida", "Druid", "Controla a natureza e transforma o próprio corpo.", "Commands nature and reshapes its own body."),
    "necromancer": ("Necromante", "Necromancer", "Energias espirituais e mortos-vivos; ganha Espírito ao matar.", "Spirit energies and the undead; gains Spirit on kills."),
    "warlock": ("Bruxo", "Warlock", "Pactos com entidades sobrenaturais.", "Pacts with supernatural entities."),
    "shaman": ("Xamã", "Shaman", "Espíritos ancestrais e forças elementais.", "Ancestral spirits and elemental forces."),
    "bard": ("Bardo", "Bard", "Música e encantamentos que mudam o combate.", "Music and enchantments that shape combat."),
    "dragon_knight": ("Cavaleiro Dracônico", "Dragon Knight", "Combate armado com poderes de dragões.", "Armed combat empowered by dragons."),
    "war_arcanist": ("Arcano de Guerra", "War Arcanist", "Alterna armas e feitiços no mesmo combate.", "Weaves weapons and spells in the same fight."),
    "artificer": ("Artífice", "Artificer", "Dispositivos e equipamentos mágicos; usa Cargas.", "Magical devices and gear; uses Charges."),
    "alchemist": ("Alquimista", "Alchemist", "Poções, compostos e substâncias especiais.", "Potions, compounds and special substances."),
    "arcane_blade": ("Lâmina Arcana", "Arcane Blade", "Encanta armas e conjura magia através dos golpes.", "Enchants weapons and casts magic through strikes."),
    "summoner": ("Invocador", "Summoner", "Convoca criaturas mágicas para lutar ao seu lado.", "Calls magical creatures to fight at its side."),
}

SUBCLASSES = {
    "guardian": ("Guardião", "Guardian", "Escudo, proteção e resistência.", "Shield, protection and endurance."),
    "duelist": ("Duelista", "Duelist", "Esquiva, contra-ataques e precisão.", "Evasion, counters and precision."),
    "weapon_master": ("Mestre de Armas", "Weapon Master", "Domínio de diferentes armas e técnicas.", "Mastery of many weapons and techniques."),
    "furious": ("Furioso", "Furious", "Mais poder conforme mantém o ritmo de combate.", "More power the longer the fight goes."),
    "colossus": ("Colosso", "Colossus", "Resistência e golpes de impacto.", "Endurance and impact blows."),
    "predator": ("Predador", "Predator", "Mobilidade e perseguição de alvos.", "Mobility and target pursuit."),
    "templar": ("Templário", "Templar", "Defesa e proteção de aliados.", "Defense and protection of allies."),
    "inquisitor": ("Inquisidor", "Inquisitor", "Poderes sagrados ofensivos.", "Offensive holy powers."),
    "redeemer": ("Redentor", "Redeemer", "Cura, bênçãos e purificação.", "Healing, blessings and purification."),
    "ronin": ("Ronin", "Ronin", "Lâmina solitária de golpes devastadores.", "Lone blade of devastating cuts."),
    "blade_master": ("Mestre da Lâmina", "Blade Master", "Sequências de cortes rápidos.", "Rapid cut sequences."),
    "war_dancer": ("Dançarino de Guerra", "War Dancer", "Giros e movimento constante.", "Spins and constant motion."),
    "celestial_fist": ("Punho Celestial", "Celestial Fist", "Rajadas de golpes velozes.", "Flurries of swift blows."),
    "shadow_master": ("Mestre das Sombras", "Shadow Master", "Aparece atrás do inimigo e o cega.", "Appears behind the foe and blinds it."),
    "spirit_disciple": ("Discípulo Espiritual", "Spirit Disciple", "Equilíbrio interior e autocura.", "Inner balance and self-healing."),
    "champion": ("Campeão", "Champion", "Inspira aliados e intimida inimigos.", "Inspires allies and cows enemies."),
    "slasher": ("Retalhador", "Slasher", "Sangramentos e roubo de vida.", "Bleeds and life steal."),
    "arena_idol": ("Ídolo da Arena", "Arena Idol", "Sobrevive com o favor da multidão.", "Survives on the crowd's favor."),
    "assassin": ("Assassino", "Assassin", "Elimina alvos únicos com golpes letais.", "Eliminates single targets with lethal strikes."),
    "trickster": ("Trapaceiro", "Trickster", "Fumaça, cegueira e fuga.", "Smoke, blindness and escape."),
    "relic_thief": ("Ladrão de Relíquias", "Relic Thief", "Rouba recursos durante a luta.", "Steals resources mid-fight."),
    "marksman": ("Atirador", "Marksman", "Disparos que atravessam linhas inteiras.", "Shots that pierce whole lines."),
    "hunter": ("Caçador", "Hunter", "Marca presas para dano extra.", "Marks prey for extra damage."),
    "ranger_tamer": ("Domador", "Beast Tamer", "Um falcão espiritual o acompanha.", "A spirit hawk fights alongside."),
    "sharpshooter": ("Franco-atirador", "Sharpshooter", "Tiros extremamente longos e letais.", "Extremely long, lethal shots."),
    "gadgeteer": ("Engenhoqueiro", "Gadgeteer", "Armadilhas mecânicas.", "Mechanical traps."),
    "piercer": ("Perfurador", "Piercer", "Quebra armaduras e expõe fraquezas.", "Breaks armor and exposes weaknesses."),
    "musketeer": ("Mosqueteiro", "Musketeer", "Salvas poderosas que empurram.", "Powerful knockback volleys."),
    "rune_pistoleer": ("Pistoleiro Rúnico", "Rune Pistoleer", "Rajadas rápidas de balas rúnicas.", "Rapid bursts of rune rounds."),
    "artillerist": ("Artilheiro", "Artillerist", "Bombardeios de área.", "Area bombardments."),
    "fencer": ("Esgrimista", "Fencer", "Estocadas velozes em sequência.", "Rapid thrust sequences."),
    "dancing_blade": ("Espada Dançante", "Dancing Blade", "Mobilidade aérea e saltos.", "Aerial mobility and leaps."),
    "challenger": ("Desafiante", "Challenger", "Desafia um inimigo para um duelo.", "Challenges a foe to a duel."),
    "beastmaster": ("Mestre das Feras", "Beastmaster", "Comanda uma matilha.", "Commands a pack."),
    "creature_shepherd": ("Pastor de Criaturas", "Creature Shepherd", "Protege e cura companheiros.", "Protects and heals companions."),
    "alpha": ("Alfa", "Alpha", "Lidera e fortalece as feras.", "Leads and empowers the beasts."),
    "elementalist": ("Elementalista", "Elementalist", "Combina fogo e gelo em área.", "Combines fire and frost in areas."),
    "arcanist": ("Arcanista", "Arcanist", "Barragens de mísseis arcanos.", "Barrages of arcane missiles."),
    "battlemage": ("Mago de Batalha", "Battlemage", "Magia defensiva para lutar de perto.", "Defensive magic for close combat."),
    "draconic_heir": ("Herdeiro Dracônico", "Draconic Heir", "Sopro de dragão.", "Dragon breath."),
    "tempestarian": ("Tempestário", "Tempestarian", "Invoca tempestades de raios.", "Calls lightning storms."),
    "abyssal": ("Abissal", "Abyssal", "Poderes sombrios do abismo.", "Dark powers of the abyss."),
    "living_flame": ("Chama Viva", "Living Flame", "Envolto em fogo constante.", "Wrapped in constant flame."),
    "incendiary": ("Incendiário", "Incendiary", "Meteoros e destruição em área.", "Meteors and area destruction."),
    "ash_lord": ("Senhor das Cinzas", "Ash Lord", "Tempestades de cinzas que cegam.", "Blinding ash storms."),
    "glacial": ("Glacial", "Glacial", "Aprisiona inimigos no gelo.", "Imprisons foes in ice."),
    "permafrost": ("Permafrost", "Permafrost", "Campos gelados persistentes.", "Lingering frozen fields."),
    "blizzard_caller": ("Nevasca", "Blizzard", "Nevascas que congelam tudo ao redor.", "Blizzards that freeze everything around."),
    "storm": ("Tempestade", "Storm", "Raios repetidos numa área.", "Repeated lightning over an area."),
    "conductor": ("Condutor", "Conductor", "Raios que saltam entre inimigos.", "Lightning that jumps between foes."),
    "voltaic_arc": ("Arco Voltaico", "Voltaic Arc", "Teletransporte elétrico.", "Electric teleportation."),
    "earthquake": ("Terremoto", "Earthquake", "Tremores que derrubam inimigos.", "Tremors that topple foes."),
    "crystalline": ("Cristalino", "Crystalline", "Armadura de cristal.", "Crystal armor."),
    "stone_titan": ("Titã de Pedra", "Stone Titan", "Forma colossal e resistente.", "Colossal, sturdy form."),
    "mirror_master": ("Mestre dos Espelhos", "Mirror Master", "Inimigos atacam uns aos outros.", "Enemies attack each other."),
    "deceiver": ("Enganador", "Deceiver", "Finge a morte e desaparece.", "Feigns death and vanishes."),
    "nightmare_weaver": ("Tecelão de Pesadelos", "Nightmare Weaver", "Pesadelos que enfraquecem.", "Nightmares that weaken."),
    "black_well": ("Poço Negro", "Black Well", "Fenda que consome e devolve energia.", "A rift that consumes and returns energy."),
    "living_orbit": ("Órbita Viva", "Living Orbit", "Pedras orbitam e protegem.", "Orbiting stones that protect."),
    "collapse": ("Colapso", "Collapse", "Atrai e esmaga inimigos num ponto.", "Pulls and crushes foes into one point."),
    "priest": ("Sacerdote", "Priest", "Consagra o chão para curar.", "Consecrates the ground to heal."),
    "oracle": ("Oráculo", "Oracle", "Prevê ataques e expõe inimigos.", "Foresees attacks and exposes foes."),
    "exorcist": ("Exorcista", "Exorcist", "Bane mortos-vivos.", "Banishes the undead."),
    "shapeshifter": ("Metamorfo", "Shapeshifter", "Assume a forma de uma fera.", "Takes the form of a beast."),
    "forest_guardian": ("Guardião da Floresta", "Forest Guardian", "Raízes que prendem e curam.", "Roots that bind and heal."),
    "beast_circle": ("Círculo das Feras", "Beast Circle", "Estouro de feras espirituais.", "Stampede of spirit beasts."),
    "necro_summoner": ("Invocador", "Summoner", "Legiões de mortos-vivos.", "Legions of the undead."),
    "lich": ("Lich", "Lich", "Transcende a morte por instantes.", "Briefly transcends death."),
    "soul_reaper": ("Ceifador de Almas", "Soul Reaper", "Drena a vida em área.", "Drains life in an area."),
    "demonic_pact": ("Pactuário Demoníaco", "Demonic Pact", "Fogo infernal a custo da própria vida.", "Hellfire at the cost of one's own life."),
    "fey_pact": ("Pactuário Feérico", "Fey Pact", "Passos feéricos e confusão.", "Fey steps and confusion."),
    "ancient_pact": ("Pactuário Antigo", "Ancient Pact", "Sinais de entidades primordiais.", "Signs of primordial entities."),
    "ancestral_spirit": ("Espírito Ancestral", "Ancestral Spirit", "Os ancestrais fortalecem o grupo.", "Ancestors empower the party."),
    "totemic": ("Totêmico", "Totemic", "Totens que protegem a área.", "Totems that guard an area."),
    "medium": ("Médium", "Medium", "Elos espirituais que drenam vida.", "Spirit links that drain life."),
    "minstrel": ("Menestrel", "Minstrel", "Baladas que regeneram.", "Regenerating ballads."),
    "enchanter": ("Encantador", "Enchanter", "Notas que confundem inimigos.", "Notes that confuse foes."),
    "war_herald": ("Arauto de Guerra", "War Herald", "Hinos que levam o grupo à vitória.", "Anthems that drive the party to victory."),
    "igneous_dragon": ("Dracônico Ígneo", "Igneous Dragon", "Mergulhos flamejantes.", "Flaming dives."),
    "glacial_dragon": ("Dracônico Glacial", "Glacial Dragon", "Rugidos congelantes.", "Freezing roars."),
    "ancestral_dragon": ("Dracônico Ancestral", "Ancestral Dragon", "Desperta o dragão interior.", "Awakens the inner dragon."),
    "war_spellblade": ("Lâmina Arcana", "Spellblade", "Golpes de lâmina carregados de magia.", "Blade strikes charged with magic."),
    "battle_conjurer": ("Conjurador de Combate", "Battle Conjurer", "Conjura arsenais de projéteis.", "Conjures arsenals of projectiles."),
    "mystic_guardian": ("Guardião Místico", "Mystic Guardian", "Runas protetoras.", "Protective runes."),
    "arcane_smith": ("Ferreiro Arcano", "Arcane Smith", "Tempera e repara armas em combate.", "Tempers and repairs weapons in combat."),
    "combat_engineer": ("Engenheiro de Combate", "Combat Engineer", "Torres automáticas.", "Automatic turrets."),
    "field_alchemist": ("Alquimista de Campo", "Field Alchemist", "Remédios para o grupo.", "Remedies for the party."),
    "transmuter": ("Transmutador", "Transmuter", "Transmuta a própria carne.", "Transmutes one's own flesh."),
    "bomber": ("Bombardeiro", "Bomber", "Nuvens tóxicas e bombas.", "Toxic clouds and bombs."),
    "elixir_master": ("Mestre dos Elixires", "Elixir Master", "Elixires perfeitos.", "Perfect elixirs."),
    "rune_dancer": ("Dançarino Rúnico", "Rune Dancer", "Valsas rúnicas em movimento.", "Runic waltzes in motion."),
    "elemental_sword": ("Espada Elemental", "Elemental Sword", "Fogo e gelo na lâmina.", "Fire and frost on the blade."),
    "spectral_blade": ("Lâmina Espectral", "Spectral Blade", "Lâminas fantasmas à distância.", "Phantom blades at range."),
    "elemental_summoner": ("Invocador Elemental", "Elemental Summoner", "Invoca um guardião de pedra.", "Summons a stone guardian."),
    "dimensional_conjurer": ("Conjurador Dimensional", "Dimensional Conjurer", "Hordas de outras dimensões.", "Hordes from other dimensions."),
    "familiar_master": ("Mestre dos Familiares", "Familiar Master", "Matilhas de familiares.", "Packs of familiars."),
}

# Nomes das habilidades da DSL (descrição gerada automaticamente)
ABILITY_NAMES = {
    "bulwark": ("Baluarte", "Bulwark"), "lightning_riposte": ("Contra-ataque Relâmpago", "Lightning Riposte"),
    "arsenal_storm": ("Tempestade de Armas", "Arsenal Storm"), "savage_cleave": ("Golpe Selvagem", "Savage Cleave"),
    "earthshatter": ("Estilhaçar a Terra", "Earthshatter"), "hunt": ("Caçada", "The Hunt"), "holy_strike": ("Golpe Sagrado", "Holy Strike"),
    "purification": ("Purificação", "Purification"), "iaijutsu": ("Iaijutsu", "Iaijutsu"), "wandering_blade": ("Lâmina Errante", "Wandering Blade"),
    "thousand_cuts": ("Mil Cortes", "Thousand Cuts"), "shadow_palm": ("Palma das Sombras", "Shadow Palm"), "inner_peace": ("Paz Interior", "Inner Peace"),
    "arena_strike": ("Golpe da Arena", "Arena Strike"), "champions_roar": ("Rugido do Campeão", "Champion's Roar"),
    "bleeding_frenzy": ("Frenesi Sangrento", "Bleeding Frenzy"), "crowd_favor": ("Favor da Multidão", "Crowd's Favor"),
    "blade_whirl": ("Redemoinho de Lâminas", "Blade Whirl"), "pilfer": ("Pilhagem", "Pilfer"), "hunters_mark": ("Marca do Caçador", "Hunter's Mark"),
    "spirit_hawk": ("Falcão Espiritual", "Spirit Hawk"), "bolt_barrage": ("Rajada de Virotes", "Bolt Barrage"), "deadeye": ("Olho Mortal", "Deadeye"),
    "clockwork_trap": ("Armadilha de Engrenagem", "Clockwork Trap"), "armor_breaker": ("Quebra-Armadura", "Armor Breaker"),
    "arcane_shot": ("Tiro Arcano", "Arcane Shot"), "volley_fire": ("Salva de Mosquete", "Volley Fire"), "rune_rounds": ("Balas Rúnicas", "Rune Rounds"),
    "bombardment": ("Bombardeio", "Bombardment"), "lunge": ("Estocada", "Lunge"), "flurry_thrust": ("Estocadas Velozes", "Flurry Thrust"),
    "duel_challenge": ("Desafio", "Duel Challenge"), "summon_companion": ("Chamar Companheiro", "Summon Companion"),
    "herd_blessing": ("Bênção do Rebanho", "Herd Blessing"), "alpha_howl": ("Uivo do Alfa", "Alpha Howl"),
    "elemental_burst": ("Explosão Elemental", "Elemental Burst"), "arcane_barrage": ("Barragem Arcana", "Arcane Barrage"),
    "spellblade_aura": ("Aura Encantada", "Spellblade Aura"), "chaos_bolt": ("Raio do Caos", "Chaos Bolt"), "dragon_breath": ("Sopro Dracônico", "Dragon Breath"),
    "storm_call": ("Chamado da Tempestade", "Storm Call"), "abyss_gaze": ("Olhar do Abismo", "Abyss Gaze"), "fireball": ("Bola de Fogo", "Fireball"),
    "flame_cloak": ("Manto de Chamas", "Flame Cloak"), "ash_storm": ("Tempestade de Cinzas", "Ash Storm"), "frost_bolt": ("Raio Gélido", "Frost Bolt"),
    "ice_prison": ("Prisão de Gelo", "Ice Prison"), "permafrost_field": ("Campo de Permafrost", "Permafrost Field"), "spark": ("Centelha", "Spark"),
    "thunderstorm": ("Trovoada", "Thunderstorm"), "chain_lightning": ("Raio em Cadeia", "Chain Lightning"), "voltaic_dash": ("Avanço Voltaico", "Voltaic Dash"),
    "stone_spike": ("Espinho de Pedra", "Stone Spike"), "quake": ("Terremoto", "Quake"), "crystal_armor": ("Armadura de Cristal", "Crystal Armor"),
    "titan_form": ("Forma de Titã", "Titan Form"), "mirror_image": ("Imagem Espelhada", "Mirror Image"), "hall_of_mirrors": ("Salão dos Espelhos", "Hall of Mirrors"),
    "false_death": ("Morte Fingida", "False Death"), "nightmare": ("Pesadelo", "Nightmare"), "orbiting_stones": ("Pedras em Órbita", "Orbiting Stones"),
    "foresight": ("Presságio", "Foresight"), "banish": ("Banir", "Banish"), "spirit_stampede": ("Estouro Espiritual", "Spirit Stampede"),
    "lich_form": ("Forma de Lich", "Lich Form"), "eldritch_blast": ("Rajada Sobrenatural", "Eldritch Blast"), "hellfire": ("Fogo Infernal", "Hellfire"),
    "fey_step": ("Passo Feérico", "Fey Step"), "elder_sign": ("Sinal Ancestral", "Elder Sign"), "spirit_bolt": ("Raio Espiritual", "Spirit Bolt"),
    "ancestors_call": ("Chamado Ancestral", "Ancestors' Call"), "war_totem": ("Totem de Guerra", "War Totem"), "spirit_link": ("Elo Espiritual", "Spirit Link"),
    "war_anthem": ("Hino de Guerra", "War Anthem"), "dragon_claw": ("Garra Dracônica", "Dragon Claw"), "inferno_dive": ("Mergulho Ígneo", "Inferno Dive"),
    "frost_roar": ("Rugido Glacial", "Frost Roar"), "dragon_ascendance": ("Ascensão Dracônica", "Dragon Ascendance"), "runic_strike": ("Golpe Rúnico", "Runic Strike"),
    "arcane_cleave": ("Clivagem Arcana", "Arcane Cleave"), "conjured_arsenal": ("Arsenal Conjurado", "Conjured Arsenal"),
    "tempered_arms": ("Armas Temperadas", "Tempered Arms"), "field_remedy": ("Remédio de Campo", "Field Remedy"),
    "transmute_flesh": ("Transmutar Carne", "Transmute Flesh"), "enchanted_edge": ("Fio Encantado", "Enchanted Edge"), "rune_waltz": ("Valsa Rúnica", "Rune Waltz"),
    "elemental_edge": ("Fio Elemental", "Elemental Edge"), "phantom_blades": ("Lâminas Fantasmas", "Phantom Blades"),
    "summon_familiar": ("Invocar Familiar", "Summon Familiar"), "elemental_guardian": ("Guardião Elemental", "Elemental Guardian"),
    "rift_horde": ("Horda Dimensional", "Rift Horde"), "familiar_pack": ("Matilha de Familiares", "Familiar Pack"),
    # raciais
    "determination": ("Determinação", "Determination"), "elven_grace": ("Graça Élfica", "Elven Grace"), "stoneskin": ("Pele de Pedra", "Stoneskin"),
    "war_cry": ("Grito de Guerra", "War Cry"), "infernal_rebuke": ("Repreensão Infernal", "Infernal Rebuke"),
    "breath_of_ancestors": ("Sopro dos Ancestrais", "Breath of Ancestors"), "lucky_escape": ("Fuga de Sorte", "Lucky Escape"),
    "fey_blink": ("Lampejo Feérico", "Fey Blink"), "deathless": ("Imorredouro", "Deathless"), "prismatic_flash": ("Clarão Prismático", "Prismatic Flash"),
    "spore_cloud": ("Nuvem de Esporos", "Spore Cloud"), "shadow_meld": ("Fusão Sombria", "Shadow Meld"), "glaze": ("Vitrificar", "Glaze"),
    "tidal_surge": ("Onda da Maré", "Tidal Surge"), "photosynthesis": ("Fotossíntese", "Photosynthesis"), "rewind": ("Retroceder", "Rewind"),
    "ink_splash": ("Jato de Tinta", "Ink Splash"), "starfall": ("Chuva Estelar", "Starfall"),
}

FX = {
    "blink": ("Teleporta até %s blocos à frente", "Teleports up to %s blocks ahead"),
    "dash": ("Avança rapidamente", "Dashes forward"),
    "behind": ("Surge atrás do alvo", "Appears behind the target"),
    "dash_to": ("Salta até o alvo", "Leaps to the target"),
    "self": ("Você recebe %s", "You gain %s"),
    "heal_self": ("Cura %s de vida", "Heals %s health"),
    "restore": ("Recupera %s de recurso", "Restores %s resource"),
    "self_damage": ("Custa %s de vida", "Costs %s health"),
    "allies": ("Aliados e invocações em %s blocos: ", "Allies and summons within %s blocks: "),
    "heal": ("cura %s", "heal %s"),
    "cleanse": ("remove venenos e fraquezas", "cleanses poisons and weakness"),
    "summon.servant": ("Invoca %s servo(s) por %ss", "Summons %s servant(s) for %ss"),
    "summon.wolf": ("Invoca %s lobo(s) espiritual(is) por %ss", "Summons %s spirit wolf(s) for %ss"),
    "summon.golem": ("Invoca %s guardião(ões) de pedra por %ss", "Summons %s stone guardian(s) for %ss"),
    "arrows": ("Dispara %s projéteis de %s de dano", "Fires %s projectiles dealing %s"),
    "taunt": ("Provoca inimigos em %s blocos", "Taunts enemies within %s blocks"),
    "vanish": ("Inimigos em %s blocos perdem você de vista", "Enemies within %s blocks lose track of you"),
    "damage_physical": ("%s de dano físico", "%s physical damage"),
    "damage_magic": ("%s de dano mágico", "%s magic damage"),
    "vs_undead": ("dobrado contra mortos-vivos", "doubled vs undead"),
    "root": ("prende por %ss", "roots for %ss"),
    "ignite": ("incendeia por %ss", "ignites for %ss"),
    "freeze": ("congela", "freezes"),
    "knockback": ("repele", "knocks back"),
    "knockup": ("arremessa para cima", "launches upward"),
    "pull": ("puxa para o centro", "pulls inward"),
    "mark": ("marca (+%s%% de dano recebido por %ss)", "marks (+%s%% damage taken for %ss)"),
    "drain": ("cura %s%% do dano causado", "heals %s%% of damage dealt"),
    "lightning": ("invoca raios", "calls lightning"),
    "confuse": ("inimigos atacam uns aos outros", "enemies attack each other"),
    "target": ("No alvo (até %s blocos): ", "On target (up to %s blocks): "),
    "around": ("Em %s blocos ao seu redor: ", "Within %s blocks around you: "),
    "area_at": ("Em área de %s blocos no ponto mirado (até %s): ", "In a %s-block area at the aimed spot (up to %s): "),
    "line": ("Em linha reta (%s blocos): ", "In a straight line (%s blocks): "),
    "chain": ("Salta entre até %s inimigos: ", "Jumps between up to %s enemies: "),
    "pulses": (" — %s vezes, a cada %ss", " — %s times, every %ss"),
    "delay": (" — após %ss", " — after %ss"),
    "pilfer": ("rouba 1–3 Fragmentos de Aster", "steals 1–3 Aster Fragments"),
    "chaos": ("efeito aleatório: fogo, lentidão ou fraqueza", "random effect: fire, slowness or weakness"),
    "repair": ("Repara 25% da arma em mãos", "Repairs 25% of the held weapon"),
    "rewind": ("Volta à posição de 5 segundos atrás", "Returns to where you were 5 seconds ago"),
}

# id: (pt, en, desc_pt, desc_en, passive_pt, passive_en)
RACES = {
    "human": ("Humano", "Human", "Adaptáveis e ambiciosos.", "Adaptable and ambitious.", "+10% de XP.", "+10% XP."),
    "elf": ("Elfo", "Elf", "Ágeis e perceptivos.", "Agile and perceptive.", "Velocidade e crítico; menos vida.", "Speed and crit; less health."),
    "dwarf": ("Anão", "Dwarf", "Robustos filhos da pedra.", "Sturdy children of stone.", "Pressa abaixo de Y 40.", "Haste below Y 40."),
    "orc": ("Orc", "Orc", "Força bruta e resistência.", "Brute strength and endurance.", "Mais dano e vida; menos mana.", "More damage and health; less mana."),
    "tiefling": ("Tiefling", "Tiefling", "Herdeiros de linhagens infernais.", "Heirs of infernal bloodlines.", "Imunidade a fogo.", "Fire immunity."),
    "dragonborn": ("Draconato", "Dragonborn", "Escamas e sangue de dragão.", "Scales and dragon blood.", "Mais vida e armadura; mais lento.", "More health and armor; slower."),
    "halfling": ("Halfling", "Halfling", "Pequenos, velozes e sortudos.", "Small, quick and lucky.", "Sorte e velocidade; menos vida.", "Luck and speed; less health."),
    "feyborn": ("Feérico", "Feyborn", "Tocados pela magia das fadas.", "Touched by fey magic.", "Mais mana e regeneração; menos armadura.", "More mana and regen; less armor."),
    "reborn": ("Renascido", "Reborn", "Voltaram do além.", "Returned from beyond.", "Imunes a veneno e fome.", "Immune to poison and hunger."),
    "vitrean": ("Vidrante", "Vitrean", "Seres de vidro vivo.", "Beings of living glass.", "Crítico e mana; frágeis (menos armadura).", "Crit and mana; fragile (less armor)."),
    "mycelian": ("Miceliano", "Mycelian", "Povo-fungo das profundezas.", "Fungal folk of the depths.", "Regenera no escuro; fraco sob o sol.", "Regenerates in darkness; weak in sunlight."),
    "umbral": ("Umbral", "Umbral", "Nascidos da sombra.", "Born of shadow.", "Visão noturna; +15% de dano no escuro; fraco sob o sol.", "Night vision; +15% damage in darkness; weak in sunlight."),
    "porcelain": ("Porcelânico", "Porcelain", "Corpos de porcelana encantada.", "Bodies of enchanted porcelain.", "Robustez e mana; menos vida.", "Toughness and mana; less health."),
    "abyssal": ("Abissal", "Abyssal", "Vindos das fossas oceânicas.", "From the ocean trenches.", "Respiração aquática e nado veloz.", "Water breathing and swift swimming."),
    "verdan": ("Verdano", "Verdan", "Gente-planta.", "Plant folk.", "Regenera ao sol; +25% de dano de fogo recebido.", "Regenerates in sunlight; +25% fire damage taken."),
    "horologist": ("Horólogo", "Horologist", "Engrenagens vivas que dobram o tempo.", "Living clockwork that bends time.", "-8% de recarga; mais lento.", "-8% cooldowns; slower."),
    "inkling": ("Tinteiro", "Inkling", "Seres de tinta viva.", "Beings of living ink.", "Mais mana; menos armadura.", "More mana; less armor."),
    "astrarian": ("Astrário", "Astrarian", "Filhos das estrelas.", "Children of the stars.", "+5% de poder e regeneração de recurso; menos vida.", "+5% power and resource regen; less health."),
}

TRAITS = {
    "scale": ("Tamanho: %s", "Size: %s"), "health": ("%s de vida", "%s health"), "armor": ("%s de armadura", "%s armor"),
    "toughness": ("%s de robustez", "%s toughness"), "damage": ("%s de dano", "%s damage"), "speed": ("%s de velocidade", "%s speed"),
    "luck": ("%s de sorte", "%s luck"), "mana": ("%s de mana", "%s mana"), "mana_regen": ("%s de regeneração", "%s regen"),
    "crit": ("%s de crítico", "%s crit"), "power": ("%s de poder", "%s power"), "cooldown": ("%s de redução de recarga", "%s cooldown reduction"),
    "xp": ("%s de experiência", "%s experience"),
}

PROFESSIONS = {
    "automaton_engineer": ("Engenheiro de Autômatos", "Automaton Engineer", "%s de chance de fabricar um mecanismo extra.", "%s chance to craft an extra mechanism."),
    "echo_weaver": ("Tecelão de Ecos", "Echo Weaver", "%s de chance de achar um Fragmento de Eco no sculk.", "%s chance to find an Echo Shard in sculk."),
    "metamorph_cook": ("Cozinheiro de Metamorfoses", "Metamorph Cook", "%s de chance de porção extra.", "%s chance of an extra serving."),
    "artifact_restorer": ("Restaurador de Artefatos", "Artifact Restorer", "%s de chance de obra-prima (Inquebrável + raridade).", "%s chance of a masterwork (Unbreaking + rarity)."),
    "anomaly_geologist": ("Geólogo de Anomalias", "Anomaly Geologist", "%s de chance de minério duplo; mineração mais rápida.", "%s chance of double ore; faster mining."),
    "essence_collector": ("Coletor de Essências", "Essence Collector", "%s de chance de Fragmentos de Aster ao abater.", "%s chance of Aster Fragments on kills."),
    "biome_gardener": ("Jardineiro de Biomas", "Biome Gardener", "%s de chance de colheita/madeira dupla.", "%s chance of double harvest/logs."),
    "phenomenon_tamer": ("Domador de Fenômenos", "Phenomenon Tamer", "%s de sorte (pesca e saques).", "%s luck (fishing and loot)."),
    "ruin_archaeologist": ("Arqueólogo de Ruínas", "Ruin Archaeologist", "%s de chance de esconderijo de fragmentos em baús.", "%s chance of a fragment cache in chests."),
    "relic_cartomancer": ("Cartomante de Relíquias", "Relic Cartomancer", "%s de chance de reaver lápis-lazúli ao encantar.", "%s chance to refund lapis when enchanting."),
    "memory_distiller": ("Destilador de Memórias", "Memory Distiller", "%s de chance de poção extra.", "%s chance of an extra potion."),
    "dimensional_navigator": ("Navegador Dimensional", "Dimensional Navigator", "%s de velocidade de viagem.", "%s travel speed."),
}
PROFESSION_TIERS = [("Aprendiz", "Apprentice"), ("Praticante", "Practitioner"), ("Especialista", "Specialist"), ("Mestre", "Master"), ("Lendário", "Legendary")]

PATRONS = {
    "aureon": ("Aureon", "o Sol que Restaura", "the Restoring Sun",
               ("Regeneração sob a luz do sol.", "Regeneration under sunlight."), ("+4 de vida máxima.", "+4 max health."),
               ("+30% de dano contra mortos-vivos.", "+30% damage against the undead.")),
    "tharion": ("Tharion", "Senhor das Tempestades", "Lord of Storms",
                ("Velocidade permanente.", "Permanent Speed."), ("Imunidade a dano de queda.", "Immunity to fall damage."),
                ("15% de chance de raio em golpes corpo a corpo.", "15% chance of lightning on melee hits.")),
    "sylvara": ("Sylvara", "Mãe da Vida e das Criaturas", "Mother of Life and Creatures",
                ("+2 de sorte.", "+2 luck."), ("Recupera fome a cada 30 s.", "Restores hunger every 30 s."),
                ("Imunidade a veneno e definhamento.", "Immunity to poison and wither.")),
    "nymira": ("Nymira", "a Lua das Ilusões", "the Moon of Illusions",
               ("+5% de chance de crítico (percepção).", "+5% crit chance (perception)."),
               ("Invisível ao se agachar à noite.", "Invisible while sneaking at night."),
               ("Revela monstros num raio de 16 blocos.", "Reveals monsters within 16 blocks.")),
    "thalor": ("Thalor", "o Forjador de Montanhas", "the Mountain Forger",
               ("Pressa permanente.", "Permanent Haste."), ("+2 de armadura e +1 de robustez.", "+2 armor and +1 toughness."),
               ("Resistência ao fogo e +20% de velocidade de mineração.", "Fire resistance and +20% mining speed.")),
    "ysmera": ("Ysmera", "a Senhora das Marés", "the Lady of Tides",
               ("Respiração aquática.", "Water breathing."), ("Graça do golfinho e poder do conduíte na água.", "Dolphin's grace and conduit power in water."),
               ("+25% de dano na água ou na chuva.", "+25% damage in water or rain.")),
    "morvath": ("Morvath", "o Abismo Silencioso", "the Silent Abyss",
                ("Visão noturna permanente.", "Permanent night vision."), ("Força à noite.", "Strength at night."),
                ("+20% de dano à noite.", "+20% damage at night.")),
    "vaelor": ("Vaelor", "o Juramento da Guerra", "the Oath of War",
               ("+1 de dano corpo a corpo.", "+1 melee damage."), ("Força quando abaixo de 50% de vida.", "Strength while below 50% health."),
               ("+10% de chance de crítico.", "+10% crit chance.")),
}

FACTIONS = {
    "observatory": ("Observatório de Aster", "Aster Observatory", "Pesquisadores e estudiosos. Reputação: arqueologia, navegação, cartomancia e contratos.",
                    "Researchers and scholars. Reputation: archaeology, navigation, cartomancy and contracts."),
    "wanderers": ("Companhia dos Errantes", "Wanderers' Company", "Exploradores e aventureiros. Reputação: chefes, ruínas, geologia, jardinagem e contratos.",
                  "Explorers and adventurers. Reputation: bosses, ruins, geology, gardening and contracts."),
    "gears": ("Conclave das Engrenagens", "Conclave of Gears", "Inventores e artífices. Reputação: autômatos, restauração, culinária e contratos.",
              "Inventors and artificers. Reputation: automatons, restoration, cooking and contracts."),
    "nameless": ("Círculo do Inominado", "Circle of the Nameless", "Estudiosos de mistérios proibidos. Reputação: essências, ecos, destilação e contratos.",
                 "Scholars of forbidden mysteries. Reputation: essences, echoes, distilling and contracts."),
}
FACTION_RANKS = [("Neutro", "Neutral"), ("Amigável", "Friendly"), ("Honrado", "Honored"), ("Reverenciado", "Revered"), ("Exaltado", "Exalted")]

RARITIES = [("common", "Comum", "Common"), ("uncommon", "Incomum", "Uncommon"), ("rare", "Raro", "Rare"), ("epic", "Épico", "Epic"),
            ("legendary", "Lendário", "Legendary"), ("mythic", "Mítico", "Mythic"), ("divine", "Divino", "Divine")]

QUESTS = {
    "star_charts": ("Cartas Estelares", "Star Charts", "Entregue 16 fragmentos de ametista ao Observatório.", "Deliver 16 amethyst shards to the Observatory."),
    "far_horizons": ("Horizontes Distantes", "Far Horizons", "Entregue 6 pérolas do Ender.", "Deliver 6 ender pearls."),
    "road_wardens": ("Guardiões da Estrada", "Road Wardens", "Mate 8 saqueadores.", "Kill 8 pillagers."),
    "cave_sweep": ("Varredura de Cavernas", "Cave Sweep", "Mate 12 aranhas de caverna.", "Kill 12 cave spiders."),
    "spare_parts": ("Peças de Reposição", "Spare Parts", "Entregue 48 de redstone ao Conclave.", "Deliver 48 redstone to the Conclave."),
    "copper_contract": ("Contrato de Cobre", "Copper Contract", "Entregue 32 lingotes de cobre.", "Deliver 32 copper ingots."),
    "soul_tithe": ("Dízimo de Almas", "Soul Tithe", "Entregue 8 Fragmentos de Alma ao Círculo.", "Deliver 8 Soul Shards to the Circle."),
    "night_rites": ("Ritos Noturnos", "Night Rites", "Mate 5 bruxas.", "Kill 5 witches."),
}

MISC = {
    "item.europa.aster_fragment": ("Fragmento de Aster", "Aster Fragment"),
    "item.europa.aster_prism": ("Prisma de Aster", "Aster Prism"),
    "block.europa.merchant_counter": ("Balcão do Mercador", "Merchant Counter"),
    "key.europa.racial": ("Habilidade racial", "Racial ability"),
    "rpg.europa.screen.tab.race": ("Raça", "Race"),
    "rpg.europa.screen.tab.factions": ("Facções", "Factions"),
    "rpg.europa.screen.resource": ("Recurso: ", "Resource: "),
    "rpg.europa.screen.race_label": ("Raça: ", "Race: "),
    "rpg.europa.screen.passive": ("Passiva: ", "Passive: "),
    "rpg.europa.racial_label": ("Racial [V]: ", "Racial [V]: "),
    "rpg.europa.choose_race": ("Escolha sua raça (definitiva até usar um Tomo do Renascimento):", "Choose your race (permanent until a Tome of Rebirth):"),
    "rpg.europa.race_locked": ("Raça escolhida. Um Tomo do Renascimento permite trocar.", "Race chosen. A Tome of Rebirth allows changing it."),
    "rpg.europa.race_chosen": ("Você agora pertence à raça %s.", "You are now of the %s race."),
    "rpg.europa.unknown_race": ("Raça desconhecida.", "Unknown race."),
    "rpg.europa.already_race": ("Você já escolheu uma raça.", "You already chose a race."),
    "rpg.europa.no_race": ("Escolha uma raça primeiro (tecla K → Raça).", "Choose a race first (K → Race)."),
    "rpg.europa.hud.no_race": ("Sem raça — tecla K", "No race — press K"),
    "rpg.europa.no_resource": ("%s insuficiente.", "Not enough %s."),
    "rpg.europa.max_level_title": ("✦ Nível máximo! Título: Lenda de Europa.", "✦ Max level! Title: Legend of Europa."),
    "rpg.europa.stone_guardian": ("Guardião de Pedra", "Stone Guardian"),
    "rpg.europa.controls": ("[K] personagem · [R] habilidade · [G] supremo · [V] racial · /europa quest · /europa shop", "[K] character · [R] ability · [G] ultimate · [V] racial · /europa quest · /europa shop"),
    "rpg.europa.profession.tier_up": ("✦ %s alcançou o tier %s!", "✦ %s reached tier %s!"),
    "rpg.europa.profession.hidden_cache": ("Esconderijo encontrado: Fragmentos de Aster!", "Hidden cache found: Aster Fragments!"),
    "rpg.europa.profession.lapis_refund": ("Cartomancia: lápis-lazúli recuperado.", "Cartomancy: lapis refunded."),
    "rpg.europa.profession.masterwork": ("Obra-prima! Inquebrável e raridade.", "Masterwork! Unbreaking and rarity."),
    "rpg.europa.faction.rank_up": ("✦ %s agora o considera %s!", "✦ %s now considers you %s!"),
    "rpg.europa.faction.hint": ("Lojas: Balcão do Mercador · Contratos: Quadro de Missões", "Shops: Merchant Counter · Contracts: Quest Board"),
    "rpg.europa.screen.hover_race": ("Passe o mouse sobre uma raça para ver traços e habilidade.", "Hover a race to see traits and ability."),
    "rpg.europa.market.header": ("═══ Mercado de Aster ═══ Saldo: %s ✦", "═══ Aster Market ═══ Balance: %s ✦"),
    "rpg.europa.market.buy": ("Comprar", "Buy"),
    "rpg.europa.market.buy_hover": ("Custa %s Fragmentos de Aster", "Costs %s Aster Fragments"),
    "rpg.europa.market.locked": ("(requer %s)", "(requires %s)"),
    "rpg.europa.market.sell_hint": ("Venda: clique no Balcão com o item (agachado = pilha). Preço: /europa shop price", "Sell: use the item on the Counter (sneak = stack). Price: /europa shop price"),
    "rpg.europa.market.not_buying": ("O mercado não compra este item.", "The market does not buy this item."),
    "rpg.europa.market.too_little": ("Venda ao menos %s unidades.", "Sell at least %s units."),
    "rpg.europa.market.sold": ("Vendeu %s por %s ✦", "Sold %s for %s ✦"),
    "rpg.europa.market.bought": ("Comprou %sx %s por %s ✦", "Bought %sx %s for %s ✦"),
    "rpg.europa.market.no_money": ("Faltam fragmentos: custa %s, você tem %s.", "Not enough fragments: costs %s, you have %s."),
    "rpg.europa.market.rank_needed": ("Requer reputação %s.", "Requires %s reputation."),
    "rpg.europa.market.price": ("%s: %s ✦ por unidade (base %s — vender muito derruba o preço).", "%s: %s ✦ each (base %s — selling a lot lowers the price)."),
    "rpg.europa.board.fragments": ("+%s Fragmentos de Aster", "+%s Aster Fragments"),
    "rpg.europa.board.rep": ("+%s de reputação: %s", "+%s reputation: %s"),
    "rpg.europa.affix.crit": ("+%s%% de chance de crítico", "+%s%% crit chance"),
    "rpg.europa.affix.lifesteal": ("+%s%% de roubo de vida", "+%s%% life steal"),
    "rpg.europa.affix.power": ("+%s%% de poder de habilidade", "+%s%% ability power"),
}


def build():
    out = {}
    for k, v in CATEGORIES.items():
        out[f"rpg.europa.category.{k}"] = v
    for k, v in RESOURCES.items():
        out[f"rpg.europa.resource.{k}"] = v
    for k, (pt, en, dpt, den) in CLASSES.items():
        out[f"rpg.europa.class.{k}"] = (pt, en)
        out[f"rpg.europa.class.{k}.desc"] = (dpt, den)
    for k, (pt, en, dpt, den) in SUBCLASSES.items():
        out[f"rpg.europa.subclass.{k}"] = (pt, en)
        out[f"rpg.europa.subclass.{k}.desc"] = (dpt, den)
    for k, v in ABILITY_NAMES.items():
        out[f"rpg.europa.ability.{k}"] = v
    for k, v in FX.items():
        out[f"rpg.europa.fx.{k}"] = v
    for k, (pt, en, dpt, den, ppt, pen) in RACES.items():
        out[f"rpg.europa.race.{k}"] = (pt, en)
        out[f"rpg.europa.race.{k}.desc"] = (dpt, den)
        out[f"rpg.europa.race.{k}.passive"] = (ppt, pen)
    for k, v in TRAITS.items():
        out[f"rpg.europa.trait.{k}"] = v
    for k, (pt, en, ppt, pen) in PROFESSIONS.items():
        out[f"rpg.europa.profession.{k}"] = (pt, en)
        out[f"rpg.europa.profession.{k}.perk"] = (ppt, pen)
    for i, v in enumerate(PROFESSION_TIERS):
        out[f"rpg.europa.profession.tier.{i}"] = v
    for k, (name, tpt, ten, t1, t2, t3) in PATRONS.items():
        out[f"rpg.europa.patron.{k}"] = (name, name)
        out[f"rpg.europa.patron.{k}.title"] = (tpt, ten)
        out[f"rpg.europa.patron.{k}.tier1"] = t1
        out[f"rpg.europa.patron.{k}.tier2"] = t2
        out[f"rpg.europa.patron.{k}.tier3"] = t3
    for k, (pt, en, dpt, den) in FACTIONS.items():
        out[f"rpg.europa.faction.{k}"] = (pt, en)
        out[f"rpg.europa.faction.{k}.desc"] = (dpt, den)
    for i, v in enumerate(FACTION_RANKS):
        out[f"rpg.europa.faction.rank.{i}"] = v
    for k, pt, en in RARITIES:
        out[f"rpg.europa.rarity.{k}"] = (pt, en)
    for k, (pt, en, dpt, den) in QUESTS.items():
        out[f"quest.europa.{k}"] = (pt, en)
        out[f"quest.europa.{k}.desc"] = (dpt, den)
    out.update(MISC)
    return out
