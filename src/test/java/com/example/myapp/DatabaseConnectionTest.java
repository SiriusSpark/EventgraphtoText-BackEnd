package com.example.myapp;

import com.example.myapp.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class DatabaseConnectionTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private Driver neo4jDriver;

    @Test
    void testMySQLConnection() {
        // Test MySQL connection by executing a simple query
        assertNotNull(userMapper.findAll(), "Should be able to query MySQL database");
    }

    @Test
    void testNeo4jConnection() {
        // Test Neo4j connection
        assertNotNull(neo4jDriver, "Neo4j driver should be autowired");
        try (var session = neo4jDriver.session()) {
            var result = session.run("MATCH (n) RETURN count(n) as count");
            assertNotNull(result.single().get("count"), "Should be able to execute Neo4j query");
        }
    }
} 