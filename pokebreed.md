**Activities for Gaining Experience Points (XP) in Pokémon**

1. **Apricorn**
   - *Collecting Apricorns:*  
     Players may earn XP by collecting Apricorns, which are used to craft Poké Balls and other items in the game.  
     - Harvesting or cultivating Apricorns might provide small amounts of XP.

2. **Battling Wild Pokémon**
   - *Defeating wild Pokémon:*  
     - Common Pokémon: Grants standard XP.  
     - Rare Pokémon (Shiny or Legendary): Grants higher XP based on rarity.

3. **Berry**
   - *Collecting and Using Berries:*  
     - Players might earn XP by planting and harvesting berries.  
     - Using berries to heal Pokémon or cure status conditions could also provide XP in some systems.

4. **Capture (Catching Pokémon)**
   - *Catching Pokémon:*  
     - Common Pokémon: Lower XP.  
     - Rare/Legendary Pokémon: Higher XP.  
     - Shiny Pokémon or high-level Pokémon might grant bonus XP.

5. **Evolution**
   - *Evolving Pokémon:*  
     Players earn XP whenever a Pokémon evolves.  
     - Additional XP could be rewarded for successful evolutions.

6. **Fishing**
   - *Catching Pokémon through fishing:*  
     Players may earn XP for fishing Pokémon, depending on the type of Pokémon caught.

7. **Hatching**
   - *Hatching Pokémon from eggs:*  
     - Each time a Pokémon egg hatches, players earn XP.  
     - The rarer the Pokémon in the egg, the higher the XP reward might be.

8. **Leveling**
   - *Leveling up Pokémon:*  
     - When your Pokémon levels up, players might also receive XP rewards.

======================================================================
**Level Reward Configuration System**
======================================================================

This system allows you to **configure rewards** for players based on their level. Admins can set custom rewards for each level, such as items, in-game currency, or special commands.

-----------------------------------
**Example Configuration:**
-----------------------------------
```json
{
  "level": "1",
  "reward": {
    "give %player% stone 1"
  }
},
{
  "level": "2",
  "reward": {
    "give %player% stone 1"
  }
}


======================================================================

**Configuration for XP System**

```json
{
  "id": "capture",
  "defaultGain": 50.0,
  "filterGains": {
    "leg shiny": 2.0
  }
}
```

Explanation

id:

"capture" refers to the event where a Pokémon is captured.
defaultGain:

Players will earn 50 XP by default for capturing any Pokémon.
filterGains:

"leg shiny": 2.0 applies a multiplier of 2 times for Pokémon that are both Legendary and Shiny.

Example Use Case

Capturing a Legendary and Shiny Pokémon:
defaultGain (50) * filterGains (2.0) = 100 XP

======================================================================

**Placeholders for Trainer Level System**

1. Display your current level:  
   `%pokebreed_level%`

2. Display the experience points you currently have:  
   `%pokebreed_exp%`

3. Display the experience points required to level up:  
   `%pokebreed_expmax%`

4. Display the experience points required to level up percentage:  
   `%pokebreed_exppercentage%`
