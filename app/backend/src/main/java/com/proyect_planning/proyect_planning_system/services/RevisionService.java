package com.proyect_planning.proyect_planning_system.services;

import com.proyect_planning.proyect_planning_system.dtos.NewRevisionDto;
import com.proyect_planning.proyect_planning_system.entities.Revision;
import com.proyect_planning.proyect_planning_system.entities.Stage;
import com.proyect_planning.proyect_planning_system.repositories.RevisionRepository;
import com.proyect_planning.proyect_planning_system.repositories.StageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RevisionService {

    @Autowired
    private RevisionRepository revisionRepository;
    @Autowired
    private StageRepository stageRepository;

    public RevisionService(RevisionRepository revisionRepository) {
        this.revisionRepository = revisionRepository;
    }

    public Revision createRevision(NewRevisionDto newRevisionDto) {
        Stage stageToRevise = stageRepository.findById(newRevisionDto.getStageId()).orElse(null);
        if (stageToRevise == null) {
            throw new IllegalArgumentException("Stage not found");
        }

        Revision revision = Revision.builder()
                .comments(newRevisionDto.getComments())
                .stage(stageToRevise)
                .build();

        stageToRevise.addRevision(revision);
        stageRepository.save(stageToRevise);

        return revisionRepository.save(revision);
    }

    public Revision markRevisionAsResolved(Long revisionId) {
        Revision revision = revisionRepository.findById(revisionId)
                .orElseThrow(() -> new IllegalArgumentException("Revision not found"));

        revision.setResolved(true);
        return revisionRepository.save(revision);
    }

    public Revision markRevisionAsApproved(Long revisionId) {
        Revision revision = revisionRepository.findById(revisionId)
                .orElseThrow(() -> new IllegalArgumentException("Revision not found"));

        revision.setApproved(true);
        return revisionRepository.save(revision);
    }
}
