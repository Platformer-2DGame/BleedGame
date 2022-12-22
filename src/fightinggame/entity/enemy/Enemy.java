package fightinggame.entity.enemy;

import fightinggame.Gameplay;
import fightinggame.animation.enemy.EnemyRunBack;
import fightinggame.animation.enemy.EnemyRunForward;
import fightinggame.entity.Animation;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Random;
import fightinggame.entity.Character;
import fightinggame.entity.CharacterState;
import fightinggame.entity.GamePosition;
import fightinggame.entity.HealthBar;
import fightinggame.entity.item.Item;
import fightinggame.resource.ImageManager;
import fightinggame.resource.SpriteSheet;

public class Enemy extends Character {

    public static Enemy ENEMY_HEALTHBAR_SHOW;

    protected Gameplay gameplay;
    protected int deathCounter = 0;
    protected int isAttackedCounter = 0;
    protected int walkCounter = 0;
    protected boolean animateChange = false;
    protected int point = 10;
    protected int stunTime = 300;

    public Enemy(int id, String name, int health, GamePosition position, Map<CharacterState, Animation> animations, Map<String, BufferedImage> characterAssets,
            Gameplay gameplay, int rangeRandomSpeed) {
        super(id, name, health, position, animations, characterAssets, false);
        this.gameplay = gameplay;
        healthBarInit(health);
        healthBar.setOvalImage(new java.awt.geom.Ellipse2D.Float(1530f, 10f, 100, 100));
        healthBar.setAppearTimeLimit(1000);
        Random rand = new Random();
        int range = rand.nextInt(rangeRandomSpeed);
        speed = rand.nextInt(range);
    }

    @Override
    protected void healthBarInit(int maxHealth) {
        SpriteSheet healthBarSheet = new SpriteSheet();
        healthBarSheet.setImages(ImageManager.loadImagesWithCutFromFolderToList("assets/res/healthbar",
                1, 2, 126, 12));
        healthBar = new HealthBar(avatar, healthBarSheet, this,
                new GamePosition(975, 20, 550, 80), new GamePosition(1540, 8, 180, 120),
                maxHealth);
    }

    public Enemy() {
    }

    @Override
    public void tick() {
        super.tick();
        healthBar.tick();
        if (isDeath) {
            deathCounter++;
            if (deathCounter >= 1500) {
                if (inventory.size() > 0) {
                    for (int i = 0; i < inventory.size(); i++) {
                        if (inventory.get(i) != null && inventory.get(i).size() > 0) {
                            for (int j = 0; j < inventory.get(i).size(); j++) {
                                Item item = inventory.get(i).get(j);
                                GamePosition itemPos = item.getPosition();
                                itemPos.setXPosition(position.getXPosition() + position.getWidth() / 2 - 20);
                                itemPos.setYPosition(position.getMaxY() - itemPos.getHeight());
                                gameplay.getItemsOnGround().add(item);
                                item.setSpawnDrop(true);
                                inventory.get(i).remove(item);
                            }
                        }
                    }
                }
                gameplay.getPlayer().addPoint(point);
                gameplay.getEnemies().remove(this);
                gameplay.getPositions().remove(name);
                deathCounter = 0;
            }
        }
        if (isAttacked && !isDeath) {
            isAttackedCounter++;
            if (isAttackedCounter >= stunTime) {
                if (isLTR) {
                    currAnimation = animations.get(CharacterState.IDLE_LTR);
                } else {
                    currAnimation = animations.get(CharacterState.IDLE_RTL);
                }
                isAttacked = false;
                isAttackedCounter = 0;
                if (isAttack) {
                    isAttack = false;
                }
            }
        }
        if (!isDeath && !isAttacked && !isAttack && !animateChange) {
            walkCounter++;
            if (walkCounter >= 100) {
                if (currAnimation == null) {
                    if (!isLTR) {
                        currAnimation = animations.get(CharacterState.RUNFORWARD);
                    } else {
                        currAnimation = animations.get(CharacterState.RUNBACK);
                    }
                } else {
                    if (!isLTR) {
                        if (currAnimation instanceof EnemyRunForward); else {
                            currAnimation = animations.get(CharacterState.RUNFORWARD);
                        }
                    } else {
                        if (currAnimation instanceof EnemyRunBack); else {
                            currAnimation = animations.get(CharacterState.RUNBACK);
                        }
                    }
                }
                if (!isLTR) {
                    if (position.getXPosition() >= gameplay.getPlayPosition().getXPosition()) {
                        position.moveLeft(speed, true);
                    } else {
                        isLTR = true;
                    }
                } else {
                    if (position.getXPosition() + position.getWidth() <= gameplay.getPlayPosition().getXPosition()
                            + gameplay.getPlayPosition().getWidth()) {
                        position.moveRight(speed, true);
                    } else {
                        isLTR = false;
                    }
                }
                walkCounter = 0;
            }
        }
    }

