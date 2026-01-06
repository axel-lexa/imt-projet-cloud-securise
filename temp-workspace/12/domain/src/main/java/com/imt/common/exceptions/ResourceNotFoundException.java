package com.imt.common.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serial;

@Getter
@RequiredArgsConstructor
public class ResourceNotFoundException extends ImtException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String message;
}
