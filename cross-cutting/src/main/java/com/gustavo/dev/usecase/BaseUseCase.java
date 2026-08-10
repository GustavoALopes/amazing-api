package com.gustavo.dev.usecase;

import org.springframework.transaction.support.TransactionTemplate;
import src.main.java.com.gustavo.dev.usecase.interfaces.IUseCase;

public abstract class BaseUseCase<TIn, TOut> implements IUseCase<TIn, TOut> {

    private final TransactionTemplate template;

    protected BaseUseCase(final TransactionTemplate template) {
        this.template = template;
    }

    public TOut execute(final TIn params) {
        return this.template.execute(transaction -> {
            return this.internalExecute(params);
        });
    }

    public TOut executeWithoutTransaction(final TIn params) {
        return this.internalExecute(params);
    }

    protected abstract TOut internalExecute(final TIn params);
}
