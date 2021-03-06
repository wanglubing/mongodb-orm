package com.mongodb.orm.engine.type;

/**
 * Long implementation of TypeHandler
 * @author: xiangping_yu
 * @data : 2014-7-25
 * @since : 1.5
 */
public class LongTypeHandler implements TypeHandler<Long>, ColumnHandler<Long> {

  @Override
  public Object getParameter(Long instance) {
    return instance;
  }

  @Override
  public Long getResult(Object instance, Object value) {
    if(value instanceof Long) {
      return (Long) value;
    }
    return Long.parseLong(value.toString());
  }

  @Override
  public Long resovleColumn(Object value) {
    if(value instanceof Long) {
      return (Long) value;
    }
    return Long.parseLong(value.toString());
  }

}
