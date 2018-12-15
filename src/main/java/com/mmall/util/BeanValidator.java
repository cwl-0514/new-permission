package com.mmall.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.mmall.exception.ParamException;
import org.apache.commons.collections.MapUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.*;

/**
 * 传入参数的校验
 */
public class BeanValidator {

    private static ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

    /**
     *
     * @param t 传入参数信息
     * @param groups 多参数
     * @param <T>
     * @return
     * ,map key是有问题的字段, value 是原因
     */
    public static <T> Map<String, String> validate(T t, Class... groups) {
        Validator validator = validatorFactory.getValidator();
        //校验参数的结果
        Set validateResult = validator.validate(t, groups);
        //参数为空
        if (validateResult.isEmpty()) {
            return Collections.emptyMap();
        } else {
            //不为空,我错误信息
            LinkedHashMap errors = Maps.newLinkedHashMap();
            Iterator iterator = validateResult.iterator();
            while (iterator.hasNext()) {
                ConstraintViolation violation = (ConstraintViolation)iterator.next();
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            return errors;
        }
    }

    /**
     * 传入的是集合参数
     * @param collection
     * @return
     */
    public static Map<String, String> validateList(Collection<?> collection) {
        //校验集合
        Preconditions.checkNotNull(collection);//是空直接抛出异常

        Iterator iterator = collection.iterator();
        Map errors;

        do {
            if (!iterator.hasNext()) {
                return Collections.emptyMap();
            }
            Object object = iterator.next();
            errors = validate(object, new Class[0]);
        } while (errors.isEmpty());

        return errors;
    }

    public static Map<String, String> validateObject(Object first, Object... objects) {
        if (objects != null && objects.length > 0) {
            return validateList(Lists.asList(first, objects));
        } else {
            return validate(first, new Class[0]);
        }
    }

    /**
     * 检验参数是否出现异常
     * @param param
     * @throws ParamException
     */
    public static void check(Object param) throws ParamException {
        Map<String, String> map = BeanValidator.validateObject(param);
        if (MapUtils.isNotEmpty(map)) {
            throw new ParamException(map.toString());
        }
    }
}
