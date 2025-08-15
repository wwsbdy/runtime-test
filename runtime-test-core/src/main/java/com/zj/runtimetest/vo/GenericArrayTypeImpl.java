package com.zj.runtimetest.vo;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;


/**
 * @author : jie.zhou
 * @date : 2025/8/15
 */
public class GenericArrayTypeImpl implements GenericArrayType {
    private final Type genericComponentType;

    // private constructor enforces use of static factory
    private GenericArrayTypeImpl(Type ct) {
        genericComponentType = ct;
    }

    /**
     * Factory method.
     *
     * @param ct - the desired component type of the generic array type
     *           being created
     * @return a generic array type with the desired component type
     */
    public static GenericArrayTypeImpl make(Type ct) {
        return new GenericArrayTypeImpl(ct);
    }


    /**
     * Returns a {@code Type} object representing the component type
     * of this array.
     *
     * @return a {@code Type} object representing the component type
     * of this array
     * @since 1.5
     */
    @Override
    public Type getGenericComponentType() {
        // return cached component type
        return genericComponentType;
    }

    @Override
    public String toString() {
        return getGenericComponentType().getTypeName() + "[]";
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(genericComponentType);
    }
}
