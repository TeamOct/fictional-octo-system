package fos.entities.bullets;

import mindustry.content.*;
import mindustry.entities.bullet.*;

public class ThunderLaser extends ContinuousLaserBulletType {
    public ThunderLaser(){
        damage = 230;
        length = 200;
        hitEffect = Fx.hitMeltdown;
        hitSize = 18;
        incendChance = 0.4f;
        incendSpread = 5;
        incendAmount = 1;
        lifetime = 40;
        drawSize = 420;
    }
}
