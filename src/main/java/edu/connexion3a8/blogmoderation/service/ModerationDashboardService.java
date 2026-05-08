package edu.connexion3a8.blogmoderation.service;

import edu.connexion3a8.blogmoderation.domain.UserRole;
import edu.connexion3a8.blogmoderation.dto.*;

import java.util.List;

public interface ModerationDashboardService {
    List<ReportRowViewModel> search(ModerationFilter filter, int page, int size);
    int count(ModerationFilter filter);
    ModerationDetailDto getDetail(long reportId);
    ModerationKpi computeKpi();
    void decide(ModerationDecisionCommand command, UserRole actorRole);
    void bulkDecide(List<ModerationDecisionCommand> commands, UserRole actorRole);
    void reassign(long reportId, long expectedVersion, String moderator, String actor);
    void addInternalNote(long reportId, String actor, String note);
    List<String> listModerators();
}
