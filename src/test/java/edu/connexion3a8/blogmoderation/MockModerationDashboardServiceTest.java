package edu.connexion3a8.blogmoderation;

import edu.connexion3a8.blogmoderation.domain.ModerationAction;
import edu.connexion3a8.blogmoderation.domain.UserRole;
import edu.connexion3a8.blogmoderation.dto.ModerationDecisionCommand;
import edu.connexion3a8.blogmoderation.dto.ModerationFilter;
import edu.connexion3a8.blogmoderation.repository.InMemoryModerationRepository;
import edu.connexion3a8.blogmoderation.service.MockModerationDashboardService;
import edu.connexion3a8.blogmoderation.service.ModerationDashboardService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockModerationDashboardServiceTest {
    @Test
    void shouldFilterByQuery() {
        ModerationDashboardService service = new MockModerationDashboardService(new InMemoryModerationRepository());
        ModerationFilter filter = new ModerationFilter();
        filter.setQuery("botpromo");
        assertEquals(1, service.count(filter));
    }

    @Test
    void shouldRejectVersionMismatch() {
        ModerationDashboardService service = new MockModerationDashboardService(new InMemoryModerationRepository());
        ModerationDecisionCommand cmd = new ModerationDecisionCommand(1001, 999, ModerationAction.HIDE, "moderator1", "abus", "");
        assertThrows(IllegalStateException.class, () -> service.decide(cmd, UserRole.SENIOR_MODERATOR));
    }

    @Test
    void shouldRejectForbiddenActionForModerator() {
        ModerationDashboardService service = new MockModerationDashboardService(new InMemoryModerationRepository());
        long version = service.search(new ModerationFilter(), 0, 20).get(0).getVersion();
        long reportId = service.search(new ModerationFilter(), 0, 20).get(0).getReportId();
        ModerationDecisionCommand cmd = new ModerationDecisionCommand(reportId, version, ModerationAction.DELETE, "moderator1", "toxic", "");
        assertThrows(SecurityException.class, () -> service.decide(cmd, UserRole.MODERATOR));
    }
}
