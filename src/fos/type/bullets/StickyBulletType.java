package fos.type.bullets;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.pooling.Pool.Poolable;
import arc.util.pooling.Pools;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.type.unit.MissileUnitType;

public class StickyBulletType extends BasicBulletType {
    /** An interval between the contact with an enemy and the explosion. */
    public int explosionDelay;

    public StickyBulletType(float speed, float damage, int explosionDelay) {
        super(speed, damage);
        this.explosionDelay = explosionDelay;
        layer = Layer.flyingUnit + 1f;
        despawnHit = true;
        pierce = true;
    }

    @Override
    public void init(Bullet b) {
        super.init(b);
        b.data = null;
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health) {
        //do not target missiles.
        if (((Unit) entity).type instanceof MissileUnitType) return;

        super.hitEntity(b, entity, health);

        b.hit(true);

        StickyBulletData data = Pools.obtain(StickyBulletData.class, StickyBulletData::new);
        data.target = (Teamc) entity;
        b.data = data;

        b.lifetime = explosionDelay;
    }

    @Override
    public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct) {
        super.hitTile(b, build, x, y, initialHealth, direct);

        //the bullet just stops.
        b.vel(Vec2.ZERO);
        b.lifetime = explosionDelay;
    }

    //FIXME
    @Override
    public void update(Bullet b) {
        super.update(b);

        StickyBulletData data = (StickyBulletData) b.data;

        if (data == null || !b.hit) return;

        if (data.target instanceof Unit u) {
            if (u.dead()) {
                data.reset();
                return;
            }

            float bx = b.x(), by = b.y();
            float ox = data.target.x(), oy = data.target.y();

            if (data.initialAngle == null) data.initialAngle = Mathf.angle(bx - ox, by - oy);
            if (data.targetRot == null) data.targetRot = u.rotation;

            float angle = data.initialAngle - data.targetRot + u.rotation;

            b.x = u.x + Mathf.cos(angle * Mathf.degreesToRadians) * u.hitSize / 2;
            b.y = u.y + Mathf.sin(angle * Mathf.degreesToRadians) * u.hitSize / 2;

            b.vel(Vec2.ZERO);
        }
    }

    @Override
    public void removed(Bullet b) {
        super.removed(b);
        createSplashDamage(b, b.x, b.y);
        if (b.data != null) ((StickyBulletData) b.data).reset();
    }

    @Override
    public void despawned(Bullet b) {
        super.despawned(b);
        createSplashDamage(b, b.x, b.y);
        if (b.data != null) ((StickyBulletData) b.data).reset();
    }

    public class StickyBulletData implements Poolable {
        public Teamc target;
        public Float initialAngle, targetRot;

        @Override
        public void reset() {
            target = null;
            initialAngle = null;
            targetRot = null;
        }
    }
}