    @Override
    public void render(Graphics g) {
        super.render(g);
        if (healthBar.isCanShow() && this.equals(ENEMY_HEALTHBAR_SHOW)) {
            healthBar.render(g);
        }
//        g.setColor(Color.red);
//        g.drawRect(position.getXPosition() - 400, position.getYPosition() - 100,
//                position.getXPosition() + position.getWidth(),
//                position.getHeight() + 200);

        //hitbox
//        g.setColor(Color.red);
//        g.drawRect(getXHitBox(), getYHitBox(),
//                position.getWidth(), position.getHeight() / 2 - 10);
    }

    public void setDefAttackedCounter() {
        isAttackedCounter = 0;
    }

    @Override
    public void healthBarTick() {

    }

    @Override
    public boolean checkHit(int attackX, int attackY, int attackHeight, boolean isAttack, int attackDmg) {
        int attackMaxY = attackY + attackHeight;
        if (isAttack && !isDeath) {
            if (attackX >= getXHitBox() && attackX <= getXMaxHitBox()
                    && ((attackY <= getYHitBox() && attackMaxY >= getYMaxHitBox()
                    || (attackY >= getYHitBox() && attackMaxY <= getYMaxHitBox())
                    || (attackY > getYHitBox() && attackY <= getYMaxHitBox()
                    && attackMaxY > getYMaxHitBox())
                    || (attackMaxY > getYHitBox() && attackMaxY <= getYMaxHitBox()
                    && attackY < getYHitBox())))) {
                setDefAttackedCounter();
                if (ENEMY_HEALTHBAR_SHOW != null) {
                    ENEMY_HEALTHBAR_SHOW.getHealthBar().resetShowCounter();
                }
                ENEMY_HEALTHBAR_SHOW = this;
                healthBar.setCanShow(true);
                if (isLTR) {
                    currAnimation = animations.get(CharacterState.GET_HIT_LTR);
                } else {
                    currAnimation = animations.get(CharacterState.GET_HIT_RTL);
                }
                isAttacked = true;
                int health = healthBar.getHealth() - attackDmg;
                if (health < 0) {
                    health = 0;
                }
                receiveDamage = attackDmg;
                healthBar.setHealth(health);
                if (health <= 0) {
                    isDeath = true;
                    if (isLTR) {
                        currAnimation = animations.get(CharacterState.DEATH_LTR);
                    } else {
                        currAnimation = animations.get(CharacterState.DEATH_RTL);
                    }
                }
                return true;
            }
        }
        return false;
    }

    public int getXHitBox() {
        return position.getXPosition();
    }

    public int getXMaxHitBox() {
        return position.getMaxX();
    }

    public int getYHitBox() {
        return position.getYPosition() + position.getHeight() / 3 - 10;
    }

    public int getYMaxHitBox() {
        return getYHitBox() + position.getHeight() / 2 - 10;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public int getStunTime() {
        return stunTime;
    }

    public void setStunTime(int stunTime) {
        this.stunTime = stunTime;
    }

}
