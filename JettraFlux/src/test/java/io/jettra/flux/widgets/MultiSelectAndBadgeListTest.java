package io.jettra.flux.widgets;

import io.jettra.flux.security.SecurityPrincipal;
import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import java.util.List;
import java.util.Set;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class MultiSelectAndBadgeListTest {

    @Test
    @DisplayName("Should render MultiSelect with options, pills, quick-actions, and hidden inputs")
    void testMultiSelectRender() {
        MultiSelect ms = MultiSelect.of("dbMultiSelect", "target_dbs")
            .label("Assigned Databases")
            .selectAllOption(true, "* (All Databases)")
            .options("customers_db", "analytics_db", "orders_db")
            .selectedValues("customers_db", "analytics_db")
            .quickActions(true);

        String html = ms.render(Themes.FlatTheme());

        assertNotNull(html);
        assertTrue(html.contains("id=\"dbMultiSelect\""), "Must contain root id");
        assertTrue(html.contains("Assigned Databases"), "Must render label");
        assertTrue(html.contains("Select All"), "Must render Select All quick action");
        assertTrue(html.contains("Clear"), "Must render Clear quick action");
        assertTrue(html.contains("name=\"target_dbs\""), "Must have checkbox input name target_dbs");
        assertTrue(html.contains("name=\"target_dbs_csv\""), "Must have CSV hidden input");
        assertTrue(html.contains("name=\"target_db\""), "Must have legacy target_db hidden input for compatibility");
        assertTrue(html.contains("* (All Databases)"), "Must render wildcard option");
        assertTrue(html.contains("customers_db"), "Must render customers_db option");
        assertTrue(html.contains("analytics_db"), "Must render analytics_db option");
        assertTrue(html.contains("orders_db"), "Must render orders_db option");
        assertTrue(html.contains("window.JettraMultiSelect"), "Must include client helper functions");
    }

    @Test
    @DisplayName("Should render BadgeList with tags and wildcard formatting")
    void testBadgeListRender() {
        BadgeList list = BadgeList.of(List.of("*", "finance_db", "inventory_db"));
        String html = list.render(Themes.FlatTheme());

        assertNotNull(html);
        assertTrue(html.contains("ALL DATABASES"), "Wildcard should be formatted as ALL DATABASES");
        assertTrue(html.contains("finance_db"), "Must render finance_db badge");
        assertTrue(html.contains("inventory_db"), "Must render inventory_db badge");
        assertTrue(html.contains("fas fa-globe"), "Wildcard should use globe icon");
        assertTrue(html.contains("fas fa-database"), "Database badge should use database icon");

        BadgeList empty = BadgeList.of(List.of()).emptyText("No Databases Assigned");
        String emptyHtml = empty.render(Themes.FlatTheme());
        assertTrue(emptyHtml.contains("No Databases Assigned"), "Empty list should render empty text");
    }

    @Test
    @DisplayName("SecurityPrincipal supports multi-database assignments and authorization check")
    void testSecurityPrincipalMultiDatabaseAuthorization() {
        SecurityPrincipal adminPrincipal = SecurityPrincipal.of("superadmin", "ADMIN", "IT", Set.of());
        assertTrue(adminPrincipal.isAuthorizedForDatabase("any_db"), "Admin has access to any database");

        SecurityPrincipal multiDbUser = SecurityPrincipal.of("analyst", "USER", "Analytics", Set.of("customers_db", "sales_db"));
        assertTrue(multiDbUser.isAuthorizedForDatabase("customers_db"), "User authorized for customers_db");
        assertTrue(multiDbUser.isAuthorizedForDatabase("sales_db"), "User authorized for sales_db");
        assertFalse(multiDbUser.isAuthorizedForDatabase("secret_db"), "User NOT authorized for secret_db");

        SecurityPrincipal wildcardUser = SecurityPrincipal.of("operator", "USER", "Ops", Set.of("*"));
        assertTrue(wildcardUser.isAuthorizedForDatabase("random_db"), "Wildcard user authorized for all databases");

        SecurityPrincipal legacyUser = SecurityPrincipal.of("dev", "USER", "", "db1, db2");
        assertTrue(legacyUser.isAuthorizedForDatabase("db1"), "CSV initialized user authorized for db1");
        assertTrue(legacyUser.isAuthorizedForDatabase("db2"), "CSV initialized user authorized for db2");
        assertFalse(legacyUser.isAuthorizedForDatabase("db3"), "CSV initialized user NOT authorized for db3");
    }

    @Test
    @DisplayName("JettraUserEditModal renders edit form with immutable username, roles, multiselect, and action buttons")
    void testUserEditModalRender() {
        JettraUserEditModal modal = JettraUserEditModal.of("editUserModal")
            .title("Edit User Profile & Permissions")
            .formAction("/users")
            .actionName("update_user")
            .roles("DB_ADMIN", "READ_WRITE", "READ_ONLY", "MANAGER")
            .databases("records_store", "analytics_db", "system_db")
            .submitText("Guardar Cambios")
            .cancelText("Cancelar");

        String html = modal.render(Themes.FlatTheme());

        assertNotNull(html);
        assertTrue(html.contains("id=\"editUserModal\""), "Must contain root overlay id");
        assertTrue(html.contains("id=\"editUserModal_username\""), "Must contain username input");
        assertTrue(html.contains("readonly"), "Username input must be readonly/immutable");
        assertTrue(html.contains("fas fa-lock"), "Username input must display lock icon");
        assertTrue(html.contains("id=\"editUserModal_role\""), "Must contain role selector");
        assertTrue(html.contains("<option value=\"DB_ADMIN\">DB_ADMIN</option>"), "Must contain DB_ADMIN role option");
        assertTrue(html.contains("<option value=\"READ_WRITE\">READ_WRITE</option>"), "Must contain READ_WRITE role option");
        assertTrue(html.contains("id=\"editUserModal_active\""), "Must contain active status selector");
        assertTrue(html.contains("id=\"editUserModal_password\""), "Must contain optional password reset input");
        assertTrue(html.contains("id=\"editUserModal_dbs\""), "Must contain embedded MultiSelect for databases");
        assertTrue(html.contains("records_store"), "Must list records_store in database options");
        assertTrue(html.contains("Guardar Cambios"), "Must render submit button text");
        assertTrue(html.contains("Cancelar"), "Must render cancel button text");
        assertTrue(html.contains("window.JettraUserEditModal"), "Must include client-side lifecycle and hydration object");
        assertTrue(html.contains("setSelectedValues"), "Client script must invoke MultiSelect.setSelectedValues");
    }
}

