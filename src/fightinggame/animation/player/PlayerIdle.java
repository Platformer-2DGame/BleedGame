package fightinggame.animation.player;

import fightinggame.entity.Animation;
import fightinggame.resource.SpriteSheet;

public class PlayerIdle extends Animation{
    
    public PlayerIdle(int id, SpriteSheet sheet, int tickToExecute) {
        super(id, sheet, tickToExecute);
    }

    public PlayerIdle(int id, SpriteSheet sheet) {
        super(id, sheet);
    }
    
    
}
