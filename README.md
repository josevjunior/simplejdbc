# Simple Jdbc
A simple database row mapper for Java

Declare the tags bellow in your pom.xml to use it with maven. **(For now only the SNAPSHOT builds are available)**

    <!-- Declare the central snapshot repository -->
    <repositories>
        <repository>
            <id>oss.sonatype.org-snapshot</id>
            <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
            <releases>
                <enabled>false</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>

    <dependency>
            <groupId>io.github.josevjunior</groupId>
            <artifactId>simplejdbc</artifactId>
            <version>1.1.1-SNAPSHOT</version>
    </dependency>



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

Example:

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
The `io.github.josevjunior.simplejdbc.Query` is the bridge between the native jdbc statement and the row mapper. To map the data itself some methods are available:

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

3. **Query.getScrollableResult()**: The overhead of the `getResultList()` can be a problem in some cases. The `io.github.josevjunior.simplejdbc.ScrollableResult` is a lazy mapper implementation is that cases you dont need all the list in memory at same time. *Note: As the ScrollableResult holds a ResultSet, it must be closed after use.*

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


## How the resources are manage
Create a `QueryCreator` can be made using a `java.sql.Connection` or a `javax.sql.DataSource`. If the `DataSource` constructor was used, the `getConnection()` method will be invoked to obtain the connection. Besides a connection, the `QueryCreator` holds all the statements created for each `Query`.

To close the connection and it repective statements, call `QueryCreator.closeAll()` for it. Call `QueryCreator.disposeResources()` to close all the resources (statements) but not the connection itself.

The `java.sql.ResultSet`'s are always closed when using `Query.getResultList()` and `Query.getFirstResult()`. If using the `Query.getScrollableResult()` the `ScrollableResult.close()` must be explicit called after use.


## Updating records
It's possible to update data using the Query class. Besides, it provides easy-to-use methods to build simples DML statements

Example 1:

        QueryCreator qc = new QueryCreator(connection);
        Query<Object[]> query = qc.create("UPDATE EMPLOYEE SET SALARY = SALARY * 2 WHERE PRODUCTIVITY = ? "); // If you dont pass a type, the ArrayRowMapper is used
        query.setParameter(1, "EXCELLENT");
        query.executeUpdate(); // Execute the update

Example 2:

        QueryCreator qc = new QueryCreator(connection);
        qc.update("EMPLOYEE")
            .set("SALARY", calculateSalary()) // Does not support SQL expression here
            .where()
            .col("PRODUCTIVITY", "EXCELLENT") 
            .col("SITUATION", "ACTIVE")
            .execute();

//The statement above will execute the following sql with the params setted:

`UPDATE EMPLOYEE SET SALARY = :PARAM_NAME WHERE PRODUCTIVITY = :PARAM_NAME AND SITUATION = :PARAM_NAME`

Example 3:

        QueryCreator qc = new QueryCreator(connection);
        qc.insert("EMPLOYEE")
            .col("NAME", "Joe")
            .col("BIRTHDAY", new Timestamp())
            .col("SALARY", 1000.0)
            .execute();

// The statement above will execute the following sql with the params setted:

`INSERT INTO EMPLOYEE NAME, BIRTHDAY SALARY VALUES(:PARAM_NAME, :PARAM_NAME, :PARAM_NAME)`


*Note: The library does not handle transactions. This must be do it by the client*


## Understanding the BasicBeanMapper

BasicBeanMapper is the default RowMapper used by the library to map the ResultSet in a bean(pojos, objects, etcs). The [Apache Commons DbUtils](https://github.com/apache/commons-dbutils) RowProcessor is used under the hood and it follows some rules that needs to be follow too.

Let's do a example. The following sql: "SELECT ID, NAME, SALARY, BIRTHDAY FROM EMPLOYEE" need to be map to the `Employee` class. The class must following the Java beans pattern, which means in provides a getter and setter for each property

    class Employe {

        // No constructor or a default constructor

        private Integer id;
        private String name;
        private BigDecimal salary;
        private Date birhtday;

        public Integer getId(){
            return id;
        }

        public void setId(Integer id){
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getSalary(){
            return salary;
        }

        public void setSalary(BigDecimal id){
            this.salary = salary;
        }

        public Date getBirhtday(){
            return birhtday;
        }

        public void setBirhtday(Date birhtday){
            this.birhtday = birhtday;
        }

    }

For each column alias declared in the sql a setter method will try to be found and invoked with the corresponding value

    setId => ID
    setName => NAME
    setSalary => SALARY
    setBirthday => BIRTHDAY

The type will be inferred too. So using a method with different type of the column may result in a exception being thrown

    
