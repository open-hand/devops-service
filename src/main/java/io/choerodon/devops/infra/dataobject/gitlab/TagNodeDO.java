package io.choerodon.devops.infra.dataobject.gitlab;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description: Tag
 * User: hfr
 * Date: 2018-02-05
 */
public class TagNodeDO implements Comparable<TagNodeDO> {
    private final int hundred;
    private final int ten;
    private final int unit;

    private TagNodeDO(int unit, int ten, int hundred) {
        this.unit = unit;
        this.ten = ten;
        this.hundred = hundred;
    }

    /**
     * Tag 转化为 TagNode
     *
     * @param tag Tag
     * @return TagNode
     */
    public static TagNodeDO tagNameToTagNode(String tag) {
        if (tag.matches("\\d+(\\.\\d+){2}")) {
            List<Integer> biteArray = Arrays.stream(tag.split("\\."))
                    .map(Integer::parseInt).collect(Collectors.toList());
            if (biteArray.size() != 3) {
                return null;
            }
            try {
                return new TagNodeDO(biteArray.get(2), biteArray.get(1), biteArray.get(0));
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public String getNextTag(Boolean isRelease) {
        return isRelease ? String.valueOf(this.hundred) + "." + (this.ten + 1) + ".0" :
                String.valueOf(this.hundred) + "." + this.ten + "." + (this.unit + 1);
    }

    @Override
    public int compareTo(TagNodeDO compare) {
        Integer result;
        result = Integer.compare(this.hundred, compare.hundred);
        if (result == 0) {
            result = Integer.compare(this.ten, compare.ten);
            if (result == 0) {
                return Integer.compare(this.unit, compare.unit);
            }
        }
        return result;
    }
}
