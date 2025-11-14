package com.proyect_planning.proyect_planning_system.services.bonita.dto;

import java.util.Comparator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchivedCaseDTO {

    private String endDate;
    private String archivedDate;
    private String searchIndex5Label;
    private String processDefinitionId;
    private String searchIndex3Value;
    private String searchIndex4Value;
    private String searchIndex2Label;
    private String start;
    private String searchIndex1Value;
    private String sourceObjectId;
    private String searchIndex3Label;
    private String startedBySubstitute;
    private String searchIndex5Value;
    private String searchIndex2Value;
    private String rootCaseId;
    private String id;
    private String state;
    private String searchIndex1Label;
    private String startedBy;
    private String searchIndex4Label;
    private String lastUpdateDate;

    /**
     * Comparador por endDate, para ordenar de forma ascendente
     */
    public static final Comparator<ArchivedCaseDTO> endDateComparator = (ArchivedCaseDTO ac1, ArchivedCaseDTO ac2) -> ac1.getEndDate().compareTo(ac2.getEndDate());
}
