package io.jettra.flux.widgets;

import io.jettra.flux.theme.Themes;
import io.jettra.test.annotation.DisplayName;
import io.jettra.test.annotation.NotRequiresRunningServer;
import io.jettra.test.annotation.Test;

import static io.jettra.test.core.JettraAssert.*;

@NotRequiresRunningServer
public class AssignmentStatusBadgeTest {

    @Test
    @DisplayName("AssignmentStatusBadge: Verify status badge types, icons, and contextual database labels")
    void testAssignmentStatusBadgeConstruction() {
        AssignmentStatusBadge auth = AssignmentStatusBadge.authorized().databaseContext("analytics_db");
        assertEquals(AssignmentStatusBadge.StatusType.AUTHORIZED, auth.statusType());
        assertTrue(auth.label().contains("Authorized (analytics_db)"));

        AssignmentStatusBadge revoked = AssignmentStatusBadge.revokedPreserved();
        assertEquals(AssignmentStatusBadge.StatusType.REVOKED_PRESERVED, revoked.statusType());
        assertTrue(revoked.label().contains("Revoked (Preserved)"));

        AssignmentStatusBadge unassigned = AssignmentStatusBadge.notAssigned();
        assertEquals("Not Assigned", unassigned.label());
    }

    @Test
    @DisplayName("AssignmentStatusBadge: Verify HTML rendering contains icons, CSS classes, and labels")
    void testAssignmentStatusBadgeRendering() {
        AssignmentStatusBadge badge = AssignmentStatusBadge.revokedPreserved().id("badge_test");
        String html = badge.render(Themes.Dark());

        assertTrue(html.contains("id=\"badge_test\""));
        assertTrue(html.contains("class=\"jettra-assignment-status-badge badge-revoked_preserved\""));
        assertTrue(html.contains("fa-user-shield"));
        assertTrue(html.contains("Revoked (Preserved)"));
    }

    @Test
    @DisplayName("IdentityPreservationNotice: Verify banner rendering conveys immutability policy")
    void testIdentityPreservationNoticeRendering() {
        IdentityPreservationNotice notice = IdentityPreservationNotice.create()
            .title("Identity Preservation Guarantee")
            .dismissible(true);

        String html = notice.render(Themes.Dark());

        assertTrue(html.contains("Identity Preservation Guarantee"));
        assertTrue(html.contains("fa-shield-alt"));
        assertTrue(html.contains("permanent in system_db"));
        assertTrue(html.contains("button type=\"button\""));
    }
}
