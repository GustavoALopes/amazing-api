package com.gustavo.dev.api.application.controllers.order;

import com.gustavo.dev.api.application.controllers.dtos.input.ImportOrderInputModel;
import com.gustavo.dev.api.application.controllers.dtos.view.ImportOrderViewModel;
import com.gustavo.dev.api.application.usecase.order.ImportOrderUseCase;
import com.gustavo.dev.application.dtos.view.DefaultReturn;
import com.gustavo.dev.domain.entities.inputs.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public final class OrderController {
    private final ImportOrderUseCase importOrderUseCase;

    public OrderController(final ImportOrderUseCase importOrderUseCase) {
        this.importOrderUseCase = importOrderUseCase;
    }

    @PostMapping("/import")
    public ResponseEntity<DefaultReturn<ImportOrderViewModel>> importOrder(
            @RequestBody final ImportOrderInputModel model) {
        final var result = importOrderUseCase.execute(model.toDomain());
        final var hasSuccess = result.messages().stream().anyMatch(m -> m.type() == Message.Type.SUCCESS);
        return hasSuccess ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }
}
