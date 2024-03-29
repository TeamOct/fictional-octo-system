package fos.ai;

import arc.math.Mathf;
import fos.gen.Bugc;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.entities.Units;
import mindustry.entities.units.AIController;
import mindustry.gen.*;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;

public class BugAI extends AIController implements TargetableAI {
    private Bugc bug;

    @Override
    public void updateUnit() {
        bug = (Bugc) unit;

        if (bug.isFollowed()) {
            int followers = Units.count(unit.x, unit.y, 240f, u -> u instanceof Bugc);

            if (followers >= 5 + Mathf.floor(Vars.state.wave / 2f)) {
                bug.invading(true);
            }
        } else {
            //check for bug swarms nearby
            bug.following(Units.closest(unit.team, unit.x, unit.y, u ->
                    (u instanceof Bugc b && b.isFollowed() && !(b.invading()))
            ));

            //become a swarm leader if none exist, or if this bug is a boss
            if (bug.following() == null || bug.isBoss()) bug.isFollowed(true);
        }

        super.updateUnit();
    }

    @Override
    public void updateMovement() {
        Tile tile = unit.tileOn();
        Tile targetTile = tile;

        if (bug.invading()) {
            target = findTarget(unit.x, unit.y, 1600f, false, true);

            if (target != null) {
                if (unit.within(target, 32f)) {
                    vec.set(target).sub(unit);
                    vec.setLength(unit.speed());
                    faceMovement();
                    unit.moveAt(vec);
                    return;
                } else {
                    targetTile = pathfindTarget(target, unit);
                }
            }
        } else if (bug.following() != null) {
            bug.invading(bug.following() instanceof Bugc bf && bf.invading());

            //if already close enough to another bug when idle, stand still
            Unit nearest = Units.closest(unit.team, unit.x, unit.y, u -> (u instanceof Bugc) && u != this.unit);
            if (nearest != null && Mathf.within(unit.x, unit.y, nearest.x, nearest.y, 12f) && !bug.invading()) return;

            targetTile = pathfindTarget(bug.following(), unit);
        } else if (!bug.idle()) {
            //find a random point to walk at
            boolean foundTile = false;
            while (!foundTile) {
                int x = Mathf.random(-5, 5);
                int y = Mathf.random(-5, 5);
                Tile t = Vars.world.tile(unit.tileX() + x, unit.tileY() + y);
                if (t != null && t.block() == Blocks.air) {
                    targetTile = pathfindTarget(vec.set(unit).add(x*8, y*8), unit);
                    foundTile = true;
                    bug.idle(true);
                }
            }
        }

        if (targetTile == tile) return;

        unit.movePref(vec.trns(unit.angleTo(targetTile.worldx(), targetTile.worldy()), unit.speed()));
        faceTarget();
    }

    @Override
    public Teamc findTarget(float x, float y, float range, boolean air, boolean ground) {
        Teamc result = findMainTarget(x, y, range, air, ground);

        return checkTarget(result, x, y, range) ? bug.closestEnemyCore() : result;
    }

    @Override
    public Teamc findMainTarget(float x, float y, float range, boolean air, boolean ground) {
        for(BlockFlag flag : unit.type.targetFlags) {
            Teamc target;
            if (flag != null) {
                target = targetFlag(x, y, flag, true);
            } else {
                target = target(x, y, range, air, ground);
            }

            if (target != null) return target;
        }

        return Units.closestTarget(unit.team, x, y, range, u -> false, b -> true);
    }
}
