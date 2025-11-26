package com.proyect_planning.proyect_planning_system.services.bonita.dto;

import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArchivedCaseDTO {

    private String endDate;
    private String archivedDate;
    private String processDefinitionId;
    private String start;
    private String sourceObjectId;
    private String startedBySubstitute;
    private String rootCaseId;
    private String id;
    private String state;
    private String startedBy;
    private String lastUpdateDate;

    @JsonProperty("end_date")
    public String getEndDate() {
        return endDate;
    }

    @JsonProperty("started_by")
    public String getStartedBy() {
        return startedBy;
    }

    @JsonProperty("last_update_date")
    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    /**
     * Comparador por endDate, para ordenar de forma ascendente
     */
    public static final Comparator<ArchivedCaseDTO> endDateComparator = (ArchivedCaseDTO ac1,
            ArchivedCaseDTO ac2) -> ac1.getEndDate().compareTo(ac2.getEndDate());
}
