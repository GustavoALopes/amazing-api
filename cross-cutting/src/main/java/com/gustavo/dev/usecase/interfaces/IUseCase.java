package src.main.java.com.gustavo.dev.usecase.interfaces;

public interface IUseCase<TIn, TOut> {
    TOut execute(final TIn params);
    TOut executeWithoutTransaction(final TIn params);
}
