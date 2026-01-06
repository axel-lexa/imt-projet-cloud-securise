package com.imt.adaptersinrest.common.model.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serial;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AbstractInput implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}