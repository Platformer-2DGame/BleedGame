package fightinggame.entity.ability.type;

import fightinggame.Gameplay;
import fightinggame.entity.Animation;
import fightinggame.entity.Character;
import fightinggame.entity.GamePosition;
import fightinggame.entity.Player;
import fightinggame.entity.SpriteSheet;
import fightinggame.entity.ability.type.skill.ActiveSkill;
import java.awt.image.BufferedImage;
import java.util.List;

public abstract class Heal extends ActiveSkill {

    protected int healPoint;

    public Heal(int healPoint, int id, String name, long resetTime, int energyLost, 
            SpriteSheet skillIcon, Animation currAnimation
            , GamePosition position, BufferedImage border, Gameplay gameplay, Character character) {
        super(border , position, id, name, resetTime, energyLost, skillIcon, currAnimation, gameplay, character);
        this.healPoint = healPoint;
    }
    
    public Heal(int healPoint, int id, String name, long resetTime, int energyLost, 
            SpriteSheet skillIcon, Animation currAnimation, Gameplay gameplay, Character character) {
        super(id, name, resetTime, energyLost, skillIcon, currAnimation, gameplay, character);
        this.healPoint = healPoint;
    }

    public boolean healing(Character character) {
        if (canUse) {
            int maxHealth = character.getStatusBar().getMaxHealth();
            int health = character.getStats().getHealth();
            if (health == maxHealth) {
                return false;
            }
            int afterHeal = health + healPoint;
            if (afterHeal > maxHealth) {
                afterHeal = maxHealth;
            }
            if (afterHeal > health) {
                if (character instanceof Player) {
                    character.getStats().useEnergy(energyLost);
                }
                character.getStats().setHealth(afterHeal);
                character.addHealingAmount(healPoint);
                canUse = false;
                return true;
            }
        }
        return false;
    }

    public boolean healing() {
        if (character == null) {
            return false;
        }
        return healing(character);
    }

    @Override
    public boolean execute() {
        if (this.character == null) {
            return false;
        }
        if (this.character.getStats().haveEnergy()) {
            return healing();
        }
        return false;
    }

    @Override
    public boolean execute(Character character) {
        if (this.character == null) {
            return false;
        }
        if (this.character.getStats().haveEnergy()) {
            return healing(character);
        }
        return false;
    }

    @Override
    public boolean execute(List<Character> characters) {
        if (this.character == null) {
            return false;
        }
        if (this.character.getStats().haveEnergy()) {
            return true;
        }
        return false;
    }

    public int getHealPoint() {
        return healPoint;
    }

    public void setHealPoint(int healPoint) {
        this.healPoint = healPoint;
    }
}
