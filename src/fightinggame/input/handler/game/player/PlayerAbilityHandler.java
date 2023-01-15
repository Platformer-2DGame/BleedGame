package fightinggame.input.handler.game.player;

import fightinggame.Game;
import fightinggame.Gameplay;
import fightinggame.entity.state.CharacterState;
import fightinggame.entity.GamePosition;
import fightinggame.entity.Player;
import fightinggame.entity.ability.type.throwable.Fireball;
import fightinggame.entity.item.Item;
import fightinggame.entity.state.GameState;
import fightinggame.input.handler.GameHandler;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class PlayerAbilityHandler extends GameHandler implements KeyListener {

    private final Player player;
    private final Gameplay gameplay;

    public PlayerAbilityHandler(Player player, String name, Gameplay gameplay) {
        super(name);
        this.player = player;
        this.gameplay = gameplay;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!player.isDeath() && Game.STATE == GameState.GAME_STATE) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_1:
                    if (!player.isDeath()) {
                        player.getAbility(0).execute();
                    }
                    break;
                case KeyEvent.VK_2:
//                    if(player.isInAir() || player.isFallDown()) {
//                        break;
//                    }
                    if (!player.isSpellCast() && !player.isAttack() && !player.getPosition().isMoving()
                            && !player.getPosition().isCrouch && !player.isDeath()) {
                        int spawnX;
                        int xChange;
                        GamePosition endPos;
                        if (player.isLTR()) {
                            xChange = 15;
                            spawnX = player.getPosition().getMaxX() + xChange;
                            endPos = new GamePosition(
                                    player.getPosition().getMaxX() + xChange, 0,
                                    gameplay.getCamera().getPosition().getMaxX() -
                                            (player.getPosition().getMaxX() + xChange), 0);

                        } else {
                            xChange = 215;
                            spawnX = player.getPosition().getXPosition() - xChange;
                            endPos = new GamePosition(
                                    gameplay.getCamera().getPosition().getXPosition() - xChange, 0,
                                    0, 0);
                        }
                        GamePosition spawnPosition
                                = new GamePosition(spawnX,
                                        player.getPosition().getYPosition() + 70, 200, 100);
                        boolean result = ((Fireball) player.getAbility(1)).execute(spawnPosition, endPos);
                        if (result) {
                            player.setIsSpellCast(true);
                            if (player.isLTR()) {
                                player.setCurrAnimation(player.getAnimations().get(CharacterState.SPELLCAST_LTR));
                            } else {
                                player.setCurrAnimation(player.getAnimations().get(CharacterState.SPELLCASTLOOP_RTL));
                            }
                        }
//                try {
//                    Thread.sleep(200);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(PlayerAbilityHandler.class.getName()).log(Level.SEVERE, null, ex);
//                }
                    }
                    break;
                case KeyEvent.VK_3:
                    if (!player.isDeath()) {
                        Item item = player.getInventory().getItemAscending();
                        if (item == null) {
                            break;
                        }
                        if (item.use()) {

                        } else {
                            System.out.println("Is Cooldown.");
                        }
                    }
                    break;
                case KeyEvent.VK_I:
                    if (!player.isDeath() && !player.isAttack() && !player.getPosition().isMoving()) {
                        player.getInventory().open();
                    }
                    break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_2:
                if (player.isSpellCast()) {
                    player.setIsSpellCast(false);
                    if (player.isLTR()) {
                        player.setCurrAnimation(player.getAnimations().get(CharacterState.IDLE_LTR));
                    } else {
                        player.setCurrAnimation(player.getAnimations().get(CharacterState.IDLE_RTL));
                    }
                }
                break;
        }
    }

    @Override
    public void tick() {

    }

}
