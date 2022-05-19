package ca.bc.gov.hlth.mohums.controller;

import java.util.List;
import java.util.Map;
import java.sql.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/dashboard/active-user-count")
    public List<Map<String, Object>> getActiveUserCount() throws SQLException {
        String sql = "SELECT realm_id AS REALM, client_id AS CLIENT, COUNT(1) AS ACTIVE_USER_COUNT FROM ( SELECT DISTINCT ue.realm_id, c.client_id, ue.username FROM event_entity ee INNER JOIN user_entity ue ON ue.id = ee.user_id INNER JOIN client c ON c.client_id = ee.client_id INNER JOIN keycloak_role kcr ON kcr.client = c.id INNER JOIN user_role_mapping urm ON urm.user_id = ee.user_id AND urm.role_id = kcr.id WHERE ee.type = 'LOGIN' AND ee.event_time > (SYSDATE-365-TO_DATE('1970-01-01','YYYY-MM-DD'))*24*60*60 AND ue.enabled = 1 UNION SELECT DISTINCT ue.realm_id, c.client_id, ue.username FROM event_entity ee INNER JOIN user_entity ue ON ue.id = ee.user_id INNER JOIN client c ON c.client_id = ee.client_id LEFT OUTER JOIN keycloak_role kcr ON kcr.client = c.id WHERE ee.type = 'LOGIN' AND ee.event_time > (SYSDATE-365-TO_DATE('1970-01-01','YYYY-MM-DD'))*24*60*60 AND ue.enabled = 1  AND kcr.id IS NULL ) WHERE NOT (client_id = 'account' AND (realm_id IN ('bceid_basic', 'bceid_business', 'idir', 'idir_aad', 'master', 'moh_applications', 'moh_citizen', 'phsa')) OR (client_id IN ('realm-management', 'USER-MANAGEMENT-SERVICE', 'PRIME-WEBAPP-ENROLLMENT'))) GROUP BY realm_id, client_id  ORDER BY realm_id ASC, client_id ASC";

        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/dashboard/total-number-of-users")
    public Object getTotalNumberOfUsers() throws SQLException {
        String sql = "SELECT COUNT(1) AS TOTAL_USER_COUNT FROM (SELECT DISTINCT id, realm_id FROM user_entity WHERE (realm_id IN ('bceid_basic', 'bceid_business', 'idir', 'phsa')) OR (realm_id = 'idir_aad' AND username NOT IN (SELECT username FROM user_entity WHERE realm_id = 'idir')) AND enabled = 1)";

        return jdbcTemplate.queryForList(sql).get(0).get("TOTAL_USER_COUNT");
    }

    @GetMapping("/dashboard/unique-user-count-by-idp")
    public List<Map<String, Object>> getUniqueUserCountByIDP() throws SQLException {
        String sql = " SELECT realm_id AS IDP, COUNT(1) AS UNIQUE_USER_COUNT FROM ( SELECT DISTINCT id, realm_id FROM user_entity WHERE realm_id IN ('bceid_basic', 'bceid_business', 'idir', 'idir_aad', 'phsa') AND enabled = 1 ) GROUP BY realm_id ORDER BY realm_id ASC";

        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/dashboard/unique-user-count-by-realm")
    public List<Map<String, Object>> getUniqueUserCountByRealm() throws SQLException {
        String sql = "SELECT realm_id AS REALM, COUNT(1) AS UNIQUE_USER_COUNT FROM ( SELECT DISTINCT id, realm_id FROM user_entity WHERE realm_id NOT IN ('bceid_basic', 'bceid_business', 'idir', 'idir_aad', 'master', 'phsa') AND enabled = 1 ) GROUP BY realm_id ORDER BY realm_id ASC";

        return jdbcTemplate.queryForList(sql);
    }

}