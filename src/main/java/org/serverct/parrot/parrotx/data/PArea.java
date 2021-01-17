package org.serverct.parrot.parrotx.data;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.Objects;
import java.util.Random;
import java.util.function.BiFunction;

@Data
public class PArea {

    private final Random random = new Random();
    private final PPlugin plugin;
    private final I18n lang;
    private Location pos1;
    private Location pos2;
    private World world;

    private PRange<Integer> xRange;
    private PRange<Integer> yRange;
    private PRange<Integer> zRange;

    public PArea(PPlugin plugin, Location pos1, Location pos2) {
        this.plugin = plugin;
        this.lang = this.plugin.getLang();
        this.pos1 = pos1;
        this.pos2 = pos2;
        loadLocation();
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
        loadLocation();
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
        loadLocation();
    }

    public void loadLocation() {
        if (BasicUtil.multiNull(pos1, pos2)) {
            lang.log.error("处理 PArea 区域数据时遇到问题: 两点坐标存在 null");
            return;
        }
        final World world1 = pos1.getWorld();
        final World world2 = pos2.getWorld();
        if (BasicUtil.multiNull(world1, world2) || !world1.equals(world2)) {
            lang.log.error("处理 PArea 区域数据时遇到问题: 两点坐标 World 不一致或存在 null");
            return;
        }
        this.world = world1;

        final int x1 = pos1.getBlockX();
        final int y1 = pos1.getBlockY();
        final int z1 = pos1.getBlockZ();

        final int x2 = pos2.getBlockX();
        final int y2 = pos2.getBlockY();
        final int z2 = pos2.getBlockZ();

        this.xRange = new PRange<>(Math.max(x1, x2), Math.min(x1, x2), (max, min) -> max - min);
        this.yRange = new PRange<>(Math.max(y1, y2), Math.min(y1, y2), (max, min) -> max - min);
        this.zRange = new PRange<>(Math.max(z1, z2), Math.min(z1, z2), (max, min) -> max - min);
    }

    @Nullable
    public Location getRandom() {
        final BiFunction<Integer, Integer, Integer> random = (min, offset) -> min + (offset == 0 ? 0 :
                this.random.nextInt(offset));
        return new Location(
                this.world,
                this.xRange.random(random),
                this.yRange.random(random),
                this.zRange.random(random)
        );
    }

    public boolean inArea(final Location location) {
        return inArea(location, false);
    }

    public boolean inArea(final Location location, final boolean ignoreY) {
        if (Objects.isNull(location)
                || Objects.isNull(location.getWorld())
                || !this.world.equals(location.getWorld())) {
            return false;
        }
        boolean result = this.xRange.inRange(location.getBlockX()) && this.zRange.inRange(location.getBlockZ());
        if (!ignoreY) {
            result = result && this.yRange.inRange(location.getBlockY());
        }
        return result;
    }

    // TODO 也需要做基于 PArea 创建 Res 领地啥的

}
