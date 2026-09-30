# Grid Inventory — V1, Parte 1

Minecraft **1.20.1** · Forge **47.4.0** · Java **17**

## Escopo desta parte

Somente o **sistema de formas dos itens vanilla** e os **espaços do inventário (grid 9x7)**.

- Formas em matriz binária (`GridShape`), com rotação de 90° sobre a forma real e `normalize()`.
- Classificação automática de itens vanilla por classe (`ItemClassifier`), sem cadastro item por item.
- Grid 9x7 = 63 células, com distribuição first-fit dos itens do inventário (`GridLayout`).
  Maiores formas primeiro; tenta sem rotação e, se não couber, girada.
- Tela de visualização (`GridInventoryScreen`), aberta com **G** (configurável em Controles).
  O inventário vanilla (E) continua intacto.

## Fora desta parte (ainda NÃO existe)

Mover/girar com o mouse ou teclado, persistência de posições (`GridState`), rede/validação server-side,
peso, tags, compatibilidade com outros mods, mochilas, inventário customizado (`AbstractContainerMenu`).

## Regras de classificação (vanilla)

| Classe | Forma |
|---|---|
| ArmorItem | 2x2 |
| PickaxeItem | `###` / `.#.` / `.#.` |
| AxeItem | `##` / `##` / `.#` |
| HoeItem | `##` / `.#` / `.#` |
| ShovelItem, SwordItem | `#` / `#` / `#` |
| ShearsItem | 2x2 |
| FishingRodItem | `##` / `#.` / `#.` |
| BowItem, CrossbowItem | `##` / `##` / `##` |
| ShieldItem | `##` / `##` / `.#` |
| comestível (`isEdible`) | 1x1 |
| PotionItem, BucketItem | 1x2 |
| restante | 1x1 |

## Build

Copie `gradlew`, `gradlew.bat` e `gradle/wrapper/gradle-wrapper.jar` de um MDK do Forge 1.20.1
(https://files.minecraftforge.net) para esta pasta e rode:

    gradlew build

O jar sai em `build/libs/`. Para testar direto: `gradlew runClient`.

## Estrutura

    common/grid/GridShape.java      forma (sem dependência do Minecraft)
    common/grid/GridLayout.java     grid 9x7 + distribuição (sem dependência do Minecraft)
    common/grid/ItemClassifier.java classe vanilla -> forma
    client/GridInventoryClient.java tecla G
    client/GridInventoryScreen.java tela de visualização
