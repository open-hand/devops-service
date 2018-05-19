package io.choerodon.devops.infra.common.util;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

public class JacksonJsonEnumHelper<E extends Enum<E>> {

    private Map<String, E> valuesMap;
    private Map<E, String> namesMap;

    public JacksonJsonEnumHelper(Class<E> enumType) {
        this(enumType, false);
    }

    /**
     * get valuesMap and namesMap
     *
     * @param enumType               enumType
     * @param firstLetterCapitalized firstLetterCapitalized
     */
    private JacksonJsonEnumHelper(Class<E> enumType, Boolean firstLetterCapitalized) {

        valuesMap = new HashMap<>();
        namesMap = new HashMap<>();

        for (E e : enumType.getEnumConstants()) {

            String name = e.name().toLowerCase();
            if (firstLetterCapitalized) {
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
            }

            valuesMap.put(name, e);
            namesMap.put(e, name);
        }
    }

    @JsonCreator
    public E forValue(String value) {
        return valuesMap.get(value);
    }

    /**
     * Get the string used by the API for this enum.
     *
     * @param e the enum value to get the API string for
     * @return the string used by the API for this enum
     */
    public String toString(E e) {
        return (namesMap.get(e));
    }
}