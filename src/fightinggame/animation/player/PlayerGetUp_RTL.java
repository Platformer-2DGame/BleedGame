package fightinggame.animation.player;

import fightinggame.entity.Animation;
import fightinggame.entity.SpriteSheet;

public class PlayerGetUp_RTL extends Animation{
    
    public PlayerGetUp_RTL(int id, SpriteSheet sheet, int tickToExecute) {
        super(id, sheet, tickToExecute);
    }
    
    @Override
    public void tick() {
        if(sheet.getSpriteIndex() == sheet.getImages().size() - 1);
        else sheet.tick(tickToExecute);
    }
}
