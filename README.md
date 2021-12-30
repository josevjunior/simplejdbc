# Simple Jdbc
A simple database row mapper for Java

When we're using jdbc api directly a very common if we don't want to handle a resultset directly in the core code, we first map it to a domain pojo


    ModelClass modelCls = new ModelClass();
    ResultSet rs = stam.executeQuery();
    while(rs.next()) {
        modelCls.setPropertyA(rs.getString("COLUMN_A"));
        modelCls.setPropertyB(rs.getDouble("COLUMN_B"));
        modelCls.setPropertyC(rs.getTimestamp("COLUMN_C"));
        ...
    }


The necessary code to achieve the model mapping is boilerplate and can be very tedious and error prone. The more column there are, higher is the chance to happen human errors

Thinking about that, the SimpleJdbc provides a simple and extensible way to map a resultset to a model class

Everything starts with the `io.github.josevjunior.simplejdbc.QueryCreator` class that can be created using a `java.sql.Connection` or a `javax.sql.DataSource`

    Connection connection = ...
    QueryCreator qc = new QueryCreator(connection);

or 

    DataSource dataSource = ...
    QueryCreator qc = new QueryCreator(dataSource);

The QueryCreator is the entrypoint where the queries are created. All the it needs is a query string and the corresponding type that will be mapped to. The code bellow shows how simple is to map a query result in a list of your own models

e.g:

    QueryCreator qc = new QueryCreator(connection);
    Query<Employee> query = qc.create("SELECT ID, NAME, SALARY, BIRTHDAY FROM EMPLOYEE", Employee.class);
    List<Employee> employees = query.getResultList();

## The RowMapper interface
The core of all map functionality leaves in the `io.github.josevjunior.simplejdbc.RowMapper<T>` which is a one method interface the gives a `java.sql.ResultSet` and `java.sql.ResultSetMetaData` and waits for a `<T>` result:

    public interface RowMapper<T> {
    
        public T map(ResultSet resultSet, ResultSetMetaData mtdt) throws SQLException;
    
    }

Every created Query contains a RowMapper implementation that will be used to transform the raw database value in a high level object of your business. The mapper will be picked by the registered associated type or you can use your own implementation

- Mapping by a type:

        QueryCreator qc = new QueryCreator(connection);
        Query<Employee> query = qc.create("SELECT ID, NAME, SALARY, BIRTHDAY FROM EMPLOYEE", Employee.class);

    In this case, will be verified if there is a RowMapper registered for the `Employee` type. If there is, it will be used. Otherwise, the `io.github.josevjunior.simplejdbc.BasicBeanMapper` will do the work.

- Using a RowMapper of your own:

        class MyEmployeeRowMapper implements RowMapper<Employee> {

            public Employee map(ResultSet resultSet, ResultSetMetaData mtdt) throws SQLException{

                Employee employee = new Employee();
                employee.setId(resultSet.getLong("ID"));
                employee.setName(resultSet.getString("NAME"));
                employee.setSalary(resultSet.getDouble("SALARY"));
                employee.setBirthday(resultSet.getDate("BIRTHDAY"));

                return employee;

            }

        }

        QueryCreator qc = new QueryCreator(connection);
        MyEmployeeRowMapper mapper = new MyEmployeeRowMapper();
        Query<Employee> query = qc.create("SELECT ID, NAME, SALARY, BIRTHDAY FROM EMPLOYEE", mapper);

    Sometimes, the default behaviour doesn't fit our necessities and a costumized job is need. So, is possible to use a different RowMapper in specific Query whithout bind it to a fixed type

- Register a RowMapper of your own:

        // once registered, you can use everywhere
        QueryCreator.setDefaultTypeMapper(Employee.class, new MyEmployeeRowMapper()); 

        QueryCreator qc = new QueryCreator(connection);
        Query<Employee> query = qc.create("SELECT ID, NAME, SALARY, BIRTHDAY FROM EMPLOYEE", Employee.class); // MyEmployeeRowMapper will be used here

    If the default behaviour not attends you at all, you can register a different default mapper for one or more type and used at your all apllication

## Out of the box RowMapper's
The library contains a set of built-in mapper that can be used from beginning. They are:

1. **io.github.josevjunior.simplejdbc.BasicBeanMapper**: The more complete and complex mapper. Used to map a ResultSet in a concrete class instance. The [Apache Commons DbUtils](https://github.com/apache/commons-dbutils) project is used as the bean mapper engine.

2. **io.github.josevjunior.simplejdbc.ArrayRowMapper**: Map all the ResultSet column values in a array. Perfect for scenarios where only the values matters

3. **io.github.josevjunior.simplejdbc.MapRowMapper**: Map all the ResultSet column values in a `java.util.Map` implementation. As the column's aliases are used as the map key, empty aliases is not good idea here

## Using a Query
The `io.github.josevjunior.simplejdbc.Query` is the bridge between the native jdbc connection and the row mapper. To map the data itself some methods are available:

1. **Query.getResultList()**: The simplest. Use it when you need a list of mapped data ready to be used. All the ResultSet will be iterate before to map the data.

    Example:
        
        QueryCreator qc = new QueryCreator(connection);
        Query<Employee> query = qc.create("SELECT ID, NAME, SALARY, BIRTHDAY FROM EMPLOYEE", Employee.class);

        List<Employee> employees = query.getResultList();
        
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(employees); // Image we're gonna send to a front end

2. **Query.getFirstResult()**: When you need only the first result of the query. The ResultSet.next() method will be called only once. If returned true, a filled `java.util.Optional` will be returned. If does not, a empty Optional will be in place.

    Example:

        QueryCreator qc = new QueryCreator(connection);
        Query<BigDecimal> query = qc.create("SELECT MAX(SALARY) FROM EMPLOYEE WHERE GROUPID = :GROUPID ", BigDecimal.class);
        query.setParameter("GROUPID", 1);
        
        BigDecimal max = query.getFirstResult().orElse(BigDecimal.ZERO);

3. **Query.getScrollableResult()**: The overhead of the `getResultList()` can be a problem in some cases. The `io.github.josevjunior.simplejdbc.ScrollableResult` is a lazy mapper implementation is that cases you dont need all the list in memory at same time. *Note: * How the ScrollableResult hold a ResultSet, it must be closed after used.

    Example:

        QueryCreator qc = new QueryCreator(connection);
        Query<Employee> query = qc.create("SELECT ID, NAME, SALARY, BIRTHDAY FROM EMPLOYEE", Employee.class);
        try(ScrollableResult<Employee> result = query.getScrollableResult()){
            while(result.next()) {
                Employee employee = result.get(); // The result will be mapped once this methos was called
                if(employee.getSalary() == 0.0) {
                    throw new Exception("A slave was identified");
                }
            }
        }


    
