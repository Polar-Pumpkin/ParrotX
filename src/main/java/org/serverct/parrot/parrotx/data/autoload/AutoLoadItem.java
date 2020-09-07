package org.serverct.parrot.parrotx.data.autoload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AutoLoadItem {
    private String path;
    private DataType type;
    private String field;

    public enum DataType {
        STRING("字符串"),
        INT("整数"),
        DOUBLE("小数(Double)"),
        LONG("长整数"),
        BOOLEAN("布尔值"),
        LIST("列表"),
        MAP_LIST("Map 列表"),
        STRING_MAP("哈希表(String)"),
        INT_MAP("哈希表(Int)"),
        SOUND("音效(Sound)枚举"),
        ITEM_STACK("物品堆(ItemStack)"),
        LOCATION("坐标"),
        COLOR("颜色"),
        SERIALIZABLE("可序列化对象"),
        UNKNOWN("未知类型");

        public final String name;

        DataType(String name) {
            this.name = name;
        }
    }
}
