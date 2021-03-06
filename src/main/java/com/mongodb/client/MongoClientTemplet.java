package com.mongodb.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.client.event.ResultContext;
import com.mongodb.client.event.ResultHandler;
import com.mongodb.exception.MongoDaoException;
import com.mongodb.orm.engine.config.AggregateConfig;
import com.mongodb.orm.engine.config.CommandConfig;
import com.mongodb.orm.engine.config.DeleteConfig;
import com.mongodb.orm.engine.config.GroupConfig;
import com.mongodb.orm.engine.config.InsertConfig;
import com.mongodb.orm.engine.config.SelectConfig;
import com.mongodb.orm.engine.config.UpdateConfig;
import com.mongodb.orm.engine.entry.Entry;
import com.mongodb.orm.engine.entry.NodeEntry;
import com.mongodb.orm.executor.ParserEngine;


/**
 * The primary Java interface implement for working with NoSqlOrm.
 * 
 * @info : Templet for MongoDB
 * @author: xiangping_yu
 * @data : 2014-6-22
 * @since : 1.5
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class MongoClientTemplet implements MongoTemplet {

  private MongoORMFactoryBean factory;

  @Override
  public <T> T selectOne(String statement) {
    return selectOne(statement, null, null, ReadPreference.secondaryPreferred());
  }

  @Override
  public <T> T selectOne(String statement, Object parameter) {
    return selectOne(statement, parameter, null, ReadPreference.secondaryPreferred());
  }

  @Override
  public void selectOne(String statement, ResultHandler handler) {
    selectOne(statement, null, handler, ReadPreference.secondaryPreferred());
  }

  @Override
  public void selectOne(String statement, Object parameter, ResultHandler handler) {
    selectOne(statement, parameter, handler, ReadPreference.secondaryPreferred());
  }

  private <T> T selectOne(String statement, Object parameter, ResultHandler handler, ReadPreference readPreference) {
    logger.debug("Execute 'selectOne' mongodb command. Statement '" + statement + "'.");

    SelectConfig config = (SelectConfig) factory.getConfiguration().getConfig(statement);
    if (config == null) {
      throw new MongoDaoException(statement, "Query statement id '" + statement + "' not found.");
    }

    String collection = config.getCollection();
    NodeEntry query = config.getQuery();
    NodeEntry field = config.getField();
    NodeEntry order = config.getOrder();

    DB db = factory.getDataSource().getDB();

    DBCollection coll = db.getCollection(collection);
    Map<String, Object> q = (Map<String, Object>) ParserEngine.toQuery(query, parameter);
    Map<String, Object> f = (Map<String, Object>) ParserEngine.toField(field, parameter);
    Map<String, Object> o = (Map<String, Object>) ParserEngine.toOrder(order, parameter);

    DBObject queryDbo = new BasicDBObject(q);
    logger.debug("Execute 'selectOne' mongodb command. Query '" + queryDbo + "'.");

    DBObject fieldDbo = (f == null) ? null : new BasicDBObject(f);
    logger.debug("Execute 'selectOne' mongodb command. Field '" + fieldDbo + "'.");

    DBObject orderDbo = (o == null) ? null : new BasicDBObject(o);
    logger.debug("Execute 'selectOne' mongodb command. Order '" + orderDbo + "'.");

    final DBObject resultSet = coll.findOne(queryDbo, fieldDbo, orderDbo, readPreference);
    logger.debug("Execute 'selectOne' mongodb command. Result set '" + resultSet + "'.");

    if (handler == null) {
      return (T) ParserEngine.toResult(field, resultSet);
    }

    handler.handleResult(new ResultContext() {
      @Override
      public Object getResultObject() {
        return resultSet;
      }

      @Override
      public int getResultCount() {
        if (resultSet == null) {
          return 0;
        }
        return 1;
      }
    });
    return null;
  }

  @Override
  public <T> List<T> selectList(String statement) {
    return selectList(statement, null, null, null, null, ReadPreference.secondaryPreferred());
  }

  @Override
  public <T> List<T> selectList(String statement, Object parameter) {
    return selectList(statement, parameter, null, null, null, ReadPreference.secondaryPreferred());
  }

  @Override
  public <T> List<T> selectList(String statement, Object parameter, Integer limit, Integer skip) {
    return selectList(statement, parameter, limit, skip, null, ReadPreference.secondaryPreferred());
  }

  @Override
  public void selectList(String statement, ResultHandler handler) {
    selectList(statement, null, null, null, handler, ReadPreference.secondaryPreferred());
  }

  @Override
  public void selectList(String statement, Object parameter, ResultHandler handler) {
    selectList(statement, parameter, null, null, handler, ReadPreference.secondaryPreferred());
  }

  @Override
  public void selectList(String statement, Object parameter, Integer limit, Integer skip, ResultHandler handler) {
    selectList(statement, parameter, limit, skip, handler, ReadPreference.secondaryPreferred());
  }

  private <T> List<T> selectList(String statement, Object parameter, Integer limit, Integer skip, ResultHandler handler,
      ReadPreference readPreference) {
    logger.debug("Execute 'selectList' mongodb command. Statement '" + statement + "'.");

    SelectConfig config = (SelectConfig) factory.getConfiguration().getConfig(statement);
    if (config == null) {
      throw new MongoDaoException(statement, "Query statement id '" + statement + "' not found.");
    }

    String collection = config.getCollection();
    NodeEntry query = config.getQuery();
    NodeEntry field = config.getField();
    NodeEntry order = config.getOrder();

    DB db = factory.getDataSource().getDB();

    DBCollection coll = db.getCollection(collection);
    coll.setReadPreference(readPreference);

    Map<String, Object> q = (Map<String, Object>) ParserEngine.toQuery(query, parameter);
    Map<String, Object> f = (Map<String, Object>) ParserEngine.toField(field, parameter);
    Map<String, Object> o = (Map<String, Object>) ParserEngine.toOrder(order, parameter);

    DBObject queryDbo = new BasicDBObject(q);
    logger.debug("Execute 'selectList' mongodb command. Query '" + queryDbo + "'.");

    DBObject fieldDbo = (f == null) ? null : new BasicDBObject(f);
    logger.debug("Execute 'selectList' mongodb command. Field '" + fieldDbo + "'.");

    DBObject orderDbo = (o == null) ? null : new BasicDBObject(o);
    logger.debug("Execute 'selectList' mongodb command. Order '" + orderDbo + "'.");

    final DBCursor resultSet = coll.find(queryDbo, fieldDbo);
    logger.debug("Execute 'selectList' mongodb command. Result set '" + resultSet + "'.");

    if (orderDbo != null) {
      resultSet.sort(orderDbo);
    }
    if (skip != null) {
      resultSet.skip(skip);
    }
    if (limit != null) {
      resultSet.limit(limit);
    }

    if (handler == null) {
      List<T> result = new ArrayList<T>(resultSet.size());
      while (resultSet.hasNext()) {
        result.add((T) ParserEngine.toResult(field, resultSet.next()));
      }
      return result;
    }

    handler.handleResult(new ResultContext() {
      @Override
      public Object getResultObject() {
        return resultSet;
      }

      @Override
      public int getResultCount() {
        return resultSet.size();
      }
    });
    return null;
  }

  @Override
  public long count(String statement) {
    return count(statement, null);
  }

  @Override
  public long count(String statement, Object parameter) {
    logger.debug("Execute 'count' mongodb command. Statement '" + statement + "'.");

    SelectConfig config = (SelectConfig) factory.getConfiguration().getConfig(statement);
    if (config == null) {
      throw new MongoDaoException(statement, "Count statement id '" + statement + "' not found.");
    }

    String collection = config.getCollection();
    NodeEntry query = config.getQuery();

    DB db = factory.getDataSource().getDB();

    DBCollection coll = db.getCollection(collection);

    Map<String, Object> q = (Map<String, Object>) ParserEngine.toQuery(query, parameter);

    DBObject queryDbo = new BasicDBObject(q);
    logger.debug("Execute 'count' mongodb command. Query '" + queryDbo + "'.");

    return coll.count(queryDbo, ReadPreference.secondaryPreferred());
  }

  @Override
  public <T> List<T> distinct(String statement, String key) {
    return distinct(statement, key, null, null, ReadPreference.secondaryPreferred());
  }

  @Override
  public <T> List<T> distinct(String statement, String key, Object parameter) {
    return distinct(statement, key, parameter, null, ReadPreference.secondaryPreferred());
  }

  @Override
  public void distinct(String statement, String key, ResultHandler handler) {
    distinct(statement, key, null, handler, ReadPreference.secondaryPreferred());
  }

  @Override
  public void distinct(String statement, String key, Object parameter, ResultHandler handler) {
    distinct(statement, key, parameter, handler, ReadPreference.secondaryPreferred());
  }

  private <T> List<T> distinct(String statement, String key, Object parameter, ResultHandler handler, ReadPreference readPreference) {
    logger.debug("Execute 'distinct' mongodb command. Statement '" + statement + "'.");

    SelectConfig config = (SelectConfig) factory.getConfiguration().getConfig(statement);
    if (config == null) {
      throw new MongoDaoException(statement, "Distinct statement id '" + statement + "' not found.");
    }

    if (StringUtils.isBlank(key)) {
      throw new MongoDaoException(statement, "Execute 'distinct' mongodb command. 'key' is blank.");
    }

    String collection = config.getCollection();
    NodeEntry query = config.getQuery();
    NodeEntry field = config.getField();

    DB db = factory.getDataSource().getDB();

    DBCollection coll = db.getCollection(collection);

    Map<String, Object> q = (Map<String, Object>) ParserEngine.toQuery(query, parameter);
    Map<String, Object> f = (Map<String, Object>) ParserEngine.toField(field, parameter);

    DBObject queryDbo = new BasicDBObject(q);
    logger.debug("Execute 'distinct' mongodb command. Query '" + queryDbo + "'.");

    DBObject fieldDbo = (f == null) ? null : new BasicDBObject(f);
    logger.debug("Execute 'distinct' mongodb command. Field '" + fieldDbo + "'.");

    final List resultSet = coll.distinct(key, queryDbo, readPreference);
    logger.debug("Execute 'distinct' mongodb command. Result set '" + resultSet + "'.");
    if (handler == null) {
      List<T> result = new ArrayList<T>(resultSet.size());
      for (Iterator<Object> iter = resultSet.iterator(); iter.hasNext();) {
        result.add((T) ParserEngine.toResult(field, iter.next()));
      }
      return result;
    }

    handler.handleResult(new ResultContext() {
      @Override
      public Object getResultObject() {
        return resultSet;
      }

      @Override
      public int getResultCount() {
        return resultSet.size();
      }
    });
    return null;
  }

  @Override
  public String insert(String statement) {
    return insert(statement, null);
  }

  @Override
  public String insert(String statement, Object parameter) {
    logger.debug("Execute 'insert' mongodb command. Statement '" + statement + "'.");

    InsertConfig config = (InsertConfig) factory.getConfiguration().getConfig(statement);
    if (config == null) {
      throw new MongoDaoException(statement, "Insert statement id '" + statement + "' not found.");
    }

    String collection = config.getCollection();
    NodeEntry document = config.getDocument();
    Entry selectKey = config.getSelectKey();

    DB db = factory.getDataSource().getDB();

    DBCollection coll = db.getCollection(collection);

    Map<String, Object> doc = (Map<String, Object>) ParserEngine.toQuery(document, parameter);

    DBObject docDbo = new BasicDBObject(doc);
    logger.debug("Execute 'insert' mongodb command. Doc '" + docDbo + "'.");

    WriteResult writeResult = coll.insert(docDbo, WriteConcern.SAFE);
    CommandResult commandResult = writeResult.getLastError(WriteConcern.SAFE);
    if (commandResult.getException() != null) {
      throw new MongoDaoException(statement, "Execute 'insert' mongodb command has exception. Cause: " + commandResult.getErrorMessage());
    }

    String newId = docDbo.get("_id").toString();
    logger.debug("Execute 'insert' mongodb command. ObjectId is '" + newId + "'.");

    if (selectKey != null) {
      ParserEngine.setSelectKey(selectKey, newId, parameter);
    }
    return newId;
  }

  @Override
  public <T> List<String> insertBatch(String statement, List<T> list) {
    logger.debug("Execute 'insertBatch' mongodb command. Statement '" + statement + "'.");

    InsertConfig config = (InsertConfig) factory.getConfiguration().getConfig(statement);
    if (config == null) {
      throw new MongoDaoException(statement, "Insert statement id '" + statement + "' not found.");
    }

    String collection = config.getCollection();
    NodeEntry document = config.getDocument();
    Entry selectKey = config.getSelectKey();

    DB db = factory.getDataSource().getDB();

    DBCollection coll = db.getCollection(collection);

    List<DBObject> docDboList = new ArrayList<DBObject>(list.size());
    for (T parameter : list) {
      Map<String, Object> doc = (Map<String, Object>) ParserEngine.toQuery(document, parameter);
      DBObject docDbo = new BasicDBObject(doc);
      docDboList.add(docDbo);
      logger.debug("Execute 'insert' mongodb command. Doc '" + docDbo + "'.");
    }

    WriteResult writeResult = coll.insert(docDboList, WriteConcern.SAFE);
    String errorMessage = writeResult.getLastError(WriteConcern.SAFE).getErrorMessage();
    if (!StringUtils.isBlank(errorMessage)) {
      throw new MongoDaoException(statement, "Execute 'insert' mongodb command has exception. Cause: " + errorMessage);
    }

    List<String> newIds = new ArrayList<String>(list.size());
    for (DBObject docDbo : docDboList) {
      String newId = docDbo.get("_id").toString();
      newIds.add(newId);
      logger.debug("Execute 'insert' mongodb command. ObjectId is '" + newId + "'.");
    }

    if (selectKey != null) {
      for (int i = 0; i < list.size(); i++) {
        T parameter = list.get(i);
        String newId = newIds.get(i);
        ParserEngine.setSelectKey(selectKey, newId, parameter);
      }
    }

    return newIds;
  }

  @Override
  public <T> T findAndModify(String statement) {
    return findAndModify(statement, null, null, false, false);
  }

  @Override
  public <T> T findAndModify(String statement, Object parameter) {
    return findAndModify(statement, parameter, null, false, false);
  }

  @Override
  public void findAndModify(String statement, ResultHandler handler) {
    findAndModify(statement, null, handler, false, false);
  }

  @Override
  public void findAndModify(String statement, Object parameter, ResultHandler handler) {
    findAndModify(statement, parameter, handler, false, false);
  }

  private <T> T findAndModify(String statement, Object parameter, ResultHandler handler, boolean returnNew, boolean upset) {
    logger.debug("Execute 'findAndModify' mongodb command. Statement '" + statement + "'.");

    UpdateConfig config = (UpdateConfig) factory.getConfiguration().getConfig(statement);
    if (config == null) {
      throw new MongoDaoException(statement, "FindAndModify statement id '" + statement + "' not found.");
    }

    String collection = config.getCollection();
    NodeEntry query = config.getQuery();
    NodeEntry action = config.getAction();
    NodeEntry field = config.getField();

    DB db = factory.getDataSource().getDB();

    DBCollection coll = db.getCollection(collection);

    Map<String, Object> q = (Map<String, Object>) ParserEngine.toQuery(query, parameter);
    Map<String, Object> a = (Map<String, Object>) ParserEngine.toAction(action, parameter);
    Map<String, Object> f = (Map<String, Object>) ParserEngine.toField(field, parameter);

    DBObject queryDbo = new BasicDBObject(q);
    logger.debug("Execute 'findAndModify' mongodb command. Query '" + queryDbo + "'.");

    DBObject actionDbo = (a == null) ? null : new BasicDBObject(a);
    logger.debug("Execute 'findAndModify' mongodb command. Action '" + actionDbo + "'.");

    DBObject fieldDbo = (f == null) ? null : new BasicDBObject(f);
    logger.debug("Execute 'findAndModify' mongodb command. Field '" + fieldDbo + "'.");

    final DBObject resultSet = coll.findAndModify(queryDbo, fieldDbo, null, false, actionDbo, returnNew, upset);
    logger.debug("Execute 'findAndModify' mongodb command. Result set '" + resultSet + "'.");

    if (handler == null) {
      return (T) ParserEngine.toResult(field, resultSet);
    }

    handler.handleResult(new ResultContext() {
      @Override
      public Object getResultObject() {
        return resultSet;
      }

      @Override
      public int getResultCount() {
        if (resultSet == null) {
          return 0;
        }
        return 1;
      }
    });
    return null;
  }

  @Override
  public int update(String statement) {
    return update(statement, null);
  }

  @Override
  public int update(String statement, Object parameter) {
    logger.debug("Execute 'update' mongodb command. Statement '" + statement + "'.");

    UpdateConfig config = (UpdateConfig) factory.getConfiguration().getConfig(statement);
    if (config == null) {
      throw new MongoDaoException(statement, "Update statement id '" + statement + "' not found.");
    }

    String collection = config.getCollection();
    NodeEntry query = config.getQuery();
    NodeEntry action = config.getAction();

    DB db = factory.getDataSource().getDB();

    DBCollection coll = db.getCollection(collection);

    Map<String, Object> q = (Map<String, Object>) ParserEngine.toQuery(query, parameter);
    Map<String, Object> a = (Map<String, Object>) ParserEngine.toAction(action, parameter);

    DBObject queryDbo = new BasicDBObject(q);
    logger.debug("Execute 'update' mongodb command. Query '" + queryDbo + "'.");

    DBObject actionDbo = (a == null) ? null : new BasicDBObject(a);
    logger.debug("Execute 'update' mongodb command. Action '" + actionDbo + "'.");

    WriteResult writeResult = coll.update(queryDbo, actionDbo, false, true, WriteConcern.SAFE);
    CommandResult commandResult = writeResult.getLastError(WriteConcern.SAFE);
    if (commandResult.getException() != null) {
      throw new MongoDaoException(statement, "Execute 'update' mongodb command has exception. Cause: " + commandResult.getErrorMessage());
    }

    return writeResult.getN();
  }

  @Override
  public int delete(String statement) {
    return delete(statement, null);
  }

  @Override
  public int delete(String statement, Object parameter) {
    logger.debug("Execute 'delete' mongodb command. Statement '" + statement + "'.");

    DeleteConfig config = (DeleteConfig) factory.getConfiguration().getConfig(statement);
    if (config == null) {
      throw new MongoDaoException(statement, "Delete statement id '" + statement + "' not found.");
    }

    String collection = config.getCollection();
    NodeEntry query = config.getQuery();

    DB db = factory.getDataSource().getDB();

    DBCollection coll = db.getCollection(collection);

    Map<String, Object> q = (Map<String, Object>) ParserEngine.toQuery(query, parameter);

    DBObject queryDbo = new BasicDBObject(q);
    logger.debug("Execute 'delete' mongodb command. Query '" + queryDbo + "'.");

    WriteResult writeResult = coll.remove(queryDbo, WriteConcern.SAFE);
    CommandResult commandResult = writeResult.getLastError(WriteConcern.SAFE);
    if (commandResult.getException() != null) {
      throw new MongoDaoException(statement, "Execute 'delete' mongodb command has exception. Cause: " + commandResult.getErrorMessage());
    }
    return writeResult.getN();
  }

  @Override
  public <T> T command(String statement) {
    return command(statement, null, null, ReadPreference.secondaryPreferred());
  }

  @Override
  public <T> T command(String statement, Object parameter) {
    return command(statement, parameter, null, ReadPreference.secondaryPreferred());
  }

  @Override
  public void command(String statement, ResultHandler handler) {
    command(statement, null, handler, ReadPreference.secondaryPreferred());
  }

  @Override
  public void command(String statement, Object parameter, ResultHandler handler) {
    command(statement, parameter, handler, ReadPreference.secondaryPreferred());
  }

  private <T> T command(String statement, Object parameter, ResultHandler handler, ReadPreference readPreference) {
    logger.debug("Execute 'command' mongodb command. Statement '" + statement + "'.");

    CommandConfig config = (CommandConfig) factory.getConfiguration().getConfig(statement);
    if (config == null) {
      throw new MongoDaoException(statement, "Command statement id '" + statement + "' not found.");
    }

    NodeEntry query = config.getQuery();
    NodeEntry field = config.getField();

    DB db = factory.getDataSource().getDB();

    Map<String, Object> q = (Map<String, Object>) ParserEngine.toQuery(query, parameter);

    DBObject queryDbo = new BasicDBObject(q);
    logger.debug("Execute 'command' mongodb command. Query '" + queryDbo + "'.");

    final CommandResult resultSet = db.command(queryDbo, 0, readPreference);
    logger.debug("Execute 'command' mongodb command. Result set '" + resultSet + "'.");

    if (handler == null) {
      return (T) ParserEngine.toResult(field, resultSet);
    }

    handler.handleResult(new ResultContext() {
      @Override
      public Object getResultObject() {
        return resultSet;
      }

      @Override
      public int getResultCount() {
        return resultSet.size();
      }
    });
    return null;
  }

  @Override
  public <T> T group(String statement) {
    return group(statement, null, null, ReadPreference.secondaryPreferred());
  }

  @Override
  public <T> T group(String statement, Object parameter) {
    return group(statement, parameter, null, ReadPreference.secondaryPreferred());
  }

  @Override
  public void group(String statement, ResultHandler handler) {
    group(statement, null, handler, ReadPreference.secondaryPreferred());
  }

  @Override
  public void group(String statement, Object parameter, ResultHandler handler) {
    group(statement, parameter, handler, ReadPreference.secondaryPreferred());
  }

  private <T> T group(String statement, Object parameter, ResultHandler handler, ReadPreference readPreference) {
    logger.debug("Execute 'group' mongodb command. Statement '" + statement + "'.");

    GroupConfig config = (GroupConfig) factory.getConfiguration().getConfig(statement);
    if (config == null) {
      throw new MongoDaoException(statement, "Group statement id '" + statement + "' not found.");
    }

    String collection = config.getCollection();
    NodeEntry key = config.getKey();
    NodeEntry condition = config.getCondition();
    NodeEntry initial = config.getInitial();
    String reduce = config.getReduce();
    String finalize = config.getFinalize();
    NodeEntry field = config.getField();

    DB db = factory.getDataSource().getDB();

    DBCollection coll = db.getCollection(collection);
    coll.setReadPreference(readPreference);

    Map<String, Object> k = (Map<String, Object>) ParserEngine.toField(key, parameter);
    Map<String, Object> c = (Map<String, Object>) ParserEngine.toQuery(condition, parameter);
    Map<String, Object> i = (Map<String, Object>) ParserEngine.toQuery(initial, parameter);
    String r = ParserEngine.toScript(reduce, parameter);
    String f = ParserEngine.toScript(finalize, parameter);

    DBObject keyDbo = new BasicDBObject(k);
    logger.debug("Execute 'group' mongodb command. Key '" + keyDbo + "'.");

    DBObject conditionDbo = new BasicDBObject(c);
    logger.debug("Execute 'group' mongodb command. Condition '" + conditionDbo + "'.");

    DBObject initialDbo = new BasicDBObject(i);
    logger.debug("Execute 'group' mongodb command. Initial '" + initialDbo + "'.");

    logger.debug("Execute 'group' mongodb command. Reduce '" + r + "'.");
    logger.debug("Execute 'group' mongodb command. Finalize '" + f + "'.");

    final DBObject resultSet = coll.group(keyDbo, conditionDbo, initialDbo, r, f, readPreference);
    logger.debug("Execute 'group' mongodb command. Result set '" + resultSet + "'.");

    if (handler == null) {
      return (T) ParserEngine.toResult(field, resultSet);
    }

    handler.handleResult(new ResultContext() {
      @Override
      public Object getResultObject() {
        return resultSet;
      }

      @Override
      public int getResultCount() {
        if (resultSet == null) {
          return 0;
        }
        return 1;
      }
    });
    return null;
  }

  @Override
  public <T> List<T> aggregate(String statement) {
    return aggregate(statement, null, null, ReadPreference.secondaryPreferred());
  }

  @Override
  public <T> List<T> aggregate(String statement, Object[] parameter) {
    return aggregate(statement, parameter, null, ReadPreference.secondaryPreferred());
  }

  @Override
  public void aggregate(String statement, ResultHandler handler) {
    aggregate(statement, null, handler, ReadPreference.secondaryPreferred());
  }

  @Override
  public void aggregate(String statement, Object[] parameter, ResultHandler handler) {
    aggregate(statement, parameter, handler, ReadPreference.secondaryPreferred());
  }

  private <T> List<T> aggregate(String statement, Object[] parameter, ResultHandler handler, ReadPreference readPreference) {
    logger.debug("Execute 'aggregate' mongodb command. Statement '" + statement + "'.");

    AggregateConfig config = (AggregateConfig) factory.getConfiguration().getConfig(statement);
    if (config == null) {
      throw new MongoDaoException(statement, "Aggregate statement id '" + statement + "' not found.");
    }

    String collection = config.getCollection();
    List<NodeEntry> function = config.getFunction();
    NodeEntry field = config.getField();

    DB db = factory.getDataSource().getDB();

    DBCollection coll = db.getCollection(collection);
    coll.setReadPreference(readPreference);

    NodeEntry firstFunction = function.remove(0);
    Map<String, Object> f = (Map<String, Object>) ParserEngine.toQuery(firstFunction, parameter);
    DBObject firstOp = new BasicDBObject(f);
    logger.debug("Execute 'aggregate' mongodb command. First Operation '" + firstOp + "'.");

    DBObject[] operations = new DBObject[function.size()];
    for (int i = 0; i < function.size(); i++) {
      NodeEntry ne = function.get(i);
      Map<String, Object> op = (Map<String, Object>) ParserEngine.toQuery(ne, parameter);
      DBObject operationDbo = new BasicDBObject(op);
      operations[i] = operationDbo;
      logger.debug("Execute 'aggregate' mongodb command. Operation '" + operationDbo + "'.");
    }

    CommandResult commandResult = coll.aggregate(firstOp, operations).getCommandResult();
    logger.debug("Execute 'aggregate' mongodb command. Result set '" + commandResult + "'.");

    final BasicDBList resultSet = (BasicDBList) commandResult.get("result");
    if (handler == null) {
      List<T> list = new ArrayList<T>(resultSet.size());
      for (Iterator iter = resultSet.iterator(); iter.hasNext();) {
        T result = (T) ParserEngine.toResult(field, iter.next());
        list.add(result);
      }
      return list;
    }

    handler.handleResult(new ResultContext() {
      @Override
      public Object getResultObject() {
        return resultSet;
      }

      @Override
      public int getResultCount() {
        return resultSet.size();
      }
    });
    return null;
  }

  @Override
  public DB getDB() {
    DB db = factory.getDataSource().getDB();
    logger.debug("Execute get mongodb. DB info '" + db + "'.");
    return db;
  }

  @Override
  public DB getDB(String dbName) {
    if (StringUtils.isBlank(dbName)) {
      throw new MongoDaoException("getDB", "Execute get mongodb command. The db name is blank.");
    }

    DB db = factory.getDataSource().getClient().getDB(dbName);
    logger.debug("Execute get mongodb '" + dbName + "'. DB info '" + db + "'.");
    return db;
  }

  public void setFactory(MongoORMFactoryBean factory) {
    this.factory = factory;
  }

}
