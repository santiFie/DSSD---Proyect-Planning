package com.proyect_planning.proyect_planning_system.controllers;

import com.proyect_planning.proyect_planning_system.dtos.NewRevisionDto;
import com.proyect_planning.proyect_planning_system.entities.Revision;
import com.proyect_planning.proyect_planning_system.services.RevisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/revisions")
@CrossOrigin(origins = "http://localhost:4200")
public class RevisionController {

    @Autowired
    private RevisionService revisionService;

    public RevisionController(RevisionService revisionService) {
        this.revisionService = revisionService;
    }

    @PostMapping
    public ResponseEntity<Revision> createRevision(@RequestBody NewRevisionDto newRevisionDto) {
        Revision createdRevision = revisionService.createRevision(newRevisionDto);
        return ResponseEntity.ok(createdRevision);
    }

    @PutMapping("/resolved/{id}")
    public ResponseEntity<Revision> markRevisionAsResolved(@PathVariable Long id) {
        Revision updatedRevision = revisionService.markRevisionAsResolved(id);
        return ResponseEntity.ok(updatedRevision);
    }

    @PutMapping("/aproved/{id}")
    public ResponseEntity<Revision> markRevisionAsApproved(@PathVariable Long id) {
        Revision updatedRevision = revisionService.markRevisionAsApproved(id);
        return ResponseEntity.ok(updatedRevision);
    }

}
