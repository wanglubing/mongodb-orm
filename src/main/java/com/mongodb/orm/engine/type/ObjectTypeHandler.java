package com.mongodb.orm.engine.type;

/**
 * Object implementation of TypeHandler
 * @author: xiangping_yu
 * @data : 2014-7-25
 * @since : 1.5
 */
public class ObjectTypeHandler implements TypeHandler<Object> {

  @Override
  public Object getParameter(Object instance) {
    return instance;
  }

  @Override
  public Object getResult(Object instance, Object value) {
    return value;
  }

}
