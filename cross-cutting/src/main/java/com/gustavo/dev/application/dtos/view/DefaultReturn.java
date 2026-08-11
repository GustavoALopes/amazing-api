package com.gustavo.dev.application.dtos.view;

import com.gustavo.dev.domain.entities.inputs.Message;

import java.util.Set;

public record DefaultReturn<T>(T data, Set<Message> messages) {
}
