/*
 * Hibernate Tools, Tooling for your Hibernate Projects
 * 
 * Copyright 2018-2020 Red Hat, Inc.
 *
 * Licensed under the GNU Lesser General Public License (LGPL), 
 * version 2.1 or later (the "License").
 * You may not use this file except in compliance with the License.
 * You may read the licence in the 'lgpl.txt' file in the root folder of 
 * project or obtain a copy at
 *
 *     http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.cfg.reveng.dialect;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.api.reveng.RevengDialect;
import org.hibernate.tool.internal.reveng.dialect.OracleMetaDataDialect;
import org.hibernate.tools.test.util.JdbcUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestCase {

    private Properties properties = null;
    private ServiceRegistry serviceRegistry;

    @Before
    public void setUp() {
        JdbcUtil.createDatabase(this);
        properties = Environment.getProperties();
        StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder();
        serviceRegistry = ssrb.build();
    }

    @After
    public void tearDown() {
        JdbcUtil.dropDatabase(this);
    }

    @Test
    public void testColumnTypeSizes() {
        RevengDialect dialect = configureOracleMetaDataDialect();

        assertSqlTypeLengths(dialect, "a_varchar2_char", "VARCHAR2", 10, 0);
        assertSqlTypeLengths(dialect, "a_varchar2_byte", "VARCHAR2", 10, 0);
        assertSqlTypeLengths(dialect, "a_varchar_char", "VARCHAR2", 10, 0);
        assertSqlTypeLengths(dialect, "a_varchar_byte", "VARCHAR2", 10, 0);
        assertSqlTypeLengths(dialect, "a_nvarchar", "NVARCHAR2", 10, 0);
        assertSqlTypeLengths(dialect, "a_char_char", "CHAR", 10, 0);
        assertSqlTypeLengths(dialect, "a_char_byte", "CHAR", 10, 0);
        assertSqlTypeLengths(dialect, "a_nchar_char", "NCHAR", 10, 0);
        assertSqlTypeLengths(dialect, "a_nchar_byte", "NCHAR", 10, 0);
        assertSqlTypeLengths(dialect, "a_number_int", "NUMBER", 10, 0);
        assertSqlTypeLengths(dialect, "a_number_dec", "NUMBER", 10, 2);
        assertSqlTypeLengths(dialect, "a_float", "FLOAT", 10, 0);
    }

    private RevengDialect configureOracleMetaDataDialect() {
        RevengDialect dialect = new OracleMetaDataDialect();
        ConnectionProvider connectionProvider = serviceRegistry.getService(ConnectionProvider.class);
        dialect.configure(connectionProvider);
        return dialect;
    }

    private void assertSqlTypeLengths(RevengDialect dialect, String columnName, String typeName, int columnSize, int decimalDigits) {
        columnName = columnName.toUpperCase();
        String catalog = properties.getProperty(AvailableSettings.DEFAULT_CATALOG);
        String schema = properties.getProperty(AvailableSettings.DEFAULT_SCHEMA);
        Iterator<Map<String, Object>> columns = dialect.getColumns(catalog, schema, "PERSON", columnName);
        boolean found = false;
        while (columns.hasNext()) {
            Map<String, Object> column = columns.next();
            assertThat(column.get("COLUMN_NAME"), equalTo(columnName.toUpperCase()));
            assertThat(column.get("TYPE_NAME"), equalTo(typeName));
            assertThat(column.get("COLUMN_SIZE"), equalTo(columnSize));
            assertThat(column.get("DECIMAL_DIGITS"), equalTo(decimalDigits));
            found = true;
        }
        assertThat("Expected column '" + columnName + "'to exist.", found, equalTo(true));
    }
}
