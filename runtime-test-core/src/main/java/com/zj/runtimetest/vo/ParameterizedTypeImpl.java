package com.zj.runtimetest.vo;

import lombok.Getter;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.StringJoiner;

/**
 * Implementing class for ParameterizedType interface.
 *
 * @author : jie.zhou
 * @date : 2025/8/15
 */

public class ParameterizedTypeImpl implements ParameterizedType {
    private final Type[] actualTypeArguments;
    /**
     * -- GETTER --
     * Returns the
     * object representing the class or interface
     * that declared this type.
     *
     */
    @Getter
    private final Class<?> rawType;
    /**
     * -- GETTER --
     * Returns a
     * object representing the type that this type
     * is a member of.  For example, if this type is
     * ,
     * return a representation of
     * .
     * <p>If this type is a top-level type,
     * is returned.
     *
     */
    @Getter
    private final Type ownerType;

    private ParameterizedTypeImpl(Class<?> rawType,
                                  Type[] actualTypeArguments,
                                  Type ownerType) {
        this.actualTypeArguments = actualTypeArguments;
        this.rawType = rawType;
        this.ownerType = (ownerType != null) ? ownerType : rawType.getDeclaringClass();
    }

    /**
     * Static factory. Given a (generic) class, actual type arguments
     * and an owner type, creates a parameterized type.
     * This class can be instantiated with a raw type that does not
     * represent a generic type, provided the list of actual type
     * arguments is empty.
     * If the ownerType argument is null, the declaring class of the
     * raw type is used as the owner type.
     * <p> This method throws a MalformedParameterizedTypeException
     * under the following circumstances:
     * If the number of actual type arguments (i.e., the size of the
     * array {@code typeArgs}) does not correspond to the number of
     * formal type arguments.
     * If any of the actual type arguments is not an instance of the
     * bounds on the corresponding formal.
     *
     * @param rawType             the Class representing the generic type declaration being
     *                            instantiated
     * @param actualTypeArguments a (possibly empty) array of types
     *                            representing the actual type arguments to the parameterized type
     * @param ownerType           the enclosing type, if known.
     * @return An instance of {@code ParameterizedType}
     * @throws MalformedParameterizedTypeException if the instantiation
     *                                             is invalid
     */
    public static ParameterizedTypeImpl make(Class<?> rawType,
                                             Type[] actualTypeArguments,
                                             Type ownerType) {
        return new ParameterizedTypeImpl(rawType, actualTypeArguments,
                ownerType);
    }


    /**
     * Returns an array of {@code Type} objects representing the actual type
     * arguments to this type.
     *
     * <p>Note that in some cases, the returned array be empty. This can occur
     * if this type represents a non-parameterized type nested within
     * a parameterized type.
     *
     * @return an array of {@code Type} objects representing the actual type
     * arguments to this type
     * @throws TypeNotPresentException             if any of the
     *                                             actual type arguments refers to a non-existent type declaration
     * @throws MalformedParameterizedTypeException if any of the
     *                                             actual type parameters refer to a parameterized type that cannot
     *                                             be instantiated for any reason
     * @since 1.5
     */
    @Override
    public Type[] getActualTypeArguments() {
        return actualTypeArguments.clone();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (ownerType != null) {
            sb.append(ownerType.getTypeName());

            sb.append("$");

            if (ownerType instanceof ParameterizedTypeImpl) {
                // Find simple name of nested type by removing the
                // shared prefix with owner.
                sb.append(rawType.getName().replace(((ParameterizedTypeImpl) ownerType).rawType.getName() + "$",
                        ""));
            } else {
                sb.append(rawType.getSimpleName());
            }
        } else {
            sb.append(rawType.getName());
        }

        if (actualTypeArguments != null) {
            StringJoiner sj = new StringJoiner(", ", "<", ">");
            sj.setEmptyValue("");
            for (Type t : actualTypeArguments) {
                sj.add(t.getTypeName());
            }
            sb.append(sj.toString());
        }

        return sb.toString();
    }
}
