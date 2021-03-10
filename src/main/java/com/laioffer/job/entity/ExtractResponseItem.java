package com.laioffer.job.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtractResponseItem {
    //extractions are the list of extracted keywords Extraction
    public List<Extraction> extractions;
}

