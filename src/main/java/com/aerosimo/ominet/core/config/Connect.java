/******************************************************************************
 * This piece of work is to enhance conflux project functionality.            *
 *                                                                            *
 * Author:    eomisore                                                        *
 * File:      Connect.java                                                    *
 * Created:   21/10/2025, 01:20                                               *
 * Modified:  21/10/2025, 01:20                                               *
 *                                                                            *
 * Copyright (c)  2025.  Aerosimo Ltd                                         *
 *                                                                            *
 * Permission is hereby granted, free of charge, to any person obtaining a    *
 * copy of this software and associated documentation files (the "Software"), *
 * to deal in the Software without restriction, including without limitation  *
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,   *
 * and/or sell copies of the Software, and to permit persons to whom the      *
 * Software is furnished to do so, subject to the following conditions:       *
 *                                                                            *
 * The above copyright notice and this permission notice shall be included    *
 * in all copies or substantial portions of the Software.                     *
 *                                                                            *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,            *
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES            *
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                   *
 * NONINFINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT                 *
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,               *
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING               *
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE                 *
 * OR OTHER DEALINGS IN THE SOFTWARE.                                         *
 *                                                                            *
 ******************************************************************************/

package com.aerosimo.ominet.core.config;

import jakarta.mail.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Utility class to fetch JavaMail Session from JNDI safely.
 */
public class Connect {

    private static final Logger log = LogManager.getLogger(Connect.class);

    private Connect() {
        // utility class, no instantiation
    }

    /**
     * Returns a JavaMail Session from JNDI ("mail/aerosimo").
     * Each call does a fresh lookup to avoid stale references.
     *
     * @return Jakarta Mail Session or null if lookup fails
     */
    public static Session email() {
        log.info("Preparing to get email session from JNDI");
        try {
            Context ctx = new InitialContext();
            try {
                Context env = (Context) ctx.lookup("java:/comp/env");
                Session sess = (Session) env.lookup("mail/aerosimo");
                log.debug("Successfully retrieved mail session via JNDI");
                return sess;
            } finally {
                ctx.close(); // prevent context leak
            }
        } catch (Exception err) {
            log.error("Email session lookup failed in {}", Connect.class.getName(), err);
            return null;
        }
    }

    /**
     * Returns a connection via JDBC Data Sources from JNDI ("jdbc/hats").
     * Each call does a fresh lookup to avoid stale references.
     *
     * @return Java SQL connection or null if lookup fails
     */
    public static Connection dbase() {
        log.debug("Fetching a new connection from Oracle DataSource");
        Connection con = null;
        try{
            log.info("Looking up JNDI DataSource for Oracle DB");
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/hats");
            con = ds.getConnection();
            log.info("Connection Established successfully");
        } catch (NamingException | SQLException err) {
            log.error("JNDI lookup for Oracle DB failed", err);
        }
        return con;
    }
}